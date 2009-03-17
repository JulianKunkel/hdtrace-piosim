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

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <stdlib.h>

#include <glib.h>

#include <sys/time.h>
#include <time.h>


#include "HDTraceWriter.h"

#define log(...) { \
  if(w_tracing){\
    elog(__VA_ARGS__);\
  }\
}

#define elog(...) { \
  sprintf(wbuff, __VA_ARGS__);\
  int len = strlen(wbuff);\
  if( flush_buff_pos + len > BUFF_SIZE){ \
    flush_log();\
  }\
  strncpy(flush_buff + flush_buff_pos, wbuff, len);\
  flush_buff_pos += len;\
}

#define info(...){\
  sprintf(wbuff, __VA_ARGS__);\
  int len = strlen(wbuff);\
  write(wrapper_info_fd, wbuff, len); \
}



static int wrapper_fd;
static int wrapper_info_fd;

static double lastTime; // the last time any MPI Function was called
static double startTime; //only relevant at rank 0

#define LENGTH 1000
static char wbuff[LENGTH];
static char cnbuff[LENGTH];

#define BUFF_SIZE 1 * 1024 * 1024
static char * flush_buff;
static size_t flush_buff_pos = 0;

extern int w_tracing;
extern int w_my_rank;
static int mpi_finalized = 0;

// Traces all functions, even those without a custom logging routine
static int trace_all_functions = 1;

// Minimum compute-time to be logged (microseconds)
#define MIN_COMPUTE_TIME 100.0

// The character(s) used to indent the xml output
#define TAB_STRING "\t"

/**
 * Robustly writes the in-memory trace to disk.
 */
static void flush_log(){
  int ret;
  char * ram_pos = flush_buff;
  size_t amount_to_write = flush_buff_pos;

  tprintf("flushing log length: %lld", (long long int) amount_to_write)

  // retry until all data is written:
  while(amount_to_write > 0){
	  ret = write(wrapper_fd, ram_pos, amount_to_write );
	  if( ret == -1){
		  switch(errno){
		  case(EFAULT):
		  case(EPIPE):
		  case(ENOSPC):
			  tprintf("Critical error during flushing of log, exiting: %s ", strerror(errno))
			  exit(1);
		  case(EINTR):
			  continue; // we are just interrupted
		  default:
			  tprintf("Unknown error during flushing of log: %s", strerror(errno))
		  }

	  }
	  amount_to_write -= ret;
	  ram_pos += ret;
  }

  flush_buff_pos = 0; // could use this one instead of amount_to_write, but easier to read.
}


static char * getCommName(MPI_Comm comm){
	// NOTE: the result becomes invalid after a consecutive
    // call to getCommName(...)
  int len = LENGTH;
  int cmp = 0;
  MPI_Comm_compare(comm, MPI_COMM_WORLD, & cmp);
  if (cmp == MPI_IDENT){
    return "WORLD";
  }
  MPI_Comm_get_name(comm, cnbuff, & len);
  return cnbuff;
}

inline static long long getTypeSize(int count, MPI_Datatype type){
  int t_size;
  MPI_Type_size(type, & t_size);
  return (count * (long long) t_size );
}



static void w_timeStamp(){
  double curTime = MPI_Wtime();
  double computeTime = (curTime - lastTime) * 1000.0 * 1000.0; // microseconds
  if(computeTime > MIN_COMPUTE_TIME){
    log("<Compute duration=\"%g\" />\n", computeTime);
  }
}

static void w_createNewtimeStamp(){
  if(  mpi_finalized == 0){
    lastTime = MPI_Wtime();
  }
}

/**
 * Print the time stamp into the log.
 */
static void logTimeStamp(){
	struct timeval tv;
    gettimeofday(&tv, NULL);

	log("%d.%.6d", (int32_t) tv.tv_sec, (int32_t) tv.tv_usec);
}


