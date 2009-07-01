#include "LMG.h"

#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <assert.h>

#include "ptError.h"
#include "serial.h"

/*
 * Resets everything in LMG.
 *
 * Return:
 * - OK
 * - ERR_ERRNO (serial_sendbreak(), serial_sendMessage())
 * - ERR_WRITE
 */
int LMG_reset(int fd)
{
    int ret;

    /*
     * Send break for resetting the LMG interface
     *
     * * Continuous mode is left
     * * Data output format change back to ASCII
     * * Command language change back to SCPI.
     */
    ret = serial_sendBreak(fd);
    SERIAL_SENDBREAK_RETURN_CHECK;

    /*
     * Reset the event register of all register structures
     * and clear the error queue.
     */
    ret = serial_sendMessage(fd, "*CLS");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Reset the messuring unit.
     */
    ret = serial_sendMessage(fd, "*RST");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    return(0);
}

/*
 * Setup the LMG.
 *
 * Return:
 * - OK
 * - ERR_ERRNO (LMG_reset(), serial_sendMessage())
 * - ERR_WRITE
 */
int LMG_setup(int fd)
{
    int ret;

    /*
     * Reset LMG's serial interface
     */
    ret = LMG_reset(fd);
    LMG_RESET_RETURN_CHECK;

    /*
     * Set command language to short
     */
    ret = serial_sendMessage(fd, ":SYSTem:LANGuage SHORT");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    return(0);
}

/*
 * Get identity string from LMG.
 *
 * Return:
 * - OK
 * - ERR_ERRNO (serial_sendMessage(), serial_readMessage())
 * - ERR_BSIZE
 * - ERR_WRITE
 */
