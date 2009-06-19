#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/select.h>
#include <string.h>
#include <arpa/inet.h>
#include <assert.h>

#include "error.h"
#include "serial.h"
#include "LMG.h"
#include "hdTopo.h"
#include "hdStats.h"
#include "hdError.h"

enum opMode {
	MODE_ASCII,
	MODE_BIN
};

/*
 * Structure for trace file description
 */
typedef struct
{
	int bin : 1;
	int ascii : 1;
	int hdstats : 1;
	int channel;
	char *output;

    char *actn; /* action script part for this trace */
    int   size;    /* size of expected response in [byte] */

    hdTopoNode tnode;   /* topology node, reference needed for destruction */
    hdStatsGroup group; /* statistics group this belongs to */
} TRACE;

/*
 * Start time storage
 */
static struct timeval tv_start;

/* read data and log into file */
static
int trace_iteration(int serial_fd, TRACE *traces, int ntraces, enum opMode mode)
{
    int ret;

    assert(mode == MODE_BIN);

    static size_t isize = 0; /* Calculate input size only once */
    if (isize == 0)
    {
        for (int i = 0; i < ntraces; ++i)
            isize += traces[i].size;
    }
    if (isize < 10)
        isize = 10;

    char buffer[isize];  /* I/O buffer */
    size_t osize;        /* Output size */

    /*
     * Read message
     */
    ret = LMG_readBinaryMessage(serial_fd, buffer, isize);
    if (ret < 0)
    	LMG_READBINARYMESSAGE_ERROR_CHECK
    if (ret != isize)
    {
        ERROR_CUSTOM("Unexpected response size.");
        return(ERR_CUSTOM);
    }


    /*
     * Write response part for each trace to the correct file
     */
    char *bufptr = buffer;
    for (int i = 0; i < ntraces; ++i)
    {
       	/* use int pointer since order_bytes32ip is assumed to be faster */
    	uint32_t *intptr = (uint32_t *) bufptr;
    	for (int j = 0; j < traces[i].size / 4; ++j)
    		order_bytes32ip(intptr+j);

    	osize = traces[i].size;

    	ret = hdS_writeEntry(traces[i].group, bufptr, osize);
    	/* TODO error check */

        /* set bufptr to the start of the next trace's part */
        bufptr+=osize;
    }

    return(OK);
}

static
int trace_data(
        int serial_fd,      /* file descriptor of serial port */
        float cycle,        /* cycle length in seconds */
        TRACE *traces,      /* array of trace descriptions */
        int ntraces,        /* length of traces array */
        enum opMode mode  /* operation mode (ASCII/BIN) */
        )
{
    int ret;

    u_int8_t done;

    fd_set input;
    struct timeval timeout;
    int n;

    int stdin_fd = fileno(stdin);

    /*
     * check type setting
     */
    if(mode != MODE_ASCII && mode != MODE_BIN)
    {
        ERROR_CUSTOM("trace_data(): Unknown type requested.");
    }

    /*
     * check value of cycle time
     */
    if(cycle < 0.05 || cycle > 60)
    {
        ERROR_CUSTOM("trace_data(): Cycle time out of range (0.05..60).");
    }

    /*
     * Define commands to be executed
     */
    size_t actn_len = 5; /* bytes for "ACTN;" */
    for(int i = 0; i < ntraces; ++i)
        actn_len += strlen(traces[i].actn);
    actn_len += ntraces; /* bytes for the separating ';' */

    char actn[actn_len];
    actn[0] = '\0';
    strcat(actn, "ACTN");
    for(int i = 0; i < ntraces; ++i)
    {
        strcat(actn, ";");
        strcat(actn, traces[i].actn);
    }

    /*
     * Send action script to LMG
     */
    ret = serial_sendMessage(serial_fd, actn);
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Set cycle time
     */
    char buffer[32];
    sprintf(buffer, "CYCL %f", cycle);
    ret = serial_sendMessage(serial_fd, buffer);
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Set output format
     */
    switch(mode)
    {
        case MODE_BIN:
            ret = serial_sendMessage(serial_fd, "FRMT PACKED");
            SERIAL_SENDMESSAGE_RETURN_CHECK
            break;
        case MODE_ASCII:
            ret = serial_sendMessage(serial_fd, "FRMT ASCII");
            SERIAL_SENDMESSAGE_RETURN_CHECK
            break;
    }
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Start continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT ON");
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Enable tracing groups
     */
    for (int i = 0; i < ntraces; ++i) {
    	hdS_enableGroup(traces[i].group);
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
          ERROR("select()");
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
                    SERIAL_SENDMESSAGE_RETURN_CHECK
                    /* now the input buffer will run empty and select will
                     * run into its timeout and so end the loop */
                    break;
            }
        }

        if (FD_ISSET(serial_fd, &input))
        {
            ret = trace_iteration(serial_fd, traces, ntraces, mode);
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
                    ERROR_UNKNOWN \
                    return(ERR_UNKNOWN); \
            }
        }
    }

    /*
     * Stop continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT OFF");
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Disable tracing groups
     */
    for (int i = 0; i < ntraces; ++i) {
    	hdS_disableGroup(traces[i].group);
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
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /* read data */

    char buffer[255];

    ret = LMG_readTextMessage(serial_fd, buffer, sizeof(buffer));
    if (ret < 0)
        LMG_READTEXTMESSAGE_ERROR_CHECK

    /* print message */
    if(puts(buffer) == EOF)
    {
        ERROR_UNKNOWN
        return(ERR_UNKNOWN);
    }


    /*
     * Set output format to binary mode
     */
    ret = serial_sendMessage(serial_fd, "FRMT PACKED");
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /*
     * Send command for binary mode
     */
    ret = serial_sendMessage(serial_fd, "UTRMS?;ITRMS?;P?");
    SERIAL_SENDMESSAGE_RETURN_CHECK

    /* read data */
    struct {
        float utrms;
        float itrms;
        float p;
    } op;

    ret = LMG_readBinaryMessage(serial_fd, &op, sizeof(op));
    if (ret < 0)
    {
        LMG_READBINARYMESSAGE_ERROR_CHECK
    }

    if (printf("%.4E;%.4E;%.4E\n", op.utrms, op.itrms, op.p ) < 0)
    {
        ERROR_UNKNOWN
        return(ERR_UNKNOWN);
    }

    return (OK);
}

