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
	ConfigStruct *config = malloc(sizeof(*config));
	config->topology = NULL;
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
	}

	/*
	 * Override configuration file options with commandline options
	 */
#define	REPLACE_CONFIG(var) \
		if (var != NULL) { \
			if (config->allocated.var) { \
				pt_free(config->var); \
				config->allocated.var = 0; \
			} \
			config->var = var; \
		}
	REPLACE_CONFIG(port)
	REPLACE_CONFIG(project)
	REPLACE_CONFIG(topo)
#undef REPLACE_CONFIG

	/*
	 * Add traces from commandline
	 */
	int ntraces = argc - optind;
	ret = parseTraceStrings(ntraces, argv+optind, config);
	switch (ret) {
	case OK:
		break;
	case ERR_SYNTAX:
		printUsage();
		cleanupConfig(config);
		return ESYNTAX;
	case ERR_MALLOC:
		ERROR_OUTPUT("Out of memory.");
		cleanupConfig(config);
		return EMEMORY;
	default:
		assert(!"Unknown return value from parseTraceStrings()");
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

	PowerTrace *trace;
	ret = createTracingThread(config, 1, &trace);
	printf("createTracingThread() returned %d\n", ret);

	ret = pt_startTracing(trace);
	printf("startTracing() returned %d\n", ret);

	sleep(10);

	ret = pt_stopTracing(trace);
	printf("stopTracing() returned %d\n", ret);

	ret = pt_finalizeTrace(trace);
	printf("finalizeTrace() returned %d\n", ret);

}

#if 0
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
#endif


/* vim: set sw=4 sts=4 et fdm=syntax: */
