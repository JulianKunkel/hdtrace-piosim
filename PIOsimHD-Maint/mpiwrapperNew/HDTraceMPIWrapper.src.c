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

#include <stdio.h>
#include <string.h>

#include "HDTraceWriter.h"

__thread TraceFileP tracefile = NULL;

#define TMP_BUF_LEN 1024 * 16

__thread char cnbuff[TMP_BUF_LEN];

static char * getCommName(MPI_Comm comm)
{
	// NOTE: the result becomes invalid after a consecutive
    // call to getCommName(...)
  int len = TMP_BUF_LEN;
  int cmp = 0;
  MPI_Comm_compare(comm, MPI_COMM_WORLD, & cmp);
  if (cmp == MPI_IDENT){
    return "WORLD";
  }
  MPI_Comm_get_name(comm, cnbuff, & len);
  return cnbuff;
}

inline static long long getTypeSize(int count, MPI_Datatype type)
{
  int t_size;
  MPI_Type_size(type, & t_size);
  return (count * (long long) t_size );
}

void after_Init(int *argc, char ***argv)
{
	int rank;

	char basename[TMP_BUF_LEN];

	char * lastSlash = strrchr(**argv , '/');
	if( lastSlash != NULL)
	{
		snprintf(basename, TMP_BUF_LEN, "trace-%s", lastSlash+1 );
	}
	else
	{
		snprintf(basename, TMP_BUF_LEN, "trace-%s", (*argv)[0]+2 );
	}
	
	hdTraceInit(basename);
	
	PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

	tracefile = hdTraceCreate(rank, pthread_self());
}

void after_Finalize(void)
{
	hdTraceFinalize(tracefile);
	tracefile = NULL;
}

void before_Abort(MPI_Comm comm, int code)
{
	hdTraceFinalize(tracefile);
	tracefile = NULL;
}