int LMG_getIdentity(int fd, char buffer[], size_t bsize)
{
    int ret;
    /*
     * Send command to device
     */
    ret = serial_sendMessage(fd, "*IDN?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Read response from device
     */
    ret = LMG_readTextMessage(fd, buffer, bsize);
    if (ret < 0)
        LMG_READTEXTMESSAGE_ERROR_CHECK;

    return(0);
}

/*
 * Read a message in ASCII format.
 *
 * Return:
 * - Length of the message (including trailing '\0')
 * - ERR_ERRNO (serial_readBytes())
 * - ERR_NO_MSG
 * - ERR_MSG_FORMAT
 * - ERR_BSIZE
 */
int LMG_readTextMessage(int fd, char buffer[], size_t bsize)
{
    int ret;

    int in_string = 0;
    char *bufptr = buffer;

    /*
     * Read the first character and test for message type.
     * ('#' indicating the start of a binary message)
     */
    ret = serial_readBytes(fd, 5, bufptr, 1);
    if (ret < 1)
    {
        if (ret >= 0)
            return(ERR_NO_MSG);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    if (*bufptr == '#')
        return(ERR_MSG_FORMAT);

    /*
     * Read the rest of the message from serial bus
     */
    while(1)
    {
        /* allready have read the first character */
        if (*bufptr == '"')
        {
            /* is this the start of end of a string? */
            in_string = in_string ? 0 : 1;
        }
        else if (!in_string && *bufptr == '\n')
        {
            /* replace line feed by end of string and stop reading */
            *bufptr='\0';
            break;
        }

        /* read next character */
        bufptr++;
        ret = serial_readBytes(fd, 5, bufptr, 1);
        if (ret < 1)
        {
            if (ret >= 0)
                return(ERR_MSG_FORMAT);
            SERIAL_READBYTES_ERROR_CHECK;
        }
    /* TODO: speedup by implementing a local buffer? */
    }

#ifdef DEBUG
    printf("Received Msg: %s\n", buffer);
#endif

    /* return the length of the message including trailing '\0' */
    return(bufptr - buffer + 1);
}

/*
 * Read a message in binary format.
 *
 * Return:
 * - Length of the message
 * - ERR_ERRNO (serial_readBytes())
 * - ERR_NO_MSG
 * - ERR_MSG_FORMAT
 * - ERR_BSIZE
 */
int LMG_readBinaryMessage(int fd, void *buffer, size_t bsize)
{
    int ret;
    int size_length;
    int binary_size;
    char locbuffer[10];

    /*
     * Read header of the binary message consisting of the start symbol '#',
     * the length of the size and the size of the binary data
     *
     * Binary Msg: "#500024xxxxxxxxxxxxxxxxxxxxxxx\n"
     *               ^   ^                      ^
     *  length of size   size of binary data   binary data
     */

    /* read the first two bytes */
    ret = serial_readBytes(fd, 10, locbuffer, 2);
    if (ret != 2)
    {
        if (ret == 1)
            return(ERR_MSG_FORMAT);
        if (ret == 0)
            return(ERR_NO_MSG);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    /* check for '#' indicating start of binary message */
    if (locbuffer[0] != '#')
    {
        return(ERR_MSG_FORMAT);
    }

    /* scan length of size of the binary data */
    locbuffer[2] = '\0';
    ret = sscanf(locbuffer+1, "%d", &size_length);
    if (ret == EOF)
    {
        ERROR_ERRNO("sscanf()");
        return(ERR_MSG_FORMAT);
    }

    /* read size of the binary part */
    ret = serial_readBytes(fd, 5, locbuffer, size_length);
    if (ret < size_length)
    {
        if (ret >= 0)
            return(ERR_MSG_FORMAT);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    locbuffer[size_length] = '\0';
    ret = sscanf(locbuffer, "%d", &binary_size);
    if (ret == EOF)
    {
        ERROR_ERRNO("sscanf()");
        return(ERR_MSG_FORMAT);
    }

    /*
     * Test buffer size (must be enough for binary data)
     */
    if (bsize < binary_size)
        return ERR_BSIZE;

    /*
     * Read binary part from serial bus
     */
    ret = serial_readBytes(fd, 5, (char *) buffer, binary_size);
    if (ret < binary_size)
    {
        if (ret >= 0)
            return(ERR_MSG_FORMAT);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    /*
     * Read next byte to see if this is the end of the message
     * or more binary data follow
     */
    ret = serial_readBytes(fd, 5, locbuffer, 1);
    if (ret < 1)
    {
        if (ret >= 0)
            return(ERR_MSG_FORMAT);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    /* check for '#' indicating start of binary message */
    switch(locbuffer[0])
    {
        case '\n':
            /* end of message */
            return(binary_size);
        case '#':
            /* there is another binary part, get recursively */
            /* TODO: work out problem with already read '#' */
            /* at the moment this will always produce a format error */
            ret = LMG_readBinaryMessage(fd,
                    ((char *) buffer)+binary_size, bsize-binary_size);
            if (ret < 0)
                LMG_READBINARYMESSAGE_ERROR_CHECK;
            /* return size sum of the parts */
            return(binary_size + ret);
        default:
            return(ERR_MSG_FORMAT);
    }

}

/*
 * Get all errors from LMG.
 *
 * Return:
 * - OK
 * - ERR_ERRNO (serial_sendMessage(), serial_readMessage())
 * - ERR_BSIZE
 * - ERR_WRITE
 */
int LMG_getAllErrors(int fd, char buffer[], size_t bsize)
{
    int ret;

    /*
     * Set output format to ASCII mode
     */
    ret = serial_sendMessage(fd, "FRMT ASCII");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Send command to device
     */
    ret = serial_sendMessage(fd, "ERRALL?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Read response from device
     */
    ret = LMG_readTextMessage(fd, buffer, bsize);
    if (ret < 0)
        LMG_READTEXTMESSAGE_ERROR_CHECK;

    return(OK);
}

/*
 * Close connection to  LMG.
 *
 * Return:
 * - OK
 * - ERR_ERRNO (serial_sendMessage(), serial_readMessage())
 * - ERR_WRITE
 */
int LMG_close(int fd)
{
    int ret;

    /*
     * Reset LMG's serial interface
     */
    ret = LMG_reset(fd);
    LMG_RESET_RETURN_CHECK;

    /*
     * Change LMG to local mode
     */
    ret = serial_sendMessage(fd, "GTL\n");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    return(OK);
}

/* vim: set sw=4 sts=4 et fdm=syntax: */
