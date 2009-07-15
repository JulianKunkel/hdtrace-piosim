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
 * This mutex is used to guarantee, that only one thread reads the
 * environment variables in the function \a readEnvVars()
 */
static pthread_mutex_t envvar_mutex = PTHREAD_MUTEX_INITIALIZER;

/**
 * This variable is set to 0 as long as the environment variables have not been read.
 * When the first thread enters \a readEnvVars(), it sets \a envvar_read to 1 so any
 * following thread does not read the variables again.
 */
static int envvar_read = 0;


/**
 * This functions reads the environment variables that control the
 * MPI wrapper. Which variables are read is defined in the global
 * variable \a control_vars[]. The values are stored in global variables
 * as defined in \a controlled_vars[].
 *
 * If the value of an environment variable can not be processed,
 * a message is printed via \a printDebugMessage()
 */
static void readEnvVars()
{
	pthread_mutex_lock(&envvar_mutex);
	if(envvar_read == 0)
	{
		// read environment variables and set corresponding control values
		char *env_var, *getenv();
		int ii = 0;
		while(control_vars[ii] && controlled_vars[ii])
		{
			if((env_var = getenv(control_vars[ii])) != NULL)
			{
				if(strcmp(env_var, "0") == 0)
				{
					*controlled_vars[ii] = 0;
				}
				else if(strcmp(env_var, "1") == 0)
				{
					*controlled_vars[ii] = 1;
				}
				else
				{
					printDebugMessage("environment variable %s has unrecognised value of %s. "
									  "0 and 1 are valid values" ,
									control_vars[ii], env_var );
				}
			}
			ii++;
		}
		envvar_read = 1;
	}
	pthread_mutex_unlock(&envvar_mutex);
}

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

int hdMPI_threadEnableTracing(){
	return hdT_enableTrace(tracefile);
}

int hdMPI_threadDisableTracing(){
	return hdT_disableTrace(tracefile);
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

	readEnvVars();

 	hdT_setNestedDepth(tracefile, trace_nested_operations * HD_LOG_MAX_DEPTH);
	hdT_setForceFlush(tracefile, trace_force_flush);

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
	hdT_finalize(tracefile);
	tracefile = NULL;
	destroyHashTables();
}
