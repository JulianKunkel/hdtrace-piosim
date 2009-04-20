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

/**
 * This global (per thread) variable holds the \a hdTrace structure that the logging
 * functions are writing to.
 */
static __thread hdTrace tracefile = NULL;

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


#include "mpi_names.c"
#include "hash_tables.c"
#include "write_info.c"

#ifdef  HDTRACE_INCLUDE_NESTED_TEST
#include "test_nested.c"
#endif

#include "common.c"

/**
 * This function translates \a rank from the MPI communicator
 * \a comm to the rank of the process in the MPI_COMM_WORLD
 * communicator.
 *
 * \param rank the rank to be translated
 * \param comm the communicator of which \a rank is a member
 *
 * \return the world rank of the process
 */
static int getWorldRank(int rank, MPI_Comm comm)
{
	if(comm == MPI_COMM_WORLD)
		return rank;

	MPI_Group group, worldgroup;
	MPI_Comm_group(MPI_COMM_WORLD, &worldgroup);
	MPI_Comm_group(comm, &group);
	int out;
	MPI_Group_translate_ranks(group, 1, &rank, worldgroup, &out);
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
 */
static long long getTypeSize(int count, MPI_Datatype type)
{
  int t_size;
  MPI_Type_size(type, & t_size);
  return (count * (long long) t_size );
}

/**
 * Get the position of the file pointer in the file \a v1.
 *
 * The position is calculated using \a PMPI_File_get_byte_offset(...)
 *
 * \param v1 the file in which the offset is queried.
 */
static long long int getByteOffset(MPI_File v1)
{
	assert(sizeof(long long int) >= sizeof(MPI_Offset));
    MPI_Offset view_offset;
    // view dependent offset:
    PMPI_File_get_position(v1, & view_offset);
    // real offset:
    MPI_Offset real_offset;
    PMPI_File_get_byte_offset(v1, view_offset, & real_offset);
	return (long long int)real_offset;
}

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
					printDebugMessage("environment variable %s has unrecognised value of %s",
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
 * This is the length of the temporary buffer that is used to
 * create the basename of the log files. The basename usually consists
 * of the \a trace_file_prefix and the name of the executable that
 * is passed to MPI_Init(int *argc, char ***argv) as *argv[0].
 */
#define TMP_BUF_LEN 1024 * 16

/**
 * This string is prepended to the name of every trace file.
 * Please note that it should not contain any underscores, because
 * this character is separating different topology levels.
 */
static const char trace_file_prefix[] = "";

/**
 * This function is called after a call to \a MPI_Init(...) or \a MPI_Init_thread(...).
 * It initializes the global variable \a tracefile by calling
 * \a hdT_createTrace(...). It also calls \a readEnvVars(...).
 * Thus, \a tracefile and the array \a controlled_vars are valid
 *
 * \param argc the argument count parameter that was passed to MPI_Init
 * \param argv the arguments that were passed to MPI_Init
 */
static void after_Init(int *argc, char ***argv)
{
	/* NAME_LEN is the length of the strings that hold the rank- and threadnames.
	 * The names are integers, starting at 0, which means a small \a NAME_LEN shoult suffice.
	 */
#define NAME_LEN 10
	static __thread char hostname[HOST_NAME_MAX];
	static __thread char rankname[NAME_LEN];
	static __thread char threadname[NAME_LEN];

	static int thread_counter = 0;
	static pthread_mutex_t thread_counter_mutex = PTHREAD_MUTEX_INITIALIZER;

	int rank;

	char basename[TMP_BUF_LEN];

	char * lastSlash = strrchr(**argv , '/');
	if( lastSlash != NULL)
	{
		snprintf(basename, TMP_BUF_LEN, "%s%s", trace_file_prefix, lastSlash+1 );
	}
	else
	{
		snprintf(basename, TMP_BUF_LEN, "%s%s", trace_file_prefix, (*argv)[0] );
	}

	PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

	// find out, which thread we're in
    pthread_mutex_lock(&thread_counter_mutex);
    int thread = thread_counter;
    ++thread_counter;
    pthread_mutex_unlock(&thread_counter_mutex);

	gethostname(hostname, HOST_NAME_MAX);

	snprintf(rankname, NAME_LEN, "%d", rank);
	snprintf(threadname, NAME_LEN, "%d", thread);

	const char *toponames[3] = {"Host", "Rank", "Thread"};
	const char *levels[3] = {hostname, rankname, threadname};

	//hdTopology topology = hdT_createTopology(hostname, rankname, "0");
	hdTopology topology = hdT_createTopology(basename, toponames, 3);
	hdTopoNode topo_names = hdT_createTopoNode(levels, 3);


	tracefile = hdT_createTrace(topo_names, topology);

	readEnvVars();

 	hdT_setNestedDepth(tracefile, trace_nested_operations * HD_LOG_MAX_DEPTH); 
	hdT_setForceFlush(tracefile, trace_force_flush);

#undef NAME_LEN
}

/**
 * This function is called after \a PMPI_Finalize(...)
 * It finalizes the global \a tracefile and destroys all
 * dynamically allocated variables.
 */
static void after_Finalize(void)
{
	hdT_finalize(tracefile);
	tracefile = NULL;
	destroyHashTables();
}

/**
 * This function is called before \a PMPI_Abort(...)
 * It finalizes the global \a tracefile and destroys all
 * dynamically allocated variables.
 *
 * \param comm The comm parameter that is passed to \a MPI_Abort(...)
 * \param code The code parameter that is passed to \a MPI_Abort(...)
 */
static void before_Abort(MPI_Comm comm, int code)
{
	hdT_finalize(tracefile);
	tracefile = NULL;
	destroyHashTables();
}

