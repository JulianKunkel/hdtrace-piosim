#ifndef PTERROR_H
#define PTERROR_H

#include <error.h>
#include <errno.h>

/* Common return values */
#define OK             0
#define ERR_ERRNO     -1
#define ERR_UNKNOWN   -2
#define ERR_CUSTOM    -3    /* Custom error */

/* Return values used in serial_* */
#define ERR_WRITE    -10    /* Write error */

/* Return values used in LMG_* */
#define ERR_NO_MSG     -20  /* No message ready */
#define ERR_MSG_FORMAT -21  /* Message does not hat the expected format */
#define ERR_BSIZE      -22  /* Buffer size is not sufficient */

/* Configuration file */
#define ERR_SYNTAX    -30

/* Macro for generate non fatal warnings */
#define WARN(msg, ...) error(0,0, "WARNING: " msg, ## __VA_ARGS__)

/* Macro for reporting custom errors (without errno set) */
#define ERROR(msg, ...) error_at_line(0, 0, __FILE__, __LINE__, msg, ## __VA_ARGS__)

/* Macro for reporting errors with errno set */
#define ERROR_ERRNO(msg, ...) error_at_line(0, errno, __FILE__, __LINE__, msg, ## __VA_ARGS__)

/* Macro for reporting unknown errors */
#define ERROR_UNKNOWN error_at_line(0, 0, __FILE__, __LINE__, "Unknown Error")

#endif
