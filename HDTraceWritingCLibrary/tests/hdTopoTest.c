/**
 * @file hdTopoTest.c
 *
 * @date 01.0o.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
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
void Test_createTopology()
{
	hdTopology myTopology;

	/* Test correct usage with string literals */
	TEST_BEGIN("Correct usage (using string literals)")

	char *levels1[] = {"Host","Process"};

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

	/* Test correct usage with dynamically allocates strings */
	TEST_BEGIN("Correct usage (dynamic strings)")


	char **levels2 = malloc(2 * sizeof(**levels2));
	levels2[0] = strdup("Host");
	levels2[1] = strdup("Process");

	char *project = strdup("MyProject");

	/* create topology */
	myTopology = hdT_createTopology(project, levels2, 2);

	/* destroy all strings */
	memcpy(project, "x", 2);
	memcpy(levels2[0], "x", 2);
	memcpy(levels2[1], "x", 2);
	free(project);
	free(levels2[0]);
	free(levels2[1]);
	free(levels2);

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
void Test_getTopoDepth()
{
	/* create topology */
	char *names[] = {"Host","Process"};
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
void Test_destroyTopology()
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
void Test_createTopoNode()
{
	hdTopoNode myTopoNode;

	/* Test correct usage with string literals */
	TEST_BEGIN("Correct usage (using string literals)")

	char *path1[] = {"host0","process0"};

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

	/* Test correct usage with dynamically allocates strings */
	TEST_BEGIN("Correct usage (dynamic strings)")


	char **path2 = malloc(2 * sizeof(**path2));
	path2[0] = strdup("host0");
	path2[1] = strdup("process0");

	/* create topology */
	myTopoNode = hdT_createTopoNode(path2, 2);

	/* destroy all strings */
	memcpy(path2[0],"x", 2);
	memcpy(path2[1],"x", 2);
	free(path2[0]);
	free(path2[1]);
	free(path2);

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
void Test_getTopoNodeLevel()
{
	/* create topology node */
	char *path[] = {"host0","process0"};
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
void Test_getTopoPathString()
{
	/* create topology node */
	char *path[] = {"host0","process0"};
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
void Test_getTopoPathLabel()
{
	/* create topology node */
	char *path[] = {"host0","process0"};
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
void Test_destroyTopoNode()
{
	/* cannot test corret call here */

	/* Test call with NULL argument */
	TEST_BEGIN("Error handling (NULL argument)")

	int ret = hdT_destroyTopoNode(NULL);

	assert(ret == -1);
	assert(errno == HD_ERR_INVALID_ARGUMENT);

	TEST_PASSED
}


int main(int argc, char **argv)
{
	/* run all tests */
	Test_createTopology();
	Test_getTopoDepth();
	Test_destroyTopology();
	Test_createTopoNode();
	Test_getTopoNodeLevel();
	Test_getTopoPathString();
	Test_getTopoPathLabel();
	Test_destroyTopoNode();
}
