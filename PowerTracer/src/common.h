#ifndef COMMON_H
#define COMMON_H

#include <error.h>
#include <errno.h>
#include <string.h>

/*
 * Debugging output
 */

/** Verbosity */
extern int pt_verbosity;

#ifdef NDEBUG
# define PTMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg "\n", ## __VA_ARGS__);
#else
# include <libgen.h>
# define PTMSG(prefix, msg, ...) \
	fflush(stdout); \
	fprintf(stderr, prefix ": " msg " in %s() (%s:%d)\n", ## __VA_ARGS__, \
		__FUNCTION__, basename(__FILE__), __LINE__);
#endif

#define DEBUGMSG(msg, ...) \
	do { if (pt_verbosity >= 3) { PTMSG("PT (3)", msg, ## __VA_ARGS__) } } while (0)

#define VERBMSG(msg, ...) \
	do { if (pt_verbosity >= 2) { PTMSG("PT (2)", msg, ## __VA_ARGS__) } } while (0)

#define WARNMSG(msg, ...) \
	do { if (pt_verbosity >= 1) { PTMSG("PT Warning", msg, ## __VA_ARGS__) } } while (0)

#define ERRORMSG(msg, ...) \
	do { if (pt_verbosity >= 0) { PTMSG("PT Error", msg, ## __VA_ARGS__) } } while (0)

#define ERRNOMSG(msg, ...) \
	do { if (pt_verbosity >= 0) { \
		PTMSG("PT Error", msg "(%s)", ## __VA_ARGS__, strerror(errno)) } } while (0)



/*
 * Error return values used inside the project
 */

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



/*
 * Wrapper for memory allocation and free
 */

/* Macro to do malloc with error handling */
#define pt_malloc(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = malloc(count * sizeof(*(var))); \
		if (var == NULL) { \
			ERRNOMSG(#var); \
			return failret; \
		} \
	} while (0)

/* Macro to do realloc with error handling */
#define pt_realloc(var, count, failret) \
	do { \
		errno = 0; /* set errno to detect error when returning NULL */ \
		var = realloc(var, count * sizeof(*(var))); \
		if (var == NULL) { \
			ERRNOMSG(#var); \
			free(var); /* still we return, free the memory */ \
			return failret; \
		} \
	} while (0)

#define pt_free(var) do { free(var); var = NULL; } while (0);

#endif /* COMMON_H */
