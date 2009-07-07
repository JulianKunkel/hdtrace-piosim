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

#define PTL_ERR_MALLOC 7000

/**
 * Verbosity
 */
extern int ptl_verbosity;


#ifdef NDEBUG
# define PTLMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg "\n", ## __VA_ARGS__);
#else
# include <libgen.h>
# define PTLMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg " in %s() (%s:%d)\n", ## __VA_ARGS__, \
		__FUNCTION__, basename(__FILE__), __LINE__);
#endif

#define DEBUGMSG(msg, ...) \
	do { if (ptl_verbosity >= 3) { PTLMSG("PTL (3)", msg, ## __VA_ARGS__) } } while (0)

#define VERBMSG(msg, ...) \
	do { if (ptl_verbosity >= 2) { PTLMSG("PTL (2)", msg, ## __VA_ARGS__) } } while (0)

#define WARNMSG(msg, ...) \
	do { if (ptl_verbosity >= 1) { PTLMSG("PTL Warning", msg, ## __VA_ARGS__) } } while (0)

#define ERRORMSG(msg, ...) \
	do { if (ptl_verbosity >= 0) { PTLMSG("PTL Error", msg, ## __VA_ARGS__) } } while (0)


/**
 * Set errno to \a eno and return \a ret
 */
#define ptl_error_return(eno,ret) \
	errno = eno; \
	return ret;

/**
 * Allocate memory and check for error
 */
#ifdef NDEBUG
# define	ptl_malloc(var, num, fail) \
	(var) = g_try_malloc((num) * sizeof(*(var))); \
	if ((var) == NULL) { ptl_error_return(PTL_ERR_MALLOC,(fail)) };
#else
# define	ptl_malloc(var, num, fail) \
	(var) = g_try_malloc0((num) * sizeof(*(var))); \
	if ((var) == NULL) { ptl_error_return(PTL_ERR_MALLOC,(fail)) };
#endif

/**
 * Free memory
 */
#define	ptl_free(var) do { g_free(var); var = NULL; } while (0)



#endif /* COMMON_H_ */
