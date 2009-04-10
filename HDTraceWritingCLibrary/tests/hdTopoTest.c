/**
 * @file hdTopoTest.c
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <errno.h>

#include "hdTopo.h"
#include "hdError.h"

#include "tests.h"

/**
 * Test hdT_createTopology.
 */
static void Test_createTopology_C1(void)
{
	hdTopology myTopology;

	/* Test correct usage (with string literals) */
	TEST_BEGIN("Correct usage")

	const char *levels1[] = {"Host","Process"};

	myTopology = hdT_createTopology("MyProject", levels1, 2);

	/* levels must have the number of levels without root */
	assert(myTopology->nlevels == 2);
	/* project must have the project name */
	assert(strcmp(myTopology->project,"MyProject") == 0);
	/* levels must be an array of the labels */
	assert(strcmp(myTopology->levels[0], "Host") == 0);
	assert(strcmp(myTopology->levels[1], "Process") == 0);

	TEST_PASSED

	/* destroy topology (assumed as working */
	hdT_destroyTopology(myTopology);
}

/**
 * Test hdT_createTopology.
 */
static void Test_createTopology_T1(void)
{
	hdTopology myTopology;

	/* Test tolerated usage with dynamically allocates strings */
	TEST_BEGIN("Tolerated usage (dynamic strings)")


	char **levels = malloc(2 * sizeof(**levels));
	levels[0] = strdup("Host");
	levels[1] = strdup("Process");

	char *project = strdup("MyProject");

	/* create topology */
	myTopology = hdT_createTopology((const char *) project,
			(const char **) levels, 2);


	/* destroy all strings */
	memcpy(project, "x", 2);
	memcpy(levels[0], "x", 2);
	memcpy(levels[1], "x", 2);

	free(project);
	free(levels[0]);
	free(levels[1]);
	free(levels);


	/* levels must have the number of levels without root */
	assert(myTopology->nlevels == 2);
	/* project must have the project name */
	assert(strcmp(myTopology->project,"MyProject") == 0);
	/* levels must be an array of the labels */
	assert(strcmp(myTopology->levels[0], "Host") == 0);
	assert(strcmp(myTopology->levels[1], "Process") == 0);

	TEST_PASSED
}

/**
 * Test hdT_getTopoDepth
 */
static void Test_getTopoDepth_C1(void)
{
	/* create topology */
	const char *names[] = {"Host","Process"};
	hdTopology myTopology =	hdT_createTopology("MyProject", names, 2);

	/* Test correct usage */
	TEST_BEGIN("Correct usage")

	int myDepth = hdT_getTopoDepth(myTopology);

	assert(myDepth == 3);

	TEST_PASSED

}

/**
 * Test hdT_destroyTopology
 */
static void Test_destroyTopology_E1(void)
{
	/* cannot test corret call here */

	/* Test call with NULL argument */
	TEST_BEGIN("Error handling (NULL argument)")

	int ret = hdT_destroyTopology(NULL);

	assert(ret == -1);
	assert(errno == HD_ERR_INVALID_ARGUMENT);

	TEST_PASSED
}


/**
 * Test hdT_createTopoNode
 */
static void Test_createTopoNode_C1(void)
{
	hdTopoNode myTopoNode;

	/* Test correct usage with string literals */
	TEST_BEGIN("Correct usage")

	const char *path1[] = {"host0","process0"};

	myTopoNode = hdT_createTopoNode(path1, 2);

	/* length must have the number of path elements */
	assert(myTopoNode->length == 2);
	/* path must be an array of the path elements */
	assert(strcmp(myTopoNode->path[0], "host0") == 0);
	assert(strcmp(myTopoNode->path[1], "process0") == 0);
	/* sting must have the correct string representation of the path */
	assert(strcmp(myTopoNode->string, "host0.process0") == 0);

	TEST_PASSED

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode);
}

/**
 * Test hdT_createTopoNode
 */
static void Test_createTopoNode_T1(void)
{
	hdTopoNode myTopoNode;

	/* Test tolerated usage with dynamically allocates strings */
	TEST_BEGIN("Tolerated usage (dynamic strings)")

	char **path = malloc(2 * sizeof(**path));
	path[0] = strdup("host0");
	path[1] = strdup("process0");

	/* create topology */
	myTopoNode = hdT_createTopoNode((const char **)path, 2);

	/* destroy all strings */
	memcpy(path[0],"x", 2);
	memcpy(path[1],"x", 2);
	free(path[0]);
	free(path[1]);
	free(path);

	/* length must have the number of path elements */
	assert(myTopoNode->length == 2);
	/* path must be an array of the path elements */
	assert(strcmp(myTopoNode->path[0], "host0") == 0);
	assert(strcmp(myTopoNode->path[1], "process0") == 0);
	/* sting must have the correct string representation of the path */
	assert(strcmp(myTopoNode->string, "host0.process0") == 0);

	TEST_PASSED
}

/**
 * Test hdT_getTopoNodeLevel
 */
static void Test_getTopoNodeLevel_C1(void)
{
	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode =	hdT_createTopoNode(path, 2);

	/* Test correct usage */
	TEST_BEGIN("Correct usage")

	int myLevel = hdT_getTopoNodeLevel(myTopoNode);

	assert(myLevel == 2);

	TEST_PASSED

}

/**
 * Test hdT_getTopoPathString
 */
static void Test_getTopoPathString_C1(void)
{
	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(path, 2);

	/* Test correct usage */
	TEST_BEGIN("Correct usage")

	const char *myString = hdT_getTopoPathString(myTopoNode);

	assert(strcmp(myString, "host0.process0") == 0);

	TEST_PASSED

}

/**
 * Test hdT_getTopoPathLabel
 */
static void Test_getTopoPathLabel_C1(void)
{
	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode =	hdT_createTopoNode(path, 2);

	/* Test correct usage */
	TEST_BEGIN("Correct usage")

	const char * myLabel1 = hdT_getTopoPathLabel(myTopoNode, 1);

	assert(strcmp(myLabel1, "host0") == 0);

	const char * myLabel2 = hdT_getTopoPathLabel(myTopoNode, 2);

	assert(strcmp(myLabel2, "process0") == 0);

	TEST_PASSED

}

/**
 * Test hdT_destroyTopoNode
 */
static void Test_destroyTopoNode_E1(void)
{
	/* cannot test correct call here */

	/* Test call with NULL argument */
	TEST_BEGIN("Error handling (NULL argument)")

	int ret = hdT_destroyTopoNode(NULL);

	assert(ret == -1);
	assert(errno == HD_ERR_INVALID_ARGUMENT);

	TEST_PASSED
}


int main(void)
{
	/* run all tests */
	Test_createTopology_C1();
	Test_createTopology_T1();
	Test_getTopoDepth_C1();
	Test_destroyTopology_E1();

	Test_createTopoNode_C1();
	Test_createTopoNode_T1();
	Test_getTopoNodeLevel_C1();
	Test_getTopoPathString_C1();
	Test_getTopoPathLabel_C1();
	Test_destroyTopoNode_E1();

	puts("hdTopoTest: All tests passed!");
}
