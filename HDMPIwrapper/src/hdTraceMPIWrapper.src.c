/**
 * @file HDTraceMPIWrapper.src.c
 *
 * This file is concatenated with the output of create-sim_wrapper.py
 * to create HDTraceMPIWrapper.c. The latter is then compiled.
 *
 * This file contains functions and variables that are used by the
 * generated MPI_* tracing functions.
 *
 * @author Paul Mueller <pmueller@ix.urz.uni-heidelberg.de>
 */

/*
 * Copyright (c) 2008 Julian M. Kunkel
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include <mpi.h>
//#include <mpio.h>
#include <pthread.h>

#include <limits.h>
#include <stdlib.h>

#include <stdio.h>
#include <string.h>
#include <glib.h>

#include <assert.h>
#include <unistd.h>
#include <errno.h>

#include <stdarg.h>

#include "hdTrace.h"
#include "hdTopo.h"
#include "hdMPITracer.h"

#include "hdmpi-wrapper-pkg.h"

#ifdef ENABLE_UTILIZATION_TRACE
#include "RUT.h"

static UtilTrace *rutStatistics = NULL;
#endif

#ifdef ENABLE_HDMRUT
#include "mrut.h"

static mrutTracer mrutTraceVar = NULL;
#endif

#ifdef ENABLE_POWER_TRACE
#include "pt.h"

static PowerTrace *ptStatistics = NULL;
#endif

#ifdef ENABLE_PVFS2_INTERNAL_TRACING
#include "pint-event-hd-client.h"

#warning "Using PVFS internal tracing"
#endif

#ifdef ENABLE_LIKWID_HDTRACE
#include "hdtraceLikwid.h"

#warning "Using Likwid"
#endif

#ifdef ENABLE_SOTRACER
#include "sotracer.h"

#warning "Using soTracer"

#ifdef ENABLE_UTILIZATION_TRACE
#warning "soTracer & RUT should not be used simultainiously! This will lead to race conditions if GlibC is traced by soTracer!"
#endif

#endif

/**
 * Defines if the MPI tracing is started.
 */
static int mpiTracingStarted = 0;

static int mpiTracingEnabledManually = 0;

static int mpiTraceNesting = 1;

/**
 * This is a global variable that regulates whether all functions listed in
 * \a interesting_funcs.h are traced, or only those that have custom arguments
 * specified in the file \a scripts/wrapper_conf.py.
 * If \a trace_all_functions is set to one, the default log method is logging the
 * function name and the start and end times of the call.
 *
 * The value of this variable can be changed by setting the environment variable
 * \a HDTRACE_ALL_FUNCTIONS.
 */
static int trace_all_functions = 1;

/**
 * This global variable determines, whether nested calls to MPI functions from within
 * MPI functions should be traced.
 *
 * The value of this variable can be changed by setting the environment variable
 * \a HDTRACE_NESTED.
 */
static int trace_nested_operations = 1;

/**
 * This variable determines, if the MPI_Info structure that is given
 * to \a MPI_File_delete, \a MPI_File_set_view, \a MPI_File_set_info or
 * \a MPI_File_open should be logged.
 *
 * The value of this variable can be changed by setting the environment
 * variable \a HDTRACE_FILE_INFO.
 */
static int trace_file_info = 1;

/**
 * This variable determines, if flushing the log should occur on each
 * write. If \a trace_force_flush is set to 0, flushing occurs only
 * when the HDTraceWritingLibrary decides to do so. Otherwise,
 * \a hdT_forceFlush(tracefile) is called.
 */
static int trace_force_flush = 0;

/**
 * The topology (host, rank, thread)
 */
static hdTopology *topology;

/**
 * This array defines the name of the environment variables which
 * are read by the program. The values of the environment variables
 * are then stored in those variables to which elements of the
 * array \a controlled_vars are pointing.
 *
 * Example
 * \code
 * HDTRACE_NESTED=1
 * \endcode
 * will lead to setting
 * \code
 * *controlled_vars[1] = 1;
 * \endcode
 * which is equivalent to
 * \code
 * trace_nested_operations = 1;
 * \endcode
 */
static const char * control_vars[] = { "HDTRACE_ALL_FUNCTIONS",
								"HDTRACE_NESTED",
								"HDTRACE_FILE_INFO",
								"HDTRACE_FORCE_FLUSH",
								NULL };

