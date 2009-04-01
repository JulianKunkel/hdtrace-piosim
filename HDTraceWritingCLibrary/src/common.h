/**
 * @file common.h
 *
 * Declarations of functions used by hdTrace and hdStats in common
 *
 * @date 28.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef COMMON_H_
#define COMMON_H_

#include "hdTopo.h"

#define hdinfo(msg) \
	fprintf(stderr, "I: %s (%s:%d): %s\n", __FILE__, __FUNCTION__, __LINENO_, msg)

#define hddebug(msg) \
	fprintf(stderr, "D: %s (%s:%d): %s\n", __FILE__, __FUNCTION__, __LINENO_, msg)

#define hderror(msg) \
	fprintf(stderr, "E: %s (%s:%d): %s\n", __FILE__, __FUNCTION__, __LINENO_, msg)

/**
 * Generate well formed filename.
 */
char * generateFilename(
		const char *project,
		const hdTopoNode topology,
		int level,
		const char *groupname,
		const char *affix
		);

#endif /* COMMON_H_ */
