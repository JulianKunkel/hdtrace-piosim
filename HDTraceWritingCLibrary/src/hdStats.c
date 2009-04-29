/**
 * @file hdStats.c
 *
 * Implementations of all functions for writing statistics
 *
 * @ifnot api_only
 *  @ingroup hdStats
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#include "hdStats.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>
#include <assert.h>

#include "config.h"
#include "common.h"
#include "util.h"
#include "hdError.h"

/* ************************************************************************* *
 *                        DOCUMENTATION STRUCTURING                          *
 * ************************************************************************* */

/** @cond api_only */
/**
 * @enum _hdStatsValueType
 * @ingroup hdStats
 */
/**
 * @var _hdStatsValueType INT32
 * @ingroup hdStats
 */
/**
 * @var _hdStatsValueType INT64
 * @ingroup hdStats
 */
/**
 * @var _hdStatsValueType FLOAT
 * @ingroup hdStats
 */
/**
 * @var _hdStatsValueType DOUBLE
 * @ingroup hdStats
 */
/**
 * @var _hdStatsValueType STRING
 * @ingroup hdStats
 */
/**
 * @typedef hdStatsValueType
 * @ingroup hdStats
 */
/**
 * @typedef hdStatsGroup
 * @ingroup hdStats
 */
/**
 * @def order_bytes32ip
 * @ingroup hdStats
 */
/**
 * @def order_bytes64ip
 * @ingroup hdStats
 */
/**
 * @def order_bytes32fp
 * @ingroup hdStats
 */
/**
 * @def order_bytes64fp
 * @ingroup hdStats
 */
/** @endcond */


/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/**
 * Type of usage for the group buffer
 */
enum _hdStatsBufferType {
	/** Buffer for creating group header */
	HDS_HEADER_BUFFER,//!< HDS_HEADER_BUFFER
	/** Buffer for collection values for entry */
	HDS_ENTRY_BUFFER  //!< HDS_ENTRY_BUFFER
};

/**
 * @internal
 * Structure representing one statistics group.
 *
 * Do not use directly, use \ref hdStatsGroup instead
 */
struct _hdStatsGroup {
	/**
	 * Name of the group
	 */
	char *name;

    /**
     * File descriptor of the statistics group file
     */
	int fd;

    /**
     * Filename of the statistics group file (for error output only)
     */
    char *tracefile;

    /**
     * Buffer for creating header and collecting entries
     */
    char *buffer;

    /**
     * Current type of \a buffer
     */
    enum _hdStatsBufferType btype;

    /**
     * Offset for buffer to write next byte
     */
    int offset;

    /**
     * Length that an entry should have
     */
    size_t entryLength;

    /**
     * Types of the defined values (for error checking)
     * '-1' terminated in @ref hdS_commitGroup
     */
    hdStatsValueType *valueTypes;

    /**
     * Index of the next value to write (for error checking)
     */
    int nextValueIdx;

    /**
     * True if string values are defined
     * => reduced error checking
     */
    unsigned int hasString : 1;

    /**
     * True if the group is committed (for error checking)
     */
    unsigned int isCommitted : 1;

    /**
     * True if the group is enabled to trace
     */
    unsigned int isEnabled : 1;
};


/* ************************************************************************* *
 *                     STATIC FUNCTION DECLARATIONS                          *
 * ************************************************************************* */

/**
 * Get the length a value type
 */
static size_t getValueLength(hdStatsValueType type);

/**
 * Get the string representation of a type
 */
static const char * getTypeString(hdStatsValueType type);

/**
 * Append formated string to group buffer.
 */
static int appendFormatToGroupBuffer(hdStatsGroup group,
		const char *format, ...) __attribute__((format(printf,2,3)));
/**
 * Append indentations to group buffer.
 */
static int appendIndentToGroupBuffer(hdStatsGroup group, int num);

/**
 * Write timestamp to the start of the group buffer
 */
static int writeTimestampToGroupBuffer(hdStatsGroup group);

/**
 * Append value to group buffer.
 */
static int appendValueToGroupBuffer(hdStatsGroup group, void * value_p, hdStatsValueType type);

/**
 * Append group buffer to group file and reset offset.
 */
