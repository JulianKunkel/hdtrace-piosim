/**
 * @file hdTopo.h
 *
 * Declarations of functions and types concerning the HDTrace Topologys
 *
 * @ifnot api_only
 *  @ingroup hdTopo
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#ifndef HDTOPO_H_
#define HDTOPO_H_

/**
 * @addtogroup hdTopo HDTrace Topology
 *
 * TODO: Explanation
 */

/**
 * @internal
 * Structure representing one topology object.
 *
 * No not use directly, use \ref hdTopology instead.
 */
struct _hdTopology {
	/**
	 * Project name and label of the topology level 0.
	 */
	char *project;
	/**
	 * Names of the topology levels >0.
	 */
	char **levels;
	/**
	 * Number of levels the topology has beside root level 0
	 */
	int  nlevels;
};

/**
 * Type for using topology objects.
 * Use \ref hdT_createTopology to get one of this objects.
 */
typedef struct _hdTopology * hdTopology;

/**
 * @internal
 * Structure representing one topology node object.
 *
 * No not use directly, use \ref hdTopoNode instead.
 */
struct _hdTopoNode {
	/**
	 * owning topology
	 */
	hdTopology topology;

	/**
	 * Path to the node.
	 */
	char **path;
	/**
	 * Length of the path.
	 */
	int  length;
	/**
	 * Path in string representation (for error reporting)
	 */
	char *string;
};

/**
 * Type for using topology node objects
 * Use \ref hdT_createTopoNode to get one of this objects.
 */
typedef struct _hdTopoNode * hdTopoNode;

/**
 * Create new topology.
 */
hdTopology hdT_createTopology(
		const char *project,
		const char **levels,
		int nlevels
		);

/**
 * Get the depth of a topology.
 */
int hdT_getTopoDepth(hdTopology topology);

/**
 * Destroy topology object.
 */
int hdT_destroyTopology(
		hdTopology topology
		);

/**
 * Create new topology node.
 */
hdTopoNode hdT_createTopoNode(
		hdTopology topology,
		const char **path,
		int length
		);

/**
 * Get the topology tree level of a node.
 */
int hdT_getTopoNodeLevel(hdTopoNode node);

/**
 * Get the path string of a topology node.
 */
const char * hdT_getTopoPathString(hdTopoNode node);

/**
 * Get the label of one node at the passed \a node's path.
 */
const char * hdT_getTopoPathLabel(hdTopoNode node, int level);

/**
 * Destroy topology node.
 */
int hdT_destroyTopoNode(hdTopoNode node);

#endif /* HDTOPO_H_ */
