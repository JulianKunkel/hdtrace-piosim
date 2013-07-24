/**
 * @file traceLoop.c
 *
 * Example program showing how to use libRUT.
 * The program simply starts tracing for all sources groups given as command
 *  line arguments. To stop it send SIGINT (Ctrl+C).
 *
 * @date 06.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id: traceAll.c 498 2009-07-06 00:04:51Z krempel $
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>

#include "hdTopo.h"
#include "RUT.h"

static struct {
	hdTopology *topology;
	hdTopoNode *toponode;
	UtilTrace *trace;
} fstuff = { NULL, NULL, NULL };

void sighandler(int sig) {

	if (sig != SIGINT)
		return;

	/* stop tracing */
	rut_stopTracing(fstuff.trace);

	/* destroy UtilTrace object */
	rut_finalizeTrace(fstuff.trace);

	/* destroy all the rest */
	hdT_destroyTopoNode(fstuff.toponode);
	hdT_destroyTopology(fstuff.topology);

	exit(0);
}


int main(int argc, char **argv)
{
	int ret;

	struct sigaction act;
	act.sa_handler = sighandler;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGINT, &act, NULL);
	unlink("MyProject_host0_Utilization.stat");

	/* create topology */

	const char *levels[] = {"Host","Process"};
	hdTopology *myTopology = hdT_createTopology("MyProject", levels, 2);
	fstuff.topology = myTopology;


	/* create topology node */

	const char *path[] = {"host0","process0"};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 2);
	fstuff.toponode = myTopoNode;


	/* create sources */

	rutSources mySources;
	RUTSRC_UNSET_ALL(mySources);

	/* set sources */
	for (int i = 1; i < argc; ++i) {
		if (strcmp(argv[i], "ALL") == 0)
			RUTSRC_SET_ALL(mySources);
		if (strcmp(argv[i], "CPU") == 0)
			RUTSRC_SET_CPU(mySources);
		if (strcmp(argv[i], "MEM") == 0)
			RUTSRC_SET_MEM(mySources);
		if (strcmp(argv[i], "NET") == 0)
			RUTSRC_SET_NET(mySources);
		if (strcmp(argv[i], "HDD") == 0)
			RUTSRC_SET_HDD(mySources);
	}

	if(argc == 1){
		RUTSRC_SET_ALL(mySources);
	}

	/* create UtilTrace object */

	UtilTrace *myTrace;
	ret = rut_createTrace(myTopoNode, 1, mySources, 1000, &myTrace);

	if (ret != RUT_SUCCESS)
	{
		fputs("Failed to create PerfTrace object.", stderr);
		return -1;
	}
	fstuff.trace = myTrace;


	/* start tracing */

	rut_startTracing(myTrace);


	/* loop */

	while (1)
		/* sleep, SIGINT signal is processed nevertheless */
		sleep(600);


	/* never come here */

	return 0;
}
