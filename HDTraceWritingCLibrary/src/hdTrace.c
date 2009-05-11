/**
 * @file hdTrace.c
 * @ingroup hdTrace
 *
 * Implementations of all functions for writing statistics
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include "hdTrace.h"

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdarg.h>
#include <string.h>
#include <errno.h>
#include <assert.h>

#include "hdError.h"
#include "common.h"
#include "util.h"


/**
 * Macro to print debugging message (printf like) (was tprintf)
 * TODO: better adjust to new topology concept
 */
#define hdt_debugf(trace, format, ...) \
	printf("D: [TRACER][%s] %s (%s:%d): " format "\n", \
			hdT_getTopoPathString(trace->topoNode), \
			__FUNCTION__, __FILE__, __LINE__, __VA_ARGS__);

/**
 * Macro to print debugging message (fix string) (was tsprintf)
 */
#define hdt_debug(trace, string) \
	printf("D: [TRACER][%s] %s (%s:%d): %s\n", \
			hdT_getTopoPathString(trace->topoNode), \
			__FUNCTION__, __FILE__, __LINE__, string);

/**
 * Macro to print info message (printf like)
 * TODO: better adjust to new topology concept
 */
#define hdt_infof(trace, format, ...) \
	printf("I: [TRACER][%s]: " format "\n", \
			hdT_getTopoPathString(trace->topoNode), __VA_ARGS__);

/**
 * Macro to print info message (fix string)
 */
#define hdt_info(trace, string) \
	printf("I: [TRACER][%s]: %s\n", \
			hdT_getTopoPathString(trace->topoNode), string);


//////////////////////////////////
// Static function declarations //
//////////////////////////////////

/**
 * sprintf like function writing to trace log instead of string
 */
static int writeLogf(hdTrace trace, const char * format, ...)
__attribute__ ((format (printf, 2, 3)));

/**
 * vsprintf like function writing to trace log instead of string
 */
static int writeLogfv(hdTrace trace, const char * format,
		va_list valist);

/**
 * Write a message to the trace log buffer and flush to file if needed.
 */
static int writeLog(hdTrace trace, const char * message);

/**
 * Flush the buffer of trace log
 */
static int flushLog(hdTrace trace);

/**
 * Write given number of indentations to trace log
 */
static int writeLogIndentation(hdTrace trace, int count);

/**
 * Write state to trace log
 */
static int writeState(hdTrace trace);


/////////////////////////////////////
// Public function implementations //
/////////////////////////////////////

/**
 * Create, open and initialize trace for given topology.
 *
 * This function creates and opens the trace files for the given topology. The
 * filenames are built using the rules for hdTrace files an the given topology.
 *
 * For example:
 * @code
 * hdTopoNode myTopo = hdT_createTopoNode("myhost", "myrank", "mythread");
 * hdTopology myTopoNames = hdT_createTopology("Host", "Rank", "Thread");
 * hdT_createTrace(myTopo, myTopoNames);
 * @endcode
 * creates the following files:
 * - \c Project_myhost_myrank_mythread.trc as trace log file
 * - \c Project_myhost_myrank_mythread.info as trace info file
 * - \c ... (TODO)
 *
 * TODO: Where to get Project?
 *
 * @return Trace which must be used for further input or NULL on error
 *
 * @retval hdTrace  created trace (hides a pointer to the descriptive struct of the trace)
 * @retval NULL     on error setting errno
 *
 * @errno
 * - @ref HD_ERR_INVALID_ARGUMENT
 * - @ref HD_ERR_MALLOC
 * - @ref HD_ERR_CREATE_FILE
 *
 * @sa hdT_createTopoNode, hdT_createTopology
 */
