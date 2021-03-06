/**
 * @file pt.c
 *
 * @date 01.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <string.h>
#include <pthread.h>
#include <sys/types.h>
#include <signal.h>
#include <limits.h>
#include <assert.h>

#include "ptInternal.h"
#include "pt.h"
#include "common.h"
#include "conf.h"
#include "tracing.h"
#include "serial.h"
#include "LMG.h"
#include "topo.h"

/** @cond api_only */
/**
 * @typedef PowerTrace
 * @ingroup PT
 */
/**
 * @def PT_SUCCESS
 * @ingroup PT
 */
/**
 * @def PT_ECONFNOTFOUND
 * @ingroup PT
 */
/**
 * @def PT_ECONFINVALID
 * @ingroup PT
 */
/**
 * @def PT_ENOTRACES
 * @ingroup PT
 */
/**
 * @def PT_EMEMORY
 * @ingroup PT
 */
/**
 * @def PT_EHDLIB
 * @ingroup PT
 */
/**
 * @def PT_EDEVICE
 * @ingroup PT
 */
/**
 * @def PT_ETHREAD
 * @ingroup PT
 */
/** @endcond */

/** Verbosity */
int pt_verbosity = 0;

/** Direct Output */
int pt_directOutput = 0;

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
    INFOMSG("%s", buffer);

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

    INFOMSG("%.4E;%.4E;%.4E\n", op.utrms, op.itrms, op.p );

    return OK;
}

/**
 * Do tracing.
 *
 * This function is the main function used by the commandline tool and the
 *  library API in common after creating the configuration differently.
 *
 * @param trace  Power Trace
 *
 * @return Error state
 *
 * @retval PT_EDEVICE
 * @retval PT_EMEMORY
 */
static int doTracing(PowerTrace *trace) {

    ConfigStruct *config = trace->config;

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
            return PT_EDEVICE;
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
        return PT_EDEVICE;
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
        default:
            assert(!"Unknown return state of LMG_setup().");
        }
        serial_closePort(serial_fd);
        return PT_EDEVICE;
    }

    /*
     * Get and print identity string from LMG
     */
    ret = LMG_getIdentity(serial_fd, buffer, sizeof(buffer));
    if (ret != OK) {
    	switch(ret) {
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
            return PT_EMEMORY;
        case ERR_BSIZE:
            ERROR_OUTPUT("Buffer size to low while getting identity.");
            break;
        default:
            assert(!"Unknown return state of LMG_getIdentity().");
        }
        serial_closePort(serial_fd);
        return PT_EDEVICE;
    }

    INFOMSG("%s", buffer);

    memset(buffer, '\0', 255);

    ret = test_binmode(serial_fd);
    if (ret != OK) {
        switch(ret) {
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
            return PT_EMEMORY;
        case ERR_BSIZE:
            ERROR_OUTPUT("Buffer size to low while testing binary mode.");
            break;
        default:
            assert(!"Unknown return state of test_binmode().");
        }
        serial_closePort(serial_fd);
        return PT_EDEVICE;
    }


    INFO_OUTPUT("Start tracing!");

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
            return PT_EMEMORY;
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
        return PT_EDEVICE;
    }


    INFO_OUTPUT("End tracing!");



    /*
     * Get and print all errors from LMG
     */
    ret = LMG_getAllErrors(serial_fd, buffer, sizeof(buffer));
    if (ret != OK) {
        switch(ret) {
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
            return PT_EMEMORY;
        case ERR_BSIZE:
            ERROR_OUTPUT("Buffer size to low while getting all errors.");
            break;
        default:
            assert(!"Unknown return state of LMG_getAllErrors().");
        }
        serial_closePort(serial_fd);
        return PT_EDEVICE;
    }

    INFOMSG("%s", buffer);

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
        return PT_EDEVICE;
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
        return PT_EDEVICE;
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
 * @if api_only
 *  @ingroup PT
 * @endif
 *
 * Use this function to create a new power trace. It will setup a
 * new statistics trace with the configuration read from the file.
 *
 * For the format of the configuration file, please take a look at the
 *  example in \ref seclu
 *
 * The tracing will not start until \ref pt_startTracing is called for the
 * PowerTrace object returned by this function.
 *
 * @param configfile Name of the configuration file
 * @param topology   Topology to override default or config file choice (NULL not to override)
 * @param trace      Location to store the PowerTrace pointer (OUTPUT)
 *
 * @return  Error state
 *
 * @retval PT_SUCCESS        Success
 * @retval PT_ECONFNOTFOUND  Could not find configuration file
 * @retval PT_EMEMORY        Out of memory
 * @retval PT_ECONFINVALID   Configuration read from file is invalid
 * @retval PT_ENOTRACES      No traces found in configuration
 * @retval PT_EHDLIB         Error in HDTrace library
 * @retval PT_ETHREAD        Cannot create tracing thread
 */
