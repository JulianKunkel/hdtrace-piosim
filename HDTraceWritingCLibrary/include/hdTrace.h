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
 * This constant determines the size of the trace writing buffer.
 * If flushing is not forced via hdT_setForceFlush(), the buffer
 * is flushed only after \a HD_LOG_BUF_SIZE characters have been
 * written
 */
#define HD_LOG_BUF_SIZE (1024 * 1024)

/**
 * The xml trace file is formatted for better readability.
 * This string is used to indent different lines.
 * "\t" or "  " are the most obvious choices
 */
#define HD_LOG_TAB_STRING "  "

/**
 * This is the number of characters that can be used for
 * writing xml elements between a \a hdT_logStateStart(...) and
 * \a hdT_logStateEnd(...)
 */
#define HD_LOG_COMMAND_BUF_SIZE 1024 * 16

/**
 * This is the maximum length of the string that holds the name
 * of an element that is logged using \a hdT_logElement(...)
 */
#define HD_LOG_ELEMENT_NAME_BUF_SIZE 1024

/**
 * Size of temporary buffers.
 */
#define HD_TMP_BUF_SIZE 1024 * 16

/**
 * Maximum depth of nested tracing calls that is logged.
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
     * the initial time when the trace file was opened
     */
	struct timeval init_time;

	/**
	 * Name of the log file (for error output)
	 */
	char *logfile;

	/**
	 * Name of the info file (for error output)
	 */
	char *infofile;

	/**
	 * This array of strings is used to save the name of the
	 * last state that has been started via \a hdT_logStateStart(...)
	 * in a certain level.
	 * For example, calling
	 * \code
	 * hdT_logStateStart(trace, "A");
	 * hdT_logStateStart(trace, "B");
	 * hdT_logStateEnd(trace);
	 * hdT_logStateEnd(trace);
	 * \endcode
	 * will lead to
	 * \code
	 * state_name[0] = "A"
	 * state_name[1] = "B"
	 * \endcode
	 */
	char state_name[HD_LOG_MAX_DEPTH][HD_LOG_ELEMENT_NAME_BUF_SIZE];

	/**
	 * This array of strings is used to buffer the attribute
	 * string for the corresponding level. That means,
	 * \a attributes[i] holds the attribute string for the
	 * i-th level of nesting.
	 */
	char attributes[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];

	/**
	 * This array holds the position, at which the writing has
	 * stopped in tha attributes string with the corresponding level.
	 */
	size_t attributes_pos[HD_LOG_MAX_DEPTH];

	/**
	 * This array of strings holds the element string that has
	 * been written via \a hdT_logElement(...).
	 * \a elements[i] holds the string for the i-th level
	 * of nesting
	 */
	char elements[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];

	/**
	 * \a elements_pos[i] holds the write position for \a elements[i]
	 */
	size_t elements_pos[HD_LOG_MAX_DEPTH];

	/**
	 * This is the write buffer of the trace file. Data is not written
	 * directly to the output file but to this buffer, unless
	 * flushing is forced (\a hdT_setForceFlush(...))
	 */
	char buffer[HD_LOG_BUF_SIZE];

	/**
	 * This variable keeps track of the position at which writing
	 * can occur on \a buffer.
	 */
	size_t buffer_pos;

	/**
	 * \a start_time[i] holds the start time of the last logged tate in
	 * the i-th nesting level.
	 */
	struct timeval start_time[HD_LOG_MAX_DEPTH];

	/**
	 * \a end_time[i] holds the ending time of the last
	 * logged state in the i-th nesting level.
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
	 * \a isEnabled = 1 if the trace is enabled.
	 * \a isEnabled = 0 if the trace is desabled.
	 *
	 * Enabling and disabling can be done via
	 * \a hdT_enableTrace(...) and \a hdT_disableTrace(...)
	 */
	int isEnabled;

	/**
	 * if \a always_flush = 0,  \a buffer is used to cache
	 * write operations on the trace file. Otherwise, every write
	 * operation is performed via the \a write function
	 */
	int always_flush;

	/**
	 * TODO: Description
	 */
	int trace_nested_operations;

    /**
     * \a has_nested[i] = 1 if functions with depth i+1 have been logged.
	 * otherwise 0. The variable is reset after \a hdT_logStateEnd is called with
	 * \a function_depth == i.
	 *
	 * In other words, \a has_nested[i] == 1 if and only if the
	 * state with nesting level \a i has nested operations.
	 */
	int has_nested[HD_LOG_MAX_DEPTH];
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
int hdT_setNestedDepth(
		hdTrace trace,
		int depth
		);

/**
 * Enable trace.
 */
int hdT_enableTrace(hdTrace trace);

/**
 * Disable trace.
 */
int hdT_disableTrace(hdTrace trace);

/**
 * Get if trace is enabled.
 */
int hdT_isEnabled(hdTrace trace);

/**
 * Set flushing behavior for trace file
 */
int hdT_setForceFlush(
		hdTrace trace,
		int flush
		);

/**
 * Log Element
 */
int hdT_logElement(
		hdTrace trace,
		const char * name,
		const char* valueFormat,
		...
		) __attribute__ ((format (printf, 3, 4)));

/**
 * Write info message to trace (printf like).
 */
int hdT_writeInfo(
		hdTrace trace,
		const char * message,
		...
	) __attribute__ ((format (printf, 2, 3)));

/**
 * Log Attributes
 */
int hdT_logAttributes(
		hdTrace trace,
		const char* valueFormat,
		...
		) __attribute__ ((format (printf, 2, 3)));

/**
 * Mark the start of a new state
 */
int hdT_logStateStart(
		hdTrace trace,
		const char * stateName
		);

/**
 * Mark the end of a state and write it
 */
int hdT_logStateEnd(
		hdTrace trace
		);

/**
 * Not yet implemented
 */
int hdT_logEventStart(
		hdTrace trace,
		char * eventName );

/**
 * Not yet implemented
 */
int hdT_logEventEnd(
		hdTrace trace,
		char* sprinhdStringForFurtherValues,
		...
		) __attribute__ ((format (printf, 2, 3)));

/*
 * Finalize and close trace.
 */
int hdT_finalize(
		hdTrace trace
		);

#endif /* HDTRACE_H_ */