void w_Init(char *** args){
  char filename[500];
  MPI_Comm_rank(MPI_COMM_WORLD, & w_my_rank);

  char * name = strdup(args[0][0]);
  char * dirname = ".";

  char * lastSlash = strrchr(name , '/');

  if( lastSlash != NULL){
    dirname = name;
    name = lastSlash + 1;
    *lastSlash = 0;
  }

  sprintf(filename, "%s/trace-%s-%d.xml", dirname, name, w_my_rank);

  tprintf("opening file %s for XML output\n", filename);

  wrapper_fd = open( filename, O_CREAT | O_WRONLY | O_TRUNC  | O_NONBLOCK, 0662);

  sprintf(filename, "%s/trace-%s-%d.info", dirname, name, w_my_rank);
  wrapper_info_fd = open( filename, O_CREAT | O_TRUNC | O_WRONLY  | O_NONBLOCK, 0662);

  if(wrapper_fd <= 0 || wrapper_info_fd <= 0){
    perror("Error opening file:");
    MPI_Abort(MPI_COMM_WORLD, 1);
  }

  flush_buff = malloc(BUFF_SIZE);
  if (flush_buff <= 0){
    perror("Error allocation flush buffer:");
    MPI_Abort(MPI_COMM_WORLD, 1);
  }
  flush_buff_pos = 0;

  elog("<Rank number=\"%d\"><Program>\n", w_my_rank)

  if(w_my_rank == 0){
    // create an program definition file:
    sprintf(filename, "%s/trace-desc-%s.info", dirname, name);
    int fd = open( filename, O_CREAT | O_WRONLY  | O_NONBLOCK, 0662);
    int i;
    if(fd <= 0){
      perror("Error opening file:");
      MPI_Abort(MPI_COMM_WORLD, 1);
    }

    int size = 0;
    MPI_Comm_size(MPI_COMM_WORLD,& size);

    sprintf(wbuff, "%d\n", size);
    write(fd, wbuff, strlen(wbuff));
    for (i=0; i < size; i++){
      sprintf(filename, "trace-%s-%d\n", name, i);
      write(fd, filename, strlen(filename));
    }
    close(fd);
  }

  lastTime = MPI_Wtime();
  startTime = lastTime;
}

void w_priorFinalize(){
  mpi_finalized = 1;

  double delta_time = MPI_Wtime() - startTime;
  tprintf("Runtime %fs", delta_time)
  elog("<!-- <time run=\"%f\"/> -->",delta_time);

  elog("</Program></Rank>\n")

  flush_log();
  close(wrapper_fd);
  close(wrapper_info_fd);
}

void w_Finalize(){

}

void w_Send(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  int t_size;
  MPI_Type_size(type, & t_size);

  log("<Send size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", (count * (long long) t_size ), rank, tag, getCommName(comm) )
}

void w_Bsend(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  int t_size;
  MPI_Type_size(type, & t_size);

  log("<Bsend size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", (count * (long long) t_size ), rank, tag, getCommName(comm) )
}

void w_Ssend(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  int t_size;
  MPI_Type_size(type, & t_size);

  log("<Ssend size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", (count * (long long) t_size ), rank, tag, getCommName(comm) )
}

/*
void w_Isend(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  int t_size;
  MPI_Type_size(type, & t_size);

  log("<Isend size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", (count * (long long) t_size ), rank, tag, getCommName(comm) )
}
*/

void w_Receive(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  log("<Receive fromRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", rank, tag, getCommName(comm) )
}

void w_Barrier(MPI_Comm comm){
  log("<Barrier comm=\"%s\"/>\n", getCommName(comm) )
}

// v2 , v3 , v4, v5, v9, v10, v11
void w_Sendrecv(int count, MPI_Datatype type, int rank, int tag, int source,
				int recvtag, MPI_Comm comm)
{
  log("<Sendrecv size=\"%lld\" toRank=\"%d\" to-tag=\"%d\" fromRank=\"%d\" fromTag=\"%d\" comm=\"%s\"/>\n",
	  getTypeSize(count, type) , rank, tag, source, recvtag, getCommName(comm) )
}

void w_Sendrecv_replace(int count, MPI_Datatype type, int dest, int sendtag,
						int source, int recvtag, MPI_Comm comm)
{
	log("<Sendrecv_replace size='%lld' dest='%d' sendTag='%d' source='%d' recvTag='%d' "
		"comm='%s' />\n", getTypeSize(count, type), dest, sendtag, source, recvtag,
		getCommName(comm))
}

