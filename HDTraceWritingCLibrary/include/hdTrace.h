/**
 * @file hdTrace.h
 * @ingroup hdTrace
 *
 * Declarations of all functions and types for writing statistics
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */


/**
 * @addtogroup hdTrace HDTrace Writing Library
 *
 * @details
 * \....
 */

#ifndef HDTRACE_H_
#define HDTRACE_H_

#include <stdio.h>
#include <sys/time.h>

#include "hdError.h"      /* Include error definitions */
#include "hdTopo.h"       /* Include topology stuff */

/*
 * Defines of sizes
 */
/**
 * TODO: Description
 */
#define HD_LOG_BUF_SIZE (1024 * 1024)
//#define HD_LOG_BUF_SIZE (1024)

/**
 * TODO: Description
 */
#define HD_LOG_TAB_STRING "  "

/**
 * TODO: Description
 */
#define HD_LOG_COMMAND_BUF_SIZE 1024 * 16

/**
 * TODO: Description
 */
#define HD_LOG_ELEMENT_NAME_BUF_SIZE 1024

/**
 * TODO: Description
 */
#define HD_TMP_BUF_SIZE 1024 * 16

/**
 * TODO: Description
 */
#define HD_LOG_MAX_DEPTH 4


/**
 * Structure representing one trace
 */
struct _hdTrace {
	/**
	 * File descriptor of the XML file
	 */
	int log_fd;

	/**
	 * File descriptor for info file
	 */
	int info_fd;

	/**
	 * Name of the log file (for error output)
	 */
	char *logfile;

	/**
	 * Name of the info file (for error output)
	 */
	char *infofile;

	/**
	 * TODO: Description
	 */
	char state_name[HD_LOG_MAX_DEPTH][HD_LOG_ELEMENT_NAME_BUF_SIZE];

	/**
	 * TODO: Description
	 */
	char attributes[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];

	/**
	 * TODO: Description
	 */
	size_t attributes_pos[HD_LOG_MAX_DEPTH];

	/**
	 * TODO: Description
	 */
	char elements[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];

	/**
	 * TODO: Description
	 */
	size_t elements_pos[HD_LOG_MAX_DEPTH];

	/**
	 * TODO: Description
	 */
	char buffer[HD_LOG_BUF_SIZE];

	/**
	 * TODO: Description
	 */
	size_t buffer_pos;

	/**
	 * TODO: Description
	 */
	struct timeval start_time[HD_LOG_MAX_DEPTH];

	/**
	 * TODO: Description
	 */
	struct timeval end_time[HD_LOG_MAX_DEPTH];

	/**
	 * Keeps track of the depth of nested function calls
	 */
	int function_depth;

	/**
	 * Topology leaf this trace belongs to.
	 */
	hdTopoNode topoNode;

	/**
	 * TODO: Description
	 */
	int trace_enable;

	/**
	 * TODO: Description
	 */
	int always_flush;

	/**
	 * TODO: Description
	 */
	int trace_nested_operations;

	/**
	 * TODO: Description
	 */
	int has_nested[HD_LOG_MAX_DEPTH];
    /* has_nested[i] = 1 if functions with depth i+1 have been logged.
	 * otherwise 0. the variable is reset after hdT_StateEnd is called with
	 * function_depth == i
	 */
};

/**
 * Type to use for traces
 */
typedef struct _hdTrace * hdTrace;


/**
 * Create, open and initialize trace for given topology.
 */
hdTrace hdT_createTrace(
		hdTopoNode topoNode,
		hdTopology topology
		);

/**
 * Set depth of nested operations to log into trace
 */
int hdT_TraceNested(
		hdTrace trace,
		int depth
		);

/**
 * Enable/disable trace writing
 */
int hdT_Enable(
		hdTrace trace,
		int enable
		);

/**
 * Set flushing behavior for trace file
 */
int hdT_ForceFlush(
		hdTrace trace,
		int flush
		);

/**
 * Log Element
 */
int hdT_LogElement(
		hdTrace trace,
		const char * name,
		const char* valueFormat,
		...
		);

/**
 * Write info message to trace (printf like).
 */
int hdT_LogInfo(
		hdTrace trace,
		const char * message,
		...
		);

/**
 * Log Attributes
 */
int hdT_LogAttributes(
		hdTrace trace,
		const char* valueFormat,
		...
		);

/**
 * Mark the start of a new state
 */
int hdT_StateStart(
		hdTrace trace,
		const char * stateName
		);

/**
 * Mark the end of a state and write it
 */
int hdT_StateEnd(
		hdTrace trace
		);

/**
 * Not yet implemented
 */
int hdT_EventStart(
		hdTrace trace,
		char * eventName );

/**
 * Not yet implemented
 */
int hdT_EventEnd(
		hdTrace trace,
		char* sprinhdStringForFurtherValues,
		...
		);

/*
 * Finalize and close trace.
 */
int hdT_Finalize(
		hdTrace trace
		);

#endif /* HDTRACE_H_ */
