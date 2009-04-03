/**
 * @file hdStats.c
 * @ingroup hdStats
 *
 * Implementations of all functions for writing statistics
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include "hdStats.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <assert.h>

#include "config.h"
#include "common.h"
#include "util.h"
#include "hdError.h"

/**
 * Print a number of indentations to string.
 */
int sprintIndent(char* string, size_t size, int num);

/**
 * Get the length a value type
 */
int getValueLength(hdStatsValueType type);

/**
 * Create a new statistics group.
 *
 * Creates and opens the file for a new statistics group. The filename is built
 * using the rules for HDTrace statistics files an the given topology and
 * level.
 *
 * For example:
 * @code
 * hdTopoNode myTopoNode = hdT_createTopoNode({"myhost", "myrank", "mythread"} , 3);
 * hdS_createGroup("Energy", myTopoNode, 1);
 * @endcode
 * creates a file named @c Project_myhost_Energy.dat
 *
 * Header part of the file written here looks like:
 * @code
 * XXXXX
 * <TopologyNode>
 *         <Label value="host0">
 *                 <Label value="process0" \>
 *         </Label>
 * </TopologyNode>
 * <Energy timestampDatatype="EPOCH">
 *
 * @endcode
 *
 * @param groupName  Name of the new statistics group
 * @param topology   Topology to use (for project name)
 * @param topoNode   Topology node to use
 * @param topoLevel  Topology level the group shell belong to
 *
 * @retval Statistics group on success
 * @retval NULL error, setting errno
 *
 * @errno
 *  - HD_ERR_INVALID_ARGUMENT
 *  - HD_ERR_MALLOC
 *  - HD_ERR_BUFFER_OVERFLOW
 *  - HD_ERR_CREATE_FILE
 *
 *  @sa hdT_createTopoNode, hdT_createTopology
 */
hdStatsGroup hdS_createGroup (
        const char *groupName, /* Name of the new statistics group */
        hdTopology topology,   /* Topology to use, only needed for project name */
        hdTopoNode topoNode,   /* Topology node to use */
        int topoLevel          /* Topology level the group shell belong to */
        )
{
	/* check input */
	if (isValidTagString(groupName) || topology == NULL
			|| hdT_getTopoNodeLevel(topoNode) < topoLevel)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* generate filename of the form Project_Level1_Level2..._Group.dat */
	char *filename = generateFilename(topology->project,
			topoNode, topoLevel, groupName, ".dat");
	 if (filename == NULL)
	 {
		 /* errno set by generateFilename(): MALLOC or BUFFER_OVERFLOW */
		 return NULL;
	 }

	/* open file and truncate to zero if it already exists  */
	int fd = open(filename, O_CREAT | O_WRONLY | O_TRUNC | O_NONBLOCK, 0662);
	if (fd < 0)
	{
		hd_error_msg("Could not open file %s: %s", filename, strerror(errno));
	 	hd_error_return(HD_ERR_CREATE_FILE, NULL);
 	}

	/* create group */
	hdStatsGroup newGroup = malloc(sizeof(*newGroup));

	/* allocate buffer for header */
	char *buffer = malloc(HDS_HEADER_BUF_SIZE * sizeof(*buffer));

	/* set buffer offset to start */
	int offset = 0;


	/*
	 *  Write beginning of group header
	 */

	/* set buffer offset to header start byte */
	offset = HDS_HEADER_SIZE_LENGTH;

	/* write newline to buffer */
	buffer[offset++] = '\n';

	int sret;
#define ERROR_CHECK_AND_UPDATE \
	if (sret >= HDS_HEADER_BUF_SIZE - offset) { \
		hd_error_msg("Overflow of HDS_HEADER_BUF_SIZE buffer" \
	" during creation of header for %s", filename); \
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, NULL); } \
	if (sret < 0) { hd_error_return(HD_ERR_UNKNOWN, NULL); } \
	offset += sret; \
	assert(buffer[offset] == '\0');

	/* write TopologyNode start tag to buffer */
	sret = snprintf(buffer, HDS_HEADER_BUF_SIZE - offset, "%s\n",
			"<TopologyNode>");
	ERROR_CHECK_AND_UPDATE;

	for (int i = 1; i < topoLevel; ++i)
	{
		/* write Indentation for i-th Label start tag to buffer */
		sret = sprintIndent(buffer+offset, HDS_HEADER_BUF_SIZE - offset, i);
		ERROR_CHECK_AND_UPDATE;

		/* write i-th Label start tag to buffer */
		sret = snprintf(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
				"<Label name=\"%s\">\n", hdT_getTopoPathLabel(topoNode,i));
		ERROR_CHECK_AND_UPDATE;
	}

	/* write Indentation for last Label tag to buffer */
	sret = sprintIndent(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
			topoLevel);
	ERROR_CHECK_AND_UPDATE;

	/* write last Label tag to buffer */
	sret = snprintf(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
			"<Label name=\"%s\" />\n",
			hdT_getTopoPathLabel(topoNode,topoLevel));
	ERROR_CHECK_AND_UPDATE;

	for (int i = topoLevel - 1; i >= 1; --i)
	{
		/* write Indentation for i-th Label end tag to buffer */
		sret = sprintIndent(buffer+offset, HDS_HEADER_BUF_SIZE - offset, i);
		ERROR_CHECK_AND_UPDATE;

		/* write i-th Label end tag to buffer */
		sret = snprintf(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
				"%s\n", "</Label>");
		ERROR_CHECK_AND_UPDATE;
	}

	/* write TopologyNode end tag to buffer */
	sret = snprintf(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
			"%s\n", "</TopologyNode>");
	ERROR_CHECK_AND_UPDATE;

	/* write statistics group start tag to buffer */
	sret = snprintf(buffer+offset, HDS_HEADER_BUF_SIZE - offset,
			"<%s timestampDatatype=\"EPOCH\">\n", groupName);
	ERROR_CHECK_AND_UPDATE;

