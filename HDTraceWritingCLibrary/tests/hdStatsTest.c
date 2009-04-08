/**
 * @file hdStatsTest.c
 *
 * @date 08.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#undef NDEBUG /* all tests depend on working assert */
#include <assert.h>
#include <errno.h>

#include "hdStats.h"
#include "hdError.h"
#include "../src/config.h"

#include "tests.h"

/**
 * Copies from hdStats.c for direct access while testing
 */
enum _hdStatsBufferType {
	/** Buffer for creating group header */
	HDS_HEADER_BUFFER,//!< HDS_HEADER_BUFFER
	/** Buffer for collection values for entry */
	HDS_ENTRY_BUFFER  //!< HDS_ENTRY_BUFFER
};
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
     * True if string values are defined
     * => reduced error checking
     */
    int hasString;

    /**
     * Length that an entry should have
     */
    int entryLength;

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
     * True if the group is committed (for error checking)
     */
    int isCommitted;

    /**
     * True if the group is enabled to trace
     */
    int isEnabled;
};

/**
 * Return standard testing group (just created)
 */
static hdStatsGroup getGroup(void)
{
	/* create topology */
	char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	return hdS_createGroup("MyGroup", myTopology, myTopoNode, 1);
}

/**
 * Destroy group and remove trace file
 */
static void destroyGroup(hdStatsGroup myGroup)
{
	/* save filename */
	char *filename = strdup(myGroup->tracefile);

	/* finalize group (assumed as working) */
	myGroup->isCommitted = 1;
	assert(hdS_finalize(myGroup) == 0);

	/* remove group trace file */
	assert(remove(filename) == 0);

	free(filename);
}

/* ************************************************************************* *
 *                     BEGIN Tests of hdS_createGroup                        *
 * ************************************************************************* */
#define ERROR_CHECK \
	if(myGroup == NULL) \
		switch(errno) { \
		case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT"); \
		case HD_ERR_MALLOC: assert(!"HD_ERR_MALLOC"); \
		case HD_ERR_BUFFER_OVERFLOW: assert(!"HD_ERR_BUFFER_OVERFLOW"); \
		case HD_ERR_CREATE_FILE: assert(!"HD_ERR_CREATE_FILE"); \
		default: assert(myGroup != NULL); \
		}
/**
 * Test_createGroup: Correct usage (leaf node of topology)
 */
static void Test_createGroup1()
{
	hdStatsGroup myGroup;

	/* create topology */
	char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	/* Test correct usage with leaf node of a topology */
	TEST_BEGIN("Correct usage (leaf node of topology)")

	myGroup = hdS_createGroup("MyGroup", myTopology, myTopoNode, 2);
	ERROR_CHECK

	/* check name */
	assert(strcmp(myGroup->name, "MyGroup") == 0);
	/* check filename */
	assert(strcmp(myGroup->tracefile, "MyProject_host0_process0_MyGroup.dat") == 0);
	/* check buffer type */
	assert(myGroup->btype == HDS_HEADER_BUFFER);
	/* check buffer content */
	assert(strcmp(myGroup->buffer + HDS_HEADER_SIZE_LENGTH,
			"\n<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\">\n"
			HD_INDENT_STRING HD_INDENT_STRING "<Label value=\"process0\" />\n"
			HD_INDENT_STRING "</Label>\n"
			"</TopologyNode>\n"
			"<MyGroup timestampDatatype=\"EPOCH\">\n") == 0);
	/* check offset */
	assert(myGroup->offset == HDS_HEADER_SIZE_LENGTH
			+ 16 + 22 + 27 + 9 + 16 + 36 + 4 * (int) strlen(HD_INDENT_STRING));

	TEST_PASSED

	destroyGroup(myGroup);
}

/**
 * Test_createGroup: Correct usage (inner node of topology)
 */
static void Test_createGroup2()
{
	hdStatsGroup myGroup;

	/* create topology */
	char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (inner node of topology)")

	myGroup = hdS_createGroup("MyGroup", myTopology, myTopoNode, 1);
	ERROR_CHECK

	/* check name */
	assert(strcmp(myGroup->name, "MyGroup") == 0);
	/* check filename */
	assert(strcmp(myGroup->tracefile, "MyProject_host0_MyGroup.dat") == 0);
	/* check buffer type */
	assert(myGroup->btype == HDS_HEADER_BUFFER);
	/* check buffer content */
	assert(strcmp(myGroup->buffer + HDS_HEADER_SIZE_LENGTH,
			"\n<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\" />\n"
			"</TopologyNode>\n"
			"<MyGroup timestampDatatype=\"EPOCH\">\n") == 0);
	/* check offset */
	assert(myGroup->offset == HDS_HEADER_SIZE_LENGTH
			+ 16 + 24 + 16 + 36 + (int) strlen(HD_INDENT_STRING));
	/* check enable state */
	assert(myGroup->isEnabled == 0);
	/* check commit state */
	assert(myGroup->isCommitted == 0);

	TEST_PASSED

	destroyGroup(myGroup);
}
#undef ERROR_CHECK
/* ************************************************************************* *
 *                      END Tests of hdS_createGroup                         *
 * ************************************************************************* */

/* ************************************************************************* *
 *                      BEGIN Tests of hdS_addValue                          *
 * ************************************************************************* */
#define ERROR_CHECK \
	if (ret < 0) \
		switch (errno) { \
		case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT"); \
		case HD_ERR_BUFFER_OVERFLOW: assert(!"HD_ERR_BUFFER_OVERFLOW"); \
		case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE"); \
		default: assert(myGroup != NULL); \
		}
