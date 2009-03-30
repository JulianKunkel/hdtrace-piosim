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

#include "HDTraceWriter.h"

static __thread TraceFileP tracefile = NULL;

#define TMP_BUF_LEN 1024 * 16

static __thread char cnbuff[TMP_BUF_LEN];

// Traces all functions, even those without a custom logging routine
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

int MPI_hdT_Test_nested(int rec, int max);

static int PMPI_hdT_Test_nested(int rec, int max)
{
	if(rec < max)
	{
		MPI_hdT_Test_nested(rec + 1, max);
		MPI_hdT_Test_nested(rec + 1, max);
	}

	return 0;
}


__thread GHashTable *comm_to_id = NULL;
__thread gint comm_id_counter = 0;

static void writeCommInfo(MPI_Comm comm, gint comm_id)
{
	int size;
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	int *ranks_world = malloc(size * sizeof(int));
	int i;

	for(i = 0; i < size; ++i)
	{
		ranks_world[i] = i;
	}
	int *ranks_comm = malloc(size * sizeof(int));

	MPI_Group world_group, comm_group;
	MPI_Comm_group(MPI_COMM_WORLD, &world_group);
	MPI_Comm_group(comm, &comm_group);
	
	MPI_Group_translate_ranks(world_group, size, ranks_world, comm_group, ranks_comm);
	
	char buffer[TMP_BUF_LEN];
	size_t position = 0;
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "Comm map='");
	position = hdT_min(position, TMP_BUF_LEN);

	for(i = 0; i < size; ++i)
	{
		if(ranks_comm[i] != MPI_UNDEFINED ) 
		{
			position += snprintf(buffer + position, TMP_BUF_LEN - position, "%d->%d;", i, ranks_comm[i]);
			position = hdT_min(position, TMP_BUF_LEN);
		}
	}
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "' id=%d name='%s'\n", comm_id, getCommName(comm));
	position = hdT_min(position, TMP_BUF_LEN);

	hdT_LogInfo(tracefile, buffer);
}

static gint getCommId(MPI_Comm comm)
{
	if(comm_to_id == NULL)
	{
		comm_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}	
	gpointer result = g_hash_table_lookup(comm_to_id, &comm);
	if(result == NULL)
	{
		gint * g_comm = malloc(sizeof(gint));
		*g_comm = (gint)comm;
		gint * g_id = malloc(sizeof(gint));
		*g_id = (gint)comm_id_counter;
		g_hash_table_insert(comm_to_id, g_comm, g_id);
		comm_id_counter++;

		writeCommInfo(comm, *g_id);
		return *g_id;
	}
	return *(gint*)result;
}

__thread GHashTable *file_handle_to_id = NULL;
__thread GHashTable *file_name_to_id = NULL;
__thread gint file_id_counter = 0;

static gint getFileId(MPI_File fh)
{
	if(file_handle_to_id == NULL)
	{
		file_handle_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}	
	gpointer result = g_hash_table_lookup(file_handle_to_id, &fh);
	if(result == NULL)
	{
		gint * g_handle = malloc(sizeof(gint));
		*g_handle = (gint)fh;
		gint * g_id = malloc(sizeof(gint));
		*g_id = (gint)file_id_counter;
		g_hash_table_insert(file_handle_to_id, g_handle, g_id);
		file_id_counter++;

		tprintf(tracefile, "getFileId: got File handle without associated id. new id=%d", (*g_id));

		return *g_id;
	}
	return *(gint*)result;
}

static gint getFileIdFromName(const char * name)
{
	if(file_name_to_id == NULL)
	{
		file_name_to_id = g_hash_table_new_full(g_str_hash, g_str_equal, free, free);
	}

	
	/* NOTE: it would be nice to canonicalize the file name, e.g. with 
	 * nrealpath(name, NULL);. Unfortunately, this doesn't work with names 
     * beginning with "pvfs://"
	 */ 
	char * real_path = g_strdup(name);

	gpointer result =  g_hash_table_lookup(file_name_to_id, real_path); 
	if(result == NULL)
	{
		gint *g_id = malloc(sizeof(gint));
		*g_id = file_id_counter;
		g_hash_table_insert(file_name_to_id, real_path, g_id);
		file_id_counter++;

		hdT_LogInfo(tracefile, 
						"File name=\"%s\" Size=0 id=%d\n",
						name, *g_id);

		return *g_id;
	}
	return *(gint*)result;
}