void w_Allreduce(int count, MPI_Datatype type, MPI_Comm comm){
  log("<Allreduce size=\"%lld\" comm=\"%s\"/>\n", getTypeSize(count, type), getCommName(comm) )
}

void w_Reduce(int count, MPI_Datatype type, int root, MPI_Comm comm){
  log("<Reduce size=\"%lld\" rootRank=\"%d\" comm=\"%s\"/>\n",
	  getTypeSize(count, type), root, getCommName(comm) )
}

void w_Bcast(int count, MPI_Datatype type, int root, MPI_Comm comm){
  log("<Bcast size=\"%lld\" rootRank=\"%d\" comm=\"%s\"/>\n",
	  getTypeSize(count, type), root, getCommName(comm) )
}


void w_Gather(int sendcnt, MPI_Datatype sendtype, int recvcnt, MPI_Datatype recvtype,
			  int root, MPI_Comm comm)
{
	log("<Gather size='%lld' recvSize='%lld' root='%d' comm='%s' />\n",
		getTypeSize(sendcnt, sendtype), getTypeSize(recvcnt, recvtype), root,
		getCommName(comm))
}

void w_Gatherv(int sendcnt, MPI_Datatype sendtype, int *recvcnts, int *displs,
			   MPI_Datatype recvtype, int root, MPI_Comm comm)
{
	log("<Gatherv size='%lld' root='%d' comm='%s' />\n",
		getTypeSize(sendcnt, sendtype), root, getCommName(comm))
		// TODO: also save recvcnts ?
}

void w_Scatter(int sendcnt, MPI_Datatype sendtype, int recvcnt,
			   MPI_Datatype recvtype, int root, MPI_Comm comm)
{
	log("<Scatter size='%lld' recvSize='%lld' root='%d' comm='%s' />\n",
		getTypeSize(sendcnt, sendtype), getTypeSize(recvcnt, recvtype), root,
		getCommName(comm))
}

void w_Scatterv(int *sendcnts, MPI_Datatype sendtype, int recvcnt,
			   MPI_Datatype recvtype, int root, MPI_Comm comm)
{
	log("<Scatterv recvSize='%lld' root='%d' comm='%s' />\n",
		getTypeSize(recvcnt, recvtype), root,
		getCommName(comm))
	/*
	int commsize,
		sendcnt = 0,
		i;
	MPI_Comm_size(comm, commsize);
	for(i = 0; i< commsize; ++i)
		sendcnt += sendcnts[i];

	log("<Scatterv size='%lld' recvSize='%lld' root='%d' comm='%s' />\n",
		getTypeSize(sendcnt, sendtype), getTypeSize(recvcnt, recvtype), root,
		getCommName(comm))
	*/
}

void w_Allgather(int sendcount, MPI_Datatype sendtype,
				 int recvcount, MPI_Datatype recvtype, MPI_Comm comm)
{
	log("<Allgather size='%lld' recvSize='%lld' comm='%s' />\n",
		getTypeSize(sendcount, sendtype), getTypeSize(recvcount, recvtype), getCommName(comm))
}

void w_Allgatherv(int sendcount, MPI_Datatype sendtype,
				  int *recvcounts, MPI_Datatype recvtype, MPI_Comm comm)
{
	log("<Allgatherv size='%lld' comm='%s' />\n",
		getTypeSize(sendcount, sendtype), getCommName(comm))
}

void w_Alltoall(int sendcount, MPI_Datatype sendtype,
				int recvcount, MPI_Datatype recvtype, MPI_Comm comm)
{
	log("Alltoall size='%lld' comm='%s' />\n",
		getTypeSize(sendcount, sendtype), getCommName(comm))
}

void w_Alltoallv(int *sendcnts, MPI_Datatype sendtype, int *recvcnts,
				 MPI_Datatype recvtype,	 MPI_Comm comm)
{
	int size, i;
	MPI_Comm_size(comm, &size);
	log("<Alltoall comm='%s'>\n", getCommName(comm));
	for(i = 0; i < size; ++i)
		log(TAB_STRING "<Send rank='%d' size='%lld' />\n", 	// TODO: call this something other than "Send"
			i, getTypeSize(sendcnts[i], sendtype));


	log("</Alltoall>\n")
}