hdTrace hdT_createTrace(hdTopoNode topoNode, hdTopology topology)
{
	/* good to know that hdTopoNode is the same as hdTopology ;) */
	if (hdT_getTopoNodeLevel(topoNode) <= 0
			|| hdT_getTopoNodeLevel(topoNode)
			!= hdT_getTopoDepth(topology) - 1)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* create trace file structure */
	hdTrace trace = malloc(sizeof(*trace));
	if (!trace)
	{
		errno = HD_ERR_MALLOC;
		return NULL;
	}

	/*
	 * Create trace and info file
	 */

	// generate filename
	trace->logfile = generateFilename(topology->project, topoNode,
			hdT_getTopoNodeLevel(topoNode), NULL, ".trc");
	assert(isValidString(trace->logfile));

	// create and open file
	trace->log_fd = open(trace->logfile,
			O_CREAT | O_WRONLY | O_TRUNC | O_NONBLOCK, 0662);
	if (trace->log_fd == -1)
	{
		hdt_debugf(trace, "Could not open file %s: %s",
				trace->logfile, strerror(errno));
		hd_error_return(HD_ERR_CREATE_FILE, NULL);
	}

	/*
	 * Create trace info file
	 */

	// generate filename
	trace->infofile = generateFilename(topology->project, topoNode,
			hdT_getTopoNodeLevel(topoNode), (char*) NULL, ".info");
	assert(isValidString(trace->infofile));

	// create and open file
	trace->info_fd = open(trace->infofile,
			O_CREAT | O_WRONLY | O_TRUNC | O_NONBLOCK, 0662);
	if (trace->info_fd == -1)
	{
		hdt_debugf(trace, "Could not open file %s: %s",
				trace->infofile, strerror(errno));
		hd_error_return(HD_ERR_CREATE_FILE, NULL);
	}

	/* initialize remaining trace file structure */
	trace->function_depth = -1;
	trace->buffer_pos = 0;
	trace->buffer[0] = '\0';
	trace->topoNode = topoNode;

	trace->always_flush = 0;
	trace->isEnabled = 1;
	trace->trace_nested_operations = 0;


	int i;
	for (i = 0; i < HD_LOG_MAX_DEPTH; ++i)
	{
		trace->has_nested[i] = 0;
		trace->state_name[i][0] = '\0';
	}

	if (gettimeofday(& trace->init_time, NULL) != 0)
	{
		hdt_debugf(trace,
				"Problems getting time, stop logging: %s", strerror(errno));
		hdT_disableTrace(trace);
		errno = HD_ERR_GET_TIME;
		return trace;
	}

	writeLogf(trace,
			"<Program timeAdjustment='%u'>\n",
			(unsigned) trace->init_time.tv_sec
			);

	return trace;
}

/**
 * Set depth of nested operations to log into trace.
 * \a depth = 0 means, only the top level calls are logged.
 * This function may only be called at the lowest nesting depth.
 * Calls between \a hdT_logStateStart(...) and \a hdT_logStateEnd(...)
 * are erroneous, will fail and set \a HD_ERR_INVALID_CONTEXT.
 * If \a depth is greater or equal to \a HD_LOG_MAX_DEPTH, the
 * maximum depth is set to
 * \a HD_LOG_MAX_DEPTH - 1
 *
 * @param trace  Trace to use
 * @param depth   Depth to set for logging.
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_INVALID_CONTEXT
 */