/**
 * This array stores the addresses of the global control
 * variables. The environment variable, that controls a certain global
 * variable is listed in \a control_vars and has the same index.
 */
static int * controlled_vars[] = { &trace_all_functions,
								   &trace_nested_operations,
								   &trace_file_info,
								   &trace_force_flush,
								   NULL };

/**
 * This string is prepended to the name of every trace file.
 * Please note that it should not contain any underscores, because
 * this character is separating different topology levels.
 *
 * It consists of the program name
 */
static char * trace_file_prefix = NULL;

/**
 * Throttling:
 *  Record throttle_states_to_record, then Pause for (throttle_cycle_length - throttle_states_to_record)
 */
static int throttle_states_to_record = 0;
static int throttle_cycle_length = 0;
static int throttle_disable_trace = 0;
static int number_of_states_recorded = 0;



int hdMPI_threadEnableTracing(){
  int ret = 1;
  if (! mpiTracingEnabledManually){
    if(! throttle_disable_trace){
      ret = hdT_enableTrace(hdMPI_getThreadTracefile());
    }
    mpiTracingEnabledManually = 1;
  }
  return ret;
}

int hdMPI_threadDisableTracing(){
  int ret = 1;
  if (mpiTracingEnabledManually){
    if(! throttle_disable_trace){
      ret = hdT_disableTrace(hdMPI_getThreadTracefile());
    }
    mpiTracingEnabledManually = 0;
  }
  return ret;
}


/*
 * We include *.c files so there is only one object file.
 * This is desireable because then, all but the MPI_* functions
 * can be declared static.
 */
#include "../src/mpi_names.c"
#include "../src/hash_tables.c"
#include "../src/write_info.c"
#include "../src/hdMPITracer.c"

#ifdef  HDTRACE_INCLUDE_NESTED_TEST
#include "../src/test_nested.c"
#endif

#include "../src/common.c"


#ifdef ENABLE_LIKWID_HDTRACE
void hdMPI_threadLogStateStart(const char * stateName){
  if( ! mpiTracingEnabledManually ) return;

    mpiTraceNesting++;
#ifdef ENABLE_SOTRACER
    sotracer_disable();
#endif
    if(mpiTraceNesting == 1){
      hdLikwidResults results;

      hdLikwid_end(& results);

      if (results.runtime != 0.0){
	hdT_logAttributes(hdMPI_getThreadTracefile(), "wallclock=\"%f\" runtime=\"%fs\" ipc=\"%f\" clock=\"%f\" memBandwidth=\"%f\" remReadBW=\"%f\" scalar=\"%f\" packed=\"%f\" sp=\"%f\" dp=\"%f\"", results.wallclocktime, results.runtime, results.IPC, results.clock, results.memBandwidth, results.remReadBW, results.sse_scalar, results.sse_packed, results.sse_sp, results.sse_dp);
      }

      hdT_logStateEnd(hdMPI_getThreadTracefile());
      hdT_logStateStart(hdMPI_getThreadTracefile(), stateName);

      number_of_states_recorded++;
    }else{
      hdT_logStateStart(hdMPI_getThreadTracefile(), stateName);
    }

#ifdef ENABLE_SOTRACER
    sotracer_enable();
#endif
}

void hdMPI_threadLogStateEnd(void){
    if(! mpiTracingEnabledManually) return;

    mpiTraceNesting--;
#ifdef ENABLE_SOTRACER
    sotracer_disable();
#endif
    if(mpiTraceNesting == 0){
      hdT_logStateEnd(hdMPI_getThreadTracefile());
      hdT_logStateStart(hdMPI_getThreadTracefile(), "ECOMPUTE");
      hdLikwid_start();
    } else {
      hdT_logStateEnd(hdMPI_getThreadTracefile());
    }

#ifdef ENABLE_SOTRACER
    sotracer_enable();
#endif
}

#else  /* NO ENABLE_LIKWID_HDTRACE */

void hdMPI_threadLogStateStart(const char * stateName){

  mpiTraceNesting++;

  if(! mpiTracingEnabledManually) return;

    if(mpiTraceNesting == 1){

      if(throttle_cycle_length > 0){
	const int throttle_rest = ( number_of_states_recorded % throttle_cycle_length );

	if(throttle_rest == 0){
	  throttle_disable_trace = 0;
	  hdT_enableTrace(tracefile);

	}else if(throttle_rest == throttle_states_to_record){

	  hdT_disableTrace(tracefile);
	  /* disable tracing */
	  throttle_disable_trace = 1;
#ifdef ENABLE_SOTRACER
	  sotracer_disable();
#endif
	}
      }

      number_of_states_recorded++;
    }

    if(throttle_disable_trace) return;

#ifdef ENABLE_SOTRACER
    sotracer_disable();
#endif
      hdT_logStateStart(hdMPI_getThreadTracefile(), stateName);
#ifdef ENABLE_SOTRACER
    sotracer_enable();
#endif
}

