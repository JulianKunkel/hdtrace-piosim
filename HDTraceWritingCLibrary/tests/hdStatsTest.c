/**
 * @file hdStatsTest.c
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>
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
	HDS_HEADER_BUFFER,
	HDS_ENTRY_BUFFER
};
struct _hdStatsGroup {
	char *name;
	int fd;
    char *tracefile;
    char *buffer;
    enum _hdStatsBufferType btype;
    int offset;
    size_t entryLength;
    hdStatsValueType *valueTypes;
    int nextValueIdx;
    unsigned int hasString : 1;
    unsigned int isCommitted : 1;
    unsigned int isEnabled : 1;
};

/**
 * Return standard testing group (just created)
 */
static hdStatsGroup getGroup(void)
{
	/* create topology */
	const char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	const char *path[] = {"host0","process0"};
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
static void Test_createGroup_C1(void)
{
	hdStatsGroup myGroup;

	/* create topology */
	const char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	/* Test correct usage with leaf node of a topology */
	TEST_BEGIN("Correct usage (leaf node of topology)")

	myGroup = hdS_createGroup("MyGroup", myTopology, myTopoNode, 2);
	ERROR_CHECK

	/* check name */
	assert(strcmp(myGroup->name, "MyGroup") == 0);
	/* check filename */
	assert(strcmp(myGroup->tracefile, "MyProject_host0_process0_MyGroup.stat") == 0);
	/* check buffer type */
	assert(myGroup->btype == HDS_HEADER_BUFFER);
	/* check buffer content */
	int offset;
	const char *refstring;
	int strcmp_result;
	offset = HDS_HEADER_SIZE_LENGTH;
	refstring = "\n"
			"<Statistics>\n"
			"<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\">\n"
			HD_INDENT_STRING HD_INDENT_STRING "<Label value=\"process0\" />\n"
			HD_INDENT_STRING "</Label>\n"
			"</TopologyNode>\n"
			"<Group name=\"MyGroup\" timestampDatatype=\"EPOCH\" timeAdjustment=\"";
	strcmp_result = strcmp(myGroup->buffer + offset, refstring);
	assert(strcmp_result > 0);
	offset += (int) strlen(refstring);

	/* jump over timeAdjustment value (%010d.%09d) */
	offset += 20;

	refstring = "\">\n";
	strcmp_result = strcmp(myGroup->buffer + offset, refstring);
	assert(strcmp_result == 0);
	offset += (int) strlen(refstring);

	/* check offset */
	assert(myGroup->offset == offset);

	TEST_PASSED

	destroyGroup(myGroup);
}

/**
 * Test_createGroup: Correct usage (inner node of topology)
 */
static void Test_createGroup_C2(void)
{
	hdStatsGroup myGroup;

	/* create topology */
	const char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);

	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage (inner node of topology)")

	myGroup = hdS_createGroup("MyGroup", myTopology, myTopoNode, 1);
	ERROR_CHECK

	/* check name */
	assert(strcmp(myGroup->name, "MyGroup") == 0);
	/* check filename */
	assert(strcmp(myGroup->tracefile, "MyProject_host0_MyGroup.stat") == 0);
	/* check buffer type */
	assert(myGroup->btype == HDS_HEADER_BUFFER);
	/* check buffer content */
	int offset;
	int strcmp_result;
	const char *refstring;
	offset = HDS_HEADER_SIZE_LENGTH;
	refstring = "\n"
			"<Statistics>\n"
			"<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\" />\n"
			"</TopologyNode>\n"
			"<Group name=\"MyGroup\" timestampDatatype=\"EPOCH\" timeAdjustment=\"";
	strcmp_result = strcmp(myGroup->buffer + offset, refstring);
	assert(strcmp_result > 0);
	offset += (int) strlen(refstring);

	/* jump over timeAdjustment value (%010d.%09d) */
	offset += 20;

	refstring = "\">\n";
	strcmp_result = strcmp(myGroup->buffer + offset, refstring);
	assert(strcmp_result == 0);
	offset += (int) strlen(refstring);

	/* check offset */
	assert(myGroup->offset == offset);

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
		default: assert(ret == 0); \
		}
/**
 * Test hdS_addValue: Correct usage
 */
