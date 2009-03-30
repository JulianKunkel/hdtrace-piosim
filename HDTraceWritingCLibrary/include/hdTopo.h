/**
 * @file hdTopo.h
 *
 * Declarations of functions and types concerning the HDTrace Topologys
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef HDTOPO_H_
#define HDTOPO_H_

/**
 * Structure representing one topology instance
 */
struct _hdTopology {
	const char *labelDepth1;
	const char *labelDepth2;
	const char *labelDepth3;
};

/**
 * Type for topologies
 */
typedef struct _hdTopology * hdTopology;

/**
 * Type for topology names
 */
typedef struct _hdTopology * hdTopoNames;

/**
 * Create new topology.
 */
hdTopology hdT_createTopology(
                const char *label1,
                const char *label2,
                const char *label3
                );

/**
 * Create new topology names.
 */
hdTopoNames hdT_createTopoNames(
                const char *name1,
                const char *name2,
                const char *name3
                );

/**
 * Get the depth of the passed topology.
 */
int hdT_getTopoDepth(
		hdTopology topology
		);

/**
 * Get one level of the passed topology.
 */
const char * hdT_getTopoLevel(
		hdTopology topology,
		int level
		);

#endif /* HDTOPO_H_ */