char** parsePath(char *output, int *plen) {

	// count number of tokens in output delimited by '_'
	*plen = 1;
	char *ptr = output;
	while ((ptr = index(ptr, '_')) != NULL)
		++*plen, ++ptr;

	// allocate space for pointers
	char ** path;
	path = malloc(*plen * sizeof(*path));

	// allocate space for strings
	int outlen = strlen(output);
	if (outlen <= 0)
		return NULL;
	path[0] = malloc(outlen+1 * sizeof(*(path[0])));

	// copy output string to allocated memory
	strcpy(path[0], output);

	// create path string array
	// by setting the pointers and replacing '_' with '\0'
	ptr = path[0];
	for (int i = 1; i < *plen; ++i) {
		ptr = index(ptr, '_');
		assert(*ptr == '_');
		*ptr = '\0';
		path[i] = ++ptr;
	}

	return path;
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
 *  form "path1_path2"
 *
 * PORT = "/dev/ttyUSB0"
 *
 * @return error code
 */
int main(int argc, char **argv)
{
	int ret;

	/*
	 * Set defaults
	 */
	char *project = "MyProject";
	char *topo = "Host_Process_Thread";
	char *port = "/dev/ttyUSB0";

    int mode = MODE_BIN;
    float cycle = 0.05;


	/*
	 * Read commandline parameters
	 */
	char o;
	while ((o = getopt(argc, argv, "P:t:p:")) >= 0) {
		switch(o) {
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

	printf("Project: %s\n", project);
	printf("Port: %s\n", port);

	/*
	 * Create topology
	 */
	hdTopology myTopology;
	int tlen;
	char ** levels = parsePath(topo, &tlen);
	myTopology = hdT_createTopology((const char *) project,
			(const char **) levels, tlen);

	printf("Topology: %s", levels[0]);
	for (int i = 1; i < tlen; ++i) {
		printf(" - %s", levels[i]);
	}
	printf("\n");

	// free memory allocated by parsePath()
	free(levels[0]);
	free(levels);


	/*
	 * Build array of traces
	 */

	int ntraces = argc - optind;
	TRACE *traces = malloc(ntraces * sizeof(*traces));

	// parse all traces
	for (int i = optind; i < argc; ++i) {

		char *ptr = argv[i];
		int tnr = i-optind;

		// parse types of trace
		traces[tnr].bin = 0;
		traces[tnr].ascii = 0;
		traces[tnr].hdstats = 0;
		while (*ptr != ':') {
			switch(*ptr) {
			case 'b':
				traces[tnr].bin = 1;
				break;
			case 'a':
				traces[tnr].ascii = 1;
				break;
			case 's':
				traces[tnr].hdstats = 1;
				break;
			case '\0':
				puts("Usage()");
				exit(-1);
				break;
			default:
				puts("Wrong type letter.");
				exit(-1);
				break;
			}
			ptr++;
		}

		assert(*ptr == ':');
		ptr++;

		// parse channel of trace
		char *tmp = index(ptr, ':');                      // find next colon
		if (tmp == NULL)
			puts("Usage()"), exit(-1);
		*tmp = '\0';                                      // write '\0' there
		ret = sscanf(ptr, "%d", &(traces[tnr].channel));  // scan channel number
		if (ret < 1)
			puts("Usage()"), exit(-1);
		ptr = tmp + 1;                                    // set ptr to the beginning of the next token

		traces[tnr].output = ptr;
		if (ptr == '\0')
			puts("Usage()"), exit(-1);


		char **path = NULL;
		int plen = 0;
		if (traces[tnr].hdstats)
			path = parsePath(traces[tnr].output, &plen);

		// print parsed trace config
		printf("%d: ", tnr);
		if (traces[tnr].bin)
			printf("BINARY, ");
		if (traces[tnr].ascii)
			printf("ASCII, ");
		if (traces[tnr].hdstats)
			printf("HDSTATS, ");

		printf("Channel: %d", traces[tnr].channel);

		if (traces[tnr].hdstats) {
			printf(", Path: %s", path[0]);
			for (int i = 1; i < plen; ++i)
				printf(" - %s", path[i]);
		}
		if (traces[tnr].bin || traces[tnr].ascii) {
			printf(", Filename: %s", traces[tnr].output);
		}
		printf("\n");

		if (traces[tnr].hdstats) {
			// create topology node
			traces[tnr].tnode =
				hdT_createTopoNode(myTopology,(const char **) path, plen);
			/* TODO error checking */

			traces[tnr].group =
				hdS_createGroup("Energy", traces[tnr].tnode, 1);
			/* TODO error checking */

			ret = hdS_addValue(traces[tnr].group, "Utrms", FLOAT, "V", "Voltage");
			/* TODO error checking */
			ret = hdS_addValue(traces[tnr].group, "Itrms", FLOAT, "A", "Current");
			/* TODO error checking */
			ret = hdS_addValue(traces[tnr].group, "P", FLOAT, "W", "Power");
			/* TODO error checking */
			ret = hdS_commitGroup(traces[tnr].group);
			/* TODO error checking */

		}

		// free memory allocated by parsePath()
		free(path[0]);
		free(path);

		traces[tnr].actn = malloc(20 * sizeof(*(traces[tnr].actn)));
		sprintf(traces[tnr].actn, "UTRMS%d?;ITRMS%d?;P%d?",
				traces[tnr].channel, traces[tnr].channel, traces[tnr].channel);
		traces[tnr].size = (mode == MODE_BIN) ? (3 * 4) : (3 * 9 + 2);

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
    serial_fd = serial_openPort(port);
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
    SERIAL_SETUPPORT_RETURN_CHECK

    /*
     * Setup LMG
     */
    ret = LMG_setup(serial_fd);
    LMG_SETUP_RETURN_CHECK

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
        ERROR_UNKNOWN
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

    ret = trace_data(serial_fd, cycle, traces, ntraces, mode);
    printf("Returns %d\n", ret);
    /* TODO: error handling */

    puts("End tracing!");

    /*
     * Close files and free memory
     */
    for(int i = 0; i < ntraces; ++i)
    {
    	hdS_finalize(traces[i].group);
    	hdT_destroyTopoNode(traces[i].tnode);

    	free(traces[i].output); // allocated by sscanf
    	free(traces[i].actn);

    	free(traces);
    }
    hdT_destroyTopology(myTopology);


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
        ERROR_UNKNOWN
    }

    /*
     * Close LMG connection
     */
    ret = LMG_close(serial_fd);
    LMG_CLOSE_RETURN_CHECK

    /*
     * Close serial port
     */
    ret = serial_closePort(serial_fd);
    SERIAL_CLOSEPORT_RETURN_CHECK


}

/* vim: set sw=4 sts=4 et fdm=syntax: */
