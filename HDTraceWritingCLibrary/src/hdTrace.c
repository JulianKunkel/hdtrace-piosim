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

#include "hdError.h"

/**
 * @details
 * This function creates and opens the trace file for the given topology. The
 * filename is built using the rules for hdTrace files an the given topology.
 *
 * For example:
 * @code
 * hdTopology myTopo = hdT_createTopology("myhost", "myrank", "mythread");
 * hdTopoNames myTopoNames = hdT_createTopoNames("Host", "Rank", "Thread");
 * hdS_initTrace(myTopo, myTopoNames);
 * @endcode
 * creates a file named Project_myhost_myrank_mythread.xml
 *
 * TODO: Where to get Project?
 *
 * @retval hdTrace  created trace (hides a pointer to the descriptive struct of the trace)
 * @retval NULL     on error setting errno
 *
 * @errno
 * - @ref HD_INVALIDARG
 * - @ref HD_MALLOC
 * - @ref HD_CREATEFILE
 *
 * @sa hdT_createTopology, hdT_createTopoNames
 */
hdTrace hdT_initTrace(
		hdTopology topology,
		hdTopoNames names
		)
{
	hdTrace newTrace;

	return newTrace;
}