int hdT_setNestedDepth(hdTrace trace, int depth)
{
	if (trace == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	if (trace -> function_depth != -1)
	{
		errno = HD_ERR_INVALID_CONTEXT;
		return -1;
	}

	if(depth >= HD_LOG_MAX_DEPTH)
		depth = HD_LOG_MAX_DEPTH - 1;

	trace->trace_nested_operations = depth;
	return 0;
}

/**
 * Enable trace.
 *
 * This function does not set errno!
 *
 * @param trace  Trace to enable
 *
 * @retval  1 Success, was already enabled
 * @retval  0 Success, is now enabled
 * @retval -1 Error: \a group is NULL
 *
 * @sa hdT_disableTrace
 */
int hdT_enableTrace(hdTrace trace)
{
	if (trace == NULL)
		return -1;

	if (trace->isEnabled == 1)
		return 1;

	trace->isEnabled = 1;
	return 0;
}

/**
 * Disable trace.
 *
 * This function does not set errno!
 * So it can easier be used as reaction of errors.
 *
 * @param trace  Trace to disable
 *
 * @retval  1 Success, was already disabled
 * @retval  0 Success, is now disabled
 * @retval -1 Error: \a group is NULL
 *
 * @sa hdT_enableTrace
 */
int hdT_disableTrace(hdTrace trace)
{
	if (trace == NULL)
		return -1;

	if (trace->isEnabled == 0)
		return 1;

	trace->isEnabled = 0;
	return 0;
}

/**
 * Get if trace is enabled.
 *
 * This function produces no error, a NULL group is always disabled;
 *
 * @param trace  Trace to ask
 *
 * @retval  1 Trace is enabled
 * @retval  0 Trace is disabled
 *
 * @sa hdT_enableTrace, hdT_disableTrace
 */
int hdT_isEnabled(hdTrace trace)
{
	if (trace == NULL)
		return 0;

	return trace->isEnabled;
}

/**
 * Set flushing behavior for trace file
 *
 * flush = 0->flush on full buffer<br>
 * flush = 1->flush after every write
 *
 * @param trace  Trace to set parameter for
 * @param flush  Truth value to set/unset forced flushing
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
int hdT_setForceFlush(hdTrace trace, int flush)
{
	if (trace == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}
	trace->always_flush = flush;
	return 0;
}

/**
 * Write info message to trace (printf like).
 *
 * Write data to \a tracefile->info_fd. This output is not buffered.
 *
 * @param trace   Trace to write info message to
 * @param format  \a printf like format string for the message
 * @param ...     Variable number of arguments matching \a format
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_BUFFER_OVERFLOW
 * - HD_ERR_TIMEOUT
 * - HD_ERR_MALLOC
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_UNKNOWN
 */
int hdT_writeInfo(hdTrace trace, const char *format, ...)
{
	if (trace == NULL || !isValidString(format))
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	if (!hdT_isEnabled(trace))
		return 0;

	char buffer[HD_TMP_BUF_SIZE];
	va_list argptr;
	size_t count;
	char *buf = buffer;

	va_start(argptr, format);
	count = (size_t) vsnprintf(buffer, HD_TMP_BUF_SIZE, format, argptr);
	va_end( argptr );

	if (count >= HD_TMP_BUF_SIZE)
	{
		hdt_debug(trace, "Temporary buffer too small for message.");
		return HD_ERR_BUFFER_OVERFLOW;
	}

	ssize_t written = writeToFile(trace->info_fd, buf, count, trace->infofile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hdt_info(trace,	"Timeout during writing of trace info,"
					" stop logging");
		case HD_ERR_MALLOC:
			hdt_info(trace,
					"Out of memory during writing of trace info,"
					" stop logging");
		case HD_ERR_WRITE_FILE:
			hdt_info(trace, "Write error during writing of trace info,"
					" stop logging");
		case HD_ERR_UNKNOWN:
			hdt_info(trace, "Unknown error during writing of trace info,"
					" stop logging");
		default:
			assert(written >= 0);
		}

		/* disable further tracing (does not touch errno) */
		hdT_disableTrace(trace);

		/* do not change errno, just return error */
		return -1;
	}

	return 0;
}

/**
 * Log Element
 *
 * Logs an element for the latest open state. A state is open
 * if \a hdT_logStateStart without a corresponding \a hdT_logStateEnd
 * has been called.
 *
 * @param trace        Trace to use
 * @param name         Name of the element
 * @param valueFormat  Format of the element's value (printf style)
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_BUFFER_OVERFLOW
 */