static void removeHandle(MPI_File fh)
{
	g_hash_table_remove(file_handle_to_id, &fh);
}

/**
 * This function should be used, whenever a file is acessed by name and its 
 * file pointer is known (File_open...)
 * If the file has been accessed before (by name), the same id can
 * be associated again.
 *
 * If the file has not yet been opened, an entry is written to the trace file
 * 
 */
static gint getFileIdEx(MPI_File fh, const char * name)
{
	if(file_name_to_id == NULL)
	{
		file_name_to_id = g_hash_table_new_full(g_str_hash, g_str_equal, free, free);
	}
	if(file_handle_to_id == NULL)
	{
		file_handle_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}	

	/* NOTE: it would be nice to canonicalize the file name, e.g. with 
	 * nrealpath(name, NULL);. Unfortunately, this doesn't work with names 
     * beginning with "pvfs://"
	 */ 
	char * real_path = g_strdup(name);

	gpointer name_result = g_hash_table_lookup(file_name_to_id, real_path); 
	gpointer handle_result = g_hash_table_lookup(file_handle_to_id, &fh);

	if(name_result == NULL) //don't know the file
	{
		if(handle_result != NULL)
		{
			//the file has a handle without us knowing the name. this is bad
			tsprintf(tracefile, "getFileIdEx(...): file handle without matching filename");
			return -1;
		}
		else
		{
			// this is a new file
			gint *g_id = malloc(sizeof(gint)); 
			gint *g_id2 = malloc(sizeof(gint)); // this makes it easier to automatically free the memory
			gint *g_fh = malloc(sizeof(gint));
			assert(sizeof(gint) >= sizeof(MPI_File));
			
			*g_fh = (gint)fh;
			*g_id = file_id_counter;
			*g_id2 = file_id_counter;

			g_hash_table_insert(file_name_to_id, real_path, g_id);
			g_hash_table_insert(file_handle_to_id, g_fh, g_id2);

			long long int fileSize;
			PMPI_File_get_size(fh, &fileSize);
			hdT_LogInfo(tracefile, 
						"File name=\"%s\" Size=%lld id=%d\n",
						name, fileSize, *g_id);
			
			file_id_counter++;

			return *g_id;
		}
	}
	else
	{
		// know the file
		if(handle_result != NULL)
		{
			// file probably has been opened before...
			if( *(gint*)name_result == *(gint*)handle_result )
			{
				return *(gint*)name_result;
			}
			else
			{
				// fix the fh -> id mapping and warn 
				gint *g_id = malloc(sizeof(gint));
				gint *g_fh = malloc(sizeof(gint));
				*g_id = *(gint*)name_result;
				*g_fh = *(gint*)fh;

				tprintf(tracefile, "file handle map and file name map do not match. id=%d", *g_id);

				g_hash_table_insert(file_handle_to_id, g_fh, g_id);
				return *g_id;
			}
		}
		else
		{
			// know the file, don't know the handle
			// insert the fh -> id mapping
			gint *g_id = malloc(sizeof(gint));
			gint *g_fh = malloc(sizeof(gint));
			*g_id = *(gint*)name_result;
			*g_fh = *(gint*)fh;

			g_hash_table_insert(file_handle_to_id, g_fh, g_id);
			return *g_id;
		}
	}
}