static void Test_addValue_C1(void)
{

	hdStatsGroup myGroup = getGroup();

	int offset = myGroup->offset;

	int ret;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage")

	ret = hdS_addValue(myGroup, "Int32Value", INT32, "unit0", NULL);
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "Int64Value", INT64, "unit1", NULL);
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "FloatValue", FLOAT, "unit2", NULL);
	ERROR_CHECK
	ret = hdS_addValue(myGroup, "DoubleValue", DOUBLE, "unit3", NULL);
	ERROR_CHECK

	/* check value types array */
	assert(myGroup->valueTypes[0] == INT32);
	assert(myGroup->valueTypes[1] == INT64);
	assert(myGroup->valueTypes[2] == FLOAT);
	assert(myGroup->valueTypes[3] == DOUBLE);
	assert(myGroup->nextValueIdx == 4);
	/* check buffer content */
	int strcmp_result;
	const char *refstring;
	refstring =
		HD_INDENT_STRING "<Value name=\"Int32Value\" type=\"INT32\" unit=\"unit0\" />\n"
		HD_INDENT_STRING "<Value name=\"Int64Value\" type=\"INT64\" unit=\"unit1\" />\n"
		HD_INDENT_STRING "<Value name=\"FloatValue\" type=\"FLOAT\" unit=\"unit2\" />\n"
		HD_INDENT_STRING "<Value name=\"DoubleValue\" type=\"DOUBLE\" unit=\"unit3\" />\n";
	strcmp_result = strcmp(myGroup->buffer + offset, refstring);
	assert(strcmp_result == 0);
	/* check offset */
	assert(myGroup->offset == offset + (int) strlen(refstring));
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
static void Test_commitGroup_C1(void)
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
		default: assert(ret == 0); \
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
	FILE *file;
	assert((file = fopen(filename, "r")) != NULL);
	int boff = 0;
	while(fgets(buffer + boff, HDS_HEADER_BUF_SIZE - boff, file))
	{
		boff = (int) strlen(buffer);
	}
	fclose(file);

	/* create reference header */
	snprintf(reference, HDS_HEADER_BUF_SIZE,
			"%05d\n"
			"<Statistics>\n"
			"<TopologyNode>\n"
			HD_INDENT_STRING "<Label value=\"host0\" />\n"
			"</TopologyNode>\n"
			"<Group name=\"MyGroup\" timestampDatatype=\"EPOCH\""
					" timeAdjustment=\"[0-9]{10}\\.[0-9]{9}\">\n"
			"</Group>\n"
			"</Statistics>\n",
			13 + 15 + strlen(HD_INDENT_STRING) + 24 + 16 + 87 + 9 + 14);

	/* create reference header regexp */
	regex_t refregexp;
	ret = regcomp(&refregexp, reference, REG_EXTENDED | REG_NOSUB);
	assert(ret == 0);

	/* check data read from file */
	ret = regexec(&refregexp, buffer, 0, NULL, 0);
	assert(ret == 0);

	regfree(&refregexp);

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

	hdS_addValue(myGroup, "Int32Value", INT32, "unit0", NULL);
	hdS_addValue(myGroup, "Int64Value", INT64, "unit1", NULL);
	hdS_addValue(myGroup, "FloatValue", FLOAT, "unit2", NULL);
	hdS_addValue(myGroup, "DoubleValue", DOUBLE, "unit3", NULL);

	hdS_commitGroup (myGroup);

	return myGroup;
}

/* ************************************************************************* *
 *                     BEGIN Tests of hdS_enableGroup                        *
 * ************************************************************************* */
/**
 * Test hdS_enableGroup: Correct usage (disabled before)
 */
