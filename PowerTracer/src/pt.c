#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <sys/time.h>
#include <sys/select.h>
#include <string.h>
#include <arpa/inet.h>
#include <assert.h>

#include "ptError.h"
#include "ptInternal.h"
#include "conf.h"
#include "trace.h"
#include "serial.h"
#include "LMG.h"
#include "hdTopo.h"
#include "hdStats.h"
#include "hdError.h"


/*
 * Start time storage
 */
static struct timeval tv_start;

/*
 * Read data of one measuring iteration,
 *  split, prepare and write into the files
 */
static
int trace_iteration(int serial_fd, ConfigStruct *config)
{
    int ret;

    /* currently only binary operation mode is used */
    assert(config->mode == MODE_BIN);

    /* in binary mode each value has 4 bytes */
    assert(config->isize % 4 == 0);

    size_t isize = config->isize;

    char buffer[isize];  /* I/O buffer */
    size_t osize;        /* Output size */

    /*
     * Read message
     */
    ret = LMG_readBinaryMessage(serial_fd, buffer, isize);
    if (ret < 0)
    	LMG_READBINARYMESSAGE_ERROR_CHECK;
    if (ret != isize)
    {
        ERROR("Unexpected response size.");
        return(ERR_CUSTOM);
    }


    /*
     * Write response part for each trace to the correct file
     */
    char *bufptr = buffer;
    for FOR_TRACES(config)
    {
       	/* use int pointer since order_bytes32ip is assumed to be faster */
    	uint32_t *intptr = (uint32_t *) bufptr;
    	for (int j = 0; j < trace->size / 4; ++j)
    		order_bytes32ip(intptr+j);

    	osize = trace->size;

    	ret = hdS_writeEntry(trace->group, bufptr, osize);
    	if (ret < 0) {
    		// all these errors are programmer's faults, not user's faults
    		assert(errno != HD_ERR_INVALID_ARGUMENT);
			assert(errno != HD_ERR_TRACE_DISABLED);
			assert(errno != HDS_ERR_GROUP_COMMIT_STATE);
			assert(errno != HDS_ERR_UNEXPECTED_ARGVALUE);
			assert(errno != HDS_ERR_ENTRY_STATE);
			ERROR_UNKNOWN;
			return ERR_UNKNOWN;
    	}

        /* set bufptr to the start of the next trace's part */
        bufptr+=osize;
    }

    return(OK);
}

static
int trace_data(
        int serial_fd,      /* file descriptor of serial port */
        ConfigStruct *config
        )
{
    int ret;

    u_int8_t done;

    fd_set input;
    struct timeval timeout;
    int n;

    int stdin_fd = fileno(stdin);

    /*
     * check value of cycle time
     */
    if(config->cycle < 0.05 || config->cycle > 60)
    {
        ERROR("trace_data(): Cycle time out of range (0.05..60).");
    }

    /*
     * Define commands to be executed
     */
    size_t actn_len = 5; /* bytes for "ACTN;" */

	for FOR_TRACES(config)
        actn_len += strlen(trace->actn) + 1; /* 1 byte for the separating ';' */

    char actn[actn_len];
    actn[0] = '\0';
    strcat(actn, "ACTN");
    for FOR_TRACES(config) {
        strcat(actn, ";");
        strcat(actn, trace->actn);
    }

    /*
     * Send action script to LMG
     */
    ret = serial_sendMessage(serial_fd, actn);
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Set cycle time
     */
    char buffer[32];
    sprintf(buffer, "CYCL %f", config->cycle);
    ret = serial_sendMessage(serial_fd, buffer);
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Set output format
     */
    switch(config->mode)
    {
        case MODE_BIN:
            ret = serial_sendMessage(serial_fd, "FRMT PACKED");
            SERIAL_SENDMESSAGE_RETURN_CHECK;
            break;
        case MODE_ASCII:
            ret = serial_sendMessage(serial_fd, "FRMT ASCII");
            SERIAL_SENDMESSAGE_RETURN_CHECK;
            break;
    }
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Start continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT ON");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Enable tracing groups
     */
    for FOR_TRACES(config) {
    	hdS_enableGroup(trace->group);
    }

    /*
     * Tracing iterations loop
     */
    done = 0;
    while(!done)
    {
        /* Initialize the input set */
        FD_ZERO(&input);
        FD_SET(serial_fd, &input);
        FD_SET(stdin_fd, &input);

        /* Initialize the timeout structure */
        timeout.tv_sec  = 2;
        timeout.tv_usec = 0;

        /* Do the select */
        n = select(serial_fd+1, &input, NULL, NULL, &timeout);
        if (n < 0)
        {
          ERROR_ERRNO("select()");
          return(ERR_ERRNO);
        }
        else if (n == 0)
        {
            /* timeout */
            done = 1;
            break;
        }

        if (FD_ISSET(stdin_fd, &input))
        {
            switch(getchar())
            {
                case 'q':
                case 'Q':
                    /* end continous mode */
                    ret = serial_sendMessage(serial_fd, "CONT OFF");
                    SERIAL_SENDMESSAGE_RETURN_CHECK;
                    /* now the input buffer will run empty and select will
                     * run into its timeout and so end the loop */
                    break;
            }
        }

        if (FD_ISSET(serial_fd, &input))
        {
            ret = trace_iteration(serial_fd, config);
            switch(ret)
            {
                case OK:
                    break;
                case ERR_ERRNO: /* read(), readBytes() */ \
                case ERR_BSIZE: \
                case ERR_NO_MSG: \
                case ERR_MSG_FORMAT: \
                case ERR_UNKNOWN: \
                case ERR_CUSTOM: \
                    return(ret); \
                default: \
                    ERROR_UNKNOWN; \
                    return(ERR_UNKNOWN); \
            }
        }
    }

    /*
     * Stop continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT OFF");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Disable tracing groups
     */
    for FOR_TRACES(config) {
    	hdS_disableGroup(trace->group);
    }

    return (OK);
}