static const char * getCombinerName(int combiner)
{
	if(combiner == MPI_COMBINER_NAMED)
		return "MPI_COMBINER_NAMED";
	else if(combiner == MPI_COMBINER_DUP)
		return "MPI_COMBINER_DUP";
	else if(combiner == MPI_COMBINER_CONTIGUOUS)
		return "MPI_COMBINER_CONTIGUOUS";
	else if(combiner == MPI_COMBINER_VECTOR)
		return "MPI_COMBINER_VECTOR";
	else if(combiner == MPI_COMBINER_HVECTOR_INTEGER)
		return "MPI_COMBINER_HVECTOR_INTEGER";
	else if(combiner == MPI_COMBINER_HVECTOR)
		return "MPI_COMBINER_HVECTOR";
	else if(combiner == MPI_COMBINER_INDEXED)
		return "MPI_COMBINER_INDEXED";
	else if(combiner == MPI_COMBINER_HINDEXED_INTEGER)
		return "MPI_COMBINER_HINDEXED_INTEGER";
	else if(combiner == MPI_COMBINER_HINDEXED)
		return "MPI_COMBINER_HINDEXED";
	else if(combiner == MPI_COMBINER_INDEXED_BLOCK)
		return "MPI_COMBINER_INDEXED_BLOCK";
	else if(combiner == MPI_COMBINER_STRUCT_INTEGER)
		return "MPI_COMBINER_STRUCT_INTEGER";
	else if(combiner == MPI_COMBINER_STRUCT)
		return "MPI_COMBINER_STRUCT";
	else if(combiner == MPI_COMBINER_SUBARRAY)
		return "MPI_COMBINER_SUBARRAY";
	else if(combiner == MPI_COMBINER_DARRAY)
		return "MPI_COMBINER_DARRAY";
	else if(combiner == MPI_COMBINER_F90_REAL)
		return "MPI_COMBINER_F90_REAL";
	else if(combiner == MPI_COMBINER_F90_COMPLEX)
		return "MPI_COMBINER_F90_COMPLEX";
	else if(combiner == MPI_COMBINER_F90_INTEGER)
		return "MPI_COMBINER_F90_INTEGER";
	else if(combiner == MPI_COMBINER_RESIZED)
		return "MPI_COMBINER_RESIZED";
	else
		return "UNKNOWN";
}

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


static gint getTypeId(MPI_Datatype type);

static void writeTypeInfo(MPI_Datatype type, gint id)
{
	int max_integers, 
		max_addresses, 
		max_datatypes, 
		combiner;

	MPI_Type_get_envelope(type, &max_integers, &max_addresses, &max_datatypes, &combiner);

	char typename[MPI_MAX_OBJECT_NAME];
	int resultlen;
	MPI_Type_get_name(type, typename, &resultlen);

	if(combiner == MPI_COMBINER_NAMED) 	// cannot call get_contents on this
	{
		hdT_LogInfo(tracefile, "Type id='%d' combiner='%s' name='%s'\n",
				  (int)id, getCombinerName(combiner), typename);
	}
	else
	{
		int *integers = malloc(sizeof(int) * max_integers);
		MPI_Aint *addresses = malloc(sizeof(MPI_Aint) * max_addresses);
		MPI_Datatype *datatypes = malloc(sizeof(MPI_Datatype) * max_datatypes);
		MPI_Type_get_contents(type, max_integers, max_addresses, max_datatypes, 
							  integers, addresses, datatypes);


		char buffer[TMP_BUF_LEN];
		int pos = 0;
		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"Type id='%d' combiner='%s' name='%s' integers='", (int)id, getCombinerName(combiner), typename);
		pos = hdT_min(pos, TMP_BUF_LEN);

		int i;
		for(i = 0; i < max_integers; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%d;", integers[i] );
			pos = hdT_min(pos, TMP_BUF_LEN);
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"' addresses='");
		pos = hdT_min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_addresses; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%lld;", (long long int)addresses[i] );
			pos = hdT_min(pos, TMP_BUF_LEN);
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"' types='");
		pos = hdT_min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_datatypes; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%d;", getTypeId(datatypes[i]) );
			pos = hdT_min(pos, TMP_BUF_LEN);
		}
		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"'\n");
		pos = hdT_min(pos, TMP_BUF_LEN);

		hdT_LogInfo(tracefile, buffer);

		free(integers);
		free(addresses);
		free(datatypes);
	}
}