void hdMPI_threadLogStateEnd(void){
  mpiTraceNesting--;

  if(! mpiTracingEnabledManually) return;

  if(throttle_disable_trace) return;

#ifdef ENABLE_SOTRACER
    sotracer_disable();
#endif
      hdT_logStateEnd(hdMPI_getThreadTracefile());
#ifdef ENABLE_SOTRACER
    sotracer_enable();
#endif
}

#endif

/**
 * This function translates \a rank from the MPI communicator
 * \a comm to the rank of the process in the MPI_COMM_WORLD
 * communicator.
 *
 * \param rank the rank to be translated
 * \param comm the communicator of which \a rank is a member
 *
 * \return the world rank of the process or -1 on error.
 *
 * If an error occurs, \a errno holds the MPI error code.
 */
static int getWorldRank(int rank, MPI_Comm comm)
{
	int ret;
	if(comm == MPI_COMM_WORLD)
		return rank;

	MPI_Group group, worldgroup;
	ret = PMPI_Comm_group(MPI_COMM_WORLD, &worldgroup);
	if(ret != MPI_SUCCESS)
	{
		errno = ret;
		return -1;
	}

	ret = PMPI_Comm_group(comm, &group);
	if(ret != MPI_SUCCESS)
	{
		errno = ret;
		return -1;
	}
	int out;

	ret = PMPI_Group_translate_ranks(group, 1, &rank, worldgroup, &out);
	if(ret != MPI_SUCCESS)
	{
		errno = ret;
		return -1;
	}

	return out;
}

/**
 * This function returns a crude approximation of the size of
 * \a count items of the type \a type. It simply computes
 * \a count * \a size(type), where \size is identified by calling
 * \a MPI_Type_size.
 *
 * \param count the number of items in a message
 * \param type the MPI Datatype of a message
 *
 * \return the estimated type size or -1 on error
 *
 * If an error occurs, \a errno holds the MPI error code.
 */
static long long getTypeSize(int count, MPI_Datatype type)
{
  int t_size;
  int ret;
  ret = PMPI_Type_size(type, & t_size);
  if(ret != MPI_SUCCESS)
  {
	  errno = ret;
	  return -1;
  }
  return (count * (long long) t_size );
}

/**
 * Get the position of the file pointer in the file \a v1.
 *
 * The position is calculated using \a PMPI_File_get_byte_offset(...)
 *
 * \param v1 the file in which the offset is queried.
 *
 * \return position of the file pointer or -1 if an error occured.
 *
 * If an error occurs, \a errno holds the MPI error code.
 */
static long long int getByteOffset(MPI_File v1)
{
	assert(sizeof(long long int) >= sizeof(MPI_Offset));
    MPI_Offset view_offset;
	int ret;
    // view dependent offset:
    ret = PMPI_File_get_position(v1, & view_offset);
	if(ret != MPI_SUCCESS)
	{
		errno = ret;
		return -1;
	}

    // real offset:
    MPI_Offset real_offset;
    ret = PMPI_File_get_byte_offset(v1, view_offset, & real_offset);
	if(ret != MPI_SUCCESS)
	{
		errno = ret;
		return -1;
	}
	return (long long int)real_offset;
}

static void before_Init()
{
#ifdef ENABLE_SOTRACER
  sotracer_initalize();
#endif
}


/**
 * Startup creation of the traceing, can be used multiple times
 * to create multiple trace results from one file.
 */
