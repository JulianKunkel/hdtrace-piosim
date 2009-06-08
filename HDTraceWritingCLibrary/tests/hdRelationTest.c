/**
 * @file hdRelationTest.c
 *
 * @date 08.06.2009
 * @author Julian M. Kunkel
 * @version 0.6
 */


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <errno.h>

#include "hdTopo.h"
#include "hdError.h"
#include "hdRelation.h"

#include "tests.h"

/**
 * Test hdT_createTopology.
 */
static void Test_createRelationAndCleanup(void)
{
	hdTopoNode myTopoNode1;
	hdTopoNode myTopoNode2;
	const char *levels1[] = {"Host","Process"};
	hdTopology topology = hdT_createTopology("/tmp/test",levels1, 2 );



	/* Test correct usage (with string literals) */
	TEST_BEGIN("Create and finalize relation topology")

	const char *path1[] = {"host0","process0"};
	myTopoNode1 = hdT_createTopoNode(topology, path1, 2);

	const char *path2[] = {"host0","process1"};
	myTopoNode2 = hdT_createTopoNode(topology, path2, 2);


	hdR_topoToken topoToken1;
	hdR_topoToken topoToken2;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);
	// must fail, because topology already registered:
	assert( hdR_initTopology(myTopoNode1, & topoToken2) == -1);

	assert( hdR_initTopology(myTopoNode2, & topoToken2) == 0);


	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	STATE_BEGIN("Get local token and compare results")

	char* tokenStr = hdR_getLocalToken(token1);
	printf("\t%s\n", tokenStr);
	assert(strcmp(tokenStr, "0:0") == 0);
	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);


	assert( hdR_finalize(myTopoNode1) == 0);
	assert( hdR_finalize(myTopoNode2) == 0);

	//already finalized:
	assert( hdR_finalize(myTopoNode2) == -1);

	TEST_PASSED

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
}


/**
 * Test hdT_createTopology.
 */
static void Test_createRelationHandling(void)
{
	const char *levels1[] = {"Host","Process"};
	hdTopology topology = hdT_createTopology("/tmp/test",levels1, 2 );


	hdTopoNode myTopoNode1;


	/* Test correct usage (with string literals) */
	TEST_BEGIN("Test handling of remote relations")

	const char *path1[] = {"host0","process0"};
	myTopoNode1 = hdT_createTopoNode(topology, path1, 2);


	hdR_topoToken topoToken1;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);

	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	char * tokenStr = hdR_getUniqueToken(token1);
	assert(tokenStr != NULL);
	printf("Unique token id is: %s\n" , tokenStr);

	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);

	assert( hdR_finalize(myTopoNode1) == 0);

	TEST_PASSED

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
}


/**
 * Test hdT_createTopology.
 */
static void Test_remoteRelationHandling(void)
{
	const char *levels1[] = {"Host","Process"};
	hdTopology topology = hdT_createTopology("/tmp/test",levels1, 2 );


	hdTopoNode myTopoNode1;
	hdTopoNode myTopoNode2;

	TEST_BEGIN("Create")

	const char *path1[] = {"host0","process0"};
	myTopoNode1 = hdT_createTopoNode(topology, path1, 2);

	const char *path2[] = {"host0","process1"};
	myTopoNode2 = hdT_createTopoNode(topology, path2, 2);


	hdR_topoToken topoToken1;
	hdR_topoToken topoToken2;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);
	assert( hdR_initTopology(myTopoNode2, & topoToken2) == 0);


	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	STATE_BEGIN("Unique token handling")
	char * tokenStr = hdR_getUniqueToken(token1);
	assert(tokenStr != NULL);

	hdR_token token2 = hdR_relateRemoteToken(topoToken2, tokenStr);
	assert(token2 != NULL);

	STATE_BEGIN("CLEANUP")
	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);
	assert(hdR_destroyRelation(& token2) == 0);


	assert( hdR_finalize(myTopoNode1) == 0);
	assert( hdR_finalize(myTopoNode2) == 0);

	TEST_PASSED

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
}


int main(void)
{
	/* run all tests */
	Test_createRelationAndCleanup();
	Test_createRelationHandling();
	Test_remoteRelationHandling();

	puts("hdRelationTest: All tests passed!");

	return 0;
}
