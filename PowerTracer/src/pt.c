#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <string.h>
#include <pthread.h>
#include <assert.h>

#include "pt.h"
#include "ptError.h"
#include "ptInternal.h"
#include "conf.h"
#include "tracing.h"
#include "serial.h"
#include "LMG.h"
#include "hdTopo.h"
#include "hdStats.h"
#include "hdError.h"


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

    char buffer[255] = { 0 };

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
 */
static int doTracing(PowerTrace *trace) {

	ConfigStruct *config = trace->config;
	int directOutput = trace->directOutput;

	int ret;

	int serial_fd;
    char buffer[255] = { 0 };  /* Input buffer */

    /*
     * Open serial port
     */
    serial_fd = serial_openPort(config->port);
    if(serial_fd < 0) {
    	switch (serial_fd) {
    	case ERR_ERRNO:
    		ERROR_OUTPUT("Problem while opening serial port: %s", strerror(errno));
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
		    	serial_closePort(serial_fd);
		    	return EMEMORY;
			case ERR_BSIZE:
				ERROR_OUTPUT("Buffer size to low while getting identity.");
				break;
			default:
				assert(!"Unknown return state of LMG_getIdentity().");
		}
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
		    	serial_closePort(serial_fd);
		    	return EMEMORY;
	        case ERR_BSIZE:
	        	ERROR_OUTPUT("Buffer size to low while testing binary mode.");
	            break;
	        default:
				assert(!"Unknown return state of test_binmode().");
	    }
	    serial_closePort(serial_fd);
	    return EDEVICE;
    }


    puts("Start tracing!");

    ret = traceLoop(serial_fd, trace);
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
			serial_closePort(serial_fd);
			return EMEMORY;
		case ERR_BSIZE:
			ERROR_OUTPUT("Buffer size to low while tracing data.");
			break;
		case ERR_TIMEOUT:
			ERROR_OUTPUT("Timeout while tracing data.");
			break;
		default:
			assert(!"Unknown return value from trace_data().");
    	}
    	serial_closePort(serial_fd);
    	return EDEVICE;
    }


    puts("End tracing!");



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


/* ************************************************************************ *
 * Library API functions
 */

struct tracingThreadReturn_s {
	int ret;
};

static void * doTracingThread(void *param);

/**
 * Create a power trace using the passed configuration file
 *
 * @param configfile Name of the configuration file
 * @param trace      Location to store the PowerTrace pointer (OUTPUT)
 *
 * @return  Error state
 */
int pt_createTrace(const char* configfile, PowerTrace **trace) {

	int ret;

	if (configfile == NULL)
		return ECONFNOTFOUND;

	int directOutput = 0;

	/*
	 * Initialize configuration
	 */
	ConfigStruct *config;
	pt_malloc(config, 1, EMEMORY);

	config->topology = NULL;

	/*
	 * Set defaults
	 */
	config->device = "LMG450";
    config->mode = MODE_BIN;
	config->host = NULL;
	config->port = "/dev/ttyUSB0";
	config->cycle = 100;
	config->project = "MyProject";
	config->topo = "Host_Process_Thread";
	config->traces.last = NULL;

	/*
	 * Read configuration file if given
	 */
	ret = readConfigFromFile(configfile, config);
	switch (ret) {
	case OK:
		break;
	case ERR_MALLOC:
		ERROR_OUTPUT("Out of memory while reading configuration file.");
		cleanupConfig(config);
		return EMEMORY;
	case ERR_FILE_NOT_FOUND:
		ERROR_OUTPUT("Configuration file not found.");
		cleanupConfig(config);
		return ECONFNOTFOUND;
	case ERR_ERRNO:
		ERROR_OUTPUT("Problem while processing configuration file: %s", strerror(errno));
		cleanupConfig(config);
		return ECONFINVALID;
	case ERR_SYNTAX:
		ERROR_OUTPUT("Configuration file invalid.");
		cleanupConfig(config);
		return ECONFINVALID;
	default:
		assert(!"Unknown return value from readConfigFromFile()");
	}

	/*
	 * Do consistency check of the final configuration
	 */
	ret = checkConfig(config);
	switch (ret) {
	case OK:
		break;
	default:
		ERROR_OUTPUT("Configuration is not valid.");
		cleanupConfig(config);
		exit(-1);
	}


	/*
	 * Print configuration
	 */
	printf("Device: %s\n", config->device);
	printf("Host: %s\n", config->host == NULL ? "NULL" : config->host);
	printf("Port: %s\n", config->port);
	printf("Project: %s\n", config->project);


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

	return createTracingThread(config, 0, trace);

}

int createTracingThread(ConfigStruct *config, int directOutput,
		PowerTrace **trace) {

	int ret;

	/*
	 * Create PowerTrace object
	 */
	pt_malloc(*trace, 1, EMEMORY);
	(*trace)->config = config;
	(*trace)->directOutput = directOutput;

	(*trace)->control.started = 0;
	(*trace)->control.terminate = 0;
	ret = pthread_cond_init(&((*trace)->control.cond), NULL);
	ret = pthread_mutex_init(&((*trace)->control.mutex), NULL);

	/*
	 * Create tracing thread
	 */
	ret = pthread_create(&((*trace)->thread), NULL, doTracingThread,
			(void *) *trace);
	if (ret != 0) {
		switch (errno) {
		case EAGAIN:
			return ETHREAD;
		default:
			assert(!"Unknown return value from pthread_create().");
		}
	}

	return OK;
}

static void * doTracingThread(void *param) {

	PowerTrace *trace = (PowerTrace *) param;

	struct tracingThreadReturn_s *ret;
	pt_malloc(ret, 1, NULL);

	ret->ret = doTracing(trace);

	return (void *) ret;
}


/**
 * Return the hostname with the measuring device connected
 */
char *pt_getHostname(PowerTrace *trace) {
	return trace->config->host;
}

/**
 * Start the power tracing
 */
int pt_startTracing(PowerTrace *trace) {
	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.started = 1;
	pthread_cond_signal(&(trace->control.cond));
	pthread_mutex_unlock(&(trace->control.mutex));
}

/**
 * Stop the power tracing
 */
int pt_stopTracing(PowerTrace *trace) {
	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.started = 0;
	pthread_mutex_unlock(&(trace->control.mutex));
}

/**
 * Finalize and free a power trace
 */
int pt_finalizeTrace(PowerTrace *trace) {

	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.terminate = 1;
	pthread_mutex_unlock(&(trace->control.mutex));

	struct tracingThreadReturn_s *threadRet;
	pthread_join(trace->thread, (void *) &threadRet);

	int retval = threadRet->ret;

	/* free memory */
	cleanupConfig(trace->config);
	pt_free(threadRet);
	pt_free(trace->config);
	pt_free(trace);

	return retval;
}

#undef ERROR_OUTPUT
/* vim: set sw=4 sts=4 et fdm=syntax: */
