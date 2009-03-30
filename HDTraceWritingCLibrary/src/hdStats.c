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

#include "common.h"
#include "util.h"

/**
 * Maximum of statistics values per group
 */
#define MAX_STATISTICS_VALUES_PER_GROUP = 256

/**
 * Create a new statistics group.
 *
 * Creates and opens the file for a new statistics group. The filename is built
 * using the rules for hdTrace statistics files an the given topology and
 * level.
 *
 * For example:
 * @code
 * hdTopology myTopo = hdT_createTopology("myhost", "myrank", "mythread");
 * hdS_createGroup("Energy", myTopo, 1);
 * @endcode
 * creates a file named @c Project_myhost_Energy.dat
 *
 * TODO: Need to get the project name
 *
 * @param groupName      Name of the new statistics group
 * @param topology       Name of the file to create for the group
 * @param topologyLevel  Level of topology the group belongs to
 *
 * @retval hdStatsGroup New statistics group
 * @retval NULL error, setting errno
 *
 * @errno
 *  - HD_ERR_INVALID_ARGUMENT
 *  - HD_ERR_MALLOC
 *  - HD_ERR_CREATE_FILE
 *
 *  @sa hdT_createTopology
 */
hdStatsGroup hdS_createGroup (
        const char *groupName, /* Name of the new statistics group */
        hdTopology topology,   /* Name of the file to create for the group */
        int topologyLevel      /* Level of topology the group belongs to */
        )
{
	size_t ret;

	/* TODO where to get project? */
	char *project = "Project";

	/* check input */
	if (isValidString(groupName) || hdT_getTopoDepth(topology) <= topologyLevel)
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* generate filename of the form Project_Level1_Level2..._Group.dat */
	char *filename = generateFilename(project, topology, topologyLevel, groupName, ".dat");
	 if (filename == NULL)
	 {
		 return NULL;
	 }

	/* open file and truncate if it already exists  */
	FILE *tracefile = fopen(filename,"wb");
	/* TODO: error checking */
	free(filename);

	/*
	 *  Write group header
	 */
	int headerLength = 5; /* 5 bytes for writing the length later */

	/* position the file pointer to the header text start position */
	ret = fseeko(tracefile, headerLength, SEEK_SET);
	/* TODO: error checking */

	/* actually write header */
	char* header = "This is the File header";

#define FPUTS_ERRORCHECK(str) \
	if (ret != strlen(str)) \
	{ \
		/* error */ \
		/* TODO: Check errno and return */ \
	}

	ret = fputs(header, tracefile);
	FPUTS_ERRORCHECK(header)
	headerLength += ret;

#undef FPUTS_ERRORCHECK

	/* write header length to the space left at the beginning of the file */
	fpos_t fpos;

	ret = fgetpos(tracefile, &fpos);
	/* TODO: error checking */

	ret = fseeko(tracefile, 0, SEEK_SET);
	/* TODO: error checking */

	ret = fprintf(tracefile,"%5d", headerLength);
	/* TODO: error checking */

	ret = fsetpos(tracefile, &fpos);
	/* TODO: error checking */

	/* create group */
	hdStatsGroup newGroup;

	newGroup->tracefile = tracefile;
    newGroup->hasString = 0;
    newGroup->entryLength = 0;
    newGroup->valueTypes = NULL;
    newGroup->nextValueIdx = 0;
    newGroup->isCommited = 0;

    return newGroup;
}

/**
 * Add a new value to statistics group.
 *
 * TODO: Description
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
	/* TODO: implement function */
	return 0;
}

/**
 * Commit Group, closes initialization step.
 *
 * TODO: Description
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
	/* TODO: implement function */
	return 0;
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
	/* TODO: implement function */
	return 0;
}

