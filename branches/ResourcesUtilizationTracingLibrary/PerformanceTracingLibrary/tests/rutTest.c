/**
 * @file rutTest.c
 *
 * @date 30.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#undef NDEBUG /* all tests depend on working assert */
#include <assert.h>

#include "hdTopo.h"
#include "RUT.h"

#include "tests.h"


#if 0
/**
 * Copies from RUT.c for direct access while testing
 */
struct UtilTrace_s
{
	GThread *tracingThread;
	tracingControlStruct *tracingControl;
};
#endif


/**
 * Return standard testing topology
 */
static hdTopology * getTopology(void)
{
	/* create topology */
	const char *levels[] = {"Host","Process"};
	hdTopology *myTopology = hdT_createTopology("MyProject", levels, 2);
	return myTopology;
}

/**
 * Return standard testing topology node
 */
static hdTopoNode * getTopoNode(hdTopology *myTopology)
{
	/* create topology node */
	const char *path[] = {"host0","process0"};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 2);
	return myTopoNode;
}


/* ************************************************************************* *
 *                                 BEGIN Tests                               *
 * ************************************************************************* */
/**
 * Test rut_createTrace: Correct usage
 */
static void Test_C1(void)
{
	/* create topology and topology node */
	hdTopology *myTopology = getTopology();
	hdTopoNode *myTopoNode = getTopoNode(myTopology);

	/* create sources */
	rutSources mySources;
	RUTSRC_UNSET_ALL(mySources);

	/* set some sources */
	mySources.CPU_LOAD = 1;
	mySources.MEM_USED = 1;
	mySources.MEM_FREE = 1;
	mySources.MEM_BUFFER = 1;
	mySources.HDD_READ = 1;
	mySources.HDD_WRITE = 1;

	TEST_BEGIN("ptl_createTrace: Correct usage");

	UtilTrace *myTrace;
	myTrace = rut_createTrace(myTopoNode, 1, mySources, 700);

	assert(myTrace != NULL);

	sleep(2);

	rut_startTrace(myTrace);

	sleep(5);

	rut_stopTrace(myTrace);

	sleep(3);

	rut_startTrace(myTrace);

	sleep(2);

	TEST_PASSED

	rut_finalizeTrace(myTrace);

	hdT_destroyTopoNode(myTopoNode);
	hdT_destroyTopology(myTopology);

	remove("MyProject_host0_Utilization.stat");

}


int main(void)
{
	/* setup glib memory debugging */
	setenv("G_SLICE", "debug-blocks", 1);

	/* setup hdd mountpoint */
	setenv("RUT_HDD_MOUNTPOINT", "/", 1);

	/* run all tests */
	Test_C1();

	puts("ptlTest: All tests passed!");
}
