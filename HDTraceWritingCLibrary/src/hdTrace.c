/**
 * @file hdTrace.c
 *
 * Implementations of all functions for writing statistics
 *
 * @ifnot api_only
 *  @ingroup hdTrace
 * @endif
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de> (original version)
 * @author 2011: Nathanael Hübbe (Rewrote large parts to phase out a number of fixed length arrays and split the implementation into two classes.)
 * @version /$Id$
 */

#define _GNU_SOURCE

#include "hdTrace.h"
#include "hdTraceInternal.h"

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
#include "hostInformation.h"

///////////////////////////
// Error handling macros //
///////////////////////////

/// Macro to set the errno global variable, disable further tracing and return -1.
#define hdT_fatalError(errorCode) do {\
	trace->isEnabled = 0;\
	errno = errorCode;\
	return -1;\
} while (0)

///////////////////////////////////////
// Auxiliary message printing macros //
///////////////////////////////////////

/// Macro to print debugging message (printf like) (was tprintf)
// TODO: better adjust to new topology concept
#define hdT_debugf(trace, format, ...) \
	printf("D: [TRACER][%s] %s (%s:%d): " format "\n", \
			hdT_getTopoPathString(trace->topoNode), \
			__FUNCTION__, __FILE__, __LINE__, __VA_ARGS__)

/// Macro to print debugging message (fix string) (was tsprintf)
#define hdT_debug(trace, string) \
	printf("D: [TRACER][%s] %s (%s:%d): %s\n", \
			hdT_getTopoPathString(trace->topoNode), \
			__FUNCTION__, __FILE__, __LINE__, string)

/// Macro to print info message (printf like)
// TODO: better adjust to new topology concept
#define hdT_infof(trace, format, ...) \
	printf("I: [TRACER][%s]: " format "\n", \
			hdT_getTopoPathString(trace->topoNode), __VA_ARGS__)

