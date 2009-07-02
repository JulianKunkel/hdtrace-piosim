#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <sys/time.h>
#include <sys/select.h>
#include <string.h>
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


#if 0
/*
 * Start time storage
 */
static struct timeval tv_start;
#endif

/**
 * Read data of one measuring iteration,
 *  split, prepare and write into the files
 *
 * @param serial_fd  File descriptor of the serial port connected to the device
 * @param config     Configuration
 *
 * @return Error state
 *
 * @retval OK           Success
 * @retval ERR_MSGSIZE  Message from device has unexpected size
 * @retval -->          \ref LMG_readBinaryMessage()
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
        return(ERR_MSGSIZE);
    }


    /*
     * Write response part for each trace to the correct file
     */
    char *bufptr = buffer;
    FOR_TRACES(config->traces) {
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
			assert(!"Unknown return value from hdS_writeEntry()");
    	}

        /* set bufptr to the start of the next trace's part */
        bufptr+=osize;
    }

    return(OK);
}

/**
 * Trace all the data configured
 *
 * @param serial_fd  File descriptor of the serial port connected to the device
 * @param config     Configuration
 *
 * @return Error state
 *
 * @retval OK    Success
 * @retval -->   \ref serial_sendMessage()
 * @retval -->   \ref trace_iteration()
 */
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

	FOR_TRACES(config->traces)
        actn_len += strlen(trace->actn) + 1; /* 1 byte for the separating ';' */

    char actn[actn_len];
    actn[0] = '\0';
    strcat(actn, "ACTN");
    FOR_TRACES(config->traces) {
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
            break;
        case MODE_ASCII:
            ret = serial_sendMessage(serial_fd, "FRMT ASCII");
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
    FOR_TRACES(config->traces) {
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

        /* input from standard input ready */
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

        /* input from device ready */
        if (FD_ISSET(serial_fd, &input))
        {
            ret = trace_iteration(serial_fd, config);
            switch(ret)
            {
                case OK:
                    break;
                case ERR_MSGSIZE:
                case ERR_ERRNO: /* read(), readBytes() */
                case ERR_NO_MSG:
                case ERR_MSG_FORMAT:
                case ERR_MALLOC:
                case ERR_BSIZE:
                    return(ret);
                default:
					assert(!"Unknown return value from trace_iteration()");
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
    FOR_TRACES(config->traces) {
    	ret = hdS_disableGroup(trace->group);
    	assert(ret >= 0);
    }

    return (OK);
}

/**
 * Tests switching to binary mode, assumes to be in ASCII mode when called
 *
 * @param serial_fd  File descriptor of the port connected to the device
 *
 * @return Error state
 *
 * @retval  OK  Success
 * @retval -->  \ref serial_sendMessage();
 * @retval -->  \ref LMG_readTextMessage();
 * @retval -->  \ref LMG_readBinaryMessage();
 */
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
    puts(buffer);

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

    printf("%.4E;%.4E;%.4E\n", op.utrms, op.itrms, op.p );

    return OK;
}

/*
 * Define error states
 */
#define EOK            0
#define ESYNTAX       -1
#define ECONFNOTFOUND -2
#define ECONFINVALID  -3
#define ENOTRACES     -4
#define EMEMORY       -5
#define EHDLIB        -6
#define EDEVICE       -7

#define EOTHER      -100

#define ERROR_OUTPUT(msg, ...) \
	if (directOutput) \
		fprintf(stderr, "PowerTracer: " msg "\n", ## __VA_ARGS__)

/**
 * Do tracing.
 *
 * This function is the main function used by the commandline tool and the
 *  library API in common after creating the configuration differently.
 *
 * @param config         Configuration
 * @param directOutput   Direct output to the console
 *                       (true for commandline tool, false for library use
 *
 * @return Error state
 *
 *
 */
int doTracing(ConfigStruct *config, int directOutput) {

	int ret;

	/*
	 * Create the traces found in configuration
	 */
	ret = createTraces(config);
	switch (ret) {
	case OK:
		break;
	case ERR_NO_TRACES:
		ERROR_OUTPUT("No traces configured.");
		cleanupConfig(config);
		return ENOTRACES;
	case ERR_MALLOC:
		ERROR_OUTPUT("Out of memory while creating traces.");
		cleanupConfig(config);
		return EMEMORY;
	case ERR_HDLIB:
		ERROR_OUTPUT("Error occurred in hdTraceWritingLibrary while creating traces.");
		cleanupConfig(config);
		return EHDLIB;
	default:
		assert(!"Unknown return value from createTraces()");
	}


#if 0
    /*
     * Take start time
     */
    ret = gettimeofday(&tv_start, NULL);
#endif


	int serial_fd;
    char buffer[255];  /* Input buffer */

    /*
     * Open serial port
     */
    serial_fd = serial_openPort(config->port);
    if(serial_fd < 0) {
    	switch (serial_fd) {
    	case ERR_ERRNO:
    		ERROR_OUTPUT("Problem while opening serial port: %s", strerror(errno));
    		cleanupConfig(config);
    		return EDEVICE;
    	default:
    		assert(!"Unknown return value from serial_openPort().");
        }
    }

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
    switch(ret) {
    case OK:
    	break;
    case ERR_ERRNO: /* tcgetattr(), cfsetispeed(), cfsetospeed(), tcsetattr(), tcflush() */
		ERROR_OUTPUT("Problem while setting up serial port: %s", strerror(errno));
		cleanupConfig(config);
		return EDEVICE;
    default:
    	assert(!"Unknown return state of serial_setupPort().");
    }

    /*
     * Setup LMG
     */
    ret = LMG_setup(serial_fd);
    if (ret != OK) {
	    switch(ret) {
	    case ERR_ERRNO: /* LMG_reset(), serial_sendMessage() */
	    	ERROR_OUTPUT("Problem while setting up LMG device: %s", strerror(errno));
	    	break;
	    case ERR_WRITE:
	    	ERROR_OUTPUT("Write error while setting up LMG device.");
	    	break;
	    default: \
	    	assert(!"Unknown return state of LMG_setup().");
	    }
    	cleanupConfig(config);
    	serial_closePort(serial_fd);
    	return EDEVICE;
    }

    /*
     * Get and print identity string from LMG
     */
    ret = LMG_getIdentity(serial_fd, buffer, sizeof(buffer));
    if (ret != OK) {
    	switch(ret)
		{
			case ERR_ERRNO:
				ERROR_OUTPUT("Problem while getting identity: %s", strerror(errno));
				break;
			case ERR_WRITE:
				ERROR_OUTPUT("Write error while getting identity.");
				break;
			case ERR_NO_MSG:
				ERROR_OUTPUT("No message ready while getting identity.");
				break;
			case ERR_MSG_FORMAT:
				ERROR_OUTPUT("Incorrect message format while getting identity.");
				break;
			case ERR_MALLOC:
				ERROR_OUTPUT("Out of memory while getting identity.");
				cleanupConfig(config);
		    	serial_closePort(serial_fd);
		    	return EMEMORY;
			case ERR_BSIZE:
				ERROR_OUTPUT("Buffer size to low while getting identity.");
				break;
			default:
				assert(!"Unknown return state of LMG_getIdentity().");
		}
    	cleanupConfig(config);
    	serial_closePort(serial_fd);
    	return EDEVICE;
    }

    ret = puts(buffer);
    assert(ret != EOF);

    memset(buffer, '\0', 255);

    ret = test_binmode(serial_fd);
    if (ret != OK) {
	    switch(ret)
	    {
	        case ERR_ERRNO:
				ERROR_OUTPUT("Problem while testing binary mode: %s", strerror(errno));
	            break;
	        case ERR_WRITE:
	        	ERROR_OUTPUT("Write error while testing binary mode.");
	            break;
	        case ERR_NO_MSG:
	        	ERROR_OUTPUT("No message ready while testing binary mode.");
	            break;
	        case ERR_MSG_FORMAT:
	        	ERROR_OUTPUT("Incorrect Message format while testing binary mode.");
	            break;
			case ERR_MALLOC:
				ERROR_OUTPUT("Out of memory while getting identity.");
				cleanupConfig(config);
		    	serial_closePort(serial_fd);
		    	return EMEMORY;
	        case ERR_BSIZE:
	        	ERROR_OUTPUT("Buffer size to low while testing binary mode.");
	            break;
	        default:
				assert(!"Unknown return state of test_binmode().");
	    }
	    cleanupConfig(config);
	    serial_closePort(serial_fd);
	    return EDEVICE;
    }


    puts("Start tracing!");

    ret = trace_data(serial_fd, config);
    if (ret != OK) {
    	switch (ret) {
    	case ERR_ERRNO:
			ERROR_OUTPUT("Problem while tracing data: %s", strerror(errno));
			break;
		case ERR_WRITE:
			ERROR_OUTPUT("Write error while tracing data.");
			break;
		case ERR_NO_MSG:
			ERROR_OUTPUT("No message ready while tracing data.");
			break;
		case ERR_MSG_FORMAT:
			ERROR_OUTPUT("Incorrect message format while tracing data.");
			break;
		case ERR_MALLOC:
			ERROR_OUTPUT("Out of memory while tracing data.");
			cleanupConfig(config);
			serial_closePort(serial_fd);
			return EMEMORY;
		case ERR_BSIZE:
			ERROR_OUTPUT("Buffer size to low while tracing data.");
			break;
		default:
			assert(!"Unknown return value from trace_data().");
    	}
		cleanupConfig(config);
    	serial_closePort(serial_fd);
    	return EDEVICE;
    }


    puts("End tracing!");


    cleanupConfig(config);

    /*
     * Get and print all errors from LMG
     */
    ret = LMG_getAllErrors(serial_fd, buffer, sizeof(buffer));
    if (ret != OK) {
	    switch(ret)
	    {
	        case OK:
	            break;
	        case ERR_ERRNO:
				ERROR_OUTPUT("Problem while getting all errors: %s", strerror(errno));
	            break;
	        case ERR_WRITE:
	            ERROR_OUTPUT("Write error while getting all errors.");
	            break;
	        case ERR_NO_MSG:
	            ERROR_OUTPUT("No message ready while getting all errors.");
	            break;
	        case ERR_MSG_FORMAT:
	            ERROR_OUTPUT("Incorrect message format while getting all errors.");
	            break;
			case ERR_MALLOC:
				ERROR_OUTPUT("Out of memory while getting all errors.");
				cleanupConfig(config);
		    	serial_closePort(serial_fd);
		    	return EMEMORY;
	        case ERR_BSIZE:
	            ERROR_OUTPUT("Buffer size to low while getting all errors.");
	            break;
	        default:
				assert(!"Unknown return state of LMG_getAllErrors().");
	    }
	    serial_closePort(serial_fd);
	    return EDEVICE;
    }

    ret = puts(buffer);
    assert(ret != EOF);

    /*
     * Close LMG connection
     */
    ret = LMG_close(serial_fd);
    if (ret != OK) {
	    switch(ret) {
	        case ERR_ERRNO: /* LMG_reset(), serial_sendMessage() */
				ERROR_OUTPUT("Problem while closing LMG device: %s", strerror(errno));
				break;
	        case ERR_WRITE:
				ERROR_OUTPUT("Write problem while closing LMG device.");
				break;
	        default:
				assert(!"Unknown return state of LMG_getAllErrors().");
	    }
	    serial_closePort(serial_fd);
	    return EDEVICE;
    }

    /*
     * Close serial port
     */
    ret = serial_closePort(serial_fd);
	switch(ret) {
		case OK:
			break;
		case ERR_ERRNO:
			ERROR_OUTPUT("Problem while closing serial port: %s", strerror(errno));
			return EDEVICE;
	    default:
	    	assert(!"Unknown return state of serial_closePort().");
	    }

    return 0;

}


static void printUsage() {
	puts(
			"Usage: pt [-P PROJECT] [-t TOPOLOGY] [-p PORT] TRACE [TRACE [TRACE [...]]]]\n"
			"\n"
			"PROJECT = Name of the project (default: MyProject)\n"
			"TOPOLOGY = level1_level2_level3 (default: Host_Process_Thread)\n"
			"TRACE = TYPES:CHANNEL:OUTPUT\n"
			"\n"
			"TYPES = String containing 'a' (ASCII), 'b' (BINARY), 's' (HDSTATS)\n"
			"CHANNEL = Number of the input channel\n"
			"OUTPUT = If TYPES contains 'b' or 'a', this is simply the output file without\n"
			"         the extension. For 'a' .txt is added and '.bin' for 'b'.\n"
			"         If TYPES contains 's', this describes a topology node (see hdStats\n"
			"         documentation) and must have the  form \"path1_path2\"\n"
			"         (ONLY 's' IS IMPLEMENTED YET!)\n"
			"\n"
		);
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
 * @return Error state
 *
 * @retval   0   OK
 * @retval  -1   Syntax error
 * @retval  -2   Configuration file not found
 * @retval  -3   Configuration file invalid
 * @retval  -4   No traces configured
 * @retval  -5   Out of Memory
 * @retval  -6   External error in hdTraceWritingLibrary
 * @retval  -7   Something went wrong with the measuring device
 * @retval -100  Another error occurred
 */
int main(int argc, char **argv)
{

	int ret;

	int directOutput = 1;

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
			printUsage();
			return ESYNTAX;
		}
	}


	/*
	 * Read configuration file if given
	 */
	if (configfile != NULL) {
		ret = readConfigFromFile(configfile, &config);
		switch (ret) {
		case OK:
			break;
		case ERR_MALLOC:
			ERROR_OUTPUT("Out of memory while reading configuration file.");
			cleanupConfig(&config);
			return EMEMORY;
		case ERR_FILE_NOT_FOUND:
			ERROR_OUTPUT("Configuration file not found.");
			cleanupConfig(&config);
			return ECONFNOTFOUND;
		case ERR_ERRNO:
			ERROR_OUTPUT("Problem while processing configuration file: %s", strerror(errno));
			cleanupConfig(&config);
			return ECONFINVALID;
		case ERR_SYNTAX:
			ERROR_OUTPUT("Configuration file invalid.");
			cleanupConfig(&config);
			return ECONFINVALID;
		default:
			assert(!"Unknown return value from readConfigFromFile()");
		}
	}

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
	switch (ret) {
	case OK:
		break;
	case ERR_SYNTAX:
		printUsage();
		cleanupConfig(&config);
		return ESYNTAX;
	case ERR_MALLOC:
		ERROR_OUTPUT("Out of memory.");
		cleanupConfig(&config);
		return EMEMORY;
	default:
		assert(!"Unknown return value from parseTraceStrings()");
	}

	/*
	 * Do consistency check of the final configuration
	 */
	ret = checkConfig(&config);
	switch (ret) {
	case OK:
		break;
	default:
		ERROR_OUTPUT("Configuration is not valid.");
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

	return doTracing(&config, directOutput);
}

#undef ERROR_OUTPUT
/* vim: set sw=4 sts=4 et fdm=syntax: */
