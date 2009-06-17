/**
 * @file common.h
 *
 * Declarations of functions used by hdTrace and hdStats in common
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef COMMON_H_
#define COMMON_H_

#include <sys/types.h>

#include "hdTopo.h"
#include "config.h"

/**
 * Printf format specifier for printing int64_t.
 * On 64bit it is long int, on 32bit long long int.
 */
#if __WORDSIZE == 64
# define INT64_FORMAT "ld"
# define UINT64_FORMAT "lu"
# define llu(x) ((long unsigned) (x))

#else

# define INT64_FORMAT "lld"
# define UINT64_FORMAT "llu"
# define llu(x) ((long long unsigned) (x))
#endif

/**
 * Size of temporary buffers.
 */
#define HD_TMP_BUF_SIZE 1024 * 16

/**
 * Print X followed by the code position followed by formated message
 * Do not use directly, use hd_(info|debug|error)_msg instead.
 */
#ifdef NDEBUG
# define hd_X_msg(X, format, ...) \
	fprintf(stderr, X ": " format "\n", __VA_ARGS__)
#else
# define hd_X_msg(X, format, ...) \
	fprintf(stderr, X ": %s (%s:%d): " format "\n", __FILE__, __FUNCTION__, __LINE__, __VA_ARGS__)
#endif /* NDEBUG */

/**
 * Print a formated error message prefixed by "E:" and code position
 */
#define hd_error_msg(format, ...) \
	if (verbosity >= V_ERROR) \
		hd_X_msg("E", format, __VA_ARGS__)

/**
 * Print a formated info message prefixed by "I:" and the code position
 */
#define hd_info_msg(format, ...) \
	if (verbosity >= V_INFO) \
		hd_X_msg("I", format, __VA_ARGS__)

/**
 * Print a formated debug message prefixed by "D:" and the code position
 */
#define hd_debug_msg(format, ...) \
	if (verbosity >= V_DEBUG) \
		hd_X_msg("D", format, __VA_ARGS__)

/**
 * Set errno to \a eno and return \a ret
 */
#define hd_error_return(eno,ret) \
	do { \
		errno = eno; \
		return ret; \
	} while (0)

/**
 * Allocate memory and check for error
 */
#define	hd_malloc(var, num, fail) \
	do { \
		var = malloc((num) * sizeof(*(var))); \
		if (var == NULL) \
			hd_error_return(HD_ERR_MALLOC,fail); \
	} while (0)

/**
 * Free memory and set pointer to null for easier debugging
 */
#define	hd_free(var) \
	do { \
		(var) ? free(var) : (void)(0); \
		var = NULL; \
	} while (0)

/**
 * Verbosity levels of the libraries.
 */
enum verbosity {
	/**
	 * Print only error messages. (default)
	 */
	V_ERROR,//!< V_ERROR
	/**
	 * Print additional info messages.
	 */
	V_INFO, //!< V_INFO
	/**
	 * Print detailed debugging messages.
	 */
	V_DEBUG //!< V_DEBUG
};

/**
 * Print message at given verbosity level
 */
void printMessage(enum verbosity vlvl, const char *format, ...);

/**
 * Generate well formed filename.
 */
char * generateFilename(
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

/**
 * Print a number of indentations to a string.
 */
int snprintIndent(char* string, size_t size, int num);

#endif /* COMMON_H_ */
