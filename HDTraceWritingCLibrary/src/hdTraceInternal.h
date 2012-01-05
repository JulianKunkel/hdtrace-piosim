/**
 * @file hdTraceInternal.h
 *
 * @date 15.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de> (original version)
 * @author 2011: Nathanael Hübbe (Phased out a number of fixed length arrays and split the implementation into two classes.)
 * @version 0.2
 */

#ifndef HDTRACE_INTERNAL_H_
#define HDTRACE_INTERNAL_H_

typedef struct _hdTraceState hdTraceState;

/**
 * @internal
 * Structure representing one trace
 */
struct _hdTrace {
	int log_fd;	///< File descriptor of the XML file
	int info_fd;	///< File descriptor for info file
	struct timeval init_time;	///< the initial time when the trace file was opened
	char *logfile;	///< Name of the log file (for error output)
	char *infofile;	///< Name of the info file (for error output)
	hdTopoNode *topoNode;	///< Topology leaf this trace belongs to.

	int isEnabled;	///< Selects whether tracing is enabled, can be set with \a hdT_enableTrace() and \a hdT_disableTrace().
	int always_flush;	///< Switches off the use of the buffer to force every write operation to be written directly to disk.

	long max_nesting_depth;	///< The maximal nesting level to which calls are recorded. Calls exeeding this limit will not show up in the trace.
	long function_depth;	///< Keeps track of the depth of nested function calls
	hdTraceState** states;	///< This state array keeps track of all the nested calls encountered (has up to max_nesting_depth entries). The current state is always states[function_depth] (iff function_depth is not negative).
	long statesSize;	///< The current size of the states array in elements. Some may be NULL. Also controls the size of the whitespace string.
	char* tabString;	///< The string to be used for indentation, default is "\t".
	long tabStringLength;	///< strlen() of tabString.
	char* whitespaceString;	///< statesSize+1 copies of tabString concatenated. Thus, for any function_depth, the correct indentation string can be found at &whitespaceString[(statesSize+1 - function_depth)*tabStringLength], or, even simpler, by calling hdT_getIndentationString().

	/**
	 * This is the write buffer of the trace file. Data is not written
	 * directly to the output file but to this buffer, unless
	 * flushing is forced (\a hdT_setForceFlush(...))
	 */
	char * buffer;

	/**
	 * This variable keeps track of the position at which writing
	 * can occur on \a buffer.
	 */
	size_t buffer_pos;
};

/// sprintf like function writing to trace log instead of string.
static int writeLogf(hdTrace *trace, const char * format, ...) __attribute__ ((format (printf, 2, 3)));

/// vsprintf like function writing to trace log instead of string.
static int writeLogfv(hdTrace *trace, const char * format, va_list valist);

/// Write a message to the trace log buffer and flush to file if needed.
static int writeLog(hdTrace *trace, const char * message);

static int flushLog(hdTrace *trace);	///< Flush the buffer of trace log.
static int writeState(hdTrace *trace);	///< Write state to trace log.
static char* hdT_getIndentationString(hdTrace *trace, int count);	///< Get a string containing count concatenated copies of tabString.

////////////////////////////////////

/**
 * @internal
 * Structure to accumulate the infos for a traced call (=state).
 */
struct _hdTraceState {
	char* name;	///< The name of the state.
	char* attributes;	///< The attributes string. These are included in the opening tag of the state.
	char* elements;	///< The elements string. These are included between the opening and closing tags for the state.
	struct timeval start_time, end_time;	///< The timing information that will be appended to the attribute list.
	int hasNested;	///< True if there have been nested calls in this state.

	long nameLength, attributesLength, elementsLength;	///< The number of chars in each string, excluding the termination.
	long nameSize, attributesSize, elementsSize;	///< The amount of space allocated for each string.
};

static hdTraceState* hdTS_create();
static void hdTS_construct(hdTraceState* self);

static int hdTS_start(hdTraceState* self, const char* name);
static void hdTS_setNested(hdTraceState* self);
static int hdTS_getNested(hdTraceState* self);
static int hdTS_end(hdTraceState* self);
static char* hdTS_createTag(hdTraceState* self, const char* indentation, long offsetSeconds);	///< The caller is responsible to free the return value.

static int hdTS_ensureNameSpace(hdTraceState* self, long lengthRequest);
static int hdTS_ensureAttributesSpace(hdTraceState* self, long lengthRequest);
static int hdTS_ensureElementsSpace(hdTraceState* self, long lengthRequest);
static int hdTS_appendAttributes(hdTraceState* self, const char* someAttributes);
static int hdTS_appendElements(hdTraceState* self, const char* someElements);

static void hdTS_destruct(hdTraceState* self);
static void hdTS_delete(hdTraceState* self);

#endif /* HDTRACE_INTERNAL_H_ */
