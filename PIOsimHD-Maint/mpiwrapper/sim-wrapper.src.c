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

#include "sim-wrapper.h"

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

#define BUFF_SIZE 1 * 1024 * 1024
static char * flush_buff;
static long long int flush_buff_pos = 0;

extern int w_tracing;
extern int w_my_rank;
static int mpi_finalized = 0;

static void flush_log(){
  tprintf("flushing log length: %lld", flush_buff_pos)
  write(wrapper_fd, flush_buff, flush_buff_pos );
  flush_buff_pos = 0;
}


static char * getCommName(MPI_Comm comm){
  int len = LENGTH;
  int cmp = 0;
  MPI_Comm_compare(comm, MPI_COMM_WORLD, & cmp);
  if (cmp == MPI_IDENT){
    return "WORLD";
  }
  MPI_Comm_get_name(comm, wbuff, & len);
  return wbuff;
}

inline static long long getTypeSize(int count, MPI_Datatype type){
  int t_size;
  MPI_Type_size(type, & t_size);
  return (count * (long long) t_size );
}



static void w_timeStamp(){
  double curTime = MPI_Wtime();
  long long int computeCycles = (curTime - lastTime) * 1000.0 * 1000.0; // microseconds
  if(computeCycles > 100){
    log("<Compute cycles=\"%lld\"/>\n", computeCycles);
  }
}

static void w_createNewtimeStamp(){
  if(  mpi_finalized == 0){
    lastTime = MPI_Wtime();
  }
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
}

void w_Finalize(){
  elog("</Program></Rank>\n")
  
  flush_log();
  close(wrapper_fd);
  close(wrapper_info_fd);
}

void w_Send(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  int t_size;
  MPI_Type_size(type, & t_size);

  log("<Send size=\"%lld\" toRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", (count * (long long) t_size ), rank, tag, getCommName(comm) )
}

void w_Receive(int count, MPI_Datatype type, int rank, int tag, MPI_Comm comm){
  log("<Receive fromRank=\"%d\" tag=\"%d\" comm=\"%s\"/>\n", rank, tag, getCommName(comm) )
}

void w_Barrier(MPI_Comm comm){
  log("<Barrier comm=\"%s\"/>\n", getCommName(comm) )
}

// v2 , v3 , v4, v5, v9, v10, v11
void w_Sendrecv(int count, MPI_Datatype type, int rank, int tag, int source, int recvtag, MPI_Comm comm){
  log("<Sendrecv size=\"%lld\" toRank=\"%d\" to-tag=\"%d\" fromRank=\"%d\" fromTag=\"%d\" comm=\"%s\"/>\n", getTypeSize(count, type) , rank, tag, source, recvtag, getCommName(comm) )
}


void w_Allreduce(int count, MPI_Datatype type, MPI_Comm comm){
  log("<Allreduce size=\"%lld\" comm=\"%s\"/>\n", getTypeSize(count, type), getCommName(comm) )
}

void w_Reduce(int count, MPI_Datatype type, int root, MPI_Comm comm){
  log("<Reduce size=\"%lld\" rootRank=\"%d\" comm=\"%s\"/>\n", getTypeSize(count, type), root, getCommName(comm) )
}

void w_Bcast(int count, MPI_Datatype type, int root, MPI_Comm comm){
  log("<Bcast size=\"%lld\" rootRank=\"%d\" comm=\"%s\"/>\n", getTypeSize(count, type), root, getCommName(comm) )
}


/** hash table to map file names */
static GHashTable * files = NULL; // maps the fh to the actual ID
// avoid to create several infos about the same file
static GHashTable * filesStringMap = NULL; // maps filename to actual ID

// how is ensured that all clients use the same ID ?
// each client chooses an ID which is 10000 x (RANK+1) + nextFileID
static int nextFileID = 0;

/**
 * TODO: Right now we assume MPI_File is of type int, this function is called AFTER the File got opened by MPI => fh is valid
 */
void w_File_open(MPI_Comm comm, char * name, int flags, MPI_Info info, MPI_File * fh){
  if(files == NULL){
	files = g_hash_table_new (g_int_hash, g_int_equal);
	filesStringMap = g_hash_table_new (g_str_hash, g_str_equal);
  }

  log("<!-- <FileOpen comm=\"%s\" name=\"%s\" flags=\"%d\" fh=\"%p\"/> -->\n", getCommName(comm), name, flags, fh)

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
  		info("File_open name=\"%s\" comm=\"%s\" flags=%d InitialSize=%lld id=%d\n", name,  getCommName(comm), flags, (long long int) size, (int) *id);
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


void w_File_write_at(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type){
  gint * file =  g_hash_table_lookup(files, (gint *) &fh);

  log("<FileWrite file=\"%d\"> <Data offset=\"%lld\" size=\"%lld\"/> </FileWrite>\n", (int) *file, (long long int) offset, getTypeSize(count, type))  
}

void w_File_read_at(MPI_File fh, MPI_Offset offset, int count, MPI_Datatype type){
  gint * file =  g_hash_table_lookup(files, (gint *) &fh);

  log("<FileRead file=\"%d\"> <Data offset=\"%lld\" size=\"%lld\"/> </FileWrite>\n", (int) *file, (long long int) offset, getTypeSize(count, type))  
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

