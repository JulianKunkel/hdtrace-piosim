#include "LMG.h"

#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <assert.h>
#include <string.h>

#include "ptError.h"
#include "ptInternal.h"
#include "serial.h"

/**
 * Resets everything in LMG.
 *
 * @param fd  File descriptor of the serial port
 *
 * @return  Error state
 *
 * @retval  OK    Success
 * @retval  -->   \ref serial_sendbreak()
 * @retval  --!   \ref serial_sendMessage()
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

    return OK;
}

/**
 * Setup the LMG.
 *
 * @param fd  File descriptor of the serial port
 *
 * @return  Error state
 *
 * @retval  OK    Success
 * @retval  -->   \ref LMG_reset()
 * @retval  -->   \ref serial_sendMessage()
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

/**
 * Get identity string from LMG.
 *
 * @param fd      File descriptor of the serial port
 * @param buffer  Buffer to store response
 * @param bsize   Size of the buffer
 *
 * @return  Error state
 *
 * @retval  OK    Success
 * @retval  -->   \ref serial_sendMessage()
 * @retval  -->   \ref LMG_readTextMessage()
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

    return OK;
}

/**
 * Read a message in text (ASCII) format
 *
 * @param fd  File descriptor of the serial port
 * @param buffer  Buffer to store message
 * @param bsize   Size of the buffer
 *
 * @return  Length of the message read or error
 *
 * @retval >=0            Length of the message (including trailing '\0')
 * @retval ERR_NO_MSG     No message to read
 * @retval ERR_MSG_FORMAT Message is not a correct text message
 * @retval ERR_BSIZE      Buffer too small for message
 *                        (the message is lost, you cannot read it again)
 * @retval -->            \ref serial_readBytes()
 */
int LMG_readTextMessage(int fd, char buffer[], size_t bsize)
{
    int ret;

    /* Indicator if we are in a string surrounded by '"' */
    int in_string = 0;

    /*
     * Allocate memory for message
     */
    size_t tbsize = 100;
    char *tmpbuffer;
    pt_malloc(tmpbuffer, tbsize, ERR_MALLOC);

    /* Pointer to current location */
    char *bufptr = tmpbuffer;

    /* Size of the whole message */
    size_t msg_size = 0;

    /*
     * Read the first character and test for message type.
     * ('#' indicating the start of a binary message)
     */
    ret = serial_readBytes(fd, 5, bufptr, 1);
    if (ret < 1)
    {
    	pt_free(tmpbuffer);
        if (ret == 0)
            return(ERR_NO_MSG);
        SERIAL_READBYTES_ERROR_CHECK;
    }

    /* Return error if the message is binary */
    if (*bufptr == '#') {
    	pt_free(tmpbuffer);
        return(ERR_MSG_FORMAT);
    }

    /*
     * Read the rest of the message from serial bus
     */
    while(1)
    {
        /* already have read the first character */
    	if (*bufptr == '\0') {
    		/* this is not allowed */
    		pt_free(tmpbuffer);
    		return ERR_MSG_FORMAT;
    	}
    	else if (*bufptr == '"') {
            /* is this the start or end of a string? */
            in_string = in_string ? 0 : 1;
        }
        else if (!in_string && *bufptr == '\n') {
            /* replace line feed with end of string and stop reading */
            *bufptr='\0';
            break;
        }

        /* check if we have still space in buffer */
        assert(msg_size <= tbsize);
        if (msg_size == tbsize) {
        	tbsize *= 2;
        	pt_realloc(tmpbuffer, tbsize, ERR_MALLOC);
        	bufptr = tmpbuffer + msg_size;
        }

        /* read next character */
        bufptr++;
        ret = serial_readBytes(fd, 5, bufptr, 1);
        if (ret < 1)
        {
        	pt_free(tmpbuffer);
        	if (ret >= 0)
                return(ERR_MSG_FORMAT);
            SERIAL_READBYTES_ERROR_CHECK;
        }
        msg_size++;

    /* TODO: speedup by using a local buffer reading more bytes at once? */
    }

    DEBUGMSG("Received Msg: %s\n", tmpbuffer);

    /* copy message to output buffer it it fits */
   	if (msg_size > bsize) {
   		/* throw message away and return */
   		// TODO better solution
   		pt_free(tmpbuffer);
   	    DEBUGMSG("Message doesn't fit into output buffer.\n");
   		return ERR_BSIZE;
   	}

	/* copy message to output buffer and return */
	memcpy(buffer, tmpbuffer, msg_size);
	pt_free(tmpbuffer);


    /* return the length of the message including trailing '\0' */
    return(msg_size);
}

/**
 * Read a message in binary format.
 *
 * @param fd      File descriptor of the serial port
 * @param buffer  Buffer to store message
 * @param bsize   Size of the buffer
 *
 * @return  Length of the message read or error
 *
 * @retval >=0            Length of the message (including trailing '\0')
 * @retval ERR_NO_MSG     No message to read
 * @retval ERR_MALLOC     Out of memory
 * @retval ERR_MSG_FORMAT Message is not a correct binary message
 * @retval ERR_BSIZE      Buffer too small for message
 *                        (the message is lost, you cannot read it again)
 * @retval -->            \ref serial_readBytes()
 */
