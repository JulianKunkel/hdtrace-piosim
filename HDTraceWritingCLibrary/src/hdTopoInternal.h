/**
 * @file hdTopoInternal.h
 *
 * @ifnot api_only
 *  @ingroup hdTopo
 * @endif
 *
 * @date 15.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef HDTOPO_INTERNAL_H_
#define HDTOPO_INTERNAL_H_

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
 * @internal
 * Structure representing one topology node object.
 *
 * No not use directly, use \ref hdTopoNode instead.
 */
struct _hdTopoNode {
	/**
	 * owning topology
	 */
	hdTopology *topology;

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


#endif /* HDTOPO_INTERNAL_H_ */