// Macro to print info message (fix string)
#define hdT_info(trace, string) \
	printf("I: [TRACER][%s]: %s\n", \
			hdT_getTopoPathString(trace->topoNode), string)


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
 * hdTopoNode *myTopo = hdT_createTopoNode("myhost", "myrank", "mythread");
 * hdTopology *myTopoNames = hdT_createTopology("Host", "Rank", "Thread");
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
hdTrace * hdT_createTrace(hdTopoNode *topoNode) {
	/* good to know that hdTopoNode is the same as hdTopology ;) */
	if (hdT_getTopoNodeLevel(topoNode) <= 0) hd_error_return(HD_ERR_INVALID_ARGUMENT, NULL);

	/* create trace file structure */
	hdTrace *trace = malloc(sizeof(*trace));
	if (!trace) hd_error_return(HD_ERR_MALLOC, NULL);

	trace->topoNode = topoNode;

	/*
	 * Create trace and info file
	 */

	// generate filename
	trace->logfile = generateFilename(topoNode,	hdT_getTopoNodeLevel(topoNode), NULL, ".trc");
	assert(isValidString(trace->logfile));

	// create and open file
	trace->log_fd = open(trace->logfile,
			O_CREAT | O_WRONLY |  O_TRUNC | (hdt_options.overwrite_existing_files == 0 ? O_EXCL : 0) | O_NONBLOCK, 0662);
	if (trace->log_fd == -1)
	{
		hdT_debugf(trace, "Could not open file %s: %s",
				trace->logfile, strerror(errno));
		free(trace->logfile);
		free(trace);
		hd_error_return(HD_ERR_CREATE_FILE, NULL);
	}

	/*
	 * Create trace info file
	 */

	// generate filename
	trace->infofile = generateFilename(topoNode, hdT_getTopoNodeLevel(topoNode), (char*) NULL, ".info");
	assert(isValidString(trace->infofile));

	// create and open file
	trace->info_fd = open(trace->infofile,
			O_CREAT | O_WRONLY |  O_TRUNC | (hdt_options.overwrite_existing_files == 0 ? O_EXCL : 0) | O_NONBLOCK, 0662);
	if (trace->info_fd == -1)
	{
		hdT_debugf(trace, "Could not open file %s: %s",
				trace->infofile, strerror(errno));
		free(trace);
		hd_error_return(HD_ERR_CREATE_FILE, NULL);
	}

	trace->buffer = malloc(hdt_options.buffer_size);
	/* initialize remaining trace file structure */
	trace->function_depth = -1;
	trace->buffer_pos = 0;
	trace->buffer[0] = '\0';
	trace->isEnabled = 1;

	trace->always_flush = 0;
	trace->max_nesting_depth = hdt_options.max_nesting_depth;
	trace->statesSize = 8;
	trace->states = calloc(trace->statesSize, sizeof(hdTraceState*));
	trace->tabString = "\t";
	trace->tabStringLength = strlen(trace->tabString);
	//Setup the whitespaceString.
	trace->whitespaceString = malloc((trace->statesSize + 1)*trace->tabStringLength + 1);
	if(!trace->whitespaceString) hd_error_return(HD_ERR_MALLOC, NULL);
	long i, j;
	for(i = 0; i <= trace->statesSize; i++) {
		for(j = 0; j < trace->tabStringLength; j++) {
			trace->whitespaceString[i*trace->tabStringLength + j] = trace->tabString[j];
		}
	}
	trace->whitespaceString[(trace->statesSize + 1)*trace->tabStringLength] = 0;

	if (gettimeofday(& trace->init_time, NULL) != 0)
	{
		hdT_debugf(trace,
				"Problems getting time, stop logging: %s", strerror(errno));
		hdT_disableTrace(trace);
		errno = HD_ERR_GET_TIME;
		return trace;
	}

	char * processorModelNameVar =  processorModelName();
	int tlen = strlen(processorModelNameVar)*3;

	char processorModelNameVarbuff[tlen];
	escapeXMLString(processorModelNameVarbuff, tlen, processorModelNameVar);
	free(processorModelNameVar);

	writeLogf(trace,
			"<Program timeAdjustment='%u' processorSpeedinMHZ='%u' processorModelName='%s'>\n",
			(unsigned) trace->init_time.tv_sec, processorCPUspeedinMHZ(),
			processorModelNameVarbuff
			);


	trace->isEnabled = 0;
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
int hdT_setNestedDepth(hdTrace *trace, int depth) {
	if (trace == NULL) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	if (trace -> function_depth != -1) hd_error_return(HD_ERR_INVALID_CONTEXT, -1);
	trace->max_nesting_depth = depth;
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
int hdT_enableTrace(hdTrace *trace) {
	if (trace == NULL) return -1;

	if (trace->isEnabled) return 1;
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
int hdT_disableTrace(hdTrace *trace) {
	if (trace == NULL) return -1;

	if (!trace->isEnabled) return 1;
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
int hdT_isEnabled(hdTrace *trace) {
	if (trace == NULL) return 0;
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
int hdT_setForceFlush(hdTrace *trace, int flush) {
	if (trace == NULL) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
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
 * - HD_ERR_TIMEOUT
 * - HD_ERR_MALLOC
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_UNKNOWN
 */
int hdT_writeInfo(hdTrace *trace, const char *format, ...) {
	if (trace == NULL || !isValidString(format)) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);

	va_list argptr;
	size_t count;
	char *buffer = NULL;

	va_start(argptr, format);
	count = (size_t) vasprintf(&buffer, format, argptr);
	va_end( argptr );
	if (count < 0) hd_error_return(HD_ERR_MALLOC, -1);

	ssize_t written = writeToFile(trace->info_fd, buffer, count, trace->infofile);
	free(buffer);
	if (written < 0) {
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hdT_info(trace,	"Timeout during writing of trace info,"
					" stop logging");
		case HD_ERR_MALLOC:
			hdT_info(trace,
					"Out of memory during writing of trace info,"
					" stop logging");
		case HD_ERR_WRITE_FILE:
			hdT_info(trace, "Write error during writing of trace info,"
					" stop logging");
		case HD_ERR_UNKNOWN:
			hdT_info(trace, "Unknown error during writing of trace info,"
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
int hdT_logElement(hdTrace *trace, const char * name, const char * valueFormat, ...) {
	if (trace == NULL || !isValidString(name) || !isValidString(valueFormat) ) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	if (trace->function_depth < 0) hd_error_return(HD_ERR_INVALID_CONTEXT, -1);
	if (!hdT_isEnabled(trace)) return 0;
	if (trace->function_depth >= trace->max_nesting_depth ) return 0;

	va_list valist;
	va_start(valist, valueFormat);
	char* elementString = NULL;
	char* valueString = NULL;
	int error = 0;
	if(vasprintf(&valueString, valueFormat, valist) >= 0) {
		if(asprintf(&elementString, "%s<%s %s/>\n", hdT_getIndentationString(trace, trace->function_depth+1), name, valueString) >= 0) {
			if(hdTS_appendElements(trace->states[trace->function_depth], elementString)) error = HD_ERR_MALLOC;
			free(elementString);
		} else {
			error = HD_ERR_MALLOC;
		}
		free(valueString);
	} else {
		error = HD_ERR_MALLOC;
	}
	va_end(valist);

	if(error) hd_error_return(error, -1);
	return 0;
}

/**
 * Log Attributes
 *
 * Logs an attribute for the latest open state. A state is open
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
int hdT_logAttributes(hdTrace *trace, const char * valueFormat, ...) {
	if (trace == NULL || !isValidString(valueFormat) ) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	if (trace->function_depth < 0) hd_error_return(HD_ERR_INVALID_CONTEXT, -1);
	if (!hdT_isEnabled(trace)) return 0;
	if (trace->function_depth >= trace->max_nesting_depth ) return 0;

	va_list valist;
	va_start(valist, valueFormat);
	char* attributeString = NULL;
	char* valueString = NULL;
	int error = 0;
	if(vasprintf(&valueString, valueFormat, valist) >= 0) {
		if(asprintf(&attributeString, "%s ", valueString) >= 0) {
			if(hdTS_appendAttributes(trace->states[trace->function_depth], attributeString)) error = HD_ERR_MALLOC;
			free(attributeString);
		} else {
			error = HD_ERR_MALLOC;
		}
		free(valueString);
	} else {
		error = HD_ERR_MALLOC;
	}
	va_end(valist);

	if(error) hd_error_return(error, -1);
	return 0;
}

/**
 * Mark the start of a new state
 *
 * @param trace      Trace to use
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
int hdT_logStateStart(hdTrace *trace, const char * stateName) {
	//Check errors & noop conditions.
	if (trace == NULL || !isValidString(stateName) ) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	trace->function_depth++;
	if (trace->function_depth < 0) trace->function_depth = 0;
	if ( trace->function_depth >= trace->max_nesting_depth) return 0;
	if (!hdT_isEnabled(trace)) return 0;

	//Check that we can store another state.
	if(trace->function_depth >= trace->statesSize) {
		long newSize = trace->statesSize*2, i, j;
		hdTraceState** newStates = NULL;
		if(newSize > trace->max_nesting_depth) newSize = trace->max_nesting_depth;
		if(newSize <= trace->function_depth) newSize = trace->function_depth+1;
		newStates = malloc(sizeof(hdTraceState*)*newSize);
		if(!newStates) hdT_fatalError(HD_ERR_MALLOC);
		for(i = 0; i < trace->statesSize; i++) newStates[i] = trace->states[i];
		for(; i < newSize; i++) newStates[i] = NULL;
		free(trace->states);
		trace->states = newStates;
		trace->statesSize = newSize;
		//Setup the whitespaceString anew.
		free(trace->whitespaceString);
		trace->whitespaceString = malloc((trace->statesSize + 1)*trace->tabStringLength + 1);
		if(!trace->whitespaceString) abort();	//No save continuation possible.
		for(i = 0; i <= trace->statesSize; i++) {
			for(j = 0; j < trace->tabStringLength; j++) {
				trace->whitespaceString[i*trace->tabStringLength + j] = trace->tabString[j];
			}
		}
		trace->whitespaceString[(trace->statesSize + 1)*trace->tabStringLength] = 0;
	}

	//Check that a valid state exists for this recursion level.
	if(!trace->states[trace->function_depth]) {
		trace->states[trace->function_depth] = hdTS_create();
		if(!trace->states[trace->function_depth]) hdT_fatalError(HD_ERR_MALLOC);
	}

	//Init the current state for this new call.
	if(hdTS_start(trace->states[trace->function_depth], stateName)) hdT_fatalError(HD_ERR_MALLOC);

	//Handle the nesting.
	if(trace->function_depth > 0) {
		if(!hdTS_getNested(trace->states[trace->function_depth-1])) {
			if(writeLogf(trace, "%s<Nested>\n", hdT_getIndentationString(trace, trace->function_depth - 1))) hd_error_return(HD_ERR_WRITE_FILE, -1);
			hdTS_setNested(trace->states[trace->function_depth-1]);
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
int hdT_logStateEnd(hdTrace *trace) {
	long oldDepth;
	char* tag;
	//Check errors and noop conditions.
	if(trace == NULL) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	oldDepth = trace->function_depth--;
	assert(oldDepth >= 0);
	if(oldDepth >= trace->max_nesting_depth || !trace->isEnabled) return 0;
	if(!trace->states[oldDepth]) hd_error_return(HD_ERR_UNKNOWN, -1);

	//Close the state.
	if(hdTS_end(trace->states[oldDepth])) hd_error_return(HD_ERR_UNKNOWN, -1);

	//Handle the nesting.
	if(hdTS_getNested(trace->states[oldDepth])) {
		if(writeLogf(trace, "%s</Nested>\n", hdT_getIndentationString(trace, oldDepth))) hd_error_return(HD_ERR_WRITE_FILE, -1);
	}

	//Add a tag for the closed state.
	tag = hdTS_createTag(trace->states[oldDepth], hdT_getIndentationString(trace, oldDepth), trace->init_time.tv_sec);
	if(!tag) hd_error_return(HD_ERR_MALLOC, -1);
	if(writeLog(trace, tag)) hd_error_return(HD_ERR_WRITE_FILE, -1);
	return 0;
}

/**
 * Not yet implemented
 */
int hdT_logEventStart(
		hdTrace *trace,
		char * eventName )
{
	return 0;
}

/**
 * Not yet implemented
 */
int hdT_logEventEnd(
		hdTrace *trace,
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
int hdT_finalize(hdTrace *trace) {
	long i;
	if (trace == NULL) hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	trace->isEnabled = 1;

	if (writeLog(trace, "</Program>\n\n") != 0) hd_error_return(HD_ERR_WRITE_FILE, -1);	// finalize trace log file
	if (flushLog(trace) != 0) hd_error_return(HD_ERR_WRITE_FILE, -1);	// flush trace log
	if (hdT_writeInfo(trace, "\n\n") != 0) hd_error_return(HD_ERR_WRITE_FILE, -1);	// finalize trace info file

	// Close trace info file.
	while (close(trace->info_fd) != 0) {
		if(errno == EBADF) break;	// fd isn’t a valid open file descriptor
		if(errno == EINTR) continue;	// close() call was interrupted by a signal
		if(errno == EIO) hd_error_return(HD_ERR_CLOSE_FILE, -1);	// I/O error
		hd_error_return(HD_ERR_CLOSE_FILE, -1);	// unknown error
	}

	// Close trace log file.
	while (close(trace->log_fd) != 0) {
		if(errno == EBADF) break;	// fd isn’t a valid open file descriptor
		if(errno == EINTR) continue;	// close() call was interrupted by a signal
		if(errno == EIO) hd_error_return(HD_ERR_CLOSE_FILE, -1);	// I/O error
		hd_error_return(HD_ERR_CLOSE_FILE, -1);	// unknown error
	}

	// Free the memory allocated for the states.
	for(i = 0; i < trace->statesSize; i++) if(trace->states[i]) hdTS_delete(trace->states[i]);

	// Free memory allocated by generateFilename().
	free(trace->logfile);
	free(trace->infofile);
	free(trace->buffer);

	// Free memory allocated by hdT_createTrace().
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
static int writeLogf(hdTrace *trace, const char * format, ...)
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
 * - HD_ERR_MALLOC
 * - each from \sa hdT_LogWrite
 */
static int writeLogfv(hdTrace *trace, const char * format,
		va_list valist)
{
	assert(trace && isValidString(format));

	if (!hdT_isEnabled(trace))
		return 0;
	char* buffer;
	int written;
	written = vasprintf(&buffer, format, valist);
	if (written < 0) hd_error_return(HD_ERR_MALLOC, -1);
	if (writeLog(trace, buffer) != 0) return -1;
	free(buffer);

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
static int writeLog(hdTrace *trace, const char * message)
{
	assert(trace && isValidString(message));

	if (!hdT_isEnabled(trace))
		return 0;
	size_t len = strlen(message);
        // synchronzie buffer if necessary!
	if (trace->buffer_pos + len >= hdt_options.buffer_size)
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
static int flushLog(hdTrace *trace)
{
	assert(trace);

	int fd = trace->log_fd;
	char *buf = trace->buffer;
	size_t count = trace->buffer_pos;

	hdT_debugf(trace, "flushing log length: %lld", (long long int) count);

	ssize_t written = writeToFile(fd, buf, count, trace->logfile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hdT_info(trace,	"Timeout during flushing of trace log,"
					" stop logging");
		case HD_ERR_MALLOC:
			hdT_info(trace,
					"Out of memory during flushing of trace log,"
					" stop logging");
		case HD_ERR_WRITE_FILE:
			hdT_info(trace, "Write error during flushing of trace log,"
					" stop logging");
		case HD_ERR_UNKNOWN:
		default:
			hdT_info(trace, "Unknown error during flushing of trace log,"
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
 * @retval a string Success
 * @retval NULL Error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
static char* hdT_getIndentationString(hdTrace *trace, int count) {
	if(!trace || count < 0 || count > trace->statesSize) hd_error_return(HD_ERR_INVALID_ARGUMENT, NULL);
	return &trace->whitespaceString[(trace->statesSize+1 - count)*trace->tabStringLength];
}

/////////////////////////////////////////////
// hdTraceState
/////////////////////////////////////////////

/** Malloc and init */
static hdTraceState* hdTS_create() {
	hdTraceState* self = malloc(sizeof(hdTraceState));
	if(self) hdTS_construct(self);
	return self;
}

/** Constructor */
static void hdTS_construct(hdTraceState* self) {
	assert(self);
	int result = gettimeofday(&self->start_time, NULL);
	result |= gettimeofday(&self->end_time, NULL);
	assert(!result);	//Error getting the time?!?
	self->name = NULL;
	self->attributes = NULL;
	self->elements = NULL;
	self->hasNested = 0;
	self->nameLength = self->attributesLength = self->elementsLength = -1;	//There is not even a termination byte, so it must be -1.
	self->nameSize = self->attributesSize = self->elementsSize = 0;
}

/** Set the state to trace another function call, hdTS_construct() must have been called first. */
static int hdTS_start(hdTraceState* self, const char* name) {
	assert(self && name);
	long nameLength = strlen(name);
	int result;

	//Copy the name string.
	self->nameLength = -1;	//Do not preserve anything when enlarging the buffer.
	result = hdTS_ensureNameSpace(self, nameLength);
	if(!result) {
		memcpy(self->name, name, nameLength + 1);
		self->nameLength = nameLength;
	}

	//Reset the other buffers.
	self->attributesLength = self->elementsLength = -1;

	//Set the times.
	result |= gettimeofday(&self->start_time, NULL);
	result |= gettimeofday(&self->end_time, NULL);
	self->hasNested = 0;
	return result;
}

/** Raises the hasNested flag. */
static void hdTS_setNested(hdTraceState* self) {
	assert(self);
	self->hasNested = 1;
}

/** Returns the hasNested flag. */
static int hdTS_getNested(hdTraceState* self) {
	assert(self);
	return self->hasNested;
}


/** Record the end time of the state. */
static int hdTS_end(hdTraceState* self) {
	assert(self);
	return gettimeofday(&self->end_time, NULL);
}

/** Format the state into an XML tag. offsetSeconds will be subtracted from both times. */
static char* hdTS_createTag(hdTraceState* self, const char* indentation, long offsetSeconds) {
	assert(self);
	char* result = NULL;
	char* name = (self->nameLength > 0) ? self->name : "";	//In case self->name == NULL
	char* attributes = (self->attributesLength > 0) ? self->attributes : "";	//In case self->attributes == NULL
	if(self->elementsLength > 0) {
		if(asprintf(&result, "%s<%s %s time='%ld.%.6ld' end='%ld.%.6ld'>\n%s</%s>\n",
			indentation,
			name,
			attributes,
			self->start_time.tv_sec - offsetSeconds,
			self->start_time.tv_usec,
			self->end_time.tv_sec - offsetSeconds,
			self->end_time.tv_usec,
			self->elements,
			name
		) < 0) return 0;
	} else {
		if(asprintf(&result, "%s<%s %s time='%ld.%.6ld' end='%ld.%.6ld'/>\n",
			indentation,
			name,
			attributes,
			self->start_time.tv_sec - offsetSeconds,
			self->start_time.tv_usec,
			self->end_time.tv_sec - offsetSeconds,
			self->end_time.tv_usec
		) < 0) return 0;
	}
	return result;
}

/** Ensure that a buffer has at least the given size. Returns true if an error occured. */
static int hdTS_ensureSpace(void** buffer, long* bufferSize, long bufferContentSize, long sizeRequest) {
	assert(buffer && bufferSize);
	void* temp;
	long newSize;

	if(sizeRequest <= *bufferSize) return 0;
	newSize = *bufferSize * 2;
	if(newSize < sizeRequest) newSize = sizeRequest;
	if(!(temp = malloc(newSize))) return -1;
	if(*buffer) {
		if(bufferContentSize > 0) memcpy(temp, *buffer, bufferContentSize);
		free(*buffer);
	}
	*buffer = temp;
	*bufferSize = newSize;
	return 0;
}

/** Ensure that the name buffer is large enough to hold lengthRequest characters + termination. */
static int hdTS_ensureNameSpace(hdTraceState* self, long lengthRequest) {
	assert(self);
	return hdTS_ensureSpace((void**)&self->name, &self->nameSize, self->nameLength + 1, lengthRequest + 1);
}

/** Ensure that the attributes buffer is large enough to hold lengthRequest characters + termination. */
static int hdTS_ensureAttributesSpace(hdTraceState* self, long lengthRequest) {
	assert(self);
	return hdTS_ensureSpace((void**)&self->attributes, &self->attributesSize, self->attributesLength + 1, lengthRequest + 1);
}

/** Ensure that the elements buffer is large enough to hold lengthRequest characters + termination. */
static int hdTS_ensureElementsSpace(hdTraceState* self, long lengthRequest) {
	assert(self);
	return hdTS_ensureSpace((void**)&self->elements, &self->elementsSize, self->elementsLength + 1, lengthRequest + 1);
}

/** Add something to a string. */
static int hdTS_appendString(char** stringBuffer, long* length, long* bufferSize, const char* something) {
	assert(stringBuffer && length && bufferSize);
	long appendedLength = strlen(something);
	int result;
	if(*length < 0) *length = 0;	//The difference between -1 and 0 is the termination byte, and that is not retained anyway.
	result = hdTS_ensureSpace((void**)stringBuffer, bufferSize, *length, *length + appendedLength + 1);
	if(result) return result;
	memcpy(&(*stringBuffer)[*length], something, appendedLength + 1);
	*length += appendedLength;
	return 0;
}

/** Add some attributes. Stupid function, does not add any spacing etc. */
static int hdTS_appendAttributes(hdTraceState* self, const char* someAttributes) {
	assert(self && someAttributes);
	return hdTS_appendString(&self->attributes, &self->attributesLength, &self->attributesSize, someAttributes);
}

/** Add some elements. Stupid function, does not add any spacing etc. */
static int hdTS_appendElements(hdTraceState* self, const char* someElements) {
	assert(self && someElements);
	return hdTS_appendString(&self->elements, &self->elementsLength, &self->elementsSize, someElements);
}

/** Destructor */
static void hdTS_destruct(hdTraceState* self) {
	assert(self);
	if(self->name) free(self->name);
	if(self->attributes) free(self->attributes);
	if(self->elements) free(self->elements);
}

/** Destruct and free */
static void hdTS_delete(hdTraceState* self) {
	assert(self);
	hdTS_destruct(self);
	free(self);
}
