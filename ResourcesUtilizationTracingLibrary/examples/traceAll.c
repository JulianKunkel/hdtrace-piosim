/**
 * @file traceAll.c
 *
 * Example program showing how to use libRUT.
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
#include "RUT.h"

int main(void)
{
	int ret;

	/* create topology */

	const char *levels[] = {"Host","Process"};
	hdTopology *myTopology = hdT_createTopology("MyProject", levels, 2);


	/* create topology node */

	const char *path[] = {"host0","process0"};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 2);


	/* create sources */

	rutSources mySources;
	RUTSRC_UNSET_ALL(mySources);


	/* set all available sources */

	RUTSRC_SET_ALL(mySources);


	/* create UtilTrace object */

	UtilTrace *myTrace;
	ret = rut_createTrace(myTopoNode, 1, mySources, 700, &myTrace);

	if (ret != RUT_SUCCESS)
	{
		fputs("Failed to create PerfTrace object.", stderr);
		return -1;
	}


	/* start tracing */

	rut_startTracing(myTrace);


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
		fac/sum;
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
		fac/sum;
	}


	/* free the memory */

	free(myMemory);

	/* last sleep a little while */

	sleep(3);


	/* stop tracing */

	rut_stopTracing(myTrace);


	/* destroy UtilTrace object */

	rut_finalizeTrace(myTrace);

	hdT_destroyTopoNode(myTopoNode);
	hdT_destroyTopology(myTopology);

	return 0;
}