int hdMPI_PrepareTracing(const char * filePrefix){
   if( mpiTracingStarted ) return 1;

   trace_file_prefix = strdup(filePrefix);

   const char *toponames[3] = {"Host", "Rank", "Thread"};
	topology = hdT_createTopology(trace_file_prefix, toponames, 3);

    {
      const char * tmp;
      tmp = getenv("HDTRACE_THROTTLE_CYCLE_LENGTH");
      if (tmp != NULL){
	throttle_cycle_length = atoi(tmp);
      }
      tmp = getenv("HDTRACE_THROTTLE_STATES_TO_RECORD");
      if (tmp != NULL){
	throttle_states_to_record = atoi(tmp);
      }

      /* Check parameters */
      if(throttle_cycle_length > 0){
	if (throttle_states_to_record > throttle_cycle_length){
	  printf("HDTRACE ERROR the environment variable HDTRACE_THROTTLE_CYCLE_LENGTH <  HDTRACE_THROTTLE_STATES_TO_RECORD in line %d in %s\n", __LINE__, __FILE__);
	}
	if (throttle_states_to_record < 1){
	  printf("HDTRACE ERROR the environment variable HDTRACE_THROTTLE_STATES_TO_RECORD < 1 in line %d in %s\n", __LINE__, __FILE__);
	}
      }
    }

#ifdef ENABLE_FUNCTION_WRAPPER
    /* initalize MPI main thread */
    hdMPI_threadInitTracing();
#endif

#ifdef ENABLE_HDMRUT
# define ENABLE_UTILIZATION_OR_POWER_TRACE
#endif

#ifdef ENABLE_UTILIZATION_TRACE
# define ENABLE_UTILIZATION_OR_POWER_TRACE
#else
# ifdef ENABLE_POWER_TRACE
#  define ENABLE_UTILIZATION_OR_POWER_TRACE
# endif
#endif

#ifdef ENABLE_UTILIZATION_OR_POWER_TRACE
	{
	/* JK: use the powertracer, the powertracer must be started only once per node */
	/* therefore determine full qualified hostname and send it to all other ranks */
	/* the rank with the lowest number on each host starts the powertrace */
        int size;
        int ret;
        int rank;
        int rankForThisHost = -1;

        PMPI_Comm_size(MPI_COMM_WORLD, &size);
        PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

        char hostname[HOST_NAME_MAX+1];
        ret = gethostname(hostname, HOST_NAME_MAX+1);
        if( ret != 0){
                printf("Error while determine hostname in rank: %d\n", rank);
                PMPI_Abort(MPI_COMM_WORLD, 2);
        }

        /* send hostname to each process */
        char * recvBuff = malloc(size * (HOST_NAME_MAX+1));
        if (recvBuff == 0){
                printf("Error while reserving memory for recv buffer: %d\n", rank);
                PMPI_Abort(MPI_COMM_WORLD, 2);
        }

        PMPI_Allgather(hostname, (HOST_NAME_MAX+1), MPI_CHAR, recvBuff,
        		(HOST_NAME_MAX+1), MPI_CHAR, MPI_COMM_WORLD);

        /* Scan results to lookup first occurence of hostname */
        int i;
        for (i=0; i < size; i++){
            char * cur =  & recvBuff[(HOST_NAME_MAX+1)*i];
            /* printf("got %s \n", cur); */
            if (strcmp(cur, hostname) == 0){
                rankForThisHost = i;
                break;
            }
        }

       free(recvBuff);

        if(rankForThisHost == rank){

# ifdef ENABLE_UTILIZATION_TRACE

            printf("Start performance tracer on host %s by rank: %d\n", hostname, rank);

        	rutSources statistics;

            // create labels and values for the project topology
            const char *levels[1] = {hostname};
            hdTopoNode *topoNode = hdT_createTopoNode(topology, levels, 1);

            RUTSRC_SET_ALL(statistics);

            // set the global variable
    		ret = rut_createTrace(topoNode, 1, statistics, 1000, &rutStatistics);
        	if (ret != RUT_SUCCESS) {
        		printf("Error while starting resources utilization tracing: %d\n", rank);
        		PMPI_Abort(MPI_COMM_WORLD, 2);
        	}

        rut_startTracing(rutStatistics);

# endif /* ENABLE_UTILIZATION_TRACE */

# ifdef ENABLE_HDMRUT
	{
	GError * error = NULL;
        const char *levels[1] = {hostname};
        hdTopoNode *topoNode = hdT_createTopoNode(topology, levels, 1);

	const char * mrutConfig = getenv("HDTRACE_MRUT_CONFIG");

	if(mrutConfig == NULL){
	    printf("[HDMPIWRAPPER] mrut environment variable HDTRACE_MRUT_CONFIG not set => disabling mrut\n");
	}else{
	  if (! mrut_init(& mrutTraceVar, topoNode , mrutConfig , & error) ){
	      printf("[HDMPIWrapper] mrut init error %s\n", error->message );
	      g_error_free(error);
	  }

	  if (! mrut_enable(& mrut_enableTracing, & error) ){
	      printf("[HDMPIWrapper] mrut init error %s\n", error->message );
	      g_error_free(error);
	  }
	}
	}
# endif

# ifdef ENABLE_POWER_TRACE

    		/* Read config files for power tracer from environment */
    		char *tmp = getenv("HDTRACE_PT_CFG_FILES");
    		if (tmp == NULL || *tmp == '\0') {
    			printf("Power tracing activated but HDTRACE_PT_CFG_FILES not set: %d\n", rank);
    			PMPI_Abort(MPI_COMM_WORLD, 2);
    		}

    		/* Copy string since strtok will change it */
            char configfiles[strlen(tmp)];
            strcpy(configfiles, tmp);

            /* Try all configuration files (separated by ':') */
            char *saveptr;
            for( char *ptConfigfile = strtok_r(configfiles, ":", &saveptr);
					ptConfigfile != NULL;
					ptConfigfile = strtok_r(NULL, ":", &saveptr)) {
            	printf("Testing power tracer configuration file %s on host %s"
            			" by rank: %d\n", ptConfigfile, hostname, rank);

            	/* create power trace and read in configuration file */
            	ret = pt_createTrace(ptConfigfile, topology, &ptStatistics);
            	if (ret == PT_SUCCESS) {
            		printf("Start power tracer on host %s by rank: %d\n", hostname, rank);
            		pt_startTracing(ptStatistics);
            	}
            	else if (ret == PT_EWRONGHOST) {
            		/* this host is not connected to the measuring device */
            		ptStatistics = NULL;
            	}
            	else {
            		printf("Error while starting power tracing: %d\n", rank);
            		PMPI_Abort(MPI_COMM_WORLD, 2);
            	}
            }

# endif /* ENABLE_POWER_TRACE */
        }
	}
#endif

#ifdef ENABLE_PVFS2_INTERNAL_TRACING
        {
        int rank;
        MPI_Comm_rank(MPI_COMM_WORLD, &rank);

        char hostname[HOST_NAME_MAX];
        char rankStr[20];

        // create labels and values for the project topology
        gethostname(hostname, HOST_NAME_MAX);
        snprintf(rankStr, 20, "%d", rank);
        const char *levels[2] = {hostname, rankStr};

        hdTopoNode *pvfs2parentNode = hdT_createTopoNode(topology, levels, 2);

        PVFS_HD_client_trace_initialize(topology, pvfs2parentNode);
        }
#endif

	// start first compute phase:
	mpiTraceNesting = 0;

#ifdef ENABLE_LIKWID_HDTRACE
	{
	  /**
	   * Determine the core number on which the process should be pinned.
	   * Algorithm: enumerate processes located on each node by their rank => local rank.
	   */

	  int size;
	  int ret;
	  int rank;

	  PMPI_Comm_size(MPI_COMM_WORLD, &size);
	  PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

	  char hostname[HOST_NAME_MAX+1];
	  ret = gethostname(hostname, HOST_NAME_MAX+1);
	  if( ret != 0){
		  printf("Error while determine hostname in rank: %d\n", rank);
		  PMPI_Abort(MPI_COMM_WORLD, 2);
	  }

	  /* send hostname to each process */
	  char * recvBuff = malloc(size * (HOST_NAME_MAX+1));
	  if (recvBuff == 0){
		  printf("Error while reserving memory for recv buffer: %d\n", rank);
		  PMPI_Abort(MPI_COMM_WORLD, 2);
	  }

	  PMPI_Allgather(hostname, (HOST_NAME_MAX+1), MPI_CHAR, recvBuff,
			  (HOST_NAME_MAX+1), MPI_CHAR, MPI_COMM_WORLD);

	  /* Scan results to lookup all occurences of hostname */
	  int i;
	  int core = 0;
	  for (i=0; i < rank; i++){
	      char * cur =  & recvBuff[(HOST_NAME_MAX+1)*i];
	      /* printf("got %s \n", cur); */
	      if (strcmp(cur, hostname) == 0){
		 core++;
	      }
	  }
	  free(recvBuff);

	  printf("[%d] run on CPU: %d\n", rank, core);

	  hdLikwid_init(core);

	  hdT_logStateStart(hdMPI_getThreadTracefile(), "ECOMPUTE");
	  hdLikwid_start();
	}
  #endif

  #ifdef ENABLE_SOTRACER
  sotracer_enable();
  #endif

  hdMPI_threadEnableTracing();

  mpiTracingStarted = 1;

  return 0;
}

