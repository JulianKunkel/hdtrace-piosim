#include "serial.h"

#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>   /* File control definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <string.h>
#include <sys/select.h>
#include <assert.h>

#include "ptError.h"

/**
 * Open serial port.
 *
 * @param device  Name of the device file of the serial port to open
 *
 * @return  File descriptor or error
 *
 * @retval     >=0     File descriptor
 * @retval  ERR_ERRNO  Error in system library call with errno set (open())
 */
int serial_openPort(char *device)
{
    int fd; /* File descriptor for the port */

    fd = open(device, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd < 0)
    {
        ERROR_ERRNO("open()");
        return(ERR_ERRNO);
    }

    fcntl(fd, F_SETFL, 0);

    /* TODO Clear input and output buffers,
     *  more than tcflush will do in setup (if possible).
     *  It seems that there could be old data in the buffers from other
     *  programs or previous run. */

    return (fd);
}

/**
 * Setup serial port.
 *
 * @param fd        File descriptor of the serial port
 * @param baudrate  Baudrate to use.
 *
 * @retval     OK      Success
 * @retval  ERR_ERRNO  Error in system library call with errno set
 *                     (tcgetattr(), cfsetispeed(), cfsetospeed(), tcsetattr(),
 *                      tcflush())
 */
int serial_setupPort(int fd, int baudrate)
{
    int ret;
    struct termios options;
    speed_t speed;

    switch(baudrate)
    {
        case 38400:
            speed = B38400;
	    break;
        case 57600:
            speed = B57600;
	    break;
        case 115200:
            speed = B115200;
	    break;
        case 230400:
            speed = B230400;
	    break;
        default:
	    fputs("Unknown baudrate, setting to 38400.", stderr);
	    speed = B38400;
    }

    /*
     * Get the current options for the port...
     */
    ret = tcgetattr(fd, &options);
    if(ret != 0)
    {
       ERROR_ERRNO("tcgetattr()");
       return(ERR_ERRNO);
    }

    /*
     * Set input flags
     *
     * IGNPAR Ignore framing errors and parity errors.
     * ICRNL  Translate  carriage  return to newline on input
     *
     * Disable all software flow control (IXON, IXOFF, IXANY)
     */
    options.c_iflag     = ( IGNPAR | ICRNL );
    options.c_iflag     &= ~( IXON | IXOFF | IXANY);

    /*
     * Set output flags
     *
     * OPOST  Enable implementation-defined output processing.
     */
    options.c_oflag     = ( OPOST );

    /*
     * Enable the receiver and set local mode...
     */
    options.c_cflag = (CLOCAL | CREAD);

    /*
     * Set data encoding to 8N1
     */
    options.c_cflag &= ~CSIZE;
    options.c_cflag |= CS8;
    options.c_cflag &= ~PARENB;
    options.c_cflag &= ~CSTOPB;

    /*
     * Enable hardware flow control
     */
#ifdef CNEW_RTSCTS
    options.c_cflag |= CNEW_RTSCTS;
#else
#ifdef CRTSCTS
    options.c_cflag |= CRTSCTS;
#endif
#endif

    /*
     * Set to raw input
     */
    options.c_lflag = 0;
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

/* TODO: eventuell später nötig
    options.c_cc[VMIN] = 0;
    options.c_cc[VTIME] = 1200;
*/
    /*
     * Set the baud rates
     */
    ret = cfsetispeed(&options, speed);
    if(ret != 0)
    {
        ERROR_ERRNO("cfsetispeed()");
        return(ERR_ERRNO);
    }

    ret = cfsetospeed(&options, speed);
    if(ret != 0)
    {
        ERROR_ERRNO("cfsetospeed()");
        return(ERR_ERRNO);
    }

    /*
     * Set the new options for the port...
     */
    ret = tcsetattr(fd, TCSANOW, &options);
    if(ret != 0)
    {
        ERROR_ERRNO("tcsetattr()");
        return(ERR_ERRNO);
    }

    ret = tcflush(fd, TCIOFLUSH);
    if(ret != 0)
    {
        ERROR_ERRNO("tcflush()");
        return(ERR_ERRNO);
    }

    return(OK);
}

/**
 * Send a Message.
 *
 * @param fd        File descriptor of the serial port
 * @param msg       String to send
 *
 * @retval     OK      Success
 * @retval  ERR_ERRNO  Error in system library call with errno set (write())
 * @retval  ERR_WRITE  Not the whole message could be written
 */