void w_Reduce_scatter(int *recvcnts, MPI_Datatype datatype,	MPI_Op op, MPI_Comm comm)
{
	int size, i;
	MPI_Comm_size(comm, &size);
	// TODO: log the MPI_Op structure?
	log("<Reduce_scatter>\n");
	for(i = 0; i < size; ++i)
		log(TAB_STRING "<Recv rank='%d' size='%lld' />\n",
			i, getTypeSize(recvcnts[i], datatype));
	// NOTE: The recvcnts[] array must be equal on all nodes, so this
    // logs redundant information
	log("</Reduce_scatter>\n");
}

void w_Scan(int count, MPI_Datatype datatype, MPI_Op op, MPI_Comm comm)
{
	log("<Scan size='%lld' comm='%s'/>\n",
		getTypeSize(count, datatype), getCommName(comm));
}

void w_Exscan(int count, MPI_Datatype datatype, MPI_Op op, MPI_Comm comm)
{
	log("<Excan size='%lld' comm='%s'/>\n",
		getTypeSize(count, datatype), getCommName(comm));
}

void w_Abort(MPI_Comm comm, int errorcode)
{
	log("<Abort comm='%s' errorcode='%d'/>\n",
		getCommName(comm), errorcode);

	elog("</Program></Rank>\n");

	flush_log();
	close(wrapper_fd);
	close(wrapper_info_fd);
}

int MPI_Init_thread(int *argc, char ***argv, int required, int *provided)
{
	int ret;
	tsprintf("Init_thread");
	if( required != MPI_THREAD_SINGLE )
	{
		tsprintf("Init_thread: multithreading required while using the non-threadsafe mpiwrapper");
		return -1; // TODO: is -1 the correct return value?
	}
	ret = PMPI_Init_thread(argc, argv, required, provided);
	return ret;
}

void logError(int ret)
{
	static char errorstring[LENGTH];
	int len;
	MPI_Error_string(ret, errorstring, &len);
	log("<Error code='%d' string='%s' />\n",
		ret, errorstring);
}


/** hash table to map file names */
static GHashTable * files = NULL; // maps the fh to the actual ID
// avoid to create several infos about the same file
static GHashTable * filesStringMap = NULL; // maps filename to actual ID

// how is ensured that all clients use the same ID ?
// each client chooses an ID which is 10000 x (RANK+1) + nextFileID
static int nextFileID = 0;


void * getFileHandleName(MPI_File * fh)
{
	return (void*)fh;
}



/**
 * TODO: Right now we assume MPI_File is of type int, this function is called
 * AFTER the File got opened by MPI => fh is valid
 */
void w_File_open(MPI_Comm comm, char * name, int flags, MPI_Info info, MPI_File * fh, int ret){
  if(files == NULL){
	files = g_hash_table_new (g_int_hash, g_int_equal);
	filesStringMap = g_hash_table_new (g_str_hash, g_str_equal);
  }

  log("<FileOpen comm=\"%s\" name=\"%s\" flags=\"%d\" fh=\"%p\" ret='%d'/>\n",
	  getCommName(comm), name, flags, getFileHandleName(fh), ret);

  if(ret)
	  logError(ret);

  // TODO fix memleak
  gchar * dup = g_strdup(name);

  // check if the file already existed
  gint * id =  g_hash_table_lookup(filesStringMap, name);

  int rank;

  if( id == NULL ){
	id = malloc(sizeof(gint));

	MPI_Comm_rank(comm, & rank);

	if (rank == 0){

		int world_rank;
		PMPI_Comm_rank(MPI_COMM_WORLD, & world_rank);
		*id = 10000 * (world_rank + 1) + (nextFileID++);

		g_hash_table_insert (filesStringMap, dup, id);

		MPI_Offset size;
		PMPI_File_get_size(* fh, & size);
  		info("File_open name=\"%s\" comm=\"%s\" flags=%d InitialSize=%lld id=%d\n",
			 name,  getCommName(comm), flags, (long long int) size, (int) *id);
	}else{
	   g_free(dup);
	}
  }else{
       g_free(dup);
  }
  // broadcast ID in case the ID changed on the clients
  if (rank == 0){
	id = (gint*) g_hash_table_lookup(filesStringMap, name);
  }

  int bID = *id;
  PMPI_Bcast(& bID, 1, MPI_INT, 0, comm);
  *id = bID;

  if (rank != 0){
	 g_hash_table_replace (filesStringMap, dup, id);
  }

  // TODO fix id memleak
  g_hash_table_replace (files, (gint *) fh, id);
}

