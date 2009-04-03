/**
 * @file common.c
 *
 * Implementation of functions used by hdTrace and hdStats in common
 *
 * @date 28.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */


#include "common.h"

#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <sys/select.h>
#include <errno.h>
#include <assert.h>

#include "hdError.h"
#include "hdTopo.h"
#include "util.h"

#include "config.h"


/**
 * Generate well formed filename.
 *
 * This function generates the filenames for the traces. If \a group is
 *  \a NULL, a name of the form "[Project]_[Level1]_[Level2]...[Affix]" is
 *  generated to be used in hdTrace. If \a group is not \a NULL a filename of
 *  the form "[Project]_[Level1]_[Level2]..._[Group][Affix]" is generated for
 *  hdStats.
 *
 * For hdTrace usage \a level should always be the number of the highest level
 *  of the topology since hdTrace files are only allowed for topology leaf
 *  nodes.
 *
 * <b>The memory for the returned \a char* pointing to is allocated by this
 *  function but the caller has to free it by himself.</b>
 *
 * Example:
 * @code
 * char * filename = generateFilename(toponode, hdT_getTopoNodeLevel(toponode), NULL, ".xml");
 * if (filename == NULL)
 *         // error
 * @endcode
 * @code
 * char * filename = generateFilename(toponode, topoLevel, groupName, ".dat");
 * if (filename == NULL)
 *         // error
 * @endcode
 *
 * @param project   Project the file is for
 * @param toponode  Topology node to use
 * @param level     Topology level to create the filename for
 * @param group     Name of statistics group or \a NULL for hdTrace filename
 * @param affix     Affix to append to the filename
 *
 * @return Generated filename of \a NULL on error setting \a errno
 *
 * @errno
 * - HD_ERR_MALLOC
 * - HD_ERR_BUFFER_OVERFLOW
 */
char * generateFilename(const char *project, hdTopoNode toponode,
		int level, const char *group, const char* affix)
{
	/* check input */
	assert(isValidString(project));
	assert(hdT_getTopoNodeLevel(toponode) >= level);
	assert(isValidString(affix));

	/* generate filename */
	assert(HD_MAX_FILENAME_LENGTH != 0);
	char *filename = malloc(HD_MAX_FILENAME_LENGTH * sizeof(*filename));
	if(filename == NULL)
	{
		hd_info_msg("malloc() error during %s filename generation for %s: %s",
				affix, toponode->string, strerror(errno));
		hd_error_return(HD_ERR_MALLOC, NULL);
	}

	size_t pos = 0;
	size_t ret;

#define ERROR_MSG \
	hd_error_msg("Overflow of HD_MAX_FILENAME_LENGTH buffer during" \
			" %s filename generation for %s", affix, toponode->string);

	strncpy(filename, project, HD_MAX_FILENAME_LENGTH);
	if (filename[HD_MAX_FILENAME_LENGTH - 1] != '\0')
	{
		ERROR_MSG
		free(filename);
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, NULL);
	}
	pos = strlen(filename);

#define ERROR_CHECK \
	if (ret >= HD_MAX_FILENAME_LENGTH - pos) \
	{ \
		ERROR_MSG \
		free(filename); \
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, NULL); \
	} \

	/* append "_level" for each topology level */
	for (int i = 1; i <= level; ++i)
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos, "_%s",
				hdT_getTopoPathLabel(toponode, i));
		ERROR_CHECK
		pos = strlen(filename);
	}

	if (group == NULL)
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos, "%s", affix);
		ERROR_CHECK
	}
	else
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos,
				"_%s%s", group, affix);
		ERROR_CHECK
	}

#undef ERROR_CHECK
#undef ERROR_MSG

	return filename;
}

/**
 * Writes data to a file at the current offset.
 *
 * @param fd       File descriptor of the file to use
 * @param buf      Data to write
 * @param count    Number of bytes to write
 * @param filename Filename for error messages
 *
 * @return Number of bytes written or -1 on error, setting errno
 *
 * @retval >=0  Success
 * @retval -1   Error, check errno
 *
 * @errno
 * - HD_ERR_TIMEOUT
 * - HD_ERR_MALLOC
 * - HD_ERR_WRITE_FILE
 * - HD_ERR_UNKNOWN
 */
ssize_t writeToFile(int fd, void *buf, size_t count, const char *filename)
{
	/* check input */
	assert(fd > 0);
	assert(buf != NULL);
	assert(isValidString(filename));


	ssize_t written = 0;

	char *buffer = buf;

	/* create select() stuff */
	fd_set writefds;

	FD_ZERO(&writefds);
	FD_SET(fd, &writefds);

	struct timeval timeout;

	/* set timeout */
	/* setting this only once results in:
	 * - aggregate multiple timeouts to this maximum on Linux
	 * - using this timeout all the time again on some other systems
	 * see select(2)
	 */
	timeout.tv_sec=HD_WRITE_TIMEOUT;
	timeout.tv_usec=0;

	/* write to fd */
	while (count > 0)
	{
		/* wait until fd is ready for writing or timeout */
		int sret = select(fd+1, NULL, &writefds, NULL, &timeout);
		if (sret == 0)
		{
			hd_info_msg("Timeout during writing to %s", filename);
			hd_error_return(HD_ERR_TIMEOUT, -1);
		}
		else if (sret < 0)
		{
			hd_info_msg("select() error during writing to %s: %s",
					filename, strerror(errno));
			switch (errno)
			{
			case EBADF: /* fd is an invalid file descriptor */
				hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
				break;
			case EINTR: /* signal was caught */
				continue;
			case ENOMEM: /* unable to allocate memory for internal tables */
				hd_error_return(HD_ERR_MALLOC, -1);
				break;
			case EINVAL:
				assert(0);
				/* fall through if NDEBUG defined */
			default:
				hd_error_return(HD_ERR_UNKNOWN, -1);
			}
		}

		/* assure fd is ready for writing */
		assert(FD_ISSET(fd, &writefds));

		/* coming here means fd is ready for writing */
		ssize_t wret = write(fd, buffer, count);
		if (wret == -1)
		{
			hd_info_msg("write() error during writing to %s: %s",
					filename, strerror(errno));
			switch (errno)
			{
			case EINTR:  /* interrupted by signal */
				/* try again */
				continue;
			case EAGAIN: /* fd marked non-blocking and the write would block */
			case EBADF:  /* fd is not a valid file descriptor */
			case EFAULT: /* buf is outside your accessible address space */
			case EINVAL: /* some of the arguments are invalid */
			case EPIPE:  /* fd is connected to pipe of socket */
				/* since this is an internal function, we did something wrong */
				assert(0);
			case EFBIG:  /* tried to write beyond allowed file size */
			case ENOSPC: /* no space left on device */
			case EIO:    /* low-level I/O error */
				hd_error_return(HD_ERR_WRITE_FILE, -1);
			default:
				hd_error_return(HD_ERR_UNKNOWN, -1);
			}
		}

		/* update number of bytes to write and pointer to next data */
		count -= wret;
		buffer += wret;

		/* update number of bytes written */
		written += wret;

	}

	return written;
}
