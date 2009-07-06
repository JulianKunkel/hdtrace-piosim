/**
 * @file tracing.c
 *
 * @date 02.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include <stdlib.h>
#include <string.h>
#include <assert.h>


#include "conf.h"
#include "trace.h"
#include "ptError.h"
#include "ptInternal.h"
#include "LMG.h"
#include "serial.h"

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
int traceIteration(int serial_fd, ConfigStruct *config)
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
 * @retval OK           Success
 * @retval ERR_TIMEOUT  Timeout while getting data from device
 * @retval -->          \ref serial_sendMessage()
 * @retval -->          \ref trace_iteration()
 */
int traceLoop(
        int serial_fd,      /* file descriptor of serial port */
        PowerTrace *trace) {

	ConfigStruct *config = trace->config;

    int ret;

    fd_set input;
    struct timeval timeout;
    int n;

    /* check value of cycle time */
    assert(config->cycle > 50 && config->cycle < 60000);

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
    sprintf(buffer, "CYCL %f", ((float) config->cycle) / 1000);
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
     * Tracing iterations loop
     */

    /* should the traces become enabled/disabled ? */
    int enable_traces = 0;
    int disable_traces = 0;

    /* terminate on error with retval */
    int terminate = 0;
    int retval = 0;

    /* are the traces and the device enabled? */
    int enabled = 0;

    while(1)
    {
    	/* error handling */
    	if (terminate) {
    		/* try to disable traces and device */
       		if (enabled) {
       			FOR_TRACES(config->traces) {
       				hdS_disableGroup(trace->group);
       			}
       			serial_sendMessage(serial_fd, "CONT OFF");
       		}
       		break;
     	}


    	pthread_mutex_lock(&(trace->control.mutex));

    	/* if the thread is stopped but the traces are enabled, disable them */
    	if (!trace->control.started && enabled) {
    		disable_traces = 1;
    	}

    	/* else wait until the thread is started or started again */
    	else {
    		while (!(trace->control.started || trace->control.terminate)) {
    			assert(!enabled);
    			assert(!enable_traces);
    			assert(!disable_traces);
    			pthread_cond_wait(&(trace->control.cond),
    					&(trace->control.mutex));
    		}

        	/* if the thread is started but the traces are disabled, enable them */
        	if (trace->control.started && !enabled) {
        		enable_traces = 1;
        	}

        	/* if termination requested, disable traces and terminate */
        	if (trace->control.terminate) {
        		if (enabled)
        			disable_traces = 1;
        		else
        			break;
        	}
    	}
    	pthread_mutex_unlock(&(trace->control.mutex));

    	/* enable and disable makes no sense together */
    	assert(!(enable_traces && disable_traces));

    	/* enable traces if necessary */
    	if (enable_traces) {

    		/* enable statistic groups */
    		FOR_TRACES(config->traces) {
    			ret = hdS_enableGroup(trace->group);
    			assert(ret >= 0);
    		}

    		/* reset serial bus to clear input buffer */
    		ret = serial_resetPort(serial_fd);

    		/* start continues mode */
    		ret = serial_sendMessage(serial_fd, "CONT ON");
   		    switch(ret) {
   		    case OK:
   		    	break;
   		    case ERR_ERRNO: /* write() */
   		    case ERR_WRITE:
   		    	retval = ret;
   		    	terminate = 1;
   		    	continue;
	        default:
	        	assert(!"Unknown return value from serial_sendMessage().");
   		    }

    		enable_traces = 0;
    		enabled = 1;
    	}

    	/* disable traces if necessary */
    	if (disable_traces) {

    		/* disable statistic groups */
    		FOR_TRACES(config->traces) {
    			ret = hdS_disableGroup(trace->group);
    			assert(ret >= 0);
    		}

    		/* start continues mode */
    		ret = serial_sendMessage(serial_fd, "CONT OFF");
   		    switch(ret) {
   		    case OK:
   		    	break;
   		    case ERR_ERRNO: /* write() */
   		    case ERR_WRITE:
   		    	retval = ret;
   		    	terminate = 1;
   		    	continue;
	        default:
	        	assert(!"Unknown return value from serial_sendMessage().");
   		    }

    		disable_traces = 0;
    		enabled = 0;

    		/* restart loop to wait for next start */
    		continue;
    	}

        /* Initialize the input set */
        FD_ZERO(&input);
        FD_SET(serial_fd, &input);

        /* Initialize the timeout structure */
        // TODO calculate timeout using cycle time
        timeout.tv_sec  = 2;
        timeout.tv_usec = 0;

        /* Do the select */
        n = select(serial_fd+1, &input, NULL, NULL, &timeout);
        if (n < 0)
        {
        	ERROR_ERRNO("select()");
        	retval = ERR_ERRNO;
        	terminate = 1;
        	continue;
        }
        else if (n == 0)
        {
            /* timeout */
        	ERROR("select() timeout");
        	retval = ERR_TIMEOUT;
        	terminate = 1;
        	continue;
        }

        /* input from device ready */
        if (FD_ISSET(serial_fd, &input))
        {
            ret = traceIteration(serial_fd, config);
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
                    retval = ret;
                    terminate = 1;
                    continue;
                default:
					assert(!"Unknown return value from trace_iteration()");
            }
        }

    }

    return retval;
}
