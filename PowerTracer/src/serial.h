#ifndef SERIAL_H
#define SERIAL_H

#include <unistd.h>

int serial_openPort(char *device);

int serial_setupPort(int fd, int baudrate);

int serial_sendMessage(int fd, const char *msg);

int serial_sendBreak(int fd);

int serial_readBytes(int fd, long tv_sec, char *buffer, size_t bsize);

int serial_closePort(int fd);

#define SERIAL_SETUPPORT_RETURN_CHECK \
	do { \
    switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* tcgetattr(), cfsetispeed(), cfsetospeed(), */ \
                        /* tcsetattr(), tcflush()                     */ \
           return(ret); \
        default: \
			assert(!"Unknown return value from serial_setupPort()."); \
    } } while (0)

#define SERIAL_SENDMESSAGE_RETURN_CHECK \
	do { \
    switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* write() */ \
        case ERR_WRITE: \
           return(ret); \
        default: \
			assert(!"Unknown return value from serial_sendMessage()."); \
    } } while (0)

#define SERIAL_SENDBREAK_RETURN_CHECK \
	do { \
    switch(ret) { \
        case OK: \
            break; \
        case ERR_ERRNO: /* tcsendbreak() */ \
           return(ret); \
        default: \
			assert(!"Unknown return value from serial_sendBreak()."); \
    } } while (0)

#define SERIAL_READBYTES_ERROR_CHECK \
	do { \
    switch(ret) { \
        case ERR_ERRNO: /* select(), read() */ \
            return(ret); \
        default: \
			assert(!"Unknown return value from serial_readBytes()."); \
    } } while (0)

#define SERIAL_CLOSEPORT_RETURN_CHECK \
	do { \
	switch(ret) { \
		case OK: \
			break; \
		case ERR_ERRNO: \
			return(ret); \
		default: \
			assert(!"Unknown return value from serial_closePort()."); \
	} } while (0)

#endif /* SERIAL_H */

/* vim: set sw=4 sts=4 et: */