#undef ERROR_CHECK

	/* write newline to buffer */
	buffer[offset++] = '\n';


	/* set remaining group attributes */

	newGroup->fd = fd;
	newGroup->tracefile = filename;
	newGroup->buffer = buffer;
	newGroup->offset = offset;
    newGroup->hasString = 0;
    newGroup->entryLength = 0;
    newGroup->valueTypes =
    	malloc(HDS_MAX_VALUES_PER_GROUP * sizeof(*(newGroup->valueTypes)));
    newGroup->nextValueIdx = 0;
    newGroup->isCommitted = 0;

    return newGroup;
}


/**
 * Add a new value to statistics group.
 *
 * TODO: Description
 * @code
 * <Voltage type="float" unit="mV"/>
 * <Current type="float" unit="mA"/>
 * <Power type="float" unit="mW"/>
 * @endcode
 *
 * @param group              Statistics Group
 * @param name               Name of the new value
 * @param type               Type of the new value
 * @param unit               Unit string of the new value
 * @param readOutMultiplier  Multiplier to match unit
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_EMPTYNAME
 */
int hdS_addValue (
        hdStatsGroup group,      /* Statistics Group */
        const char* name,        /* Name of the new value */
        hdStatsValueType type,   /* Type of the new value */
        const char* unit,        /* Unit string of the new value */
        long readOutMultiplier   /* Multiplier to match unit */
        )
{
	if(group == NULL || !isValidString(name))
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	if(group->nextValueIdx == HDS_MAX_VALUES_PER_GROUP)
	{
		hd_error_msg("Maximum number of values exceeded. Cannot add value %s"
				" (group file %s). Check HDS_MAX_VALUES_PER_GROUP.",
				name, group->tracefile);
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, -1);
	}

	/*
	 * Add value to group
	 */
	group->valueTypes[group->nextValueIdx++] = type;

	/* if there is a string value, checking the entry length is impossible */
	if(group->hasString == 0)
	{
		int valueLength = getValueLength(type);
		if (valueLength > 0)
		{
			group->entryLength += valueLength;
		}
		else
		{
			/* mark entry length check as disabled */
			group->hasString = 1;
			group->entryLength = -1;
		}
	}

	/* increase index for next value */
	group->nextValueIdx++;

	/*
	 * Write value to header buffer
	 */
	/* TODO: write header */

	assert(group->offset == (int) strlen(group->buffer));

	return 0;
}

/**
 * Commit Group, closes initialization step.
 *
 * TODO: Description
 * @code
 * </Energy>
 * @endcode
 *
 * @param group Statistics Group
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_EMPTYNAME
 */
int hdS_commitGroup (
        hdStatsGroup group       /* Statistics Group */
        )
{
	/*
	 * finalize header
	 */

	/* write group end tag */
	/* TODO: implement */

	/* write header length */
	int hlen = group->offset - HDS_HEADER_SIZE_LENGTH -1;

	snprintf(group->buffer, HDS_HEADER_SIZE_LENGTH + 1, "%5.d", hlen);
	/* TODO: Error handling */

	/* override '\0' by '\n' again */
	assert(group->buffer[HDS_HEADER_SIZE_LENGTH] == '\0');
	group->buffer[HDS_HEADER_SIZE_LENGTH] = '\n';

	/* write header to file */
	ssize_t written = writeToFile(group->fd, group->buffer,
			group->offset, group->tracefile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hd_error_msg("Timeout during writing header to %s."
					" Stop tracing statistics group.", group->tracefile);
		case HD_ERR_MALLOC:
			hd_error_msg("Out of memory during writing header to %s."
					" Stop tracing statistics group.", group->tracefile);
		case HD_ERR_WRITE_FILE:
			hd_error_msg("Write error during writing header to %s."
					" Stop tracing statistics group.", group->tracefile);
		case HD_ERR_UNKNOWN:
			hd_error_msg("Unknown error during writing header to %s."
					" Stop tracing statistics group.", group->tracefile);
		}
		/* disable current group (does not touch errno) */
		hdS_disableGroup(group);

		/* do not change errno, just return error */
		return -1;
	}

	/* free header buffer allocated in hdS_createGroup() */
	free(group->buffer);

	/* create buffer for collecting entries */
	group->buffer = malloc(HDS_ENTRY_BUF_SIZE * sizeof(*(group->buffer)));

	/* set buffer offset to start */
	group->offset = 0;

	/* mark group as committed */
	group->isCommitted = 1;

	return 0;
}


