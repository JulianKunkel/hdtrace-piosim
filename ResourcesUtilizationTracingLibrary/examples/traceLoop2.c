/**
 * @file traceLoop2.c
 *
 * Example program showing how to use libRUT.
 * The program simply starts tracing for all sources groups given as command
 *  line arguments. To stop it send SIGINT (Ctrl+C).
 *
 * @date 17.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <limits.h>

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

	if (argc < 4) {
		puts("You must give at least two arguments (Projectname, Cycle, Sources)");
		return -1;
	}

	struct sigaction act;
	act.sa_handler = sighandler;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGINT, &act, NULL);

	/* create topology */

	const char *levels[] = {"Host"};
	hdTopology *myTopology = hdT_createTopology(argv[1], levels, 1);
	fstuff.topology = myTopology;


	/* get hostname */

	char hostname[HOST_NAME_MAX+1];
    if (gethostname(hostname, HOST_NAME_MAX+1) != 0) {
    	perror("gethostname");
    	exit(-1);
    }


	/* create topology node */

	const char *path[] = {hostname};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 1);
	fstuff.toponode = myTopoNode;


	/* create sources */

	rutSources mySources;
	RUTSRC_UNSET_ALL(mySources);

	/* set sources */
	for (int i = 2; i < argc; ++i) {
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

	/* read cycle time */

	int cycle;
	if (sscanf(argv[2], "%d", &cycle) != 1) {
		puts("Error reading cycle time");
		return -1;
	}

	/* create UtilTrace object */

	UtilTrace *myTrace;
	ret = rut_createTrace(myTopoNode, 1, mySources, cycle, &myTrace);

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
