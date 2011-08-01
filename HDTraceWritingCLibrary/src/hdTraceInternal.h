/**
 * @file hdTraceInternal.h
 *
 * @date 15.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef HDTRACE_INTERNAL_H_
#define HDTRACE_INTERNAL_H_

/**
 * @internal
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
	char * buffer;

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
	hdTopoNode *topoNode;

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
	 * 
	 */
	int max_nesting_depth;

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


#endif /* HDTRACE_INTERNAL_H_ */
