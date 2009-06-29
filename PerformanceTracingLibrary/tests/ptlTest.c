/**
 * @file ptlTest.c
 *
 * @date 30.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.2
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#undef NDEBUG /* all tests depend on working assert */
#include <assert.h>

#include "hdTopo.h"
#include "PTL.h"

#include "tests.h"


#if 0
/**
 * Copies from PTL.c for direct access while testing
 */
struct PerfTrace_s
{
	GThread *tracingThread;
	tracingControlStruct *tracingControl;
};
#endif


/**
 * Return standard testing topology
 */
static hdTopology getTopology(void)
{
	/* create topology */
	const char *levels[] = {"Host","Process"};
	hdTopology myTopology = hdT_createTopology("MyProject", levels, 2);
	return myTopology;
}

/**
 * Return standard testing topology node
 */
static hdTopoNode getTopoNode(hdTopology myTopology)
{
	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode myTopoNode = hdT_createTopoNode(myTopology, path, 2);
	return myTopoNode;
}


/* ************************************************************************* *
 *                                 BEGIN Tests                               *
 * ************************************************************************* */
/**
 * Test ptl_createTrace: Correct usage
 */
static void Test_C1(void)
{
	/* create topology and topology node */
	hdTopology myTopology = getTopology();
	hdTopoNode myTopoNode = getTopoNode(myTopology);

	/* create sources */
	ptlSources mySources;
	PTLSRC_UNSET_ALL(mySources);

	/* set some sources */
	mySources.PTLSRC_CPU_LOAD = 1;
	mySources.PTLSRC_MEM_USED = 1;
	mySources.PTLSRC_MEM_FREE = 1;
	mySources.PTLSRC_MEM_BUFFER = 1;
	mySources.PTLSRC_HDD_READ = 1;
	mySources.PTLSRC_HDD_WRITE = 1;

	TEST_BEGIN("ptl_createTrace: Correct usage");

	PerfTrace myTrace;
	myTrace = ptl_createTrace(myTopoNode, 1, mySources, 700);

	assert(myTrace != NULL);

	sleep(2);

	ptl_startTrace(myTrace);

	sleep(5);

	ptl_stopTrace(myTrace);

	sleep(3);

	ptl_startTrace(myTrace);

	sleep(2);

	TEST_PASSED

	ptl_destroyTrace(myTrace);

	hdT_destroyTopoNode(myTopoNode);
	hdT_destroyTopology(myTopology);

	remove("MyProject_host0_Performance.stat");

}
int main(void)
{
	/* run all tests */
	Test_C1();

	puts("ptlTest: All tests passed!");
}