/**
 * Enable statistics group.
 *
 * This function does not set errno!
 *
 * @param group  Group to enable
 *
 * @retval  1 Success, was already enabled
 * @retval  0 Success, is now enabled
 * @retval -1 Error: \a group is NULL
 *
 * @sa hdS_disableGroup
 */
int hdS_enableGroup(hdStatsGroup group)
{
	if (group == NULL)
		return -1;

	if (group->isEnabled == 1)
		return 1;

	group->isEnabled = 1;
	return 0;
}

/**
 * Disable statistics group.
 *
 * This function does not set errno!
 * So it can easier be used as reaction of errors.
 *
 * @param group  Group to disable
 *
 * @retval  1 Success, was already disabled
 * @retval  0 Success, is now disabled
 * @retval -1 Error: \a group is NULL
 *
 * @sa hdS_enableGroup
 */
int hdS_disableGroup(hdStatsGroup group)
{
	if (group == NULL)
		return -1;

	if (group->isEnabled == 0)
		return 1;

	group->isEnabled = 0;
	return 0;
}

/**
 * Get if statistics group is enabled.
 *
 * This function produces no error, a NULL group is always disabled;
 *
 * @param group  Group to ask
 *
 * @retval  1 Group is enabled
 * @retval  0 Group is disabled
 *
 * @sa hdS_enableGroup, hdS_disableGroup
 */
int hdS_isEnabled(hdStatsGroup group)
{
	if (group == NULL)
		return 0;

	return group->isEnabled;
}

/**
 * Writes a complete entry to a statistics group.
 *
 * TODO: Description
 *
 * @param group        Statistics Group
 * @param entry        Pointer to the entry to write
 * @param entryLength  Length of the entry to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGLENGTH (only when no string values are defined)
 */
int hdS_writeEntry (
        hdStatsGroup group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        int entryLength          /* Length of the entry to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Writes 4 byte integer as next value to a statistics group.
 *
 * TODO: Description
 *
 * @param group Statistics Group
 * @param value INT32 value to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGTYPE if the next value is not of type INT32
 */
int hdS_writeInt32Value (
        hdStatsGroup group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Writes 8 byte integer as next value to a statistics group.
 *
 * TODO: Description
 *
 * @param group Statistics Group
 * @param value INT64 value to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGTYPE if the next value is not of type INT64
 */
int hdS_writeInt64Value (
        hdStatsGroup group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Writes 4 byte float as next value to a statistics group.
 *
 * TODO: Description
 *
 * @param group Statistics Group
 * @param value FLOAT value to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGTYPE if the next value is not of type FLOAT
 */
int hdS_writeFloatValue (
        hdStatsGroup group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Writes 8 byte double as next value to a statistics group.
 *
 * TODO: Description
 *
 * @param group Statistics Group
 * @param value DOUBLE value to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGTYPE if the next value is not of type DOUBLE
 */
int hdS_writeDoubleValue (
        hdStatsGroup group,      /* Statistics Group */
        double value             /* DOUBLE value to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Writes string as the next value to a statistics group.
 *
 * TODO: Description
 *
 * @param group Statistics Group
 * @param str STRING value to write
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 * - HDS_WRONGTYPE if the next value is not of type STRING
 */
int hdS_writeString (
        hdStatsGroup group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        )
{
	/* TODO: implement function */
	return 0;
}

/**
 * Finalizes a statistics group.
 *
 * This must be the last hdStatistics* function called in a program.
 *
 * @param group Statistics Group
 *
 * @retval  0 success
 * @retval -1 error, setting errno
 *
 * @errno
 * - HDS_NOGROUP
 */
int hdS_finalize(
        hdStatsGroup group      /* Statistics Group */
        )
{
	/* free memory allocated in generateFilename */
	free(group->tracefile);
	free(group->valueTypes);

	/* free memory allocated in hdS_commitGroup */
	free(group->buffer);

	/* free memory allocated in hdS_createGroup() */
	free(group);

	return 0;
}

/**
 * Print a number of indentations to string.
 *
 * @param string  String to use
 * @param size    Maximum size to write in bytes
 * @param num     Number of indentations to write
 *
 * @return  Number of bytes written to string
 */
int sprintIndent(char* string, size_t size, int num)
{
	int off;
	for (int i = 0; i < num; ++i)
	{
		int sret = snprintf(string+off, size - off, HD_INDENT_STRING);
		/* TODO: error handling? */
		off += sret;
	}
	return off;
}

/**
 * Get the length a value type
 *
 * @param type Type to get length for
 *
 * @return Length of type in byte
 */
int getValueLength(hdStatsValueType type)
{
	switch (type)
	{
	case INT32:
		return sizeof(int32_t);
	case INT64:
		return sizeof(int64_t);
	case FLOAT:
		return sizeof(float);
	case DOUBLE:
		return sizeof(double);
	case STRING:
	default:
		return -1;
	}
}

