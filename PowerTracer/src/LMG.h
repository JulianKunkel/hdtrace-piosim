/**
 * @file LMG.h
 *
 * @date 01.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef LMG_H
#define LMG_H

#include <unistd.h>

#include "conf.h"

/**
 * Resets everything in LMG.
 */
int LMG_reset(int fd);

/**
 * Setup the LMG.
 */
int LMG_setup(int fd);

/**
 * Get identity string from LMG.
 */
int LMG_getIdentity(int fd, char buffer[], size_t bsize);

/**
 * Program the LMG action script for continues mode
 */
int LMG_programAction(int fd, ConfigStruct *config);

/**
 * Start the continues mode
 */
int LMG_startContinuesMode(int fd);

/**
 * Stop the continues mode
 */
int LMG_stopContinuesMode(int fd);

/**
 * Read a message in text (ASCII) format
 */
int LMG_readTextMessage(int fd, char buffer[], size_t bsize);

/**
 * Read a message in binary format.
 */
int LMG_readBinaryMessage(int fd, void *buffer, size_t bsize);

/**
 * Get all errors from LMG.
 */
int LMG_getAllErrors(int fd, char buffer[], size_t bsize);

/**
 * Close connection to  LMG.
 */
int LMG_close(int fd);


#define LMG_RESET_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendBreak(), serial_sendMessage() */ \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_reset()."); \
		} \
	} while (0)

#define LMG_SETUP_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* LMG_reset(), serial_sendMessage() */ \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_setup()."); \
		} \
	} while (0)

#define LMG_GETIDENTITY_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendMessage(), LMG_readTextMessage() */ \
        case ERR_WRITE: \
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
        case ERR_MALLOC: \
        case ERR_BSIZE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_getIdentity()."); \
		} \
	} while (0)

#define LMG_PROGRAMACTION_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendMessage() */ \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_programAction()."); \
		} \
	} while (0)

#define LMG_STARTCONTINUESMODE_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendMessage() */ \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_startContinuesMode()."); \
		} \
	} while (0)

#define LMG_STOPCONTINUESMODE_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendMessage() */ \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_stopContinuesMode()."); \
		} \
	} while (0)

#define LMG_READTEXTMESSAGE_ERROR_CHECK \
	do { \
		switch(ret) { \
        case ERR_ERRNO: /* serial_readBytes() */ \
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
        case ERR_MALLOC: \
        case ERR_BSIZE: \
           return(ret); \
        default: \
			assert(!"Unknown return value from LMG_readTextMessage()."); \
		} \
	} while (0)

#define LMG_READBINARYMESSAGE_ERROR_CHECK \
	do { \
		switch(ret) { \
        case ERR_ERRNO: /* serial_readBytes() */ \
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
        case ERR_MALLOC: \
        case ERR_BSIZE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_readBinaryMessage()."); \
		} \
	} while (0)

#define LMG_GETALLERRORS_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendMessage(), LMG_readTextMessage() */ \
        case ERR_WRITE: \
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
        case ERR_MALLOC: \
        case ERR_BSIZE: \
			return(ret); \
        default: \
			assert(!"Unknown return value from LMG_getAllErrors()."); \
		} \
    } while (0)

#define LMG_CLOSE_RETURN_CHECK \
	do { \
		switch(ret) { \
		case OK: \
			break; \
		case ERR_ERRNO: /* LMG_reset(), serial_sendMessage() */ \
		case ERR_WRITE: \
			return(ret); \
		default: \
			assert(!"Unknown return value from LMG_close()."); \
		} \
	} while (0)

#endif /* LMG_H */

/* vim: set sw=4 sts=4 et: */