int pt_createTrace(const char* configfile, hdTopology *topology, PowerTrace **trace) {

	int ret;

	/*
	 * Set verbosity as requested by environment
	 */
	pt_verbosity = 1;
	char *verbstr = getenv("PT_VERBOSITY");
	if (verbstr != NULL)
		sscanf(verbstr, "%d", &pt_verbosity);

	DEBUGMSG("Verbosity: %d", pt_verbosity);

	if (configfile == NULL)
		return PT_ECONFNOTFOUND;


	/*
	 * Initialize configuration
	 */
	ConfigStruct *config;
	pt_malloc(config, 1, PT_EMEMORY);

	config->topology = topology;
	config->allocated.topology = (topology == NULL) ? 0 : 1;

	config->allocated.device = 0;
	config->allocated.port = 0;
	config->allocated.project = 0;
	config->allocated.topo = 0;

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
		return PT_EMEMORY;
	case ERR_FILE_NOT_FOUND:
		ERROR_OUTPUT("Configuration file not found.");
		cleanupConfig(config);
		return PT_ECONFNOTFOUND;
	case ERR_ERRNO:
		ERROR_OUTPUT("Problem while processing configuration file: %s", strerror(errno));
		cleanupConfig(config);
		return PT_ECONFINVALID;
	case ERR_SYNTAX:
		ERROR_OUTPUT("Configuration file invalid.");
		cleanupConfig(config);
		return PT_ECONFINVALID;
	default:
		assert(!"Unknown return value from readConfigFromFile()");
	}

	/*
	 * Do consistency check of the final configuration
	 */
	ret = checkConfig(config);
	switch (ret) {
	case OK:
		INFO_OUTPUT("Consistency check of configuration PASSED.");
		break;
	default:
		ERROR_OUTPUT("Consistency check of configuration FAILED. Check warnings.");
		cleanupConfig(config);
		return PT_ECONFINVALID;
	}



	/*
	 * Print configuration
	 */
	INFO_OUTPUT("Device: %s", config->device);
	INFO_OUTPUT("Host: %s", config->host == NULL ? "NULL" : config->host);
	INFO_OUTPUT("Port: %s", config->port);
	INFO_OUTPUT("Project: %s", config->project);


	/*
	 * Check if we are on the configured host if any
	 */
	if (config->host != NULL) {
		char hostname[HOST_NAME_MAX+1];
		gethostname(hostname, HOST_NAME_MAX+1);

		if (strcmp(hostname, config->host) != 0) {
			WARNMSG("Hostname found in configuration (%s) does not match"
					" this machine (%s)", config->host, hostname);
			cleanupConfig(config);
			return PT_EWRONGHOST;
		}
	}

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
		return PT_ENOTRACES;
	case ERR_MALLOC:
		ERROR_OUTPUT("Out of memory while creating traces.");
		cleanupConfig(config);
		return PT_EMEMORY;
	case ERR_HDLIB:
		ERROR_OUTPUT("Error occurred in hdTraceWritingLibrary while creating traces.");
		cleanupConfig(config);
		return PT_EHDLIB;
	default:
		assert(!"Unknown return value from createTraces()");
	}

	return createTracingThread(config, trace);

}

int createTracingThread(ConfigStruct *config, PowerTrace **trace) {

	int ret;

	/*
	 * Create PowerTrace object
	 */
	pt_malloc(*trace, 1, PT_EMEMORY);
	(*trace)->config = config;

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
			return PT_ETHREAD;
		default:
			assert(!"Unknown return value from pthread_create().");
		}
	}

	return PT_SUCCESS;
}

static void * doTracingThread(void *param) {

	PowerTrace *trace = (PowerTrace *) param;

	/* block all signals, this thread should never handle them */
	sigset_t mask;
	sigfillset(&mask);
	pthread_sigmask(SIG_BLOCK, &mask, NULL);

	/* get memory for returning thread status */
	struct tracingThreadReturn_s *ret;
	pt_malloc(ret, 1, NULL);

	/* do actual tracing */
	ret->ret = doTracing(trace);

	/* if running stand alone, send a signal to ourself to terminate waiting
	 *  loop if not already happened by a user sent SIGINT */
	if (pt_directOutput)
		kill(getpid(), SIGINT);

	return (void *) ret;
}


/**
 * Return the hostname with the measuring device connected
 *  if specified in config file.
 *
 * @if api_only
 *  @ingroup PT
 * @endif
 *
 * @param trace  Power trace object
 *
 * @return Hostname of NULL if none specified in config file
 */
char *pt_getHostname(PowerTrace *trace) {
	return trace->config->host;
}


/**
 * Start the power tracing
 *
 * @if api_only
 *  @ingroup PT
 * @endif
 *
 * @param trace  Power trace object
 *
 * @return Indicate if tracing state changed
 *
 * @retval  0  Tracing is now started (was stopped before)
 * @retval  1  Tracing is started (as it was already before)
 * @retval  2  Parameter is NULL
 */
int pt_startTracing(PowerTrace *trace) {
	if (trace == NULL)
		return 2;

	/* TODO: Implement return value as in RUT */

	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.started = 1;
	pthread_cond_signal(&(trace->control.cond));
	pthread_mutex_unlock(&(trace->control.mutex));

	return 0;
}

/**
 * Stop the power tracing
 *
 * @if api_only
 *  @ingroup PT
 * @endif
 *
 * @param trace  Power trace object
 *
 * @return Indicate if tracing state changed
 *
 * @retval  0  Tracing is now stopped (was started before)
 * @retval  1  Tracing is stopped (as it was already before)
 * @retval  2  Parameter is NULL
 */
int pt_stopTracing(PowerTrace *trace) {
	if (trace == NULL)
		return 2;

	/* TODO: Implement return value as in RUT */

	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.started = 0;
	pthread_mutex_unlock(&(trace->control.mutex));

	return 0;
}

/**
 * Finalize and free a power trace
 *
 * @if api_only
 *  @ingroup PT
 * @endif
 *
 * @param trace  Power trace object
 *
 * @return  Error state
 *
 * @retval PT_SUCCESS  Success
 * @retval PT_EDEVICE  Problem during communication with measurement device
 * @retval PT_EMEMORY  Out of memory
 */
int pt_finalizeTrace(PowerTrace *trace) {

	pthread_mutex_lock(&(trace->control.mutex));
	trace->control.terminate = 1;
	pthread_mutex_unlock(&(trace->control.mutex));

	struct tracingThreadReturn_s *threadRet;
	pthread_join(trace->thread, (void **) &threadRet);

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