int hdT_logElement(hdTrace trace, const char * name,
		const char * valueFormat, ...)
{
	if (trace == NULL || !isValidString(name) || !isValidString(valueFormat)
			|| strlen(name) >= HD_LOG_COMMAND_BUF_SIZE)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	if (trace->function_depth > trace->trace_nested_operations ||
		trace->function_depth > HD_LOG_MAX_DEPTH)
	{
		hdt_infof(trace, "maximum nesting depth exceeded. depth=%d", trace->function_depth );
		return 0;
	}

	va_list valist;
	va_start(valist, valueFormat);
	int written;
	written = snprintf(trace->elements[trace->function_depth]
			+ trace->elements_pos[trace->function_depth],
			HD_LOG_COMMAND_BUF_SIZE
					- (trace->elements_pos[trace->function_depth]),
			"<%s ", name);
	if (written >= HD_LOG_COMMAND_BUF_SIZE)
	{
		hdt_debug(trace, "Overflow of HD_LOG_COMMAND_BUF_SIZE buffer"
				"while writing element name.");
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	trace->elements_pos[trace->function_depth] = minSize(
			trace->elements_pos[trace->function_depth] + (size_t) written,
			HD_LOG_COMMAND_BUF_SIZE);
	written = vsnprintf(trace->elements[trace->function_depth]
			+ trace->elements_pos[trace->function_depth],
			HD_LOG_COMMAND_BUF_SIZE
					- trace->elements_pos[trace->function_depth],
			valueFormat, valist);
	if (written >= HD_LOG_COMMAND_BUF_SIZE)
	{
		hdt_debug(trace, "Overflow of HD_LOG_COMMAND_BUF_SIZE buffer"
				"while writing element.");
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	trace->elements_pos[trace->function_depth] = minSize(
			trace->elements_pos[trace->function_depth] + (size_t) written,
			HD_LOG_COMMAND_BUF_SIZE);
	written = snprintf(trace->elements[trace->function_depth]
			+ trace->elements_pos[trace->function_depth],
			HD_LOG_COMMAND_BUF_SIZE
					- trace->elements_pos[trace->function_depth],
			" />\n");
	if (written >= HD_LOG_COMMAND_BUF_SIZE)
	{
		hdt_debug(trace, "Overflow of HD_LOG_COMMAND_BUF_SIZE buffer"
				"while writing element.");
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	trace->elements_pos[trace->function_depth] = minSize(
			trace->elements_pos[trace->function_depth] + (size_t) written,
			HD_LOG_COMMAND_BUF_SIZE);
	va_end(valist);

	return 0;
}

/**
 * Log Attributes
 *
 * Logs an element for the latest open state. A state is open
 * if \a hdT_logStateStart without a corresponding \a hdT_logStateEnd
 * has been called.
 *
 * @param trace        Trace to use
 * @param valueFormat  Format string for attributes (printf style)
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_GET_TIME
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_BUFFER_OVERFLOW
 */
int hdT_logAttributes(hdTrace trace, const char * valueFormat, ...)
{
	if (trace == NULL || !isValidString(valueFormat))
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	if (trace->function_depth >= HD_LOG_MAX_DEPTH)
	{
		hdt_infof(trace, "maximum nesting depth exceeded. depth=%d",
				trace->function_depth );
		return 0;
	}

	va_list valist;
	va_start(valist, valueFormat);
	int written;

	written = vsnprintf(trace->attributes[trace->function_depth]
			+ trace->attributes_pos[trace->function_depth],
			HD_LOG_COMMAND_BUF_SIZE
					- trace->attributes_pos[trace->function_depth],
			valueFormat, valist);
	if (written >= HD_LOG_COMMAND_BUF_SIZE)
	{
		hdt_debug(trace, "Overflow of HD_LOG_COMMAND_BUF_SIZE buffer"
				"while writing attributes.");
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	trace->attributes_pos[trace->function_depth] = minSize(
			trace->attributes_pos[trace->function_depth] + (size_t) written,
			HD_LOG_COMMAND_BUF_SIZE);
	written = snprintf(trace->attributes[trace->function_depth]
			+ trace->attributes_pos[trace->function_depth],
			HD_LOG_COMMAND_BUF_SIZE
					- trace->attributes_pos[trace->function_depth],
			" ");
	if (written >= HD_LOG_COMMAND_BUF_SIZE)
	{
		hdt_debug(trace, "Overflow of HD_LOG_COMMAND_BUF_SIZE buffer"
				"while writing attributes.");
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	trace->attributes_pos[trace->function_depth] = minSize(
			trace->attributes_pos[trace->function_depth] + (size_t) written,
			HD_LOG_COMMAND_BUF_SIZE);
	va_end(valist);

	return 0;
}

/**
 * Mark the start of a new state
 *
 * @param trace      Trace to close
 * @param stateName  Name of the state
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_GET_TIME
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_BUFFER_OVERFLOW
 */
int hdT_logStateStart(hdTrace trace, const char * stateName)
{
	if (trace == NULL || !isValidString(stateName)
			|| strlen(stateName) >= HD_LOG_ELEMENT_NAME_BUF_SIZE)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	trace->function_depth++;
	if (trace->trace_nested_operations < trace->function_depth)
	{
		return 0;
	}

	if (trace->function_depth > 0 && (trace->function_depth - 1
			< HD_LOG_MAX_DEPTH))
	{
		if (trace->has_nested[trace->function_depth - 1] == 0)
		{
			if (writeLogIndentation(trace, trace->function_depth - 1) != 0)
			{
				errno = HD_ERR_WRITE_FILE;
				return -1;
			}
			if (writeLog(trace, "<Nested>\n") != 0)
			{
				errno = HD_ERR_WRITE_FILE;
				return -1;
			}
			trace->has_nested[trace->function_depth - 1] = 1;
		}
	}

	if (trace->function_depth < HD_LOG_MAX_DEPTH)
	{
		trace->elements_pos[trace->function_depth] = 0;
		trace->attributes_pos[trace->function_depth] = 0;

		if (gettimeofday(&trace->start_time[trace->function_depth], NULL) != 0)
		{
			hdt_debugf(trace,
					"Problems getting time, stop logging: %s", strerror(errno));
			hdT_disableTrace(trace);
			errno = HD_ERR_GET_TIME;
			return -1;
		}
		int ret = snprintf(trace->state_name[trace->function_depth],
				HD_LOG_ELEMENT_NAME_BUF_SIZE, "%s", stateName);
		if (ret >= HD_LOG_ELEMENT_NAME_BUF_SIZE)
		{
			hdt_debug(trace,"Overflow of HD_LOG_ELEMENT_NAME_BUF_SIZE buffer"
					"while writing state name");
			errno = HD_ERR_BUFFER_OVERFLOW;
			return -1;
		}
	}
	return 0;
}

/**
 * Mark the end of a state and write it
 *
 * @param trace  Trace to close
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_GET_TIME
 * - HD_ERR_WRITE_FILE
 */
int hdT_logStateEnd(hdTrace trace)
{
	if (trace == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	if (trace->function_depth > trace->trace_nested_operations)
	{
		trace->function_depth--;
		return 0;
	}

	if (trace->function_depth >= HD_LOG_MAX_DEPTH)
	{
		trace->function_depth--;
		return 0;
	}
	if (gettimeofday(&trace->end_time[trace->function_depth], NULL) != 0)
	{
		hdt_debugf(trace, "Problems getting time, stop logging: %s", strerror(errno));
		hdT_disableTrace(trace);
		errno = HD_ERR_GET_TIME;
		return -1;
	}
	if (trace->has_nested[trace->function_depth])
	{
		if (writeLogIndentation(trace, trace->function_depth) != 0)
		{
			errno = HD_ERR_WRITE_FILE;
			return -1;
		}
		if (writeLog(trace, "</Nested>\n") != 0)
		{
			errno = HD_ERR_WRITE_FILE;
			return -1;
		}
		trace->has_nested[trace->function_depth] = 0;
	}

	if (writeState(trace) != 0)
	{
		errno = HD_ERR_WRITE_FILE;
		return -1;
	}

	trace->function_depth--;
	return 0;
}

/**
 * Not yet implemented
 */
int hdT_logEventStart(
		hdTrace trace,
		char * eventName )
{
	return 0;
}

/**
 * Not yet implemented
 */
int hdT_logEventEnd(
		hdTrace trace,
		char* sprinhdStringForFurtherValues,
		...
		)
{
	return 0;
}


/**
 * Finalize and close trace.
 *
 * @param trace  Trace to close
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 * - HD_ERR_WRITE FILE
 * - HD_ERR_CLOSE_FILE
 */
int hdT_finalize(hdTrace trace)
{
	if (trace == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	// finalize trace log file
	if (writeLog(trace, "</Program>\n\n") != 0)
	{
		errno = HD_ERR_WRITE_FILE;
		return -1;
	}

	// flush trace log
	if (flushLog(trace) != 0)
	{
		errno = HD_ERR_WRITE_FILE;
		return -1;
	}

	// finalize trace info file
	if (hdT_writeInfo(trace, "\n\n") != 0)
	{
		errno = HD_ERR_WRITE_FILE;
		return -1;
	}

	// close trace info file
	while (close(trace->info_fd) != 0)
	{
		if (errno == EBADF)      // fd isn’t a valid open file descriptor
			break;
		else if (errno == EINTR)  // close() call was interrupted by a signal
			continue;
		else if (errno == EIO)   // I/O error
		{
			errno = HD_ERR_CLOSE_FILE;
			return -1;
		}
		else                     // unknown error
		{
			errno = HD_ERR_CLOSE_FILE;
			return -1;
		}
	}

	// close trace log file
	while (close(trace->log_fd) != 0)
	{
		if (errno == EBADF)      // fd isn’t a valid open file descriptor
			break;
		else if (errno == EINTR)  // close() call was interrupted by a signal
			continue;
		else if (errno == EIO)   // I/O error
		{
			errno = HD_ERR_CLOSE_FILE;
			return -1;
		}
		else                     // unknown error
		{
			errno = HD_ERR_CLOSE_FILE;
			return -1;
		}
	}

	/* free memory allocated by generateFilename() */
	free(trace->logfile);
	free(trace->infofile);

	/* free memory allocated by hdT_createTrace() */
	free(trace);
	return 0;
}


/////////////////////////////////////
// Static function implementations //
/////////////////////////////////////

/**
 * sprintf like function writing to trace log instead of string
 *
 * @param trace  Trace to use
 * @param format Format string of the message (printf like)
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - each from \sa hdT_LogWriteFormatv
 */
static int writeLogf(hdTrace trace, const char * format, ...)
{
	assert(trace && isValidString(format));

	if (!hdT_isEnabled(trace))
		return 0;
	va_list valist;
	va_start(valist, format);
	if (writeLogfv(trace, format, valist) != 0)
		return -1;
	va_end(valist);
	return 0;
}

/**
 * vsprintf like function writing to trace log instead of string
 *
 * @param trace   Trace to use
 * @param format  Format string of the message (printf like)
 * @param valist  va_list argument as described in \a vsprintf(3) and
 *                  \a stdarg(3)
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - HD_ERR_BUFFER_OVERFLOW
 * - each from \sa hdT_LogWrite
 */
static int writeLogfv(hdTrace trace, const char * format,
		va_list valist)
{
	assert(trace && isValidString(format));

	if (!hdT_isEnabled(trace))
		return 0;
	char buffer[HD_TMP_BUF_SIZE];
	int written;
	written = vsnprintf(buffer, HD_TMP_BUF_SIZE, format, valist);
	if (written >= HD_TMP_BUF_SIZE)
	{
		hdt_debugf(trace, "hdT_LogWriteFormat: buffer too small for string: %s", format);
		errno = HD_ERR_BUFFER_OVERFLOW;
		return -1;
	}
	if (writeLog(trace, buffer) != 0)
		return -1;

	return 0;
}

/**
 * Write a message to the trace log buffer and flush to file if needed.
 *
 * @param trace    Trace to use
 * @param message  Message to write
 *
 * @retval  0  Success
 * @retval -1  Error, setting \a errno
 *
 * @errno
 * - each from \sa hdT_LogFlush
 */
static int writeLog(hdTrace trace, const char * message)
{
	assert(trace && isValidString(message));

	if (!hdT_isEnabled(trace))
		return 0;
	size_t len = strlen(message);
	if (trace->buffer_pos + len >= HD_LOG_BUF_SIZE)
	{
		if (flushLog(trace) != 0)
			return -1;
	}
	strncpy(trace->buffer + trace->buffer_pos, message, len);
	trace->buffer_pos += len;
	if (trace->always_flush)
	{
		if(flushLog(trace) != 0)
			return -1;
	}
	return 0;
}

/**
 * Flush the buffer of trace log
 *
 * Flush \a trace->buffer into \a trace->log_fd
 *
 * @param trace Trace to use
 *
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - HD_ERR_TIMEOUT
 * - HD_ERR_MALLOC
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_UNKNOWN
 */
static int flushLog(hdTrace trace)
{
	assert(trace);

	int fd = trace->log_fd;
	char *buf = trace->buffer;
	size_t count = trace->buffer_pos;

	hdt_debugf(trace, "flushing log length: %lld", (long long int) count)

	ssize_t written = writeToFile(fd, buf, count, trace->logfile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hdt_info(trace,	"Timeout during flushing of trace log,"
					" stop logging");
		case HD_ERR_MALLOC:
			hdt_info(trace,
					"Out of memory during flushing of trace log,"
					" stop logging");
		case HD_ERR_WRITE_FILE:
			hdt_info(trace, "Write error during flushing of trace log,"
					" stop logging");
		case HD_ERR_UNKNOWN:
		default:
			hdt_info(trace, "Unknown error during flushing of trace log,"
					" stop logging");
		}
		/* disable further logging */
		hdT_disableTrace(trace);

		/* do not change errno, just return error */
		return -1;
	}

	trace->buffer_pos = 0;

	return 0;
}

/**
 * Write given number of indentations to trace log
 *
 * @param trace  Trace to use
 * @param count  Number of intentations
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - each from \sa hdT_LogWrite
 */
static int writeLogIndentation(hdTrace trace, int count)
{
	assert(trace && count >= 0);

	for (int i = 0; i < count; ++i)
	{
		if (writeLog(trace, HD_LOG_TAB_STRING) != 0)
			return -1;
	}
	return 0;
}

/**
 * Write state to trace log
 *
 * @param trace  Trace to use
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - each from \sa hdT_LogWrite
 * - each from \sa hdT_LogWriteIndentation
 * - each from \sa hdT_LogWriteFormat
 */
static int writeState(hdTrace trace)
{
	assert(trace);

	if (trace->function_depth >= HD_LOG_MAX_DEPTH)
		return 0;


	if (writeLogIndentation(trace, trace->function_depth) != 0)
		return -1;
	if (writeLog(trace, "<") != 0)
		return -1;
	if (writeLog(trace,
			trace->state_name[trace->function_depth]) != 0)
		return -1;
	if (writeLog(trace, " ") != 0)
		return -1;

	// write pending attributes
	if (trace->attributes_pos[trace->function_depth] != 0)
	{
		if (writeLog(trace,
				trace->attributes[trace->function_depth]) != 0)
			return -1;
	}

	// write time information
	if (writeLogf(
			trace,
			" time='%d.%.6d'",
			(unsigned) (trace->start_time[trace->function_depth].tv_sec - trace->init_time.tv_sec),
			(unsigned) trace->start_time[trace->function_depth].tv_usec) != 0)
		return -1;

	if (writeLogf(
			trace,
			" end='%d.%.6d'",
			(unsigned) (trace->end_time[trace->function_depth].tv_sec - trace->init_time.tv_sec),
			(unsigned) trace->end_time[trace->function_depth].tv_usec) != 0)
		return -1;

	// write pending elements
	if (trace->elements_pos[trace->function_depth] != 0)
	{
		if (writeLog(trace, ">\n" HD_LOG_TAB_STRING) != 0)
			return -1;
		if (writeLogIndentation(trace, trace->function_depth) != 0)
			return -1;
		if (writeLog(trace,
				trace->elements[trace->function_depth]) != 0)
			return -1;
		if (writeLogIndentation(trace, trace->function_depth) != 0)
			return -1;
		if (writeLog(trace, "</") != 0)
			return -1;
		if (writeLog(trace,
				trace->state_name[trace->function_depth]) != 0)
			return -1;
		if (writeLog(trace, ">\n") != 0)
			return -1;
	}
	else
	{
		if (writeLog(trace, "/>\n") != 0)
			return -1;
	}
	return 0;
}
