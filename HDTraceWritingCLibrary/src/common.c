/**
 * @file common.c
 *
 * Implementation of functions used by hdTrace and hdStats in common
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
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
#include "hdTopoInternal.h"
#include "util.h"

#include "config.h"

#define SOTRACE

#ifdef SOTRACE
#include <dlfcn.h>
#define GLIBC "libc.so.6"
#endif 

struct hdtrace_options hdt_options = {
    .verbosity = VLEVEL, 
    .buffer_size = 1*1024*1024, // 1 MiB of trace size
    .overwrite_existing_files = 1,
    .path_prefix = "",
    .max_nesting_depth = 8,
    .force_flush = 0,
};


/**
 * Initializes global options by reading environment variable 
 */
static void initEnvironmentVariables() {	
	/* get debug level */
	char *vlvl = getenv("HDTRACE_VERBOSITY");
	if (isValidString(vlvl))
		sscanf(vlvl, "%d", & hdt_options.verbosity);
	
	vlvl = getenv("HDTRACE_BUFFER_SIZE_KB");
	if (isValidString(vlvl)){
		sscanf(vlvl, "%lld", (long long int *) &  hdt_options.buffer_size);
		if ( hdt_options.buffer_size < 1024) {
			hd_error_msg("Invalid buffer size set in environment: %lld", (long long int)  hdt_options.buffer_size );
			hdt_options.buffer_size = 1024;
		}
		hdt_options.buffer_size *= 1024;
	}
	
	vlvl = getenv("HDTRACE_OVERWRITE_EXISTING_FILES");
	if (isValidString(vlvl))
		sscanf(vlvl, "%d", & hdt_options.overwrite_existing_files);	
	vlvl = getenv("HDTRACE_MAX_NESTING_DEPTH");
	if (isValidString(vlvl))
		sscanf(vlvl, "%d", & hdt_options.max_nesting_depth);	
	vlvl = getenv("HDTRACE_PREFIX");
	if (isValidString(vlvl)){
		hdt_options.path_prefix = strdup(vlvl);	
	}

	vlvl = getenv("HDTRACE_FORCE_FLUSH");
	if (isValidString(vlvl))
		sscanf(vlvl, "%d", & hdt_options.force_flush);	
}


void hdTrace_init(){
    initEnvironmentVariables();
}


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
 * char * filename = generateFilename(toponode, topoLevel, groupName, ".stat");
 * if (filename == NULL)
 *         // error
 * @endcode
 *
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
char * generateFilename( const hdTopoNode *toponode,
		int level, const char *group, const char* affix)
{
	/* check input */
	assert(hdT_getTopoNodeLevel(toponode) >= level);
	assert(isValidString(affix));

	/* generate filename */
	assert(HD_MAX_FILENAME_LENGTH != 0);
	char * filename = malloc(HD_MAX_FILENAME_LENGTH);
	memset(filename, 0, HD_MAX_FILENAME_LENGTH);
	if(filename == NULL)
	{
		hd_info_msg("malloc() error during %s filename generation for %s: %s",
				affix, toponode->string, strerror(errno));
		hd_error_return(HD_ERR_MALLOC, NULL);
	}

	int ret;

        strcpy (filename, hdt_options.path_prefix);
	
#define ERROR_MSG \
	hd_error_msg("Overflow of HD_MAX_FILENAME_LENGTH buffer during" \
			" %s filename generation for %s", affix, toponode->string)
	int maxLength = strlen(filename);
	strncpy(filename + maxLength, toponode->topology->project, HD_MAX_FILENAME_LENGTH - maxLength);	

	if (filename[HD_MAX_FILENAME_LENGTH - 1] != '\0')
	{
		ERROR_MSG;
		free(filename);
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, NULL);
	}