void w_File_close(MPI_File *file_handle)
{
	log("<File_close fh='%p' />\n",
		getFileHandleName(file_handle));
}

void w_File_delete(char * filename)
{
	log("<File_delete name='%s' />\n",
		filename);
}


int getFileId(MPI_File fh)
{
	gint * file = g_hash_table_lookup(files, (void *) fh);
	return (int) *file;
}


void w_File_write_at(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type)
{
  log("<FileWrite file=\"%d\"> <Data offset=\"%lld\" size=\"%lld\"/> </FileWrite>\n",
	  getFileId(fh), (long long int) offset, getTypeSize(count, type));
}

void w_File_read_at(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type)
{
  log("<FileRead file=\"%d\"> <Data offset=\"%lld\" size=\"%lld\"/> </FileRead>\n",
	  getFileId(fh), (long long int) offset, getTypeSize(count, type));
}


void w_File_write(MPI_File fh, int count, MPI_Datatype type){
  MPI_Offset view_offset;
  // view dependent offset:
  PMPI_File_get_position(fh, & view_offset);
  // real offset:
  MPI_Offset real_offset;
  PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

  w_File_write_at(fh, real_offset, count, type);
}

void w_File_read(MPI_File fh, int count, MPI_Datatype type){
  MPI_Offset view_offset;
  // view dependent offset:
  PMPI_File_get_position(fh, & view_offset);
  // real offset:
  MPI_Offset real_offset;
  PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

  w_File_read_at(fh, real_offset, count, type);
}

void w_File_set_size(MPI_File fh, MPI_Offset size)
{
	log("<File_set_size file='%d' size='%lld' />\n",
		getFileId(fh), (long long int) size);
}

void w_File_preallocate(MPI_File fh, MPI_Offset size)
{
	log("<File_preallocate file='%d' size='%lld' />\n",
		getFileId(fh), (long long int) size);
}

void w_File_get_size(MPI_File fh, MPI_Offset *size)
{
	log("<File_get_size file='%d' size='%lld' />\n",
		getFileId(fh), (long long int) *size);
}

void w_File_set_info(MPI_File fh, MPI_Info info)
{
	int nkeys, i;
	static char key[LENGTH];
	log("<File_set_info>\n");

	PMPI_Info_get_nkeys(info, &nkeys);
	for(i = 0; i < nkeys; ++i)
	{
		MPI_Info_get_nthkey(info, i, key);
		log( TAB_STRING "<key value='%s' />\n",
			key);
	}

	log("</File_set_info>\n");
}


int getFileNumber(MPI_File fh)
{
	gint * file =  g_hash_table_lookup(files, (gint *) &fh);
	return (int) *file;
}

void w_File_read_at_all(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type)
{
	log("<FileReadAll file=\"%d\">\n", getFileNumber(fh));
	log(TAB_STRING "<Data offset=\"%lld\" size=\"%lld\"/>\n",
		(long long int) offset, getTypeSize(count, type));
	log("</FileReadAll>\n");
}

void w_File_read_all(MPI_File fh, int count, MPI_Datatype type)
{
  MPI_Offset view_offset;
  // view dependent offset:
  PMPI_File_get_position(fh, & view_offset);
  // real offset:
  MPI_Offset real_offset;
  PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

  w_File_read_at_all(fh, real_offset, count, type);
}


void w_File_write_at_all(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type)
{
  log("<FileWriteAll file=\"%d\"> <Data offset=\"%lld\" size=\"%lld\"/> </FileWriteAll>\n",
	  getFileNumber(fh), (long long int) offset, getTypeSize(count, type))
}