int serial_sendMessage(int fd, const char *msg)
{
    int n;

    /*
     * Send message to device
     * TODO: integrate timeout for not blocking forever on connection problems
     */
    n = write(fd, msg, strlen(msg));
    if (n < 0)
    {
        ERROR_ERRNO("write()");
        return(ERR_ERRNO);
    }
    else if (n < strlen(msg))
    {
        ERROR("Couldn't write the whole message");
        return(ERR_WRITE);
    }

    n = write(fd, "\n", 1);
    if (n < 0)
    {
        ERROR_ERRNO("write()");
        return(ERR_ERRNO);
    }
    else if (n < 1)
    {
       ERROR("Couldn't write line feed to terminate the message");
       return(ERR_WRITE);
    }
#ifdef DEBUG
    printf("Sended Msg: %s\n", msg);
#endif

    return(0);
}

/**
 * Send a BREAK.
 *
 * @param fd        File descriptor of the serial port
 *
 * @retval     OK      Success
 * @retval  ERR_ERRNO  Error in system library call with errno set
 *                     (tcsendbreak())
 */
int serial_sendBreak(int fd)
{
    int ret;

    ret = tcsendbreak(fd, 0);
    if (ret > 0)
    {
        ERROR_ERRNO("tcsendbreak()");
        return(ERR_ERRNO);
    }

    return(OK);
}

/**
 * Reads exact the next bsize bytes from fd
 * using select with timeout in seconds.
 *
 * @param fd        File descriptor of the serial port
 * @param tv_sec    Timeout for select
 * @param buffer    Buffer to store read bytes in
 * @param bsize     Number of bytes to read
 *
 * @return  Number of bytes read or error
 *
 * @retval     >=0     Number of bytes read
 * @retval  ERR_ERRNO  Error in system library call with errno set
 *                     (select(), read())
 */
int serial_readBytes(int fd, long tv_sec, char *buffer, size_t bsize)
{
    int ret;

    fd_set input, readmask; /* fd bit masks for select */
    struct timeval timeout; /* timeout struct for select */

    char *bufptr;           /* pointer to position in buffer to write next */
    int nbytes, sumbytes;   /* counters for bytes read */

    /* Initialize the input set */
    FD_ZERO(&input);
    FD_SET(fd, &input);

    /* Set bufptr to buffer start and initialize sumbytes */
    bufptr = buffer;
    sumbytes = 0;

    if (bsize == 0)
    	return sumbytes;

    while(1)
    {
        readmask = input;

        /* Initialize the timeout structure */
        timeout.tv_sec  = tv_sec;
        timeout.tv_usec = 0;

        /* Do the select */
        ret = select(fd+1, &readmask, NULL, NULL, &timeout);

        /* See if there was an error */
        if (ret < 0)
        {
            ERROR_ERRNO("select()");
            return(ERR_ERRNO);
        }
        else if (ret == 0)
        {
            /* timeout */
            return(sumbytes);
        }

        if (FD_ISSET(fd, &readmask))
        {
            /* read all data available but max until buffer filled */
            if((nbytes = read(fd, bufptr, buffer + bsize - bufptr)) < 0)
            {
                ERROR_ERRNO("read()");
                return(ERR_ERRNO);
            }

            bufptr += nbytes;
            sumbytes += nbytes;

            /* check if enough bytes are read */
            if(sumbytes == bsize)
                return(sumbytes);
        }
    }
}

/**
 * Close serial port.
 *
 * @param fd        File descriptor of the serial port
 *
 * @return  Error state
 *
 * @retval     OK      Success
 * @retval  ERR_ERRNO  Error in system library call with errno set (close())
 */
int serial_closePort(int fd)
{
    int ret;

    /*
     * Cleanup buffers (esp. for error case)
     */
    ret = tcflush(fd, TCIOFLUSH);
    if(ret != 0)
    {
        ERROR_ERRNO("tcflush()");
        return(ERR_ERRNO);
    }

    /*
     * Close serial port
     */
    ret = close(fd);
    if (ret < 0)
    {
        ERROR_ERRNO("close()");
        return(ERR_ERRNO);
    }

    return(OK);
}

/* vim: set sw=4 sts=4 et fdm=syntax: */
