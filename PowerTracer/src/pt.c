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

#define TYPE_ASCII 0
#define TYPE_BIN   1

/*
 * Structure for trace file description
 */
typedef struct
{
    char *actn;         /* action script part for this trace */
    int   size;         /* size of expected response in [byte] */
    hdStatsGroup group; /* statistics group this belongs to */
} TRACE;

/*
 * Start time storage
 */
static struct timeval tv_start;

/* read data and log into file */
static
int trace_iteration(int serial_fd, TRACE *traces, int ntraces, int type)
{
    int ret;

    assert(type = TYPE_BIN);

    static size_t isize = 0; /* Calculate input size only once */
    if (isize == 0)
    {
        for (int i = 0; i < ntraces; ++i)
            isize += traces[i].size;
        if (type == TYPE_ASCII)
            isize += ntraces; /* separating ';' and trailing '\0' */
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
        int serial_fd,   /* file descriptor of serial port */
        float cycle,     /* cycle length in seconds */
        TRACE *traces,   /* array of trace descriptions */
        int ntraces,      /* length of traces array */
        int type         /* type of trace file (ASCII/BIN) */
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
    if(type != TYPE_ASCII && type != TYPE_BIN)
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
    switch(type)
    {
        case TYPE_BIN:
            ret = serial_sendMessage(serial_fd, "FRMT PACKED");
            SERIAL_SENDMESSAGE_RETURN_CHECK
            break;
        case TYPE_ASCII:
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
            ret = trace_iteration(serial_fd, traces, ntraces, type);
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

int main(void)
{
    int ret;

    int serial_fd;

    char buffer[255];  /* Input buffer */

    /*
     * Take start time
     */
    ret = gettimeofday(&tv_start, NULL);

    /*
     * Open serial port
     */
    serial_fd = serial_openPort("/dev/ttyUSB0");
    if(serial_fd < 0)
        return(serial_fd);

    /*
     * Setup serial port
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

    /*
     * Define topology
     */
    const char *levels[] = {"Host", "Process", "Thread"};
    hdTopology myTopology =
             hdT_createTopology("MyProject", levels, 3);
    /* TODO error checking */


    /*
     * Define traces
     */
    int type = TYPE_BIN;
    size_t ntraces = 2;
    float cycle = 0.05;
    TRACE traces[ntraces];
    hdTopoNode nodes[ntraces];
    const char *host[][1] = { { "host0" }, { "host1" }, { "host2" }, { "host3" } };

	for (int i = 0; i < ntraces; ++i) {
		nodes[i] = hdT_createTopoNode(myTopology, host[i] , 1);
		/* TODO error checking */

		traces[i].group = hdS_createGroup("Energy", nodes[i], 1);
		/* TODO error checking */

        traces[i].actn = "UTRMS1?;ITRMS1?;P1?";
        traces[i].size = (type == TYPE_BIN) ? (3 * 4) : (3 * 9 + 2);

        ret = hdS_addValue(traces[i].group, "Utrms", FLOAT, "V", "Voltage");
		/* TODO error checking */
        ret = hdS_addValue(traces[i].group, "Itrms", FLOAT, "A", "Current");
        /* TODO error checking */
        ret = hdS_addValue(traces[i].group, "P", FLOAT, "W", "Power");
        /* TODO error checking */

        ret = hdS_commitGroup(traces[i].group);
        /* TODO error checking */
	}


    puts("Start tracing!");

    ret = trace_data(serial_fd, cycle, traces, ntraces, type);
    printf("Returns %d\n", ret);
    /* TODO: error handling */

    puts("End tracing!");

    /*
     * Close files
     */
    for(int i = 0; i < ntraces; ++i)
    {
    	hdS_finalize(traces[i].group);
    	hdT_destroyTopoNode(nodes[i]);
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
