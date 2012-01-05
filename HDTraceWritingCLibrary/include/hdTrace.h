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
 * @author Stephan Krempel <stephan.krempel@gmx.de> (original version)
 * @author 2011: Nathanael Hübbe (Phased out the preprocessor constants limiting several buffer sizes.)
 * @version \$Id$
 *
 *
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

void hdTrace_init(void);	///< Initalize HDTrace common facilities.

typedef struct _hdTrace hdTrace;	///< Type to use for traces

hdTrace * hdT_createTrace(hdTopoNode *topoNode);	///< Create, open and initialize trace for given topology.

int hdT_setNestedDepth(hdTrace *trace, int depth);	///< Set depth of nested operations to log into trace.
int hdT_setForceFlush(hdTrace *trace, int flush);	///< Set flushing behavior for trace file.

int hdT_enableTrace(hdTrace *trace);	///< Enable trace.
int hdT_disableTrace(hdTrace *trace);	///< Disable trace.
int hdT_isEnabled(hdTrace *trace);	///< Get if trace is enabled.

int hdT_logElement(hdTrace *trace, const char * name, const char* valueFormat, ...)	///< Log Element.
	 __attribute__ ((format (printf, 3, 4)));

int hdT_writeInfo(hdTrace *trace, const char * message, ...)	///< Write info message to trace (printf like).
	__attribute__ ((format (printf, 2, 3)));

int hdT_logAttributes(hdTrace *trace, const char* valueFormat, ...)	///< Log Attributes.
	__attribute__ ((format (printf, 2, 3)));

int hdT_logStateStart(hdTrace *trace, const char * stateName);	///< Mark the start of a new state.
int hdT_logStateEnd(hdTrace *trace);	///< Mark the end of a state and write it.

int hdT_logEventStart(hdTrace *trace, char * eventName);	///< Not yet implemented.
int hdT_logEventEnd(hdTrace *trace, char* sprinhdStringForFurtherValues, ...)	///< Not yet implemented.
	__attribute__ ((format (printf, 2, 3)));

int hdT_finalize(hdTrace *trace);	///< Finalize and close trace.

#endif /* HDTRACE_H_ */
