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

#include "util.h"


/**
 * @details
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
 * @details
 * The names of the topology levels do have only descriptive character and are
 * not used by this library but writing them to the Trace Info.
 * (\ref hdT_initTrace) Other tools like the Project Description Merger could
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
 * @sa hdT_initTrace
 */
hdTopoNames hdT_createTopoNames(
                const char *name1,
                const char *name2,
                const char *name3
                )
{
	return (hdTopoNames) hdT_createTopology(name1, name2, name3);
}