static void Test_enableGroup_C1(void)
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
static void Test_enableGroup_C2(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* enable group */
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
static void Test_disableGroup_C1(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* enable group */
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
static void Test_disableGroup_C2(void)
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
static void Test_isEnabled_C1(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* enable group */
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
static void Test_isEnabled_C2(void)
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

/* ************************************************************************* *
 *                      BEGIN Tests of hdS_writeEntry                        *
 * ************************************************************************* */
/**
 * Test hdS_writeEntry: Correct usage
 */
static void Test_writeEntry_C1(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* enable group */
	hdS_enableGroup(myGroup);

	/* prepare entry (see committed standard test group creation) */
	char *entry = malloc(64 * sizeof(*entry));
	size_t length = 0;

	                 // mem offset: 0x00 0x01 0x02 0x04 0x05 0x06 0x07 0x08
	int32_t value0 = 12345;      // 0x39 0x30 0x00 0x00
	int64_t value1 = 67890l;     // 0x32 0x09 0x01 0x00 0x00 0x00 0x00 0x00
	float value2 = 12345.67890f; // 0xb7 0xe6 0x40 0x46
	double value3 = 67890.12345; // 0x0b 0xb5 0xa6 0xf9 0x21 0x93 0xf0 0x40

	               // mem offset: 0x00 0x01 0x02 0x04 0x05 0x06 0x07 0x08
	order_bytes32ip(&value0);  // 0x00 0x00 0x30 0x39
	order_bytes64ip(&value1);  // 0x00 0x00 0x00 0x00 0x00 0x01 0x09 0x32
	order_bytes32fp(&value2);  // 0x46 0x40 0xe6 0xb7
	order_bytes64fp(&value3);  // 0x40 0xf0 0x93 0x21 0xf9 0xa6 0xb5 0x0b

	*((int32_t *) (entry + length)) = value0;
	length += sizeof(value0);
	*((int64_t *) (entry + length)) = value1;
	length += sizeof(value1);
	*((float *) (entry + length)) = value2;
	length += sizeof(value2);
	*((double *) (entry + length)) = value3;
	length += sizeof(value3);

	assert(length == 24);

	unsigned char ref[24] = {
			0x00, 0x00, 0x30, 0x39,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x09, 0x32,
			0x46, 0x40, 0xE6, 0xB7,
			0x40, 0xf0, 0x93, 0x21, 0xf9, 0xa6, 0xb5, 0x0b
			};

	/* check entry */
	assert(memcmp(entry, ref, 24) == 0);

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Correct usage")

	int ret = hdS_writeEntry (myGroup, entry, length);
	if (ret < 0)
		switch (errno)
		{
		case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT");
		case HD_ERR_TRACE_DISABLED: assert(!"HD_ERR_TRACE_DISABLED");
		case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE");
		case HDS_ERR_UNEXPECTED_ARGVALUE: assert(!"HDS_ERR_UNEXPECTED_ARGVALUE");
		case HDS_ERR_ENTRY_STATE: assert(!"HDS_ERR_ENTRY_STATE");
		default: assert(ret == 0);
		}

	/* check if entry is complete */
	assert(myGroup->offset == 0);
	assert(myGroup->nextValueIdx == 0);

	/* persistence checks */
	assert(myGroup->isEnabled == 1);
	assert(myGroup->isCommitted == 1);

	/* save filename */
	char *filename = strdup(myGroup->tracefile);

	/* finalize group */
	hdS_finalize(myGroup);

	/*
	 * read data back from file
	 */
	/* open file */
	FILE *file;
	assert((file = fopen(filename, "r")) != NULL);
	/* read header length */
	int header_length;
	assert(fscanf(file, "%05d\n", &header_length) == 1);
	/* seek behind header */
	assert(fseek(file, header_length, SEEK_CUR) == 0);
	/* read the entry back */
	char buffer[100];
	assert(fread(buffer, HDS_TIMESTAMP_LENGTH + length, 1, file) == 1);
	/* check entry */
	assert(memcmp(buffer + HDS_TIMESTAMP_LENGTH, entry, length) == 0);

	TEST_PASSED

	/* close file */
	fclose(file);

	/* remove group trace file */
	assert(remove(filename) == 0);

	free(entry);
	free(filename);
}

/**
 * Test hdS_writeEntry: Wrong usage (Group disabled)
 */
static void Test_writeEntry_W1(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* disable group (should already be) */
	hdS_disableGroup(myGroup);

	/* prepare entry (see committed standard test group creation) */
	char *entry = malloc(64 * sizeof(*entry));
	size_t length = 24;

	/* Test correct usage with inner node of a topology */
	TEST_BEGIN("Wrong usage (Group disabled)")

	int ret = hdS_writeEntry (myGroup, entry, length);

	/* check result */
	assert(ret < 0 && errno == HD_ERR_TRACE_DISABLED);

	assert(myGroup->isEnabled == 0);

	TEST_PASSED

	destroyGroup(myGroup);
	free(entry);
}


/* ************************************************************************* *
 *                       END Tests of hdS_writeEntry                         *
 * ************************************************************************* */

/* ************************************************************************* *
 *                      BEGIN Tests of hdS_write*Value                       *
 * ************************************************************************* */
/**
 * Test hdS_write*Value: Correct usage
 */
static void Test_writeXValue_C1(void)
{
	hdStatsGroup myGroup = getCommitedGroup();

	/* enable group */
	hdS_enableGroup(myGroup);

	                 // mem offset: 0x00 0x01 0x02 0x04 0x05 0x06 0x07 0x08
	int32_t value0 = 12345;      // 0x39 0x30 0x00 0x00
	int64_t value1 = 67890l;     // 0x32 0x09 0x01 0x00 0x00 0x00 0x00 0x00
	float value2 = 12345.67890f; // 0xb7 0xe6 0x40 0x46
	double value3 = 67890.12345; // 0x0b 0xb5 0xa6 0xf9 0x21 0x93 0xf0 0x40

	int ret;
#define ERROR_CHECK \
	if (ret < 0) \
		switch (errno)	{ \
		case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT"); \
		case HD_ERR_TRACE_DISABLED: assert(!"HD_ERR_TRACE_DISABLED"); \
		case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE"); \
		case HDS_ERR_ENTRY_STATE: assert(!"HDS_ERR_ENTRY_STATE"); \
		default: assert(ret == 0);	}

	int nextIdx = 0;
	int offset = 0;

#define ASSERTS \
	assert(myGroup->nextValueIdx == nextIdx); \
	assert(myGroup->offset == offset); \
	assert(myGroup->isCommitted == 1); \
	assert(myGroup->isEnabled == 1);

	ASSERTS

	/* Test correct usage */
	TEST_BEGIN("Correct usage")

	ret = hdS_writeInt32Value(myGroup, value0);
	ERROR_CHECK
	nextIdx++;
	offset += HDS_TIMESTAMP_LENGTH + (int) sizeof(value0);
	ASSERTS

	ret = hdS_writeInt64Value(myGroup, value1);
	ERROR_CHECK
	nextIdx++;
	offset += (int) sizeof(value1);
	ASSERTS

	ret = hdS_writeFloatValue(myGroup, value2);
	ERROR_CHECK
	nextIdx++;
	offset += (int) sizeof(value2);
	ASSERTS

	ret = hdS_writeDoubleValue(myGroup, value3);
	ERROR_CHECK
	nextIdx = 0;  // entry should be flushed
	offset = 0;
	ASSERTS

#undef ERROR_CHECK
#undef ASSERTS

	size_t ref_length = 24;


	unsigned char ref_entry[24] = {
			0x00, 0x00, 0x30, 0x39,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x09, 0x32,
			0x46, 0x40, 0xE6, 0xB7,
			0x40, 0xf0, 0x93, 0x21, 0xf9, 0xa6, 0xb5, 0x0b
			};

	/* save filename */
	char *filename = strdup(myGroup->tracefile);

	/* finalize group */
	hdS_finalize(myGroup);

	/*
	 * read data back from file
	 */
	/* open file */
	FILE *file;
	assert((file = fopen(filename, "r")) != NULL);
	/* read header length */
	int header_length;
	assert(fscanf(file, "%05d\n", &header_length) == 1);
	/* seek behind header */
	assert(fseek(file, header_length, SEEK_CUR) == 0);
	/* read the entry back */
	char buffer[100];
	assert(fread(buffer, HDS_TIMESTAMP_LENGTH + ref_length, 1, file) == 1);
	/* check entry */
	assert(memcmp(buffer + HDS_TIMESTAMP_LENGTH, ref_entry, ref_length) == 0);

	TEST_PASSED

	/* close file */
	fclose(file);

	/* remove group trace file */
	assert(remove(filename) == 0);

	free(filename);

}
/* ************************************************************************* *
 *                       END Tests of hdS_write*Value                        *
 * ************************************************************************* */

#if 0

/* TODO: Write test for hdS_writeString */
int hdS_writeString (
        hdStatsGroup group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        );

/* TODO: Evaluate if test hdS_finalize would make sense */
int hdS_finalize(
        hdStatsGroup group      /* Statistics Group */
        );
#endif

int main(void)
{
	/* run all tests */
	Test_createGroup_C1();
	Test_createGroup_C2();
	Test_addValue_C1();
	Test_commitGroup_C1();
	Test_enableGroup_C1();
	Test_enableGroup_C2();
	Test_disableGroup_C1();
	Test_disableGroup_C2();
	Test_isEnabled_C1();
	Test_isEnabled_C2();
	Test_writeEntry_C1();
	Test_writeEntry_W1();
	Test_writeXValue_C1();

	puts("hdStatsTest: All tests passed!");
}
