/**
 * @file hdRelationTest.c
 *
 * @date 08.06.2009
 * @author Julian M. Kunkel
 * @version \$Id$
 */


#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <errno.h>

#include "hdTopo.h"
#include "hdError.h"
#include "hdRelation.h"

#include "tests.h"

static void cleanup(void){
	unlink("/tmp/test_host0_process0.rel");
	unlink("/tmp/test_host0_process1.rel");
	unlink("/tmp/test_host1_process0.rel");
	unlink("/tmp/test_host2_process0.rel");
}

/**
 * Test hdT_createTopology.
 */
static void Test_createRelationAndCleanup(void)
{
	hdTopoNode *myTopoNode1;
	hdTopoNode *myTopoNode2;
	const char *levels1[] = {"Host","Process"};
	hdTopology *topology = hdT_createTopology("/tmp/test",levels1, 2 );



	/* Test correct usage (with string literals) */
	TEST_BEGIN("Create and finalize relation topology");

	const char *path1[] = {"host0","process0"};
	myTopoNode1 = hdT_createTopoNode(topology, path1, 2);

	const char *path2[] = {"host0","process1"};
	myTopoNode2 = hdT_createTopoNode(topology, path2, 2);



	hdR_topoToken topoToken1;
	hdR_topoToken topoToken2;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);
	// must fail, because topology already registered:
	//assert( hdR_initTopology(myTopoNode1, & topoToken2) == -1);

	assert( hdR_initTopology(myTopoNode2, & topoToken2) == 0);

	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	STATE_BEGIN("Get local token and compare results");

	char* tokenStr = hdR_getLocalToken(token1);
	printf("\t%s\n", tokenStr);
	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);


	assert( hdR_finalize(& topoToken1) == 0);
	assert( hdR_finalize(& topoToken2) == 0);

	TEST_PASSED;

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
	hdT_destroyTopoNode(myTopoNode2);
}


/**
 * Test hdT_createTopology.
 */
static void Test_createRelationHandling(void)
{
	const char *levels1[] = {"Host","Process"};
	hdTopology *topology = hdT_createTopology("/tmp/test",levels1, 2 );


	hdTopoNode *myTopoNode1;


	/* Test correct usage (with string literals) */
	TEST_BEGIN("Test handling of remote relations");

	const char *path1[] = {"host0","process0"};
	myTopoNode1 = hdT_createTopoNode(topology, path1, 2);


	hdR_topoToken topoToken1;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);

	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	char * tokenStr = hdR_getRemoteToken(token1);
	assert(tokenStr != NULL);
	printf("Unique token id is: %s\n" , tokenStr);

	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);

	assert( hdR_finalize(& topoToken1) == 0);

	TEST_PASSED;

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
}


/**
 * Test hdT_createTopology.
 */
static void Test_remoteRelationHandling(void)
{
	const char *levels1[] = {"Host","Process"};
	hdTopology *topology = hdT_createTopology("/tmp/test",levels1, 2 );

	TEST_BEGIN("Create");

	const char *path1[] = {"host0","process0"};
	hdTopoNode *myTopoNode1 = hdT_createTopoNode(topology, path1, 2);

	const char *path2[] = {"host0","process1"};
	hdTopoNode *myTopoNode2 = hdT_createTopoNode(topology, path2, 2);

	const char *path3[] = {"host1","process0"};
	hdTopoNode *myTopoNode3 = hdT_createTopoNode(topology, path3, 2);

	const char *path4[] = {"host2","process0"};
	hdTopoNode *myTopoNode4 = hdT_createTopoNode(topology, path4, 2);

	hdR_topoToken topoToken1;
	hdR_topoToken topoToken2;
	hdR_topoToken topoToken3;
	hdR_topoToken topoToken4;

	assert( hdR_initTopology(myTopoNode1, & topoToken1) == 0);
	assert( hdR_initTopology(myTopoNode2, & topoToken2) == 0);
	assert( hdR_initTopology(myTopoNode3, & topoToken3) == 0);
	assert( hdR_initTopology(myTopoNode4, & topoToken4) == 0);


	hdR_token token1 = hdR_createTopLevelRelation(topoToken1);
	assert(token1 != NULL);

	STATE_BEGIN("Unique token handling");
	char * tokenStr = hdR_getRemoteToken(token1);
	assert(tokenStr != NULL);

        printf("tokenStr: %s\n", tokenStr);

	hdR_token token2 = hdR_relateRemoteToken(topoToken3, tokenStr);
	assert(token2 != NULL);

	hdR_token token3 = hdR_relateProcessLocalToken(topoToken2, token1);
	assert(token3 != NULL);

	char * localTokenStr = hdR_getLocalToken(token3);

	hdR_token token4 = hdR_relateLocalToken(topoToken1, localTokenStr);
	free(localTokenStr);
	assert(token4 != NULL);

	STATE_BEGIN("Try to create start & end state");

	const char * keys [] = {"schuh", "test"};
	const char * vals [] = {"leder", "#5"};
	hdR_startE(token1, "Create", 2, keys, vals, "<data>stuff</data>");
	hdR_startS(token1, "finalize");
	sleep(1);
	hdR_endS(token1);
	hdR_endE(token1, 2, keys, vals, "<data2>stuff2</data2>");


	STATE_BEGIN("CLEANUP");
	free(tokenStr);

	assert(hdR_destroyRelation(& token1) == 0);
	assert(hdR_destroyRelation(& token2) == 0);
	assert(hdR_destroyRelation(& token3) == 0);
	assert(hdR_destroyRelation(& token4) == 0);


	assert( hdR_finalize(& topoToken1) == 0);
	assert( hdR_finalize(& topoToken2) == 0);
	assert( hdR_finalize(& topoToken3) == 0);
	/* this file shall be empty and shall be deleted */
	assert( hdR_finalize(& topoToken4) == 0);

	TEST_PASSED;

	/* destroy topology (assumed as working */
	hdT_destroyTopoNode(myTopoNode1);
}


int main(void)
{
	/* run all tests */
	cleanup();
	Test_createRelationAndCleanup();
	cleanup();
	Test_createRelationHandling();
	cleanup();
	Test_remoteRelationHandling();

	puts("hdRelationTest: All tests passed!");

	return 0;
}
