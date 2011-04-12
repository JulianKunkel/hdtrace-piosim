/**
 * @file common.h
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef COMMON_H_
#define COMMON_H_

#include <stdlib.h>
#include <stdio.h>

#define RUT_ERR_MALLOC 7000

/**
 * Verbosity
 */
extern int ptl_verbosity;

/**
 * Print a formatted message, to be used only by the \b *MSG macros
 */
#ifdef NDEBUG
# define RUTMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg "\n", ## __VA_ARGS__);
#else
# include <libgen.h>
# define RUTMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg " in %s() (%s:%d)\n", ## __VA_ARGS__, \
		__FUNCTION__, basename(__FILE__), __LINE__);
#endif

/**
 * Print a formated debug message if verbosity is at least 3
 */
#define DEBUGMSG(...) \
	do { if (ptl_verbosity >= 3) { RUTMSG("RUT (3)", __VA_ARGS__) } } while (0)

/**
 * Print a formated info message if verbosity is at least 2
 */
#define INFOMSG(...) \
	do { if (ptl_verbosity >= 2) { RUTMSG("RUT (2)", __VA_ARGS__) } } while (0)

/**
 * Print a formated warning message if verbosity is at least 1
 */
#define WARNMSG(...) \
	do { if (ptl_verbosity >= 1) { RUTMSG("RUT Warning", __VA_ARGS__) } } while (0)

/**
 * Print a formated error message if verbosity is at least 0
 */
#define ERRORMSG(...) \
	do { if (ptl_verbosity >= 0) { RUTMSG("RUT Error", __VA_ARGS__) } } while (0)


/**
 * Set errno to \a eno and return \a ret
 */
#define rut_error_return(eno,ret) \
	errno = eno; \
	return ret;

/**
 * Allocate memory and check for error
 */
#ifdef NDEBUG
# define	rut_malloc(var, num, fail) \
	(var) = g_try_malloc((num) * sizeof(*(var))); \
	if ((var) == NULL) { rut_error_return(RUT_ERR_MALLOC,(fail)) };
#else
# define	rut_malloc(var, num, fail) \
	(var) = g_try_malloc0((num) * sizeof(*(var))); \
	if ((var) == NULL) { rut_error_return(RUT_ERR_MALLOC,(fail)) };
#endif

/**
 * Free memory
 */
#define	rut_free(var) do { g_free(var); var = NULL; } while (0)



#endif /* COMMON_H_ */