static
int test_binmode(int serial_fd)
{
    int ret;

    /*
     * Send command for ASCII mode
     */
    ret = serial_sendMessage(serial_fd, "INIM;UTRMS?;ITRMS?;P?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /* read data */

    char buffer[255];

    ret = LMG_readTextMessage(serial_fd, buffer, sizeof(buffer));
    if (ret < 0)
        LMG_READTEXTMESSAGE_ERROR_CHECK;

    /* print message */
    if(puts(buffer) == EOF)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }


    /*
     * Set output format to binary mode
     */
    ret = serial_sendMessage(serial_fd, "FRMT PACKED");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Send command for binary mode
     */
    ret = serial_sendMessage(serial_fd, "UTRMS?;ITRMS?;P?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /* read data */
    struct {
        float utrms;
        float itrms;
        float p;
    } op;

    ret = LMG_readBinaryMessage(serial_fd, &op, sizeof(op));
    if (ret < 0)
    {
        LMG_READBINARYMESSAGE_ERROR_CHECK;
    }

    if (printf("%.4E;%.4E;%.4E\n", op.utrms, op.itrms, op.p ) < 0)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }

    return (OK);
}


/**
 * Main function
 *
 * Takes following commandline arguments:
 *
 * [-P PROJECT] [-t TOPOLOGY] [-p PORT] TRACE [TRACE [TRACE [...]]]]
 *
 * PROJECT = Name of the project (default: MyProject)
 * TOPOLOGY = level1_level2_level3 (default: Host_Process_Thread)
 * TRACE = TYPES:CHANNEL:OUTPUT
 *
 * TYPES = String containing 'a' (ASCII), 'b' (BINARY), 's' (HDSTATS)
 * CHANNEL = Number of the input channel
 * OUTPUT = If TYPES contains 'b' or 'a', this is simply the output file
 *  without the extension. For 'a' .txt is added and '.bin' for 'b'.
 *  If TYPES contains 's', this describes a topology node and must be in the
 *  form "path1_path2" (until now only 's' is implemented)
 *
 * PORT = "/dev/ttyUSB0"
 *
 * @return error code
 */