__thread GHashTable *type_table = NULL;
__thread gint type_table_result = 1;

static gint getTypeId(MPI_Datatype type)
{
	if(type_table == NULL)
	{
		type_table = g_hash_table_new_full(g_int_hash, g_int_equal, free, NULL);
	}
	gpointer result = g_hash_table_lookup(type_table, &type);
	if(result == NULL)
	{
		gint * g_type = malloc(sizeof(gint));
		*g_type = (gint)type;
		g_hash_table_insert(type_table, g_type, &type_table_result);

		writeTypeInfo(type, (gint)type);
	}
	return type;
}

__thread GHashTable *request_to_id = NULL;
__thread GHashTable *fh_to_request_id = NULL; // used to log split-collective-calls
__thread gint request_counter = 0;

static gint getRequestId(MPI_Request request)
{
	if(request_to_id == NULL)
	{
		request_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}	
	gpointer result = g_hash_table_lookup(request_to_id, &request);
	if(result == NULL)
	{
		gint *g_request = malloc(sizeof(gint));
		*g_request = (gint)request;
		gint *g_id = malloc(sizeof(gint));
		*g_id = (gint)request_counter;
		g_hash_table_insert(request_to_id, g_request, g_id);
	    request_counter++;

		return *g_id;
	}
	return *(gint*)result;
}

static gint getRequestIdForSplit(MPI_File file)
{
	if(fh_to_request_id == NULL)
	{
		fh_to_request_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}
	gpointer result = g_hash_table_lookup(fh_to_request_id, &file);
	if(result == NULL)
	{
		gint *g_fh = malloc(sizeof(gint));
		gint *g_id = malloc(sizeof(gint));
		assert(sizeof(MPI_File) <= sizeof(gint)); // otherwise, casting to gint is bad
		*g_fh = (gint)file;
		*g_id = request_counter;
		request_counter++;
		g_hash_table_insert(fh_to_request_id, g_fh, g_id);
		
		return *g_id;
	}
	return *(gint*)result;
}


static void destroyHashTables()
{
	if(file_handle_to_id)
	{
		g_hash_table_destroy(file_handle_to_id);
		file_handle_to_id = NULL;
	}
	if(file_name_to_id)
	{
		g_hash_table_destroy(file_name_to_id);
		file_name_to_id = NULL;
	}
	if(comm_to_id)
	{
		g_hash_table_destroy(comm_to_id);
		comm_to_id = NULL;
	}
	if(type_table)
	{
		g_hash_table_destroy(type_table);
		type_table = NULL;
	}
	if(request_to_id)
	{
		g_hash_table_destroy(request_to_id);
		request_to_id = NULL;
	}
}

inline static long long getTypeSize(int count, MPI_Datatype type)
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

static const char * getWhenceString(int whence)
{
	switch(whence)
	{
	case MPI_SEEK_SET:
		return "Set";
	case MPI_SEEK_CUR:
		return "Cur";
	case MPI_SEEK_END:
		return "End";
	default:
		return "Invalid";
	}
}

static void after_Init(int *argc, char ***argv)
{
	int rank;

	char basename[TMP_BUF_LEN];

	char * lastSlash = strrchr(**argv , '/');
	if( lastSlash != NULL)
	{
		snprintf(basename, TMP_BUF_LEN, "trace_%s", lastSlash+1 );
	}
	else
	{
		snprintf(basename, TMP_BUF_LEN, "trace_%s", (*argv)[0]+2 );
	}
	
	hdT_Init(basename);
	
	PMPI_Comm_rank(MPI_COMM_WORLD, &rank);

	tracefile = hdT_Create(rank);

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
					tprintf(tracefile, "environment variable %s has unrecognised value of %s",
							control_vars[ii], env_var );
				}
			}
			ii++;
		}
		envvar_read = 1;
	}
	pthread_mutex_unlock(&envvar_mutex);

	hdT_TraceNested(tracefile, trace_nested_operations);
	hdT_ForceFlush(tracefile, trace_force_flush);
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

