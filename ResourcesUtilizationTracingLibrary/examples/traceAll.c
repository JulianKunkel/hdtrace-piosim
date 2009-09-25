/**
 * @file traceAll.c
 *
 * Example program showing how to use PTL.
 * The program simply starts tracing for all sources available, does some
 * actions resulting in changes of resource utilization and stops tracing
 * afterwards.
 *
 * @date 30.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "hdTopo.h"
#include "PTL.h"

int main(void)
{

	/* create topology */

	const char *levels[] = {"Host","Process"};
	hdTopology *myTopology = hdT_createTopology("MyProject", levels, 2);


	/* create topology node */

	const char *path[] = {"host0","process0"};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 2);


	/* create sources */

	ptlSources mySources;
	PTLSRC_UNSET_ALL(mySources);


	/* set all available sources */

	PTLSRC_SET_ALL(mySources);


	/* create PerfTrace object */

	PerfTrace *myTrace;
	myTrace = ptl_createTrace(myTopoNode, 1, mySources, 700);

	if (myTrace == NULL)
	{
		fputs("Failed to create PerfTrace object.", stderr);
		return -1;
	}


	/* start tracing */

	ptl_startTrace(myTrace);


	/* first sleep a little while */

	sleep(3);


	/* now do something CPU intensive work */

	int sum = 0;
	for(int i = 0; i < 100000; ++i)
	{
		int fac = 1;
		for (int j = 2; j <= i; ++j)
			fac *= j;
		sum += fac;
	}


	/* now allocate a large piece of memory */

	char *myMemory = malloc(200*1024*1024*sizeof(char));

	for (int i = 0; i < 200*1024*1024; ++i)
	{
		myMemory[i] = 'x';
	}


	/* again produce high CPU load */

	for(int i = 0; i < 100000; ++i)
	{
		int fac = 1;
		for (int j = 2; j <= i; ++j)
			fac *= j;
		sum += fac;
	}


	/* free the memory */

	free(myMemory);

	/* last sleep a little while */

	sleep(3);


	/* stop tracing */

	ptl_stopTrace(myTrace);


	/* destroy PerfTrace object */

	ptl_destroyTrace(myTrace);

	hdT_destroyTopoNode(myTopoNode);
	hdT_destroyTopology(myTopology);

	return 0;
}
