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
		snprintf(basename, TMP_BUF_LEN, "trace_%s", lastSlash+1 );
	}
	else
	{
		snprintf(basename, TMP_BUF_LEN, "trace_%s", (*argv)[0] );
	}
	
	//hdT_Init(basename);
	
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

int MPI_Send(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Send");

  ret = PMPI_Send( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Recv(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Status * v7){
  int ret;

  hdT_StateStart(tracefile, "Recv");

  ret = PMPI_Recv( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "fromRank='%d' tag='%d' comm='%d'", getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Bsend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Bsend");

  ret = PMPI_Bsend( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Ssend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Ssend");

  ret = PMPI_Ssend( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Rsend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Rsend");

  ret = PMPI_Rsend( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Isend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Isend");

  ret = PMPI_Isend( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d' request='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Ibsend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Ibsend");

  ret = PMPI_Ibsend( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d' request='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Issend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Issend");

  ret = PMPI_Issend( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d' request='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Irsend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Irsend");

  ret = PMPI_Irsend( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d' request='%d'", getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Irecv(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Irecv");

  ret = PMPI_Irecv( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "fromRank='%d' tag='%d' comm='%d'", getWorldRank(v4, v6), v5, getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Wait(MPI_Request * v1,  MPI_Status * v2){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(*v1));
  }
;
  ret = PMPI_Wait( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Test(MPI_Request * v1,  int * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Test");

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(*v1));
  }
;
  ret = PMPI_Test( v1,  v2,  v3);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Waitany(int v1,  MPI_Request * v2,  int * v3,  MPI_Status * v4){
  int ret;

  hdT_StateStart(tracefile, "Waitany");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Waitany( v1,  v2,  v3,  v4);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Testany(int v1,  MPI_Request * v2,  int * v3,  int * v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "Testany");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Testany( v1,  v2,  v3,  v4,  v5);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Waitall(int v1,  MPI_Request * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Waitall");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Waitall( v1,  v2,  v3);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Testall(int v1,  MPI_Request * v2,  int * v3,  MPI_Status * v4){
  int ret;

  hdT_StateStart(tracefile, "Testall");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Testall( v1,  v2,  v3,  v4);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Waitsome(int v1,  MPI_Request * v2,  int * v3,  int * v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "Waitsome");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Waitsome( v1,  v2,  v3,  v4,  v5);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Testsome(int v1,  MPI_Request * v2,  int * v3,  int * v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "Testsome");

  
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
;
  ret = PMPI_Testsome( v1,  v2,  v3,  v4,  v5);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Iprobe(int v1,  int v2,  MPI_Comm v3,  int * v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "Iprobe");

  ret = PMPI_Iprobe( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "source='%d' tag='%d' comm='%d'", getWorldRank(v1, v3), v2, getCommId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Probe(int v1,  int v2,  MPI_Comm v3,  MPI_Status * v4){
  int ret;

  hdT_StateStart(tracefile, "Probe");

  ret = PMPI_Probe( v1,  v2,  v3,  v4);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Cancel(MPI_Request * v1){
  int ret;

  hdT_StateStart(tracefile, "Cancel");

  ret = PMPI_Cancel( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Test_cancelled(MPI_Status * v1,  int * v2){
  int ret;

  hdT_StateStart(tracefile, "Test_cancelled");

  ret = PMPI_Test_cancelled( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Send_init(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Send_init");

  ret = PMPI_Send_init( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Bsend_init(void* v1,  int v2,  MPI_Datatype v3,  int v4, int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Bsend_init");

  ret = PMPI_Bsend_init( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Ssend_init(void* v1,  int v2,  MPI_Datatype v3,  int v4, int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Ssend_init");

  ret = PMPI_Ssend_init( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Rsend_init(void* v1,  int v2,  MPI_Datatype v3,  int v4, int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Rsend_init");

  ret = PMPI_Rsend_init( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Recv_init(void* v1,  int v2,  MPI_Datatype v3,  int v4, int v5,  MPI_Comm v6,  MPI_Request * v7){
  int ret;

  hdT_StateStart(tracefile, "Recv_init");

  ret = PMPI_Recv_init( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Start(MPI_Request * v1){
  int ret;

  hdT_StateStart(tracefile, "Start");

  ret = PMPI_Start( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Startall(int v1,  MPI_Request * v2){
  int ret;

  hdT_StateStart(tracefile, "Startall");

  ret = PMPI_Startall( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Sendrecv(void * v1,  int v2,  MPI_Datatype v3, int v4,  int v5,  void * v6,  int v7,  MPI_Datatype v8,  int v9,  int v10,  MPI_Comm v11,  MPI_Status * v12){
  int ret;

  hdT_StateStart(tracefile, "Sendrecv");

  ret = PMPI_Sendrecv( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8,  v9,  v10,  v11,  v12);

  hdT_LogAttributes(tracefile, "size='%lld' toRank='%d' to-tag='%d' fromRank='%d' fromTag='%d' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), getWorldRank(v4, v11), v5, getWorldRank(v9, v11), v10, getCommId(v11), v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Sendrecv_replace(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  int v6,  int v7,  MPI_Comm v8,  MPI_Status * v9){
  int ret;

  hdT_StateStart(tracefile, "Sendrecv_replace");

  ret = PMPI_Sendrecv_replace( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8,  v9);

  hdT_LogAttributes(tracefile, "sendSize='%lld' toRank='%d' to-tag='%d' fromRank='%d' fromTag='%d' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), getWorldRank(v4, v8), v5, getWorldRank(v6, v8), v7, getCommId(v8), v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Barrier(MPI_Comm  v1){
  int ret;

  hdT_StateStart(tracefile, "Barrier");

  ret = PMPI_Barrier( v1);

  hdT_LogAttributes(tracefile, "comm='%d'", getCommId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Bcast(void* v1,  int v2,  MPI_Datatype v3,  int v4,  MPI_Comm  v5){
  int ret;

  hdT_StateStart(tracefile, "Bcast");

  ret = PMPI_Bcast( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "size='%lld' rootRank='%d' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), getWorldRank(v4, v5), getCommId(v5), v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Gather(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int v5,  MPI_Datatype v6,  int v7,  MPI_Comm v8){
  int ret;

  hdT_StateStart(tracefile, "Gather");

  ret = PMPI_Gather( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8);

  hdT_LogAttributes(tracefile, "size='%lld' recvSize='%lld' root='%d' comm='%d' count='%d' type='%d' recvCount='%d' recvType='%d'", getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Gatherv(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int * v5,  int * v6,  MPI_Datatype v7,  int v8,  MPI_Comm v9){
  int ret;

  hdT_StateStart(tracefile, "Gatherv");

  ret = PMPI_Gatherv( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8,  v9);

  hdT_LogAttributes(tracefile, "size='%lld' root='%d' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), getWorldRank(v8, v9), getCommId(v9), v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Scatter(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int v5,  MPI_Datatype v6,  int v7,  MPI_Comm v8){
  int ret;

  hdT_StateStart(tracefile, "Scatter");

  ret = PMPI_Scatter( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8);

  hdT_LogAttributes(tracefile, "size='%lld' recvSize='%lld' root='%d' comm='%d' count='%d' type='%d' recvCount'%d' recvType='%d'", getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Scatterv(void*  v1,  int * v2,  int * v3,   MPI_Datatype v4,  void* v5,  int v6,  MPI_Datatype v7,  int v8,  MPI_Comm v9){
  int ret;

  hdT_StateStart(tracefile, "Scatterv");

  ret = PMPI_Scatterv( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8,  v9);

  hdT_LogAttributes(tracefile, "recvSize='%lld' root='%d' comm='%d' recvCount='%d' recvType='%d'", getTypeSize(v6, v7), getWorldRank(v8, v9), getCommId(v9), v6, getTypeId(v7));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Allgather(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int v5,  MPI_Datatype v6,  MPI_Comm v7){
  int ret;

  hdT_StateStart(tracefile, "Allgather");

  ret = PMPI_Allgather( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' recvSize='%lld' comm='%d' count='%d' type='%d' recvCount='%d' recvType='%d'", getTypeSize(v2, v3), getTypeSize(v5, v6), getCommId(v7), v2, getTypeId(v3), v5, getTypeId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Allgatherv(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int * v5,  int * v6,  MPI_Datatype v7,  MPI_Comm v8){
  int ret;

  hdT_StateStart(tracefile, "Allgatherv");

  ret = PMPI_Allgatherv( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8);

  hdT_LogAttributes(tracefile, "size='%lld' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), getCommId(v8), v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Alltoall(void*  v1,  int v2,  MPI_Datatype v3,  void* v4,  int v5,  MPI_Datatype v6,  MPI_Comm v7){
  int ret;

  hdT_StateStart(tracefile, "Alltoall");

  ret = PMPI_Alltoall( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' comm='%d' count='%d' type='%d'", getTypeSize(v2, v3), v7, v2, getTypeId(v3));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Alltoallv(void*  v1,  int * v2,  int * v3,  MPI_Datatype v4,  void* v5,  int * v6,  int * v7,  MPI_Datatype v8,  MPI_Comm v9){
  int ret;

  hdT_StateStart(tracefile, "Alltoallv");

  ret = PMPI_Alltoallv( v1,  v2,  v3,  v4,  v5,  v6,  v7,  v8,  v9);

  
  {
    int size, i;
    MPI_Comm_size(v9, &size);
    for(i = 0; i < size; ++i)
    {
      hdT_LogElement(tracefile, "Send", "rank='%d' size='%lld' count='%d' type='%d'",
                   getWorldRank(i, v9), getTypeSize(v2[i], v4), v2[i], getTypeId(v4));
    }
  }
;
  hdT_LogAttributes(tracefile, "comm='%d'", getCommId(v9));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Reduce(void*  v1,  void* v2,  int v3,  MPI_Datatype v4,  MPI_Op v5,  int v6,  MPI_Comm v7){
  int ret;

  hdT_StateStart(tracefile, "Reduce");

  ret = PMPI_Reduce( v1,  v2,  v3,  v4,  v5,  v6,  v7);

  hdT_LogAttributes(tracefile, "size='%lld' rootRank='%d' comm='%d' count='%d' type='%d'", getTypeSize(v3, v4), getWorldRank(v6, v7), getCommId(v7), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Allreduce(void*  v1,  void* v2,  int v3,  MPI_Datatype v4,  MPI_Op v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Allreduce");

  ret = PMPI_Allreduce( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' comm='%d' count='%d' type='%d'", getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Reduce_scatter(void*  v1,  void* v2,  int * v3,  MPI_Datatype v4,  MPI_Op v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Reduce_scatter");

  ret = PMPI_Reduce_scatter( v1,  v2,  v3,  v4,  v5,  v6);

  
  {
    int size, i;
    MPI_Comm_size(v6, &size);
    for(i = 0; i < size; ++i)
    {
      hdT_LogElement(tracefile, "Recv", "rank='%d' size='%lld' count='%d' type='%d'",
                   getWorldRank(i, v6), getTypeSize(v3[i], v4), v3[i], v4);
    }             
  }
;
  hdT_LogAttributes(tracefile, "comm='%d'", getCommId(v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Scan(void*  v1,  void* v2,  int v3,  MPI_Datatype v4,  MPI_Op v5,  MPI_Comm  v6){
  int ret;

  hdT_StateStart(tracefile, "Scan");

  ret = PMPI_Scan( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' comm='%d' count='%d' type='%d'", getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Init(int * v1,  char *** v2){
  int ret;

  ret = PMPI_Init( v1,  v2);

  after_Init(v1, v2);
  return ret;
}

int MPI_Init_thread(int * v1,  char *** v2,  int v3,  int * v4){
  int ret;

  hdT_StateStart(tracefile, "Init_thread");

  ret = PMPI_Init_thread( v1,  v2,  v3,  v4);

  
   {
     if(v3 != MPI_THREAD_SINGLE )
     {
       printf("Init_thread: multithreading currently not supported by HDTrace\n");
       return -1;
     }
   }
;
  hdT_StateEnd(tracefile);

  after_Init(v1, v2);
  return ret;
}

int MPI_Finalize(void){
  int ret;

  hdT_StateStart(tracefile, "Finalize");

  ret = PMPI_Finalize();

  hdT_StateEnd(tracefile);

  after_Finalize();
  return ret;
}

int MPI_Abort(MPI_Comm v1,  int v2){
  int ret;

  before_Abort(v1, v2);
  ret = PMPI_Abort( v1,  v2);

  return ret;
}

int MPI_Comm_set_name(MPI_Comm v1,  char * v2){
  int ret;

  hdT_StateStart(tracefile, "Comm_set_name");

  ret = PMPI_Comm_set_name( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_complete(MPI_Win v1){
  int ret;

  hdT_StateStart(tracefile, "Win_complete");

  ret = PMPI_Win_complete( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_fence(int v1,  MPI_Win v2){
  int ret;

  hdT_StateStart(tracefile, "Win_fence");

  ret = PMPI_Win_fence( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_free(MPI_Win * v1){
  int ret;

  hdT_StateStart(tracefile, "Win_free");

  ret = PMPI_Win_free( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_get_group(MPI_Win v1,  MPI_Group * v2){
  int ret;

  hdT_StateStart(tracefile, "Win_get_group");

  ret = PMPI_Win_get_group( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_lock(int v1,  int v2,  int v3,  MPI_Win v4){
  int ret;

  hdT_StateStart(tracefile, "Win_lock");

  ret = PMPI_Win_lock( v1,  v2,  v3,  v4);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_post(MPI_Group v1,  int v2,  MPI_Win v3){
  int ret;

  hdT_StateStart(tracefile, "Win_post");

  ret = PMPI_Win_post( v1,  v2,  v3);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_start(MPI_Group v1,  int v2,  MPI_Win v3){
  int ret;

  hdT_StateStart(tracefile, "Win_start");

  ret = PMPI_Win_start( v1,  v2,  v3);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_test(MPI_Win v1,  int * v2){
  int ret;

  hdT_StateStart(tracefile, "Win_test");

  ret = PMPI_Win_test( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_unlock(int v1,  MPI_Win v2){
  int ret;

  hdT_StateStart(tracefile, "Win_unlock");

  ret = PMPI_Win_unlock( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Win_wait(MPI_Win v1){
  int ret;

  hdT_StateStart(tracefile, "Win_wait");

  ret = PMPI_Win_wait( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Exscan(void * v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Op v5,  MPI_Comm v6){
  int ret;

  hdT_StateStart(tracefile, "Exscan");

  ret = PMPI_Exscan( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "size='%lld' comm='%d' count='%d' type='%d'", getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Grequest_complete(MPI_Request v1){
  int ret;

  hdT_StateStart(tracefile, "Grequest_complete");

  ret = PMPI_Grequest_complete( v1);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_open(MPI_Comm v1,  char * v2,  int v3,  MPI_Info v4,  MPI_File * v5){
  int ret;

  hdT_StateStart(tracefile, "File_open");

  ret = PMPI_File_open( v1,  v2,  v3,  v4,  v5);

  
  {
    MPI_Offset fileSize;
    PMPI_File_get_size(*v5, &fileSize);
  }

  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    if(trace_file_info && !((v4) == MPI_INFO_NULL))
    {
    PMPI_Info_get_nkeys((v4), &nkeys);
    for(i = 0; i < nkeys; ++i)
    {
      MPI_Info_get_nthkey((v4), i, key);
      hdT_LogElement(tracefile, "Info", "value='%s'", key);
    }
    }
  }
  ;
  hdT_LogAttributes(tracefile, "comm='%d' name='%s' flags='%d' file='%d'", getCommId(v1), v2, v3, getFileIdEx(*v5, v2));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_close(MPI_File * v1){
  int ret;

  hdT_StateStart(tracefile, "File_close");

  
  gint pre_close_id = getFileId(*v1);
  removeFileHandle(*v1);
;
  ret = PMPI_File_close( v1);

  hdT_LogAttributes(tracefile, "file='%d'", pre_close_id);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_delete(char * v1,  MPI_Info v2){
  int ret;

  hdT_StateStart(tracefile, "File_delete");

  
  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    if(trace_file_info && !((v2) == MPI_INFO_NULL))
    {
    PMPI_Info_get_nkeys((v2), &nkeys);
    for(i = 0; i < nkeys; ++i)
    {
      MPI_Info_get_nthkey((v2), i, key);
      hdT_LogElement(tracefile, "Info", "value='%s'", key);
    }
    }
  }
  ;
  ret = PMPI_File_delete( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d'", getFileIdFromName(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_set_size(MPI_File v1,  MPI_Offset v2){
  int ret;

  hdT_StateStart(tracefile, "File_set_size");

  ret = PMPI_File_set_size( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld'", getFileId(v1), (long long int)v2);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_get_size(MPI_File v1,  MPI_Offset * v2){
  int ret;

  hdT_StateStart(tracefile, "File_get_size");

  ret = PMPI_File_get_size( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld'", getFileId(v1), (long long int)*v2);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_preallocate(MPI_File v1,  MPI_Offset v2){
  int ret;

  hdT_StateStart(tracefile, "File_preallocate");

  ret = PMPI_File_preallocate( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld'", getFileId(v1), (long long int)v2);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_read( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_all(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_read_all( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_write");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_write( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_at(MPI_File v1,  MPI_Offset v2,  void*  v3,  int v4,  MPI_Datatype v5,  MPI_Status * v6){
  int ret;

  hdT_StateStart(tracefile, "File_write");

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  ret = PMPI_File_write_at( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_at_all(MPI_File v1,  MPI_Offset v2,  void*  v3,  int v4,  MPI_Datatype v5,  MPI_Status * v6){
  int ret;

  hdT_StateStart(tracefile, "File_write_all");

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  ret = PMPI_File_write_at_all( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_at(MPI_File v1,  MPI_Offset v2,  void*  v3,  int v4,  MPI_Datatype v5,  MPI_Status * v6){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  ret = PMPI_File_read_at( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_at_all(MPI_File v1,  MPI_Offset v2,  void*  v3,  int v4,  MPI_Datatype v5,  MPI_Status * v6){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  ret = PMPI_File_read_at_all( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_all(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_write_all");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_write_all( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_iread(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPIO_Request * v5){
  int ret;

  hdT_StateStart(tracefile, "FileIread");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_iread( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' request='%d'", getFileId(v1), getRequestId(*v5));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_iwrite(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPIO_Request * v5){
  int ret;

  hdT_StateStart(tracefile, "FileIwrite");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_iwrite( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' request='%d'", getFileId(v1), getRequestId(*v5));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_iwrite_at(MPI_File  v1, MPI_Offset  v2, void * v3, int  v4, MPI_Datatype  v5,  MPI_Request * v6){
  int ret;

  hdT_StateStart(tracefile, "FileIwrite");

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  ret = PMPI_File_iwrite_at( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d' request='%d'", getFileId(v1), getRequestId(*v6));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_seek(MPI_File v1,  MPI_Offset v2,  int v3){
  int ret;

  hdT_StateStart(tracefile, "File_seek");

  ret = PMPI_File_seek( v1,  v2,  v3);

  hdT_LogAttributes(tracefile, "file='%d' relative-offset='%lld' whence='%s' offset='%lld'", getFileId(v1), (long long int)v2, getWhenceString(v3), getByteOffset(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_get_position(MPI_File v1,  MPI_Offset * v2){
  int ret;

  hdT_StateStart(tracefile, "File_get_position");

  ret = PMPI_File_get_position( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_shared(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_read_shared");

  ret = PMPI_File_read_shared( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld' count='%d' type='%d'", getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_shared(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_write_shared");

  ret = PMPI_File_write_shared( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld' count='%d' type='%d'", getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_ordered(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_read_ordered");

  ret = PMPI_File_read_ordered( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld' count='%d' type='%d'", getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_ordered(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4,  MPI_Status * v5){
  int ret;

  hdT_StateStart(tracefile, "File_write_ordered");

  
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
;
  ret = PMPI_File_write_ordered( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "file='%d' size='%lld' count='%d' type='%d'", getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_seek_shared(MPI_File v1,  MPI_Offset v2,  int v3){
  int ret;

  hdT_StateStart(tracefile, "File_seek_shared");

  ret = PMPI_File_seek_shared( v1,  v2,  v3);

  hdT_LogAttributes(tracefile, "file='%d' relative-offset='%lld' whence='%s' offset='%lld'", getFileId(v1), (long long int)v2, getWhenceString(v3), getByteOffset(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_get_position_shared(MPI_File v1,  MPI_Offset * v2){
  int ret;

  hdT_StateStart(tracefile, "File_get_position_shared");

  ret = PMPI_File_get_position_shared( v1,  v2);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_at_all_begin(MPI_File v1, 	MPI_Offset  v2,  void * v3,  int  v4,  MPI_Datatype v5){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

  ret = PMPI_File_read_at_all_begin( v1,  v2,  v3,  v4,  v5);

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_at_all_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_read_at_all_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_at_all_begin(MPI_File  v1,  MPI_Offset v2,  void * v3,  int v4,  MPI_Datatype  v5){
  int ret;

  hdT_StateStart(tracefile, "File_write");

  ret = PMPI_File_write_at_all_begin( v1,  v2,  v3,  v4,  v5);

  
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_at_all_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_write_at_all_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_all_begin(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4){
  int ret;

  hdT_StateStart(tracefile, "FileRead");

    long long int byte_offset = getByteOffset(v1);;
  ret = PMPI_File_read_all_begin( v1,  v2,  v3,  v4);

  
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  ;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_all_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_read_all_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_all_begin(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4){
  int ret;

  hdT_StateStart(tracefile, "File_write_all");

    long long int byte_offset = getByteOffset(v1);;
  ret = PMPI_File_write_all_begin( v1,  v2,  v3,  v4);

  
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  ;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_all_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_write_all_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_ordered_begin(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4){
  int ret;

  hdT_StateStart(tracefile, "FileWrite");

    long long int byte_offset = getByteOffset(v1);;
  ret = PMPI_File_read_ordered_begin( v1,  v2,  v3,  v4);

  
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  ;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_read_ordered_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_read_ordered_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_ordered_begin(MPI_File v1,  void * v2,  int v3,  MPI_Datatype v4){
  int ret;

  hdT_StateStart(tracefile, "FileWrite");

    long long int byte_offset = getByteOffset(v1);;
  ret = PMPI_File_write_ordered_begin( v1,  v2,  v3,  v4);

  
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' type='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  ;
  hdT_LogAttributes(tracefile, "file='%d' aid='%d'", getFileId(v1), getRequestIdForSplit(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_write_ordered_end(MPI_File v1,  void * v2,  MPI_Status * v3){
  int ret;

  hdT_StateStart(tracefile, "Wait");

  ret = PMPI_File_write_ordered_end( v1,  v2,  v3);

  
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
;
  hdT_LogAttributes(tracefile, "");
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_get_type_extent(MPI_File v1,  MPI_Datatype v2,  MPI_Aint * v3){
  int ret;

  hdT_StateStart(tracefile, "File_get_type_extent");

  ret = PMPI_File_get_type_extent( v1,  v2,  v3);

  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_set_atomicity(MPI_File v1,  int v2){
  int ret;

  hdT_StateStart(tracefile, "File_set_atomicity");

  ret = PMPI_File_set_atomicity( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d' flag='%d'", getFileId(v1), v2);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_sync(MPI_File v1){
  int ret;

  hdT_StateStart(tracefile, "File_sync");

  ret = PMPI_File_sync( v1);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_set_info(MPI_File v1,  MPI_Info v2){
  int ret;

  hdT_StateStart(tracefile, "File_set_info");

  
  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    if(trace_file_info && !((v2) == MPI_INFO_NULL))
    {
    PMPI_Info_get_nkeys((v2), &nkeys);
    for(i = 0; i < nkeys; ++i)
    {
      MPI_Info_get_nthkey((v2), i, key);
      hdT_LogElement(tracefile, "Info", "value='%s'", key);
    }
    }
  }
  ;
  ret = PMPI_File_set_info( v1,  v2);

  hdT_LogAttributes(tracefile, "file='%d'", getFileId(v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Pcontrol(const int  v1,  ...){
  int ret;

  hdT_StateStart(tracefile, "Pcontrol");

  ret = PMPI_Pcontrol( v1);

  
  {
    if(v1 == 0) {
      hdT_Enable(tracefile, 0);
    }
    else if(v1 == 1) {
      hdT_Enable(tracefile, 1);
      hdT_ForceFlush(tracefile, 0);
    }
    else {
      hdT_Enable(tracefile, 1);
      hdT_ForceFlush(tracefile, 1);
    }
  }
;
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Type_vector(int v1,  int v2,  int v3,  MPI_Datatype v4,  MPI_Datatype * v5){
  int ret;

  hdT_StateStart(tracefile, "Type_vector");

  ret = PMPI_Type_vector( v1,  v2,  v3,  v4,  v5);

  hdT_LogAttributes(tracefile, "from_type='%d'", getTypeId(v4));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_hdT_Test_nested(int v1,  int v2){
  int ret;

  hdT_StateStart(tracefile, "hdT_Test_nested");

  ret = PMPI_hdT_Test_nested( v1,  v2);

  hdT_LogAttributes(tracefile, "depth='%d'", v1);
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_Type_commit(MPI_Datatype * v1){
  int ret;

  hdT_StateStart(tracefile, "Type_commit");

  ret = PMPI_Type_commit( v1);

  hdT_LogAttributes(tracefile, "type='%d'", getTypeId(*v1));
  hdT_StateEnd(tracefile);

  return ret;
}

int MPI_File_set_view(MPI_File v1,  MPI_Offset v2,  MPI_Datatype v3,  MPI_Datatype v4,  char * v5,  MPI_Info v6){
  int ret;

  hdT_StateStart(tracefile, "File_set_view");

  
  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    if(trace_file_info && !((v6) == MPI_INFO_NULL))
    {
    PMPI_Info_get_nkeys((v6), &nkeys);
    for(i = 0; i < nkeys; ++i)
    {
      MPI_Info_get_nthkey((v6), i, key);
      hdT_LogElement(tracefile, "Info", "value='%s'", key);
    }
    }
  }
  ;
  ret = PMPI_File_set_view( v1,  v2,  v3,  v4,  v5,  v6);

  hdT_LogAttributes(tracefile, "file='%d' offset='%lld' etype='%d' filetype='%d' representation='%s'", getFileId(v1), (long long int)v2, getTypeId(v3), getTypeId(v4), v5);
  hdT_StateEnd(tracefile);

  return ret;
}