void w_File_write_all(MPI_File fh, int count, MPI_Datatype type){
  MPI_Offset view_offset;
  // view dependent offset:
  PMPI_File_get_position(fh, & view_offset);
  // real offset:
  MPI_Offset real_offset;
  PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

  w_File_write_at_all(fh, real_offset, count, type);
}


void w_File_set_atomicity(MPI_File fh, int flag)
{
	log("<File_set_atomicity file='%d' flag='%d' />\n",
		getFileNumber(fh), flag);
}

void w_File_sync(MPI_File fh)
{
	log("<File_sync file='%d' />\n",
		getFileNumber(fh));
}

void w_File_read_shared(MPI_File fh, int count, MPI_Datatype type)
{
	log("<File_read_shared file='%d' size='%lld' />\n",
		getFileNumber(fh), getTypeSize(count, type));
}

void w_File_write_shared(MPI_File fh, int count, MPI_Datatype type)
{
	log("<File_write_shared file='%d' size='%lld' />\n",
		getFileNumber(fh), getTypeSize(count, type));
}

void w_File_read_ordered(MPI_File fh, int count, MPI_Datatype type)
{
	log("<File_read_ordered file='%d' size='%lld' />\n",
		getFileNumber(fh), getTypeSize(count, type));
}

void w_File_write_ordered(MPI_File fh, int count, MPI_Datatype type)
{
	log("<File_write_ordered file='%d' size='%lld' />\n",
		getFileNumber(fh), getTypeSize(count, type));
}

void w_File_seek_shared(MPI_File fh, MPI_Offset offset, int whence)
{
	const char * mode;
	switch(whence)
	{
	case MPI_SEEK_SET:
		mode = "Set";
		break;
	case MPI_SEEK_CUR:
		mode = "Cur";
		break;
	case MPI_SEEK_END:
		mode = "End";
		break;
	default:
		mode = "";
	}
	log("<File_seek_shared file='%d' offset='%lld' whence='%s' />\n",
		getFileNumber(fh), (long long int) offset, mode);
}


// static GHashTable * requestNumberMap = NULL;

int getRequestNumber(MPI_Request request)
{
	return (int)request;
}



/*** nonblocking operations *************************************************/

/* we need to generate an id for each operation, so we can log what we are
waiting for
*/
void w_Isend(int count, MPI_Datatype type, int dest, int tag, MPI_Comm comm, MPI_Request * request)
{
	//
	//   log("<Isend size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n",
	// (count * (long long) t_size ), rank, tag, getCommName(comm) )
	log("<Isend size='%lld' toRank='%d' tag='%d' comm='%s' request='%d'/>\n",
		getTypeSize(count, type), dest, tag, getCommName(comm), getRequestNumber(*request));
}

void w_Waitany(int count, MPI_Request array_of_requests[])
{
	int i;
	log("<Waitany>\n");
	for(i = 0; i < count; ++i )
	{
		log(TAB_STRING "<For request='%d'>\n",
			getRequestNumber(array_of_requests[i]));
	}
	log("</Waitany>\n");
}

void w_Waitsome(int count, MPI_Request array_of_requests[])
{
	int i;
	log("<Waitsome>\n");
	for(i = 0; i < count; ++i )
	{
		log(TAB_STRING "<For request='%d'>\n",
			getRequestNumber(array_of_requests[i]));
	}
	log("</Waitsome>\n");
}


void w_Waitall(int count, MPI_Request array_of_requests[])
{
	int i;
	log("<Wait>\n");
	for(i = 0; i < count; ++i )
	{
		log(TAB_STRING "<For request='%d'>\n",
			getRequestNumber(array_of_requests[i]));
	}
	log("</Wait>\n");
}

void w_Wait(MPI_Request *request)
{
	w_Waitall(1, request);
}


void w_Iprobe(  int source, int tag, MPI_Comm comm)
{
	log("<Iprobe source='%d' tag='%d' comm='%s' />\n",
		source, tag, getCommName(comm));
}

void w_Irecv(int count, MPI_Datatype datatype, int source, int tag, MPI_Comm comm, MPI_Request *request)
{
	log("<Irecv size='%lld' source='%d' tag='%d' comm='%s' request='%d' />\n",
		getTypeSize(count, datatype), source, tag, getCommName(comm), getRequestNumber(*request));
}

