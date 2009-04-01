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
#include <string.h>
#include <errno.h>

#include <assert.h>

#include "hdError.h"
#include "util.h"



/**
 * Create new topology.
 *
 * The topology is simply specified by the names of the tree levels.
 *
 * The first one (level 0) is the name of the root node and also the name of
 * the HDTrace project. The second one is the name of level 1, the third name
 * belongs to level 2 and so on.
 *
 * In HDTrace these names do have only descriptive character and are not used
 * by the library beside writing them to the Trace Info. Other tools like the
 * Project Description Merger could use this information for consistency
 * checks.
 *
 * This function generates and returns a hdTopology object that you have to
 *  destroy when no longer needed by passing it to \ref hdT_destroyTopology.
 *
 * Example usage:
 * @code
 * hdTopology myTopology = hdT_createTopology("ProjectX", {"Host", "Process", "Thread"}, 3);
 * @endcode
 *
 * @param project Name of the HDTrace project
 * @param levels  Array of names
 * @param nlevels Number of levels the topology has beside root level
 *
 * @return HDTrace topology object
 *
 * @retval topology on success
 * @retval NULL     on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 *
 * @sa hdT_destroyTopology
 */
hdTopology hdT_createTopology(
		const char *project,
		char **levels,
		int nlevels
        )
{
	/* check input */
	if (project == NULL || nlevels <= 0)
	{
		/* a topology without project name or any level is useless */
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* create new topology */
	hdTopology topology = malloc(sizeof(*topology));

	/* copy project name into topology */
	topology->project = strdup(project);

	/* allocate memory for names */
	topology->levels = malloc(nlevels * sizeof(*(topology->levels)));

	/* copy each path element */
	for (int i = 0; i < nlevels; ++i)
		topology->levels[i] = strdup(levels[i]);

	/* set nlevels */
	topology->nlevels = nlevels;

	/* return new topology */
	return topology;
}

/**
 * Get the depth of a topology.
 *
 * Returns the number of levels in the topology including root level (level 0).
 *
 * @param topology Topology to use
 *
 * @return The number of topology levels.
 *
 * @retval >0 on success
 * @retval -1 on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
int hdT_getTopoDepth(hdTopology topology)
{
	/* check input */
	if (topology == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	/* path length is equal to the level number */
	/* nlevels is the number of levels without root, so add 1 */
	return topology->nlevels + 1;
}

/**
 * Destroy topology object.
 *
 * @param topology  Topology object to destroy
 *
 * @retval  0 on success
 * @retval -1 on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 *
 * @sa hdT_createTopology
 */
int hdT_destroyTopology(
		hdTopology topology
                )
{
	/* check input */
	if (topology == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	free(topology->project);

	for (int i = 0; i < topology->nlevels; ++i)
		free(topology->levels[i]);

	free(topology->levels);
	free(topology);

	return 0;
}

/**
 * Create new topology node.
 *
 * The topology is the semantical structure of HDTrace files. It is meant to
 * map a hierarchical structure to the trace files.
 *
 * Actually the topology is a tree structure, so the place of each node in the
 * topology is exactly specified by the path to reach this node from the root
 * node. The root node is always the starting point, so it can be omitted.
 *
 * This function takes such a path and gives you a hdTopoNode object
 * representing the node you reach walking the given path. In most cases, this
 * will be a leaf node of the topology tree.
 *
 * In HDTrace, normal traces are created only for leaf nodes of the topology
 * tree. For statistics traces this restriction does not hold.
 *
 * With the path for creating nodes, you implicitly label the nodes passed in
 * each level of the topology tree. And so the tree is automatically extended
 * with each hdTopoNode object created.
 *
 *
 * A good example is the typical structure of a parallel program: Hosts,
 * Processes, Threads.
 *
 * Each thread has its own control flow an so it might be a good idea to create
 * one trace for each thread. So you will choose to have the threads as leaf
 * nodes of your tracing topology tree.<br>
 * Of cause you are free to use only two levels and so produce only one trace
 * per process.
 *
 * The node labels on the path are used to generate the canonical filenames for
 * the traces.
 *
 * Example usages:
 * @code
 * hdTopoNode myTopology = hdT_createTopoNode({hostname, pid, thread_id}, 3);
 * @endcode
 * @code
 * hdTopoNode myTopology = hdT_createTopoNode({hostname, pid}, 2);
 * @endcode
 *
 * @param path   Array of pointers to the node labels on the path.
 * @param length Length of the path.
 *
 * @return HDTrace topology node object
 *
 * @retval node  on success
 * @retval NULL  on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 *
 * @sa hdT_destroyTopoNode
 */
hdTopoNode hdT_createTopoNode(
		char **path,
		int length
		)
{
	/* check input */
	if (path == NULL || length <= 0)
	{
		/* a node without path is useless */
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* create new topology node */
	hdTopoNode node = malloc(sizeof(*node));

	/* allocate memory for path */
	node->path = malloc(length * sizeof(*(node->path)));

	/* copy each path element and get their lengths */
	int lengths[length];
	int sum = 0;
	for (int i = 0; i < length; ++i)
	{
		lengths[i] = strlen(path[i]);
		sum += lengths[i];

		node->path[i] = malloc((lengths[i]+1)*sizeof(*(node->path[i])));
		memcpy(node->path[i], path[i], lengths[i]+1);
	}

	/* set length */
	node->length = length;

	/* create string representation of path */
	node->string = malloc((sum + length) * sizeof(*(node->string)));

	int offset = 0;
	for (int i = 0; i < length; ++i)
	{
		memcpy(node->string + offset, path[i], lengths[i]);
		offset += lengths[i];
		/* write level delimiter */
		node->string[offset++] = '.';
	}
	/* mark end of string, should override last delimiter */
	assert(node->string[sum + length - 1] == '.');
	node->string[sum + length - 1] = '\0';

	/* return new node */
	return node;
}

/**
 * Get the topology tree level of a node.
 *
 * Returns the level where the \a node take place in its topology.
 *
 * @param node Topology node to use
 *
 * @return The number of the topology level \a node lives in.
 *
 * @retval >0 on success
 * @retval -1 on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
int hdT_getTopoNodeLevel(hdTopoNode node)
{
	/* check input */
	if (node == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	/* path length is equal to the level number */
	return node->length;
}

/**
 * Get the path string of a topology node.
 *
 * Returns a string representation of the node's path.
 *
 * For example: \c host1.process1.thread1
 * The \a level of \a topology is returned if it exist, else NULL is returned.
 *
 * @param node  Topology node to use
 *
 * @return String representing the node's path
 *
 * @retval String on success
 * @retval NULL   on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
const char * hdT_getTopoPathString(hdTopoNode node)
{
	/* check input */
	if (node == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* return path string */
	return node->string;
}

/**
 * Get the label of one node at the passed \a node's path.
 *
 * Searches the path of the given \a node for the topology node at the given
 * \a level and return its label.
 *
 * For example, create a node like this:
 * @code
 * hdTopoNode myTopoNode = hdT_createTopoNode({host0, process0, thread0}, 3);
 * @endcode
 * then the following call will return \c "process0":
 * @code
 * hdT_getTopoPathLabel(myTopoNode, 2);
 * @endcode
 *
 * @param node  Topology node to use
 * @param level Level to get node label for (>0)
 *
 * @return Label of the node sitting at \a level on \a node's path
 *
 * @retval String on success
 * @retval NULL   on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
const char * hdT_getTopoPathLabel(hdTopoNode node, int level)
{
	/* check input */
	if (node == NULL || level <= 0)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	return node->path[level-1];
}

/**
 * Destroy topology node.
 *
 * @param node Topology node to destroy
 *
 * @retval  0 on success
 * @retval -1 on error, setting errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 *
 * @sa hdT_createTopoNode
 */
int hdT_destroyTopoNode(
		hdTopoNode node
                )
{
	/* check input */
	if (node == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	for (int i = 0; i < node->length; ++i)
		free(node->path[i]);

	free(node->path);
	free(node->string);

	return 0;
}