/**
 * Test hdS_addValue: Correct usage
 */
void Test_addValue1()
{

	hdStatsGroup myGroup = getGroup();

	int offset = myGroup->offset;

	int ret;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage")

	ret = hdS_addValue(myGroup, "Int32Value", INT32, "unit0");
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "Int64Value", INT64, "unit1");
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "FloatValue", FLOAT, "unit2");
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "DoubleValue", DOUBLE, "unit3");
	ERROR_CHECK

	/* check value types array */
	assert(myGroup->valueTypes[0] == INT32);
	assert(myGroup->valueTypes[1] == INT64);
	assert(myGroup->valueTypes[2] == FLOAT);
	assert(myGroup->valueTypes[3] == DOUBLE);
	assert(myGroup->nextValueIdx == 4);
	/* check buffer content */
	assert(strcmp(myGroup->buffer + offset,
			HD_INDENT_STRING "<Int32Value type=\"INT32\" unit=\"unit0\" />\n"
			HD_INDENT_STRING "<Int64Value type=\"INT64\" unit=\"unit1\" />\n"
			HD_INDENT_STRING "<FloatValue type=\"FLOAT\" unit=\"unit2\" />\n"
			HD_INDENT_STRING
				"<DoubleValue type=\"DOUBLE\" unit=\"unit3\" />\n") == 0);
	/* check offset */
	assert(myGroup->offset
			== offset + 41 + 41 + 41 + 43
			+ 4 * (int) strlen(HD_INDENT_STRING));
	/* check enable state */
	assert(myGroup->isEnabled == 0);
	/* check commit state */
	assert(myGroup->isCommitted == 0);

	TEST_PASSED

	destroyGroup(myGroup);
}
#undef ERROR_CHECK
/* ************************************************************************* *
 *                       END Tests of hdS_addValue                           *
 * ************************************************************************* */

/* ************************************************************************* *
 *                     BEGIN Tests of hdS_commitGroup                        *
 * ************************************************************************* */
/**
 * Test hdS_commitGroup: Correct usage
 */
void Test_commitGroup1()
{

	hdStatsGroup myGroup = getGroup();

	int ret;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage")

	ret = hdS_commitGroup (myGroup);
	if (ret < 0)
		switch (errno)
		{
		case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT");
		case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE");
		case HD_ERR_UNKNOWN: assert(!"HD_ERR_UNKNOWN");
		default: assert(myGroup != NULL); \
		}

	/* check buffer type */
	assert(myGroup->btype == HDS_ENTRY_BUFFER);
	/* check offset */
	assert(myGroup->offset ==  0);
	/* check enable state */
	assert(myGroup->isEnabled == 0);
	/* check commit state */
	assert(myGroup->isCommitted == 1);

	/* save filename */
	char *filename = strdup(myGroup->tracefile);

	/* finalize group (assumed as working) */
	assert(hdS_finalize(myGroup) == 0);

	/* read data from file */
	char buffer[HDS_HEADER_BUF_SIZE];
	char reference[HDS_HEADER_BUF_SIZE];
	FILE *file = fopen(filename, "r");
	assert(file != NULL);
	size_t boff = 0;
	while(fgets(buffer + boff, HDS_HEADER_BUF_SIZE - boff, file))
	{
		boff = strlen(buffer);
	}
	fclose(file);

	/* create reference header */
	snprintf(reference, HDS_HEADER_BUF_SIZE,
			"%05d\n"
			"<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\" />\n"
			"</TopologyNode>\n"
			"<MyGroup timestampDatatype=\"EPOCH\">\n"
			"</MyGroup>\n",
			15 + 24 + 16 + 36 + 11 + strlen(HD_INDENT_STRING));
	/* check data read from file */
	assert(strncmp(buffer, reference, HDS_HEADER_BUF_SIZE) == 0);

	TEST_PASSED

	/* remove group trace file */
	assert(remove(filename) == 0);

	free(filename);
}