void w_Ibsend(int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm, MPI_Request *request)
{
	log("<Ibsend size='%lld' dest='%d' tag='%d' comm='%s' request='%d' />\n",
		getTypeSize(count, datatype), dest, tag, getCommName(comm), getRequestNumber(*request));
}

void w_Issend(int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm, MPI_Request *request)
{
	log("<Issend size='%lld' dest='%d' tag='%d' comm='%s' request='%d' />\n",
		getTypeSize(count, datatype), dest, tag, getCommName(comm), getRequestNumber(*request));
}

void w_Irsend(int count, MPI_Datatype datatype, int dest, int tag, MPI_Comm comm, MPI_Request *request)
{
	log("<Irsend size='%lld' dest='%d' tag='%d' comm='%s' request='%d' />\n",
		getTypeSize(count, datatype), dest, tag, getCommName(comm), getRequestNumber(*request));
}


void w_File_iread_at(MPI_File fh, MPI_Offset offset, int count,
					 MPI_Datatype datatype, MPI_Request *request)
{

	log("<Iread file='%d' request='%d'/>\n",
		getFileId(fh), getRequestNumber(*request));
	log(TAB_STRING "<Data offset='%lld' size='%lld'  />\n",
		(long long int)offset, getTypeSize(count, datatype));
	log("</Iread>\n")
}

void w_File_iread(MPI_File fh, int count, MPI_Datatype datatype, MPI_Request *request)
{
	MPI_Offset view_offset;
	// view dependent offset:
	PMPI_File_get_position(fh, & view_offset);
	// real offset:
	MPI_Offset real_offset;
	PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

	w_File_iread_at(fh, real_offset, count, datatype, request);
}

void w_File_iwrite_at(MPI_File fh, MPI_Offset offset, int count,
					  MPI_Datatype datatype, MPI_Request *request)
{
	log("<Iwrite file='%d' request='%d'/>\n",
		getFileId(fh), getRequestNumber(*request));
	log(TAB_STRING "<Data offset='%lld' size='%lld'  />\n",
		(long long int)offset, getTypeSize(count, datatype));
	log("</Iwrite>\n")
}

void w_File_iwrite(MPI_File fh, int count, MPI_Datatype datatype, MPI_Request *request)
{

	MPI_Offset view_offset;
	// view dependent offset:
	PMPI_File_get_position(fh, & view_offset);
	// real offset:
	MPI_Offset real_offset;
	PMPI_File_get_byte_offset(fh, view_offset, & real_offset);

	w_File_iwrite_at(fh, real_offset, count, datatype, request);
}


void w_File_read_at_all_begin(MPI_File mpi_fh, MPI_Offset offset, int count,
							  MPI_Datatype datatype)
{
	log("<SplitBegin>\n");
	w_File_read_at_all(mpi_fh, offset, count, datatype);
	log("</SplitBegin>\n");
}

void w_end_split(MPI_File fh)
{
	log("<SplitEnd file='%d' />\n", getFileNumber(fh));
}

void w_File_read_all_begin(MPI_File mpi_fh, int count, MPI_Datatype datatype)
{
	log("<SplitBegin>\n");
	w_File_read_all(mpi_fh, count, datatype);
	log("</SplitBegin>\n");
}

void w_File_write_at_all_begin(MPI_File mpi_fh, MPI_Offset offset, int count, MPI_Datatype datatype)
{
	log("<SplitBegin>\n");
	w_File_write_at_all(mpi_fh, offset, count, datatype);
	log("</SplitBegin>\n");
}

void w_File_write_all_begin(MPI_File fh, int count, MPI_Datatype type)
{
	log("<SplitBegin>\n");
	w_File_write_all(fh, count, type);
	log("</SplitBegin>\n");
}

void w_File_read_ordered_begin(MPI_File fh, int count, MPI_Datatype type)
{
	log("<SplitBegin>\n");
	w_File_read_ordered(fh, count, type);
	log("</SplitBegin>\n");
}

void w_File_write_ordered_begin(MPI_File fh, int count, MPI_Datatype type)
{
	log("<SplitBegin>\n");
	w_File_write_ordered(fh, count, type);
	log("</SplitBegin>\n");
}




