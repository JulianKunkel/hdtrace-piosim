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
#include <mpio.h>
#include <pthread.h>

#include <limits.h>
#include <stdlib.h>

#include <stdio.h>
#include <string.h>
#include <glib.h>

#include <assert.h>
#include <unistd.h>
#include <errno.h>

#include "hdTrace.h"
#include "hdTopo.h"

static __thread hdTrace tracefile = NULL;

#define TMP_BUF_LEN 1024 * 16

static __thread char cnbuff[TMP_BUF_LEN];

// TODO: these are not thread-safe yet

static pthread_mutex_t envvar_mutex = PTHREAD_MUTEX_INITIALIZER;
static int envvar_read = 0;

static int trace_all_functions = 1;
static int trace_nested_operations = 1;
static int trace_file_info = 1;
static int trace_force_flush = 0;

static const char * control_vars[] = { "HDTRACE_ALL_FUNCTIONS", 
								"HDTRACE_NESTED",
								"HDTRACE_FILE_INFO",
								"HDTRACE_FORCE_FLUSH",
								NULL };

static int * controlled_vars[] = { &trace_all_functions,
								   &trace_nested_operations, 
								   &trace_file_info,
								   &trace_force_flush,
								   NULL };

static size_t hdT_min(size_t a, size_t b)
{
	if(a < b)
		return a;
	return b;
}

#include "mpi_names.c"
#include "hash_tables.c"
#include "write_info.c"
#ifdef  HDTRACE_INCLUDE_NESTED_TEST
#include "test_nested.c"
#endif


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



static long long getTypeSize(int count, MPI_Datatype type)
{
  int t_size;
  MPI_Type_size(type, & t_size);
  return (count * (long long) t_size );
}

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

static void readEnvvars()
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
					//tprintf(tracefile, "environment variable %s has unrecognised value of %s",
					//		control_vars[ii], env_var );
				}
			}
			ii++;
		}
		envvar_read = 1;
	}
	pthread_mutex_unlock(&envvar_mutex);
}

static void after_Init(int *argc, char ***argv)
{
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

	char *toponames[3] = {"Host", "Rank", "Thread"};
	char *levels[3] = {hostname, rankname, threadname};

	//hdTopology topology = hdT_createTopology(hostname, rankname, "0");
	hdTopology topology = hdT_createTopology(basename, toponames, 3);
	hdTopoNode topo_names = hdT_createTopoNode(levels, 3);


	tracefile = hdT_createTrace(topo_names, topology);

	//if(tracefile == NULL)
	//     printf("tracefile == NULL, errno=%d\n", errno);

	readEnvvars();


	hdT_TraceNested(tracefile, trace_nested_operations);
	hdT_ForceFlush(tracefile, trace_force_flush);

#undef NAME_LEN
}

static void after_Finalize(void)
{
	hdT_Finalize(tracefile);
	tracefile = NULL;
	destroyHashTables();
}

static void before_Abort(MPI_Comm comm, int code)
{
	hdT_Finalize(tracefile);
	tracefile = NULL;
	destroyHashTables();
}

