/**
 * @file ptLoop.c
 *
 * Example program showing how to use libpt.
 * The program simply starts tracing with the given configuration file.
 *  To stop it send SIGINT (Ctrl+C).
 *
 * @date 08.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>

#include "pt.h"

static struct {
	PowerTrace *trace;
} fstuff = { NULL };

void sighandler(int sig) {

	if (sig != SIGINT)
		return;

	pt_stopTracing(fstuff.trace);

	exit(pt_finalizeTrace(fstuff.trace));
}


int main(int argc, char **argv)
{
	struct sigaction act;
	act.sa_handler = sighandler;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGINT, &act, NULL);

	if (argc < 2)
		return -1;


	PowerTrace *trace;

	int ret = pt_createTrace(argv[1], &trace);
	if (ret != PT_EOK)
		exit(ret);

	fstuff.trace = trace;

	pt_startTracing(trace);

	/* loop */

	while (1)
		/* sleep, SIGINT signal is processed nevertheless */
		sleep(600);


	/* never come here */

	return 0;
}
