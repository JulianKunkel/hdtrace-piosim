#ifndef LMG_H
#define LMG_H

#include <unistd.h>

int LMG_reset(int fd);

int LMG_setup(int fd);

int LMG_getIdentity(int fd, char buffer[], size_t bsize);

int LMG_readTextMessage(int fd, char buffer[], size_t bsize);

int LMG_readBinaryMessage(int fd, void *buffer, size_t bsize);

int LMG_getAllErrors(int fd, char buffer[], size_t bsize);

int LMG_close(int fd);

#define LMG_RESET_RETURN_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_sendbreak(), serial_sendMessage() */ \
        case ERR_WRITE: \
        case ERR_UNKNOWN: \
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
        case ERR_UNKNOWN: \
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
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
        case ERR_BSIZE: \
        case ERR_WRITE: \
            return(ret); \
        default: \
			assert(!"Unknown return value from LMG_getIdentity()."); \
		} \
	} while (0)

#define LMG_READTEXTMESSAGE_ERROR_CHECK \
	do { \
		switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* serial_readBytes() */ \
        case ERR_NO_MSG: \
        case ERR_MSG_FORMAT: \
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
        case ERR_BSIZE: \
        case ERR_WRITE: \
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