int LMG_readBinaryMessage(int fd, void *buffer, size_t bsize)
{
    int ret;
    int size_length;
    int binary_size;
    char locbuffer[10];

    /*
     * Allocate memory for message (fake)
     */
    void *tmpbuffer;
    pt_malloc(tmpbuffer, 0, ERR_MALLOC);

    /* Size of the whole message */
    size_t msg_size = 0;

    /*
     * Run in a loop since the message could be build with
     *  more than one binary data block
     */
    while (1) {

    	/* Size of this binary data block */
    	size_t block_size;

        /*
         * Read header of the binary block consisting of the start symbol '#',
         * the length of the size and the size of the binary data
         *
         * Binary Msg: "#500024xxxxxxxxxxxxxxxxxxxxxxx\n"
         *               ^   ^                      ^
         *  length of size   size of binary data   binary data
         */

	    /*
	     * Read next byte to see if more binary data follow
	     */
	    ret = serial_readBytes(fd, 5, locbuffer, 1);
	    if (ret < 1)
	    {
	    	pt_free(tmpbuffer);
	    	if (ret == 0) {
	    		if (msg_size == 0)
					return ERR_NO_MSG;
	    		else
	    			return ERR_MSG_FORMAT;
	    	}
	        SERIAL_READBYTES_ERROR_CHECK;
	    }

	    /* check for '#' indicating start of another binary message
	     * and '\n' indicating the end of the message */
	    switch(locbuffer[0]) {
        case '\n':
            /* end of message */
        	if (msg_size > bsize) {
        		/* throw message away and return */
        		// TODO better solution
        		pt_free(tmpbuffer);
        		return ERR_BSIZE;
        	}

        	/* copy message to output buffer and return */
        	memcpy(buffer, tmpbuffer, msg_size);
        	pt_free(tmpbuffer);
            return(msg_size);

        case '#':
            /* there is another binary block, continue */
        	break;

        default:
        	/* unexpected format */
        	pt_free(tmpbuffer);
            return(ERR_MSG_FORMAT);
	    }

	    /*
	     * Read length of size
	     */
	    ret = serial_readBytes(fd, 5, locbuffer, 1);
	    if (ret < 1)
	    {
	    	pt_free(tmpbuffer);
	    	if (ret == 0) {
	    		if (msg_size == 0)
					return ERR_NO_MSG;
	    		else
	    			return ERR_MSG_FORMAT;
	    	}
	        SERIAL_READBYTES_ERROR_CHECK;
	    }

        /* scan length of size of the binary data */
        locbuffer[1] = '\0';
        ret = sscanf(locbuffer, "%d", &size_length);
        if (ret == EOF)
        {
            ERROR_ERRNO("sscanf()");
            pt_free(tmpbuffer);
            return(ERR_MSG_FORMAT);
        }

        assert(size_length < 10);

        /* read size of the binary part */
        ret = serial_readBytes(fd, 5, locbuffer, size_length);
        if (ret < size_length)
        {
            pt_free(tmpbuffer);
            if (ret >= 0)
                return(ERR_MSG_FORMAT);
            SERIAL_READBYTES_ERROR_CHECK;
        }

        locbuffer[size_length] = '\0';
        ret = sscanf(locbuffer, "%d", &block_size);
        if (ret == EOF)
        {
            ERROR_ERRNO("sscanf()");
            pt_free(tmpbuffer);
            return(ERR_MSG_FORMAT);
        }

        /*
         * Allocate memory for message
         */
        pt_realloc(tmpbuffer, msg_size + block_size, ERR_MALLOC);

        /*
         * Read binary part from serial bus
         */
        ret = serial_readBytes(fd, 5, tmpbuffer + msg_size, block_size);
        if (ret < block_size)
        {
        	pt_free(tmpbuffer);
            if (ret >= 0)
                return(ERR_MSG_FORMAT);
            SERIAL_READBYTES_ERROR_CHECK;
        }

        /*
         * Update size of whole message
         */
        msg_size += block_size;
    }
}

/**
 * Get all errors from LMG.
 *
 * @param fd      File descriptor of the serial port
 * @param buffer  Buffer to store error message
 * @param bsize   Size of the buffer
 *
 * @return  Function error state
 *
 * @retval OK   Success
 * @retval -->  \ref serial_sendMessage()
 * @retval -->  \ref LMG_readTextMessage()
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

    return OK;
}

/**
 * Close connection to  LMG.
 *
 * @param fd  File descriptor of the serial port
 *
 * @return  Error state
 *
 * @retval  OK    Success
 * @retval  -->   \ref LMG_reset()
 * @retval  -->   \ref serial_sendMessage()
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