int main(int argc, char **argv)
{
	int ret;

	/*
	 * Initialize configuration
	 */
	ConfigStruct config;
	config.topology = NULL;

	/*
	 * Set defaults
	 */
	config.device = "LMG450";
    config.mode = MODE_BIN;
	config.host = NULL;
	config.port = "/dev/ttyUSB0";
	config.cycle = 0.1;
	config.project = "MyProject";
	config.topo = "Host_Process_Thread";
	config.traces.last = NULL;


	/*
	 * Read commandline parameters
	 */
	char *configfile = NULL;
	char *project = NULL;
	char *topo = NULL;
	char *port = NULL;
	char o;
	while ((o = getopt(argc, argv, "c:P:t:p:")) >= 0) {
		switch(o) {
		case 'c':
			configfile = optarg;
			break;
		case 'P':
			project = optarg;
			break;
		case 't':
			topo = optarg;
			break;
		case 'p':
			port = optarg;
			break;
		default:
			break;
		}
	}


	/*
	 * Read configuration file if given
	 */
	if (configfile != NULL)
		readConfigFromFile(configfile, &config);

	/*
	 * Override configuration file options with commandline options
	 */
	if (port != NULL)
		config.port = port;
	if (project != NULL)
		config.project = project;
	if (topo != NULL)
		config.topo = topo;

	/*
	 * Add traces from commandline
	 */

	int ntraces = argc - optind;
	ret = parseTraceStrings(ntraces, argv+optind, &config);

	/*
	 * Do consistency check of the final configuration
	 */
	ret = checkConfig(&config);
	if (ret != 0) {
		cleanupConfig(&config);
		exit(-1);
	}


	/*
	 * Print configuration
	 */
	printf("Device: %s\n", config.device);
	printf("Host: %s\n", config.host == NULL ? "NULL" : config.host);
	printf("Port: %s\n", config.port);
	printf("Project: %s\n", config.project);


	/*
	 * Create the traces found in configuration
	 */
	ret = createTraces(&config);
	if (ret == -1) {
		fputs("No traces configured\n", stderr);
		cleanupConfig(&config);
		exit(-1);
	}


	int serial_fd;
    char buffer[255];  /* Input buffer */

    /*
     * Take start time
     */
    ret = gettimeofday(&tv_start, NULL);

    /*
     * Open serial port
     */
    serial_fd = serial_openPort(config.port);
    if(serial_fd < 0)
        return(serial_fd);

    /*
     * Setup serial port
     *
     * Configuration on device:
     * ComA: Custom
     * ComA 8N1
     * Baudrate: 57600
     * EOS: <lf>
     * Echo: Off
     * Protocol: RTS/CTS
     */
    ret = serial_setupPort(serial_fd, 57600);
    SERIAL_SETUPPORT_RETURN_CHECK;

    /*
     * Setup LMG
     */
    ret = LMG_setup(serial_fd);
    LMG_SETUP_RETURN_CHECK;

    /*
     * Get and print identity string from LMG
     */
    ret = LMG_getIdentity(serial_fd, buffer, sizeof(buffer));
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Errno error while getting identity.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while getting identity.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while getting identity.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect message format while getting identity.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while getting identity.\n", stderr);
            break;
        default:
            fputs("Unknown error while getting identity.\n", stderr);
    }

    ret = puts(buffer);
    if(ret == EOF)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }

    ret = test_binmode(serial_fd);
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Other error while testing binary mode.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while testing binary mode.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while testing binary mode.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect Message format while testing binary mode.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while testing binary mode.\n", stderr);
            break;
        default:
            fputs("Unknown error while testing binary mode.\n", stderr);
    }


    puts("Start tracing!");

    ret = trace_data(serial_fd, &config);
    printf("Returns %d\n", ret);
    /* TODO: error handling */

    puts("End tracing!");


    cleanupConfig(&config);

    /*
     * Get and print all errors from LMG
     */
    ret = LMG_getAllErrors(serial_fd, buffer, sizeof(buffer));
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Errno error while getting all errors.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while getting all errors.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while getting all errors.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect message format while getting all errors.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while getting all errors.\n", stderr);
            break;
        default:
            fputs("Unknown error while getting all errors.\n", stderr);
    }

    ret = puts(buffer);
    if (ret == EOF)
    {
        ERROR_UNKNOWN;
    }

    /*
     * Close LMG connection
     */
    ret = LMG_close(serial_fd);
    LMG_CLOSE_RETURN_CHECK;

    /*
     * Close serial port
     */
    ret = serial_closePort(serial_fd);
    SERIAL_CLOSEPORT_RETURN_CHECK;


}

/* vim: set sw=4 sts=4 et fdm=syntax: */