/**
 * Stop tracing, it can be restarted by using hdMPI_PrepareTracing
 */
int hdMPI_FinalizeTracing(){
  if(! mpiTracingStarted) return 1;

    // ensure that we will not trace anything from MPI any more.
  hdT_disableTrace(hdMPI_getThreadTracefile());

#ifdef ENABLE_SOTRACER
  sotracer_finalize();
#endif

#ifdef ENABLE_LIKWID_HDTRACE
	{
	  // end ecompute:
	  hdLikwidResults results;

	  hdLikwid_end(& results);

	  if (results.runtime != 0.0){
	    hdT_logAttributes(hdMPI_getThreadTracefile(), "wallclock=\"%f\" runtime=\"%fs\" ipc=\"%f\" clock=\"%f\" memBandwidth=\"%f\" remReadBW=\"%f\" scalar=\"%f\" packed=\"%f\" sp=\"%f\" dp=\"%f\"", results.wallclocktime, results.runtime, results.IPC, results.clock, results.memBandwidth, results.remReadBW, results.sse_scalar, results.sse_packed, results.sse_sp, results.sse_dp);
	  }

	  hdT_logStateEnd(hdMPI_getThreadTracefile());
	  hdLikwid_finalize();
	}
#endif

	hdMPI_threadFinalizeTracing();

#ifdef ENABLE_UTILIZATION_TRACE
	if(rutStatistics != NULL){
		rut_stopTracing(rutStatistics);
		rut_finalizeTrace(rutStatistics);
	}
#endif

# ifdef ENABLE_HDMRUT
	GError * error = NULL;
	if (! mrut_finalize(& mrutTraceVar, & error) ){
	    printf("[HDMPIWrapper] error %s\n", error->message );
	     g_error_free(error);
	}
#endif

#ifdef ENABLE_POWER_TRACE
	if(ptStatistics != NULL){
		pt_stopTracing(ptStatistics);
		pt_finalizeTrace(ptStatistics);
	}
#endif

  free(trace_file_prefix);
  trace_file_prefix = NULL;

  mpiTracingStarted = 0;

  return 0;
}