static int flushGroupBuffer(hdStatsGroup group);


/* ************************************************************************* *
 *                    PUBLIC FUNCTION IMPLEMENTATIONS                        *
 * ************************************************************************* */

/**
 * Create a new statistics group.
 *
 * Creates and opens the file for a new statistics group. The filename is built
 * using the rules for HDTrace statistics files an the given topology and
 * level.
 *
 * For example:
 * @code
 * char *topo[] = {"myhost", "myrank", "mythread"};
 * hdTopoNode myTopoNode = hdT_createTopoNode(topo , 3);
 * hdS_createGroup("Energy", myTopoNode, 1);
 * @endcode
 * creates a file named @c Project_myhost_Energy.dat
 *
 * @ifnot api_only
 * The header part generated by this function is topology node tag and the
 *  start tag of the statistics group. For example:
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
 * XXXXX is space left in buffer for header length.
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
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
 * @if api_only
 *  - \ref HD_ERR_INVALID_ARGUMENT
 *  - \ref HD_ERR_MALLOC
 *  - \ref HD_ERR_BUFFER_OVERFLOW
 *  - \ref HD_ERR_CREATE_FILE
 * @else
 *  - HD_ERR_INVALID_ARGUMENT
 *  - HD_ERR_CREATE_FILE
 *  - all from \ref generateFilename
 * @endif
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
	/* get debug level */
	verbosity = VLEVEL;
	char *vlvl = getenv("HDS_VERBOSITY");
	if (isValidString(vlvl))
		sscanf(vlvl, "%d", &verbosity);

	/* check input */
	if (!isValidXMLTagString(groupName) || topology == NULL
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
		hd_error_msg("Could not open file %s: %s", filename, strerror(errno))
	 	hd_error_return(HD_ERR_CREATE_FILE, NULL)
 	}

	/* create group */
	hdStatsGroup group;
	hd_malloc(group, 1, NULL)

	/* setup group buffer for header */
    hd_malloc(group->buffer, HDS_HEADER_BUF_SIZE, NULL)

	group->btype = HDS_HEADER_BUFFER;
	group->offset = 0;

	/* set remaining group attributes */
	group->name = strdup(groupName);
	group->fd = fd;
	group->tracefile = filename;
    group->hasString = FALSE;
    group->entryLength = 0;
    hd_malloc(group->valueTypes, HDS_MAX_VALUES_PER_GROUP, NULL)
    group->nextValueIdx = 0;
    group->isCommitted = FALSE;
    group->isEnabled = FALSE;


	/*
	 *  Write beginning of group header
	 */

	/* set buffer offset to header start byte */
	group->offset = HDS_HEADER_SIZE_LENGTH;

	/* append header length terminating newline to buffer */
	group->buffer[group->offset++] = '\n';

	/* print debug output */
	hd_debug_msg("Group '%s': offset=%d, btype=%d",
			group->name, group->offset, group->btype);


	int ret;

	/* append TopologyNode start tag to buffer */
	ret = appendFormatToGroupBuffer(group, "<TopologyNode>\n");
	if (ret < 0)
		return NULL;

	for (int i = 1; i < topoLevel; ++i)
	{
		/* append Indentation for i-th Label start tag to buffer */
		ret = appendIndentToGroupBuffer(group, i);
		if (ret < 0)
			return NULL;

		/* append i-th Label start tag to buffer */
		ret = appendFormatToGroupBuffer(group, "<Label value=\"%s\">\n",
				hdT_getTopoPathLabel(topoNode,i));
		if (ret < 0)
			return NULL;
	}

	/* append Indentation for last Label tag to buffer */
	ret = appendIndentToGroupBuffer(group, topoLevel);
	if (ret < 0)
		return NULL;

	/* append last Label tag to buffer */
	ret = appendFormatToGroupBuffer(group, "<Label value=\"%s\" />\n",
			hdT_getTopoPathLabel(topoNode,topoLevel));
	if (ret < 0)
		return NULL;

	for (int i = topoLevel - 1; i >= 1; --i)
	{
		/* append Indentation for i-th Label end tag to buffer */
		ret = appendIndentToGroupBuffer(group, i);
		if (ret < 0)
			return NULL;

		/* append i-th Label end tag to buffer */
		ret = appendFormatToGroupBuffer(group, "</Label>\n");
		if (ret < 0)
			return NULL;
	}

	/* write TopologyNode end tag to buffer */
	ret = appendFormatToGroupBuffer(group, "</TopologyNode>\n");
	if (ret < 0)
		return NULL;

	/* write statistics group start tag to buffer */
	struct timeval tv;
	gettimeofday(&tv, NULL);
	ret = appendFormatToGroupBuffer(group,
			"<%s timestampDatatype=\"EPOCH\" timeOffset=\"%010d.%09d\">\n",
			groupName, (int32_t) tv.tv_sec, (int32_t) tv.tv_usec * 1000);
	if (ret < 0)
		return NULL;

    return group;
}


