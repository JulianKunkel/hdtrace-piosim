/**
 * @file ptInternal.h
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef PTINTERNAL_H_
#define PTINTERNAL_H_

#include "ptError.h"

/* Macro to do malloc with error handling */
#define PTMALLOC(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = malloc(count * sizeof(*(var))); \
		if (var == NULL) { \
			ERROR_ERRNO(#var); \
			return failret; \
		} \
	} while (0)

/* Macro to do realloc with error handling */
#define PTREALLOC(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = realloc(var, count * sizeof(*(var))); \
		if (var == NULL) { \
			ERROR_ERRNO(#var); \
			free(var); /* still we return, free the memory */ \
			return failret; \
		} \
	} while (0)

#endif /* PTINTERNAL_H_ */
