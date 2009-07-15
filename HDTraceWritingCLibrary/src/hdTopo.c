/**
 * @file hdTopo.c
 *
 * Declarations of functions and types concerning the HDTrace Topologys
 *
 * @ifnot api_only
 *  @ingroup hdTopo
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "hdTopo.h"
#include "hdTopoInternal.h"

#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include <assert.h>

#include "hdError.h"
#include "common.h"
#include "util.h"

/******* @cond api_only *******/
/**
 * @typedef hdTopology
 * @ingroup hdTopo
 */
/**
 * @typedef hdTopoNode
 * @ingroup hdTopo
 */
/********** @endcond **********/


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
 * hdTopology *myTopology =
 *         hdT_createTopology("ProjectX", {"Host", "Process", "Thread"}, 3);
 * @endcode
 *
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_MALLOC
 *
 * @sa hdT_destroyTopology
 */
hdTopology * hdT_createTopology(
		const char *project,
		const char **levels,
		int nlevels
        )
{
	/* get verbosity */
	initVerbosity();

	/* check input */
	if (project == NULL || nlevels <= 0)
	{
		/* a topology without project name or any level is useless */
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* create new topology */
	hdTopology *topology;
	hd_malloc(topology, 1, NULL);

	/* copy project name into topology */
	topology->project = strdup(project);

	/* allocate memory for names */
	hd_malloc(topology->levels, (size_t) nlevels, NULL);

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
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
int hdT_getTopoDepth(hdTopology *topology)
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
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
		hdTopology *topology
                )
{
	/* check input */
	if (topology == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	hd_free(topology->project);

	for (int i = 0; i < topology->nlevels; ++i)
		hd_free(topology->levels[i]);

	hd_free(topology->levels);
	hd_free(topology);

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
 * hdTopoNode *myTopoNode =
 *         hdT_createTopoNode(myTopology, {hostname, pid, thread_id}, 3);
 * @endcode
 * @code
 * hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, {hostname, pid}, 2);
 * @endcode
 *
 * @if api_only
 *  @ingroup hdTopo
 * @endif
 *
 * @param topology Topology this node should belong to
 * @param path     Array of pointers to the node labels on the path.
 * @param length   Length of the path.
 *
 * @return HDTrace topology node object
 *
 * @retval node  on success
 * @retval NULL  on error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_MALLOC
 *
 * @sa hdT_destroyTopoNode
 */
hdTopoNode * hdT_createTopoNode(
		hdTopology *topology,
		const char **path,
		int length
		)
{
	/* get verbosity */
	initVerbosity();

	/* check input */
	if (path == NULL || length <= 0 || topology == NULL)
	{
		/* a node without path is useless */
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* create new topology node */
	hdTopoNode *node;
	hd_malloc(node, 1, NULL);

	/* allocate memory for path */
	hd_malloc(node->path, (size_t) length, NULL);

	/* copy each path element and get their lengths */
	size_t lengths[length];
	size_t sum = 0;
	for (int i = 0; i < length; ++i)
	{
		lengths[i] = strlen(path[i]);
		sum += lengths[i];

		hd_malloc(node->path[i], lengths[i]+1, NULL);
		memcpy(node->path[i], path[i], lengths[i]+1);
	}

	/* set length */
	node->length = length;

	/* create string representation of path */
	hd_malloc(node->string, sum + (size_t) length, NULL);

	int offset = 0;
	for (int i = 0; i < length; ++i)
	{
		memcpy(node->string + offset, path[i], lengths[i]);
		offset += (int) lengths[i];
		/* write level delimiter */
		node->string[offset++] = '_';
	}
	/* mark end of string, should override last delimiter */
	assert(node->string[(int)sum + length - 1] == '_');
	node->string[(int)sum + length - 1] = '\0';

	node->topology = topology;

	/* return new node */
	return node;
}

/**
 * Get the topology tree level of a node.
 *
 * Returns the level where the \a node take place in its topology.
 *
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
int hdT_getTopoNodeLevel(hdTopoNode *node)
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
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
const char * hdT_getTopoPathString(hdTopoNode *node)
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
 * hdTopoNode *myTopoNode =
 *         hdT_createTopoNode(myTopology, {host0, process0, thread0}, 3);
 * @endcode
 * then the following call will return \c "process0":
 * @code
 * hdT_getTopoPathLabel(myTopoNode, 2);
 * @endcode
 *
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
const char * hdT_getTopoPathLabel(hdTopoNode *node, int level)
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
 * @if api_only
 *  @ingroup hdTopo
 * @endif
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
		hdTopoNode *node
                )
{
	/* check input */
	if (node == NULL)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return -1;
	}

	for (int i = 0; i < node->length; ++i)
		hd_free(node->path[i]);

	hd_free(node->path);
	hd_free(node->string);
	hd_free(node);

	return 0;
}