/**
 * Add a new value to the entry structure of a statistics group.
 *
 * By multiple calls of this function you can specify the structure of an entry
 *  to the group. This is only possible as long as the group is not committed.
 *  After committing the group by calling \ref hdS_commitGroup any modification
 *  to the entry structure of a group is impossible.<br>
 * Each call of this function adds a new value to the end of the entry
 *  structure of the group.
 *
 * @ifnot api_only
 * The header part generated by this function is the tag for one value.
 * For example:
 * @code
 * <Voltage type="FLOAT" unit="mV"/>
 * @endcode
 * or
 * @code
 * <Current type="FLOAT" unit="mA"/>
 * @endcode
 * or
 * @code
 * <Power type="FLOAT" unit="mW"/>
 * @endcode
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group      Statistics group to modify
 * @param name       Name of the new value
 * @param type       Type of the new value
 * @param unit       Unit string of the new value.
 *                   (NULL or not more than \ref HDS_MAX_UNIT_NAME_LENGTH
 *                    characters including XML escapes done automatically)
 * @param grouping   Grouping string of the new value
 *                   (NULL or not more than \ref HDS_MAX_GROUPING_NAME_LENGTH
 *                    characters including XML escapes done automatically)
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_BUFFER_OVERFLOW
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 */
int hdS_addValue (
        hdStatsGroup group,    /* Statistics Group */
        const char* name,      /* Name of the new value */
        hdStatsValueType type, /* Type of the new value */
        const char* unit,      /* Unit string of the new value */
        const char* grouping   /* Grouping string for the new value */
        )
{
	/* check input */
	if(group == NULL || !isValidXMLTagString(name))
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	char unitString[HDS_MAX_UNIT_NAME_LENGTH];
	if (unit != NULL)
	{
		if (!escapeXMLString(unitString, HDS_MAX_UNIT_NAME_LENGTH, unit))
			hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	char groupingString[HDS_MAX_GROUPING_NAME_LENGTH];
	if (grouping != NULL)
	{
		if (!escapeXMLString(groupingString, HDS_MAX_GROUPING_NAME_LENGTH, grouping))
			hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check if maximum is already reached */
	if(group->nextValueIdx >= HDS_MAX_VALUES_PER_GROUP)
	{
		hd_error_msg("Maximum number of values exceeded. Cannot add value %s"
				" (group file %s). Check HDS_MAX_VALUES_PER_GROUP.",
				name, group->tracefile);
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, -1);
	}

	/* check group commit state */
	if (group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/*
	 * Add value to group
	 */
	group->valueTypes[group->nextValueIdx] = type;

	/* if there is a string value, checking the entry length is impossible */
	if(!group->hasString)
	{
		size_t valueLength = getValueLength(type);
		if (valueLength > 0)
		{
			group->entryLength += valueLength;
		}
		else
		{
			/* mark entry length check as disabled */
			group->hasString = TRUE;
		}
	}

	/* increase index for next value */
	group->nextValueIdx++;

	/*
	 * Write value to header buffer
	 */

	int ret;  // return value

	/* write indentation to buffer */
	ret = appendIndentToGroupBuffer(group, 1);
	if (ret < 0)
		return ret;

	char ubuff[8+HDS_MAX_UNIT_NAME_LENGTH];
	if (unit != NULL)
	{
		ret = snprintf(ubuff, 8+HDS_MAX_UNIT_NAME_LENGTH,
				"unit=\"%s\" ", unitString);
		assert(ret < 8+HDS_MAX_UNIT_NAME_LENGTH);
	}

	char gbuff[12+HDS_MAX_GROUPING_NAME_LENGTH];
	if (grouping != NULL)
	{
		ret = snprintf(gbuff, 12+HDS_MAX_GROUPING_NAME_LENGTH,
				"grouping=\"%s\" ",	groupingString);
		assert(ret < 12+HDS_MAX_GROUPING_NAME_LENGTH);
	}

	/* write tag for value */
	ret = appendFormatToGroupBuffer(group, "<%s type=\"%s\" %s%s/>\n",
			name, getTypeString(type), ubuff, gbuff);
	if (ret < 0)
		return ret;

	/* assure group->buffer is '\0' terminated with length group->offset */
	assert(group->offset - HDS_HEADER_SIZE_LENGTH
			== (int) strlen(group->buffer + HDS_HEADER_SIZE_LENGTH));

	return 0;
}

/**
 * Commit statistics group, closes initialization step.
 *
 * Calling this function for a statistics group closes its initialization and
 * writes the descriptive header to the group's trace file.<br>
 * The group is not enabled automatically, so before any passed values or
 *  entries are recorded by the group, you have to call \ref hdS_enableGroup.
 *
 * @ifnot api_only
 * The header part generated by this function is only the closing tag of the
 *  statistics group. For example
 * @code
 * </Energy>
 * @endcode
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics group to commit
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HD_ERR_UNKNOWN
 */
int hdS_commitGroup (
        hdStatsGroup group       /* Statistics Group */
        )
{
	/* check input */
	if(group == NULL)
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check group commit state */
	if (group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/*
	 * finalize header
	 */

	int sret; // return value of snprintf calls

	/* write group end tag */
	sret = appendFormatToGroupBuffer(group, "</%s>\n", group->name);
	if (sret < 0)
		return sret;

	/* write header length */
	int hlen = group->offset - HDS_HEADER_SIZE_LENGTH -1;

	sret = snprintf(group->buffer, HDS_HEADER_SIZE_LENGTH + 1, "%05u", hlen);
	/* since we have already written behind, this cannot happen */
	assert(sret <= HDS_HEADER_BUF_SIZE - group->offset);
	if (sret < 0)
	{
		hd_error_return(HD_ERR_UNKNOWN, -1);
	}

	/* assure buffer is '\0' terminated (see snprintf(3)) */
	assert(group->buffer[HDS_HEADER_SIZE_LENGTH] == '\0');

	/* override '\0' by '\n' again */
	group->buffer[HDS_HEADER_SIZE_LENGTH] = '\n';

	/* write header to file */
	flushGroupBuffer(group);

	/* free header buffer allocated in hdS_createGroup() */
	hd_free(group->buffer);

	/* setup buffer for collecting entries */
	hd_malloc(group->buffer, HDS_ENTRY_BUF_SIZE, -1);
	group->btype = HDS_ENTRY_BUFFER;
	group->offset = 0;

	/* mark first value as next expected */
	group->nextValueIdx = 0;

	/* mark group as committed */
	group->isCommitted = TRUE;

	return 0;
}


/**
 * Enable statistics group.
 *
 * This function does not set errno!
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group  Statistics group to enable
 *
 * @return Error state and type
 *
 * @retval  1 Success, was already enabled
 * @retval  0 Success, is now enabled
 * @retval -1 Error: \a group is not committed
 * @retval -2 Error: \a group is NULL
 *
 * @sa hdS_disableGroup
 */
int hdS_enableGroup(hdStatsGroup group)
{
	if (group == NULL)
		return -2;

	if (!group->isCommitted)
		return -1;

	if (group->isEnabled)
		return 1;

	group->isEnabled = TRUE;
	return 0;
}

/**
 * Disable statistics group.
 *
 * This function does not set errno!
 * So it can easier be used as reaction of errors.
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group  Statistics group to disable
 *
 * @return Error state and type
 *
 * @retval  1 Success, was already disabled
 * @retval  0 Success, is now disabled
 * @retval -1 Error: \a group is not committed
 * @retval -2 Error: \a group is NULL
 *
 * @sa hdS_enableGroup
 */
int hdS_disableGroup(hdStatsGroup group)
{
	if (group == NULL)
		return -2;

	if (!group->isCommitted)
		return -1;

	if (!group->isEnabled)
		return 1;

	group->isEnabled = FALSE;
	return 0;
}

/**
 * Get if statistics group is enabled.
 *
 * This function produces no error, a NULL group is always disabled;
 *
 * @param group  Statistics group to ask
 *
 * @return Enable state of the group
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
 * @attention
 * Do only use this function if you really know what you are doing.<br>
 * No byte order conversation is done. No check for consistency with the
 *  specified entry structure beside length check is done. In groups containing
 *  string values even the length check is omitted since it is not possible.
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group        Statistics group to use
 * @param entry        Pointer to the entry to write
 * @param entryLength  Length of the entry to write
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_UNEXPECTED_ARGVALUE
 * - \ref HDS_ERR_ENTRY_STATE
 */
int hdS_writeEntry (
        hdStatsGroup group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        size_t entryLength          /* Length of the entry to write */
        )
{
	/* check input */
	if(group == NULL || entry == NULL )
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check enable state */
	if (!group->isEnabled)
	{
		hd_error_return(HD_ERR_TRACE_DISABLED, -1);
	}

	/* check group commit state */
	if (!group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/* check if the entry length is correct */
	if (!group->hasString && entryLength != group->entryLength)
	{
		hd_error_return(HDS_ERR_UNEXPECTED_ARGVALUE, -1);
	}

	/* check if we are at the very beginning of the current entry */
	if (group->offset != 0 || group->nextValueIdx != 0)
	{
		hd_error_return(HDS_ERR_ENTRY_STATE, -1);
	}

	/* write timestamp */
	writeTimestampToGroupBuffer(group);

	/* return if nothing to write */
	if (entryLength == 0)
		return 0;

	/* write entry to buffer behind timestamp */
	memcpy(group->buffer + group->offset, entry, entryLength);
	group->offset += (int) entryLength;

	/* write entry to file */
	int wret = flushGroupBuffer(group);
	if (wret < 0)
		return wret;

	return 0;
}

/**
 * Writes 4 byte integer as next value to a statistics group.
 *
 * Checks if the next value in current entry is of type \ref INT32 and
 * append it to the group buffer is so.
 * @ifnot api_only
 * This is a wrapper that cares about the byte order and then calls
 *  \ref appendValueToGroupBuffer().
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics Group
 * @param value INT32 value to write
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * @if api_only
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_ENTRY_STATE
 * @else
 * - all from \ref appendValueToGroupBuffer()
 * @endif
 */
int hdS_writeInt32Value (
        hdStatsGroup group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        )
{
	assert(sizeof(value) == getValueLength(INT32));

	int32_t v = value;

	order_bytes32ip(&value);

	int ret = appendValueToGroupBuffer(group, &value, INT32);

	/* print debug output */
	hd_debug_msg("Group '%s': type=%s value=%d",
			group->name, getTypeString(INT32), v);

	return ret;
}

/**
 * Writes 8 byte integer as next value to a statistics group.
 *
 * Checks if the next value in current entry is of type \ref INT64 and
 * append it to the group buffer is so.
 * @ifnot api_only
 * This is a wrapper that cares about the byte order and then calls
 *  \ref appendValueToGroupBuffer.
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics Group
 * @param value INT64 value to write
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * @if api_only
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_ENTRY_STATE
 * @else
 * - all from \ref appendValueToGroupBuffer
 * @endif
 */
int hdS_writeInt64Value (
        hdStatsGroup group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        )
{
	assert(sizeof(value) == getValueLength(INT64));

	int64_t v = value;

	order_bytes64ip(&value);

	int ret = appendValueToGroupBuffer(group, &value, INT64);

	/* print debug output */
	hd_debug_msg("Group '%s': type=%s value=%lld",
			group->name, getTypeString(INT64), v);

	return ret;
}

/**
 * Writes 4 byte float as next value to a statistics group.
 *
 * Checks if the next value in current entry is of type \ref FLOAT and
 * append it to the group buffer is so.
 * @ifnot api_only
 * This is a wrapper that cares about the byte order and then calls
 *  \ref appendValueToGroupBuffer.
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics Group
 * @param value FLOAT value to write
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * @if api_only
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_ENTRY_STATE
 * @else
 * - all from \ref appendValueToGroupBuffer
 * @endif
 */
int hdS_writeFloatValue (
        hdStatsGroup group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        )
{
	assert(sizeof(value) == getValueLength(FLOAT));

	float v = value;

	order_bytes32fp(&value);

	int ret = appendValueToGroupBuffer(group, &value, FLOAT);

	/* print debug output */
	hd_debug_msg("Group '%s': type=%s value=%f",
			group->name, getTypeString(FLOAT), v);

	return ret;
}

/**
 * Writes 8 byte double as next value to a statistics group.
 *
 * Checks if the next value in current entry is of type \ref DOUBLE and
 * append it to the group buffer is so.
 * @ifnot api_only
 * This is a wrapper that cares about the byte order and then calls
 *  \ref appendValueToGroupBuffer.
 * @endif
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics Group
 * @param value DOUBLE value to write
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * @if api_only
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_ENTRY_STATE
 * @else
 * - all from \ref appendValueToGroupBuffer
 * @endif
 */
int hdS_writeDoubleValue (
        hdStatsGroup group,      /* Statistics Group */
        double value             /* DOUBLE value to write */
        )
{
	assert(sizeof(value) == getValueLength(DOUBLE));

	double v = value;

	order_bytes64fp(&value);

	int ret = appendValueToGroupBuffer(group, &value, DOUBLE);

	/* print debug output */
	hd_debug_msg("Group '%s': type=%s value=%f",
			group->name, getTypeString(DOUBLE), v);

	return ret;
}

/**
 * Writes string as the next value to a statistics group.
 *
 * This function is not yet implemented.
 * TODO: Description
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics group to use
 * @param str   STRING value to write
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 */
int hdS_writeString (
        hdStatsGroup group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        )
{
	/* check input */
	if(group == NULL || !isValidString(str))
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check group commit state */
	if (!group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/* TODO: Implement hdS_writeString function */

	/* print error output */
	hd_error_msg("%s", "hdS_writeString() not yet implemented");

	return 0;
}

/**
 * Finalizes a statistics group.
 *
 * This must be the last hdStatistics* function called in a program for each
 *  statistics group.
 *
 * @if api_only
 *  @ingroup hdStats
 * @endif
 *
 * @param group Statistics group to use.
 *
 * @return Error state
 *
 * @retval  0 Success
 * @retval -1 Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 */
int hdS_finalize(
        hdStatsGroup group      /* Statistics Group */
        )
{
	/* check input */
	if(group == NULL)
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check group commit state */
	if (!group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/* free memory allocated in hdS_commitGroup */
	hd_free(group->buffer);

	/* free memory allocated in generateFilename */
	hd_free(group->tracefile);

	/* free memory allocated in hdS_createGroup() */
	hd_free(group->valueTypes);
	hd_free(group->name);
	hd_free(group);

	return 0;
}


/* ************************************************************************* *
 *                    STATIC FUNCTION IMPLEMENTATIONS                        *
 * ************************************************************************* */

/**
 * Get the length a value type
 *
 * @param type Type to get length for
 *
 * @return Length of type in byte
 */
static size_t getValueLength(hdStatsValueType type)
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
		return 0;
	default:
		assert(0);
	}
	return 0;
}

/**
 * Get the string representation of a type.
 *
 * @param type Type to get string representation of
 *
 * @return String representation of \a type
 */
static const char * getTypeString(hdStatsValueType type)
{
	switch (type)
	{
	case INT32:
		return INT32_STRING;
	case INT64:
		return INT64_STRING;
	case FLOAT:
		return FLOAT_STRING;
	case DOUBLE:
		return DOUBLE_STRING;
	case STRING:
		return STRING_STRING;
	default:
		assert(0);
	}
	return NULL;
}

/**
 * Append formated string to group buffer.
 *
 * Prints a formated string to the group buffer using snprintf(3).<br>
 * Prevents buffer overflow.
 *
 * @param group  Statistics group to use
 * @param format Format of the string to write (printf style)
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_BUFFER_OVERFLOW
 * - \ref HD_ERR_UNKNOWN
 */
static int appendFormatToGroupBuffer(hdStatsGroup group,
		const char *format, ...)
{
	assert(group != NULL);
	assert(format != NULL);

	/* switch buffer size and error message depending on buffer usage type */
	size_t bsize = 0;
	const char *errmsg = NULL;
	switch (group->btype)
	{
	case HDS_HEADER_BUFFER:
		bsize = HDS_HEADER_BUF_SIZE;
		errmsg = "Overflow of HDS_HEADER_BUF_SIZE buffer during creation"
			" of header";
		break;
	case HDS_ENTRY_BUFFER:
		bsize = HDS_ENTRY_BUF_SIZE;
		errmsg = "Overflow of HDS_ENTRY_BUF_SIZE buffer during writing"
			" value to group event buffer";
		break;
	default:
		assert(group->btype != HDS_HEADER_BUFFER
				&& group->btype != HDS_ENTRY_BUFFER);
	}

	/* use vsnprintf to actually write to the buffer */
	va_list ap;
	va_start(ap, format);
	int sret = vsnprintf(group->buffer + group->offset,
			bsize - (size_t) group->offset,	format, ap);
	va_end(ap);
	/* check for errors and set errno */
	if (sret >= (int) bsize - group->offset)
	{
		hd_error_msg("%s for group '%s' (%s)", errmsg, group->name, group->tracefile);
		hd_error_return(HD_ERR_BUFFER_OVERFLOW, -1);
	}
	if (sret < 0)
	{
		hd_error_return(HD_ERR_UNKNOWN, -1);
	}

	/* print info output */
	hd_info_msg("Appended to %s group buffer: \"%s\"",
			group->name, group->buffer + group->offset);

	/* update offset */
	group->offset += sret;

	/* print debug output */
	hd_debug_msg("Group '%s': offset=%d, btype=%d",
			group->name, group->offset, group->btype);

	return 0;
}

/**
 * Append indentations to group buffer.
 *
 * This function basically uses \ref appendFormatToGroupBuffer to append
 * \ref HD_INDENT_STRING \a num times to the group buffer.
 *
 * @param group Statistics group to use
 * @param num   Number of indentations to append
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - all from \ref appendFormatToGroupBuffer
 */
static int appendIndentToGroupBuffer(hdStatsGroup group, int num)
{
	assert(group != NULL);
	assert(num >= 0);

	int ret;
	for (int i = 0; i < num; ++i)
	{
		ret = appendFormatToGroupBuffer(group, "%s", HD_INDENT_STRING);
		if (ret < 0)
			return -1;
	}

	return 0;
}

/**
 * Write timestamp to the start of the group buffer.
 *
 * The timestamp written is the current number of seconds and nanoseconds
 * since epoch in two 32 bit integers in correct byte order as specified
 * by HDTrace format.
 *
 * @param group  Statistics group to use
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_UNKNOWN
 */
static int writeTimestampToGroupBuffer(hdStatsGroup group)
{
	assert(group != NULL);

	/* assure we are at the start of group buffer */
	assert(group->offset == 0);

	struct timeval tv;

	/* get time */
	int ret = gettimeofday(&tv, NULL);
	if (ret < 0)
		switch (errno)
		{
		case EFAULT: /* tv pointed outside the accessible address space. */
			assert(errno != EFAULT);
		case EINVAL: /* Timezone (or something else) is invalid. */
			assert(errno != EINVAL);
		default:
			hd_error_return(HD_ERR_UNKNOWN, -1);
		}

	/* convert to seconds and nanoseconds */
	int32_t sec = tv.tv_sec;
	int32_t nsec = tv.tv_usec * 1000;

	/* assure the timestamp will have the correct length */
	assert(sizeof(sec) + sizeof(nsec) == HDS_TIMESTAMP_LENGTH);

	/* print debug output */
	hd_debug_msg("Group '%s': timestamp=%10d.%09d",
			group->name, sec, nsec);

	/* do byte ordering */
	order_bytes32ip(&sec);
	order_bytes32ip(&nsec);

	/* write to buffer */
	memcpy(group->buffer + group->offset, &(sec), sizeof(sec));
	group->offset = (int) sizeof(sec);

	memcpy(group->buffer + group->offset, &(nsec), sizeof(nsec));
	group->offset += (int) sizeof(nsec);

	return 0;
}

/**
 * Append value to group buffer.
 *
 * Byte order of \a *value_p needs to be correct already before calling this
 *  function.
 *
 * @param group   Statistics group to use
 * @param value_p Pointer to the value to append to the buffer
 * @param type    Type of the value
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - \ref HD_ERR_INVALID_ARGUMENT
 * - \ref HD_ERR_TRACE_DISABLED
 * - \ref HDS_ERR_GROUP_COMMIT_STATE
 * - \ref HDS_ERR_ENTRY_STATE
 */
static int appendValueToGroupBuffer(hdStatsGroup group, void * value_p, hdStatsValueType type)
{
	/* Don't use assert here since the errors are    *
	 * simply passed through by hdS_write* functions */

	/* check input */
	if(group == NULL)
	{
		hd_error_return(HD_ERR_INVALID_ARGUMENT, -1);
	}

	/* check enable state */
	if (!group->isEnabled)
	{
		hd_error_return(HD_ERR_TRACE_DISABLED, -1);
	}

	/* check group commit state */
	if (!group->isCommitted)
	{
		hd_error_return(HDS_ERR_GROUP_COMMIT_STATE, -1);
	}

	/* check if the next value type is correct */
	if (group->valueTypes[group->nextValueIdx] != type)
	{
		hd_error_return(HDS_ERR_ENTRY_STATE, -1);
	}

	size_t vlength = getValueLength(type);

	/* assure type not to be string */
	assert(type != STRING);

	/* assure current entry is in a consistent state */
	assert((group->nextValueIdx == 0 && group->offset == 0)
			|| (!(group->nextValueIdx == 0) && !(group->offset == 0)));

	/* write timestamp if this is the first value of a new entry */
	if (group->nextValueIdx == 0)
	{
		writeTimestampToGroupBuffer(group);
	}

	/* write entry to buffer */
	memcpy(group->buffer + group->offset, value_p, vlength);
	group->offset += (int) vlength;
	group->nextValueIdx++;

	/* flush buffer if an entry is complete */
	if (group->offset == (int) group->entryLength + HDS_TIMESTAMP_LENGTH)
	{
		flushGroupBuffer(group);
		group->nextValueIdx = 0;
	}

	return 0;
}

/**
 * Append group buffer to group file and reset offset.
 *
 * This is simply a wrapper around \ref writeToFile producing error messages
 *  depending on errno set if writeToFile fails. The errno remains unchanged
 *  and can be used again by the caller.
 *
 * @param  group  Statistics group to use
 *
 * @return Error state
 *
 * @retval  0  Success
 * @retval -1  Error, setting errno
 *
 * @errno
 * - all from @ref writeToFile
 */
static int flushGroupBuffer(hdStatsGroup group)
{
	assert(group != NULL);

	ssize_t written = writeToFile(group->fd, group->buffer,
			(size_t) group->offset, group->tracefile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hd_error_msg("Timeout during writing header to %s."
					" Stop tracing statistics group '%s'.",
					group->tracefile, group->name);
		case HD_ERR_MALLOC:
			hd_error_msg("Out of memory during writing header to %s."
					" Stop tracing statistics group '%s'.",
					group->tracefile, group->name);
		case HD_ERR_WRITE_FILE:
			hd_error_msg("Write error during writing header to %s."
					" Stop tracing statistics group '%s'.",
					group->tracefile, group->name);
		case HD_ERR_UNKNOWN:
			hd_error_msg("Unknown error during writing header to %s."
					" Stop tracing statistics group '%s'.",
					group->tracefile, group->name);
		default:
			assert(written >= 0);
		}
		/* disable current group (does not touch errno) */
		hdS_disableGroup(group);

		/* do not change errno, just return error */
		return -1;
	}

	group->offset = 0;

	return 0;
}
