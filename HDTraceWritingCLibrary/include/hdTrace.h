/**
 * @file hdTrace.h
 *
 * Declarations of all functions and types for writing statistics
 *
 * @ifnot api_only
 *  @ingroup hdTrace
 * @endif
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

/* *   Do not pollute the API documentation ;)
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
 * Maximum depth of nested tracing calls that is logged.
 */
#define HD_LOG_MAX_DEPTH 4


/**
 * Initalize HDTrace common facilities.
 */
void hdTrace_init(void);

/**
 * Type to use for traces
 */
typedef struct _hdTrace hdTrace;


/**
 * Create, open and initialize trace for given topology.
 */
hdTrace * hdT_createTrace(
		hdTopoNode *topoNode
		);

/**
 * Set depth of nested operations to log into trace
 */
int hdT_setNestedDepth(
		hdTrace *trace,
		int depth
		);

/**
 * Enable trace.
 */
int hdT_enableTrace(hdTrace *trace);

/**
 * Disable trace.
 */
int hdT_disableTrace(hdTrace *trace);

/**
 * Get if trace is enabled.
 */
int hdT_isEnabled(hdTrace *trace);

/**
 * Set flushing behavior for trace file
 */
int hdT_setForceFlush(
		hdTrace *trace,
		int flush
		);

/**
 * Log Element
 */
int hdT_logElement(
		hdTrace *trace,
		const char * name,
		const char* valueFormat,
		...
		) __attribute__ ((format (printf, 3, 4)));

/**
 * Write info message to trace (printf like).
 */
int hdT_writeInfo(
		hdTrace *trace,
		const char * message,
		...
	) __attribute__ ((format (printf, 2, 3)));

/**
 * Log Attributes
 */
int hdT_logAttributes(
		hdTrace *trace,
		const char* valueFormat,
		...
		) __attribute__ ((format (printf, 2, 3)));

/**
 * Mark the start of a new state
 */
int hdT_logStateStart(
		hdTrace *trace,
		const char * stateName
		);

/**
 * Mark the end of a state and write it
 */
int hdT_logStateEnd(
		hdTrace *trace
		);

/**
 * Not yet implemented
 */
int hdT_logEventStart(
		hdTrace *trace,
		char * eventName );

/**
 * Not yet implemented
 */
int hdT_logEventEnd(
		hdTrace *trace,
		char* sprinhdStringForFurtherValues,
		...
		) __attribute__ ((format (printf, 2, 3)));

/*
 * Finalize and close trace.
 */
int hdT_finalize(
		hdTrace *trace
		);
#endif /* HDTRACE_H_ */