/**
 * This function is called after a call to \a MPI_Init(...) or \a MPI_Init_thread(...).
 * It initializes the global variable \a tracefile by calling
 * \a hdT_createTrace(...). It also calls \a readEnvVars(...).
 * After this call the global variable \a tracefile and the configuration variables
 * \a trace_* (that are listed in \a controlled_vars) by may be used.
 *
 * \param argc the argument count parameter that was passed to MPI_Init
 * \param argv the arguments that were passed to MPI_Init
 */
static void after_Init(int *argc, char ***argv)
{

	if(argc == NULL || *argc < 1)
	{
		//we don't know what the program's name is, so call this "trace"
		hdMPI_PrepareTracing("trace");
	}
	else
	{
		char * lastSlash = strrchr(**argv , '/');
		if( lastSlash != NULL)
		{
			hdMPI_PrepareTracing( lastSlash+1 );
		}
		else
		{
			hdMPI_PrepareTracing( (*argv)[0] );
		}
	}
}

/**
 * This function is called after \a PMPI_Finalize(...)
 * It finalizes the global \a tracefile and destroys all
 * dynamically allocated variables.
 */
static void after_Finalize(void)
{
  hdMPI_FinalizeTracing();
}

/**
 * This function is called before \a PMPI_Abort(...)
 * It finalizes the global \a tracefile and destroys all
 * dynamically allocated variables.
 *
 * \param comm The comm parameter that is passed to \a MPI_Abort(...)
 * \param code The code parameter that is passed to \a MPI_Abort(...)
 */
static void before_Abort()
{
    hdMPI_FinalizeTracing();
}

/**
 * If FUNCTION Wrapper is disabled, yet init PVFS2 and PowerTracer
 */
#ifndef ENABLE_FUNCTION_WRAPPER
int MPI_Init(int * v1,  char *** v2){
  int ret;
  ret = PMPI_Init( v1,  v2);
  after_Init(v1, v2);
  return ret;
}

int MPI_Finalize(void){
  int ret;
  ret = PMPI_Finalize();

  after_Finalize();
  return ret;
}
#endif

/* here the mpi tracing function definitions are appended by create_sim-wrapper.py */
