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

#include <sys/types.h>

#include "hdTopo.h"
#include "config.h"

/**
 * Print X followed by the code position followed by formated message
 * Do not use directly, use hd_(info|debug|error)_msg instead.
 */
#define hd_X_msg(X, format, ...) \
	fprintf(stderr, X ": %s (%s:%d): " format "\n", __FILE__, __FUNCTION__, __LINE__, __VA_ARGS__)

/**
 * Print a formated info message prefixed by "I:" and the code position
 */
#define hd_info_msg(format, ...) \
	if (VLEVEL >= V_INFO) hd_X_msg("I", format, __VA_ARGS__)

/**
 * Print a formated debug message prefixed by "D:" and the code position
 */
#define hd_debug_msg(format, ...) \
	if (VLEVEL >= V_DEBUG) hd_X_msg("D", format, __VA_ARGS__)

/**
 * Print a formated error message prefixed by "E:" and code position
 */
#define hd_error_msg(format, ...) \
	if (VLEVEL >= V_ERROR) hd_X_msg("E", format, __VA_ARGS__)

/**
 * Set errno to \a eno and return \a ret
 */
#define hd_error_return(eno,ret) { \
	errno = eno; \
	return ret; }

/**
 *
 */
enum verbosity {
	V_ERROR,
	V_DEBUG,
	V_INFO
};

/**
 * Print message at given verbosity level
 */
void printMessage(enum verbosity vlvl, const char *format, ...);

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

/**
 * Writes data to a file at the current offset.
 */
ssize_t writeToFile(
		int fd,
		void *buf,
		size_t count,
		const char *filename
		);

#endif /* COMMON_H_ */
