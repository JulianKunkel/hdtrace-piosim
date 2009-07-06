#ifndef PTERROR_H
#define PTERROR_H

#include <error.h>
#include <errno.h>

/* Common return values */
#define OK             0
#define ERR_ERRNO     -1
#define ERR_MALLOC    -3

/* Return values used in serial_* */
#define ERR_WRITE    -10    /* Write error */

/* Return values used in LMG_* */
#define ERR_NO_MSG     -20  /* No message ready */
#define ERR_MSG_FORMAT -21  /* Message does not hat the expected format */
#define ERR_BSIZE      -22  /* Buffer size is not sufficient */

/* Configuration file */
#define ERR_FILE_NOT_FOUND -30
#define ERR_SYNTAX         -31

/* createTraces() */
#define ERR_NO_TRACES  -40
#define ERR_HDLIB      -41

/* trace_interation() */
#define ERR_MSGSIZE    -50

/* traceLoop() */
#define ERR_TIMEOUT    -60

/* Macro for debugging output */
#ifdef DEBUG
# define DEBUGMSG(msg, ...) printf(msg, ## __VA_ARGS__)
#else
# define DEBUGMSG(msg, ...) (void) 0
#endif

/* Macro for generate non fatal warnings */
#define WARN(msg, ...) error(0,0, "WARNING: " msg, ## __VA_ARGS__)

/* Macro for reporting custom errors (without errno set) */
#define ERROR(msg, ...) error_at_line(0, 0, __FILE__, __LINE__, msg, ## __VA_ARGS__)

/* Macro for reporting errors with errno set */
#define ERROR_ERRNO(msg, ...) error_at_line(0, errno, __FILE__, __LINE__, msg, ## __VA_ARGS__)

/* Macro for reporting unknown errors */
#define ERROR_UNKNOWN error_at_line(0, 0, __FILE__, __LINE__, "Unknown Error")


/* Macro to do malloc with error handling */
#define pt_malloc(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = malloc(count * sizeof(*(var))); \
		if (var == NULL) { \
			ERROR_ERRNO(#var); \
			return failret; \
		} \
	} while (0)

/* Macro to do realloc with error handling */
#define pt_realloc(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = realloc(var, count * sizeof(*(var))); \
		if (var == NULL) { \
			ERROR_ERRNO(#var); \
			free(var); /* still we return, free the memory */ \
			return failret; \
		} \
	} while (0)

#define pt_free(var) do { free(var); var = NULL; } while (0);

#endif
