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

#include "hdError.h"      /* Include error definitions */
#include "hdTopo.h"       /* Include topology stuff */


/**
 * Structure representing one trace
 */
struct _hdTrace {
	/**
	 * Trace file to write statistics group to
	 */
	FILE *tracefile;
};

/**
 * Type to use for traces
 */
typedef struct _hdTrace * hdTrace;

/**
 * Create, open and initialize trace for given topology.
 */
hdTrace hdT_initTrace(
		hdTopology topology,
		hdTopoNames names
		);


#endif /* HDTRACE_H_ */
