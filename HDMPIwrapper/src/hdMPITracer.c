#include "hdMPITracer.h"

/**
 * This global (per thread) variable holds the \a hdTrace structure that the logging
 * functions are writing to.
 */
static __thread hdTrace *tracefile = NULL;

/**
 * This global (per thread) variable holds the \a hdTrace topology that this tracing belongs to.
 */
static __thread hdTopoNode *topoNode = NULL;

/**
 * Return the tracefile used to store the threads values in the current topology
 */
hdTrace * hdMPI_getThreadTracefile(){
	return tracefile;
}

/**
 * Return the topology for the current thread
 */
hdTopoNode * hdMPI_getThreadTopologyNode(){
	return topoNode;
}


/**
 * Initalize tracing, to allow thread to use tracing.
 */
void hdMPI_threadInitTracing(){
	/* NAME_LEN is the length of the strings that hold the rank- and thread names.
	 * The names are integers, starting at 0, which means a small \a NAME_LEN should suffice.
	 */
#define NAME_LEN 10
	char hostname[HOST_NAME_MAX];
	char rankname[NAME_LEN];
	char threadname[NAME_LEN];

	static int thread_counter = 0;
	static pthread_mutex_t thread_counter_mutex = PTHREAD_MUTEX_INITIALIZER;

	int rank;

	PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

	// find out which thread we are in
    pthread_mutex_lock(&thread_counter_mutex);
    int thread = thread_counter;
    ++thread_counter;
    pthread_mutex_unlock(&thread_counter_mutex);

	// create labels and values for the project topology
	gethostname(hostname, HOST_NAME_MAX);

	snprintf(rankname, NAME_LEN, "%d", rank);
	snprintf(threadname, NAME_LEN, "%d", thread);

	const char *levels[3] = {hostname, rankname, threadname};

	topoNode = hdT_createTopoNode(topology, levels, 3);


	tracefile = hdT_createTrace(topoNode);

#undef NAME_LEN
}

/**
 * Finalize trace file for the thread.
 *
 * This function is called after \a PMPI_Finalize(...)
 * It finalizes the global \a tracefile and destroys all
 * dynamically allocated variables.
 */
void hdMPI_threadFinalizeTracing(){
	if(tracefile != NULL){
	  hdT_finalize(tracefile);
	  tracefile = NULL;
	}
	destroyHashTables();
}
