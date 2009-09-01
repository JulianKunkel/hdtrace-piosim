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
 * The topology of a complete trace project describes a tree structure where
 * each single trace can be assigned to a tree node. As we have several singly
 * assigned trace files that build one whole trace project, this structuring
 * method allows us to describe the associations between the single traces.
 *
 * The root of the topology is defined to be the project name. The types of the
 * deeper levels can be freely specified. A topology’s structure is fully
 * specified by the topology level types. The level type should describe the
 * semantics of the nodes on the level.
 *
 * An example of a topology structure take (Project, Host, Process, Thread).
 *
 * Each trace is assigned to exactly one node of the topology called the trace’s
 * topology node. Each such topology node has a label that must be unique at the
 * node’s topology level. A node is well-defined by the unique path that leads
 * from the root of the topology to this node. The path is the list of labels of
 * each node in the order they are passed when walking on the edges of the tree
 * to the target node starting from the root node.
 *
 * An example for a leaf node in a topology with example structure as defined
 * above could be (myProject, node01, rank3, thread0).
 *
 * The topology structure represented by the topology level types is written
 * into the project file in order to be used as labels in trace visualization.
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
