/**
 * @file hdTopo.c
 *
 * Declarations of functions and types concerning the HDTrace Topologys
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include "hdTopo.h"

#include <stdlib.h>
#include <errno.h>

#include "hdError.h"
#include "util.h"


/**
 * Create new topology.
 *
 * The topology is the semantical structure of HDTrace files. It is meant to
 * map a hierarchical structure to the trace files.
 *
 * A good example are the three possible levels of a parallel program: Hosts,
 * Processes, Threads.
 *
 * If you take this example, each thread will have its own control flow an so
 * it might be a good idea to create one trace for each thread. You are free
 * not to use all three provided level, to use only one or two just pass NULL
 * to the other level labels. When you thing of the topology as a tree, normal
 * traces can only be created for the leafs. So to speak with the example, you
 * can only create normal traces for threads, not for hosts. For statistics
 * traces this restriction does not exist.
 *
 * This function gives you a representation of exactly one path from the root
 * to a leaf of the topology tree. This is meant to be the current path, in the
 * example the thread.
 *
 * The topology labels are used to generate the canonical filenames for the
 * traces.
 *
 * Example usages:
 * @code
 * hdTopology myTopology = hdT_createTopology(hostname, pid, thread_id);
 * @endcode
 * @code
 * hdTopology myTopology = hdT_createTopology(hostname, pid, NULL);
 * @endcode
 *
 * @param label1 Label of the first topology level
 * @param label2 Label of the second topology level or NULL
 * @param label3 Label of the third topology level of NULL
 *
 * @return HDTrace Topology
 *
 * @sa hdT_initTrace, hdS_createGroup
 */
hdTopology hdT_createTopology(
                const char *label1,
                const char *label2,
                const char *label3
                )
{
	/* check label1 since every topology needs at least one level */
	if (isValidString(label1))
	{
		/* error */
		return NULL;
	}

	/* create new topology */
	hdTopology newTopo;

	newTopo->labelDepth1 = label1;
	newTopo->labelDepth2 = label2;
	newTopo->labelDepth3 = label3;

	return newTopo;
}

/**
 * Create new topology names.
 *
 * The names of the topology levels do have only descriptive character and are
 * not used by this library but writing them to the Trace Info.
 * (\ref hdT_createTrace) Other tools like the Project Description Merger could
 * use this information for consistency checks.
 *
 * This function generates and returns a hdTopoNames that actually hides a
 * structure holding the given names.
 *
 * Example usage:
 * @code
 * hdTopoNames myTopoNames = hdT_createTopoNames("Host", "Process", "Thread");
 * @endcode
 * @code
 * hdTopoNames myTopoNames = hdT_createTopoNames("Host", "Process", NULL);
 * @endcode
 *
 * @param name1 Name of the first topology level
 * @param name2 Name of the second topology level or NULL
 * @param name3 Name of the third topology level of NULL
 *
 * @return HDTrace Topology Names
 *
 * @sa hdT_createTrace
 */
hdTopoNames hdT_createTopoNames(
                const char *name1,
                const char *name2,
                const char *name3
                )
{
	return (hdTopoNames) hdT_createTopology(name1, name2, name3);
}

/**
 * Get the depth of the passed topology.
 *
 * Returns the depth (not the highest level) of \a topology. A topology with
 *  maximum a maximum level of 1 is of depth 2 because it has two levels.
 *  The depth of a non existing topology (\a topology == \a NULL) or of one
 *  with no levels is 0.
 *
 * @param topology Topology to get the depth of
 *
 * @return The depth of the Topology.
 */
int hdT_getTopoDepth(hdTopology topology)
{
	if (topology == NULL)
		return 0;

	int level = 0;

	if (isValidString(topology->labelDepth1))
		++level;
	else
		return level;

	if (isValidString(topology->labelDepth2))
		++level;
	else
		return level;

	if (isValidString(topology->labelDepth3))
		++level;
	else
		return level;

	return level;
}

/**
 * Get one level of the passed topology.
 *
 * The \a level of \a topology is returned if it exist, else NULL is returned.
 *
 * @param topology Topology to get the level from
 * @param level    Number of level to get
 *
 * @return Level string
 *
 * @retval String if \a topology is at least of depth \a level
 * @retval NULL   on error
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
const char * hdT_getTopoLevel(hdTopology topology, int level)
{
	/* check input */
	if (hdT_getTopoDepth(topology) < level)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* return level string */
	switch (level)
	{
	case 1:
		return topology->labelDepth1;
		break;
	case 2:
		return topology->labelDepth2;
		break;
	case 3:
		return topology->labelDepth3;
		break;
	}
	/* should never come here */
	return NULL;
}