/* ************************************************************************* *
 *                      END Tests of hdS_commitGroup                         *
 * ************************************************************************* */

/**
 * Return standard testing group (committed)
 */
static hdStatsGroup getCommitedGroup(void)
{
	hdStatsGroup myGroup = getGroup();

	hdS_addValue(myGroup, "Int32Value", INT32, "unit0");
	hdS_addValue(myGroup, "Int64Value", INT64, "unit1");
	hdS_addValue(myGroup, "FloatValue", FLOAT, "unit2");
	hdS_addValue(myGroup, "DoubleValue", DOUBLE, "unit3");

	hdS_commitGroup (myGroup);

	return myGroup;
}

/* ************************************************************************* *
 *                     BEGIN Tests of hdS_enableGroup                        *
 * ************************************************************************* */
/**
 * Test hdS_enableGroup: Correct usage (disabled before)
 */
void Test_enableGroup1()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 0;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (disabled before)")

	int ret = hdS_enableGroup(myGroup);

	/* check return value */
	assert(ret == 0);

	/* check enable state */
	assert(myGroup->isEnabled == 1);

	TEST_PASSED

	destroyGroup(myGroup);
}

/**
 * Test hdS_enableGroup: Correct usage (enabled before)
 */
void Test_enableGroup2()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 1;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (enabled before)")

	int ret = hdS_enableGroup(myGroup);

	/* check return value */
	assert(ret == 1);

	/* check enable state */
	assert(myGroup->isEnabled == 1);

	TEST_PASSED

	destroyGroup(myGroup);
}
/* ************************************************************************* *
 *                      END  Tests of hdS_enableGroup                        *
 * ************************************************************************* */

/* ************************************************************************* *
 *                    BEGIN Tests of hdS_disableGroup                        *
 * ************************************************************************* */
/**
 * Test hdS_disableGroup: Correct usage (enabled before)
 */
void Test_disableGroup1()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 1;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (enabled before)")

	int ret = hdS_disableGroup(myGroup);

	/* check return value */
	assert(ret == 0);

	/* check enable state */
	assert(myGroup->isEnabled == 0);

	TEST_PASSED

	destroyGroup(myGroup);
}

/**
 * Test hdS_disableGroup: Correct usage (disabled before)
 */
void Test_disableGroup2()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 0;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (disabled before)")

	int ret = hdS_disableGroup(myGroup);

	/* check return value */
	assert(ret == 1);

	/* check enable state */
	assert(myGroup->isEnabled == 0);

	TEST_PASSED

	destroyGroup(myGroup);
}
/* ************************************************************************* *
 *                     END  Tests of hdS_disableGroup                        *
 * ************************************************************************* */

/* ************************************************************************* *
 *                      BEGIN Tests of hdS_isEnabled                         *
 * ************************************************************************* */
/**
 * Test hdS_isEnabled: Correct usage (enabled)
 */
void Test_isEnabled1()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 1;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (enabled)")

	int ret = hdS_isEnabled(myGroup);

	/* check return value */
	assert(ret == 1);

	/* check enable state */
	assert(myGroup->isEnabled == 1);

	TEST_PASSED

	destroyGroup(myGroup);
}

/**
 * Test hdS_isEnabled: Correct usage (disabled)
 */
void Test_isEnabled2()
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group */
	myGroup->isEnabled = 0;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (enabled)")

	int ret = hdS_isEnabled(myGroup);

	/* check return value */
	assert(ret == 0);

	/* check enable state */
	assert(myGroup->isEnabled == 0);

	TEST_PASSED

	destroyGroup(myGroup);
}

/* ************************************************************************* *
 *                     END  Tests of hdS_disableGroup                        *
 * ************************************************************************* */

int main(int argc, char **argv)
{
	/* run all tests */
	Test_createGroup1();
	Test_createGroup2();
	Test_addValue1();
	Test_commitGroup1();
	Test_enableGroup1();
	Test_enableGroup2();
	Test_disableGroup1();
	Test_disableGroup2();
	Test_isEnabled1();
	Test_isEnabled2();
}

#if 0

int hdS_writeEntry (
        hdStatsGroup group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        int entryLength          /* Length of the entry to write */
        );

int hdS_writeInt32Value (
        hdStatsGroup group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        );

int hdS_writeInt64Value (
        hdStatsGroup group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        );

int hdS_writeFloatValue (
        hdStatsGroup group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        );

int hdS_writeDoubleValue (
        hdStatsGroup group,      /* Statistics Group */
        double value             /* DOUBLE value to write */
        );

int hdS_writeString (
        hdStatsGroup group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        );

int hdS_finalize(
        hdStatsGroup group      /* Statistics Group */
        );
#endif
