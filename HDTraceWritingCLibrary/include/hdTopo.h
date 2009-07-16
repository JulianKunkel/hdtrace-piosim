/**
 * @file hdTopo.h
 *
 * Declarations of functions and types concerning the HDTrace Topologies
 *
 * @ifnot api_only
 *  @ingroup hdTopo
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef HDTOPO_H_
#define HDTOPO_H_

/**
 * @addtogroup hdTopo HDTrace Topology
 *
 * TODO: Explanation
 */

/**
 * Type for using topology objects.
 * Use \ref hdT_createTopology to get one of this objects.
 */
typedef struct _hdTopology hdTopology;

/**
 * Type for using topology node objects
 * Use \ref hdT_createTopoNode to get one of this objects.
 */
typedef struct _hdTopoNode hdTopoNode;

/**
 * Create new topology.
 */
hdTopology * hdT_createTopology(
		const char *project,
		const char **levels,
		int nlevels
		);

/**
 * Get the depth of a topology.
 */
int hdT_getTopoDepth(const hdTopology *topology);

/**
 * Destroy topology object.
 */
int hdT_destroyTopology(hdTopology *topology);

/**
 * Create new topology node.
 */
hdTopoNode * hdT_createTopoNode(
		hdTopology *topology,
		const char **path,
		int length
		);

/**
 * Get the topology tree level of a node.
 */
int hdT_getTopoNodeLevel(const hdTopoNode *node);

/**
 * Get the path string of a topology node.
 */
const char * hdT_getTopoPathString(const hdTopoNode *node);

/**
 * Get the label of one node at the passed \a node's path.
 */
const char * hdT_getTopoPathLabel(const hdTopoNode *node, int level);

/**
 * Destroy topology node.
 */
int hdT_destroyTopoNode(hdTopoNode *node);

#endif /* HDTOPO_H_ */