#define ERROR_CHECK do { \
	if (ret >= HD_MAX_FILENAME_LENGTH ) \
	{ \
		ERROR_MSG; \
		free(filename); \
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, NULL); \
	} \
	} while (0)

	/* append "_level" for each topology level */
	for (int i = 1; i <= level; ++i)
	{
		ret = snprintf(filename + strlen(filename), HD_MAX_FILENAME_LENGTH,
				"_%s", hdT_getTopoPathLabel(toponode, i));
		ERROR_CHECK;
	}

	if (group == NULL)
	{
		ret = snprintf(filename + strlen(filename), HD_MAX_FILENAME_LENGTH,
				"%s", affix);
		ERROR_CHECK;
	}
	else
	{
		/* TODO: Convert all non-alphanum chars to '_' */
		ret = snprintf(filename + strlen(filename), HD_MAX_FILENAME_LENGTH,
				"_%s%s", group, affix);
		ERROR_CHECK;
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
       static ssize_t (* my_write) ( int ,const void *,size_t  ) = & write;


#ifdef SOTRACE
#warning "Using DLOPEN to provide my_write"
        // directly map write to real write...
       void * dllFile = dlopen(GLIBC, RTLD_LAZY);
       if (dllFile == NULL){
           printf("[Error] trace wrapper - dll not found %s\n", GLIBC); 
           char *errstr;
           errstr = dlerror();
           if (errstr != NULL)
           printf ("A dynamic linking error occurred: (%s)\n", errstr);
           exit(1); 
       }
        my_write = dlsym(dllFile, "write");
#endif

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
			printf("E3\n");
			hd_info_msg("Timeout during writing to %s", filename);
			hd_error_return(HD_ERR_TIMEOUT, -1);
		}
		else if (sret < 0)
		{
			printf("E2\n");		
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
		ssize_t wret =  (* my_write)(fd, buffer, count);
		if (wret == -1)
		{
			printf("E\n");
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
		count -= (size_t) wret;
		buffer += wret;

		/* update number of bytes written */
		written += wret;

	}	
	
	return written;
}

/**
 * Print a number of indentations to a string.
 *
 * Upon successful return, this function return the number of characters
 *  printed (not including the trailing '\\0' used to end output to
 *  strings).<br>
 * The function do not write more than \a size bytes (including  the trailing
 *  '\\0'). If the output was truncated due to this limit then the return value
 *  is the number of characters (not including the trailing '\\0') which would
 *  have been written to the final string if enough space had been available.
 *  Thus, a return value of \a size  or  more  means  that  the output was
 *  truncated. (See also \c snprintf(3))<br>
 *
 * @param string  String to use
 * @param size    Maximum size to write in bytes
 * @param num     Number of indentations to write
 *
 * @return  Number of bytes written to string
 */
/* NOT USED --- REMOVE IF YOU DESIDE TO */
int snprintIndent(char* string, size_t size, int num)
{
	int off = 0;
	for (int i = 0; i < num; ++i)
	{
		int sret = snprintf(string+off, size - (size_t) off, HD_INDENT_STRING);
		if (sret >= (int) size - off)
			return off + sret;  // snprintf like return value
		if (sret < 0)
			return sret;  // should never happen

		off += sret;
	}
	return off;
}

/**
 * Return a string describing the error represented by errno.
 *
 * @if api_only
 *  @ingroup hdError
 * @endif
 *
 * @param errno errno value to get the string for
 *
 * @return Error describing string or null for unknown errno value
 */
char* hdT_strerror(int errno) {
	switch(errno) {
	/* common errors */
	case HD_ERR_INVALID_ARGUMENT:
		return "Invalid argument";
	case HD_ERR_MALLOC:
	    return "Error while memory allocation";
	case HD_ERR_BUFFER_OVERFLOW:
	    return "Error due to buffer overflow";
	case HD_ERR_GET_TIME:
	    return "Error while getting system time";
	case HD_ERR_CREATE_FILE:
	    return "Error while creating a file";
	case HD_ERR_WRITE_FILE:
	    return "Error while writing a file";
	case HD_ERR_CLOSE_FILE:
	    return "Error while closing a file";
	case HD_ERR_TIMEOUT:
		return "Timeout occurred";
	case HD_ERR_TRACE_DISABLED:
		return "Trace is disabled";
	case HD_ERR_INVALID_CONTEXT:
		return "function may not be called in this context";
	case HD_ERR_UNKNOWN:
		return "Error with unknown cause";
	/* hdTrace errors */
	case HDT_EXAMPLE:
		return "Example error";
	/* hdStats error */
	case HDS_ERR_GROUP_COMMIT_STATE:
		return "Statistics group's commit state is not the needed";
	case HDS_ERR_UNEXPECTED_ARGVALUE:
		return "One of the arguments has an unexpected value";
	case HDS_ERR_ENTRY_STATE:
		return "State of the current entry is wrong for requested action";
	default:
		return NULL;
	}
}
