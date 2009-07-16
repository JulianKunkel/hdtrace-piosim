/**
 * @file hdStatsDaemon.c
 *
 * @date 16.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#define __GNU_SOURCE

#include <stdlib.h>
#include <stdio.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <sys/select.h>

#include <string.h>
#include <limits.h>

#include <errno.h>
#include <assert.h>

#include "hdTopo.h"
#include "pt.h"
#include "PTL.h"

/* needed for cleanup */
static int fifo;
static char *fifoFile;

/* needed for traces */
static hdTopology *topology;
static hdTopoNode *toponode;

/* traces */
static PowerTrace *powerTrace;
static PerfTrace *perfTrace;

/* own exit function doing cleanup */
static void myExit(int state);


static void printHelp(void) {
	fputs("The first argument is the project name to use.", stderr);
	fputs("The second argument is the name of the socket file to use.", stderr);
	fputs("The third argument is the name of power configuration to use.", stderr);
}

static void createTraces(char *projectname, char *powerConfig) {

	/* create topology */
	const char *levels[] = {"Host"};
	topology = hdT_createTopology(projectname, levels, 1);
	if (topology == NULL) {
    	fputs("Error while creating topology,"
    			" exit state is errno set by hdT_createTopology()", stderr);
    	myExit(errno);
	}

	/* get hostname */
    char hostname[HOST_NAME_MAX+1];
    if (gethostname(hostname, HOST_NAME_MAX+1) != 0) {
    	perror("gethostname");
    	myExit(-1);
    }

	/* create topology node */
	const char *path[] = {hostname};
	toponode = hdT_createTopoNode(topology, path, 1);

	/* create sources */
	ptlSources mySources;
	PTLSRC_UNSET_ALL(mySources);
	PTLSRC_SET_ALL(mySources);

	/* create PerfTrace object */
	PerfTrace *myTrace;
	perfTrace = ptl_createTrace(toponode, 1, mySources, 500);
	if (myTrace == NULL)
	{
    	fputs("Error while creating performance trace,"
    			" exit state is errno set by ptl_createTrace()", stderr);
    	myExit(errno);
	}

	/* create power trace */
	int ret = pt_createTrace(powerConfig, NULL, &powerTrace);
	if (ret != PT_SUCCESS) {
		fputs("Error while creating power trace."
				" Exit value is returned by pt_createTrace()", stderr);
		myExit(ret);
	}
}


static int doAction(char *action) {

	if (strcmp(action, "START POWER") == 0) {
		pt_startTracing(powerTrace);
		return 1;
	}

	if (strcmp(action, "STOP POWER") == 0) {
		pt_stopTracing(powerTrace);
		return 1;
	}

	if (strcmp(action, "START PERF") == 0) {
		ptl_startTrace(perfTrace);
		return 1;
	}

	if (strcmp(action, "STOP PERF") == 0) {
		ptl_stopTrace(perfTrace);
		return 1;
	}

	if (strcmp(action, "QUIT") == 0) {
		return 0;
	}

	printf("Unknown action = %s\n", action);
	return -1;

}

int finalizeTraces(void) {

	int ret = 0;

	/*
	 * Finalize power trace
	 */
	ret = pt_finalizeTrace(powerTrace);

	/*
	 * Finalize performance trace
	 */
	ptl_destroyTrace(perfTrace);

	/* destroy all the rest */
	hdT_destroyTopoNode(toponode);
	hdT_destroyTopology(topology);

	return ret;
}

int main(int argc, char **argv) {

	char *projectname;
	char *ptConfig;

	if (argc != 4) {
		printHelp();
		exit(-1);
	}

	projectname = argv[1];
	fifoFile = argv[2];
	ptConfig = argv[3];

	/*
	 * Create FIFO and open nonblocking for reading
	 */
	if (mkfifo(fifoFile, S_IRWXU) != 0) {
		perror("mkfifo");
		exit(-1);
	}

	/* using O_RDWR is a hack to avoid misbehavior of select() */
	/* fifo(7) says: POSIX leaves this behavior undefined. */
	fifo = open(fifoFile, O_RDWR | O_NONBLOCK);
    if (fifo < 0) {
    	perror("open");
    	exit(-1);
    }

	/*
	 * Create traces
	 */
    createTraces(projectname, ptConfig);


	/*
	 * Wait for input on FIFO
	 */

	while(1) {
		fd_set readfds;

		FD_ZERO(&readfds);
		FD_SET(fifo, &readfds);

		/* run select that can wait forever */
		if(select(fifo+1, &readfds, NULL, NULL, NULL) < 0) {
			perror("select");
			exit(-1);
		}

		/* we are only waiting for fifo input */
		assert(FD_ISSET(fifo, &readfds));

		char buf[101];
		ssize_t bytes;
		size_t i = 0;
		while(bytes = read(fifo, buf+i, sizeof(buf) - i)) {
			if (bytes < 0) {
				if (errno == EAGAIN) {
					break;
				}
				else {
					perror("read");
					exit(-1);
				}
			}

			i += bytes;

			if (i == sizeof(buf)) {
				/* if the message is too long,
				 * read the rest byte by byte into last byte of buffer */
				--i;
			}

			assert(i < sizeof(buf));
		}


		/* if the last character is a line break we override it */
		if(buf[i-1] == '\n')
			i--;

		/* write terminating '\0' */
		buf[i] = '\0';

		/* Do action indicated by message read from fifo */
		if(doAction(buf) == 0)
			break;
	}

    /*
     * Exit with return value from finalizing traces
     */
	myExit(finalizeTraces());

}

static void myExit(int state) {

	close(fifo);
    unlink(fifoFile);

	exit(state);
}
