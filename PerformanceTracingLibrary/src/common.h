/**
 * @file common.h
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef COMMON_H_
#define COMMON_H_

#include <stdlib.h>

#define PTL_ERR_MALLOC 7000

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
	(var) = malloc((num) * sizeof(*(var))); \
	if ((var) == NULL) { ptl_error_return(PTL_ERR_MALLOC,(fail)) };
#else
# define	ptl_malloc(var, num, fail) \
	(var) = calloc((num),sizeof(*(var))); \
	if ((var) == NULL) { ptl_error_return(PTL_ERR_MALLOC,(fail)) };
#endif

/**
 * Free memory
 */
#define	ptl_free(var) \
	if ((var) != NULL) { free(var); } \
	var = NULL;



#endif /* COMMON_H_ */
