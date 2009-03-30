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

#include <stdio.h>
#include <stdlib.h>

#include <mpi.h>

#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <string.h>

#include <errno.h>

#include <stdarg.h>

#include <pthread.h>

#include "HDTraceWriter.h"

static size_t htT_min(size_t a, size_t b)
{
	if(a < b)
		return a;
	return b;
}


static void writeState(TraceFileP tracefile);


void hdT_TraceNested(TraceFileP file, int trace)
{
	file -> trace_nested_operations = trace;
}

void hdT_Enable(TraceFileP file, int enable)
{
	file -> trace_enable = enable;
}

/**
 * flush = 0 -> flush on full buffer
 * flush = 1 -> flush after write
 */
void hdT_ForceFlush(TraceFileP file, int flush)
{
	file -> always_flush = flush;
}

/**
 * flush tracefile -> buffer into tracefile -> trace_fd.
 */
static void hdT_LogFlush(TraceFileP tracefile)
{
	int ret;
	char * ram_pos = tracefile -> buffer;
	size_t amount_to_write = tracefile -> buffer_pos;

	tprintf(tracefile, "flushing log length: %lld", (long long int) amount_to_write)

		// retry until all data is written:
		while(amount_to_write > 0){
			ret = write(tracefile -> trace_fd, ram_pos, amount_to_write );
			if( ret == -1){
				switch(errno){
				case(EFAULT):
				case(EPIPE):
				case(EFBIG):
				case(EBADF):
					tprintf(tracefile, "Critical error during flushing of log, exiting: %s ", strerror(errno));
					exit(1);
				case(ENOSPC):
					tprintf(tracefile, "Could not flush buffer: no space left on device. %s ", strerror(errno));
					exit(1);
				case(EINTR):
					continue; // we are just interrupted
				default:
					tprintf(tracefile, "Unknown error during flushing of log: %s", strerror(errno))
						}
			}
			amount_to_write -= ret;
			ram_pos += ret;
		}

	tracefile -> buffer_pos = 0; // could use this one instead of amount_to_write, but easier to read.
}

/**
 * write data to tracefile -> info_fd. this output is not buffered
 */ 
void hdT_LogInfo(TraceFileP tracefile, const char * message, ...)
{
	if( !tracefile -> trace_enable )
		return;

	char buffer[HD_TMP_BUF_SIZE];
	va_list argptr;
	int ret;
	size_t amount_to_write;
	char * ram_pos = buffer;

	va_start(argptr, message);
	amount_to_write = vsnprintf(buffer, HD_TMP_BUF_SIZE, message, argptr);
	if(amount_to_write >= HD_TMP_BUF_SIZE)
	{
		perror("hdLogInfo: temporary buffer too small for message");
	}
	va_end( argptr );

	while(amount_to_write > 0)
	{
		ret = write(tracefile -> info_fd, ram_pos, amount_to_write );
		if( ret == -1)
		{
			switch(errno){
			case(EFAULT):
			case(EPIPE):
			case(EFBIG):
			case(EBADF):
				tprintf(tracefile, "Critical error during flushing of log, exiting: %s ", strerror(errno));
				exit(1);
			case(ENOSPC):
				tprintf(tracefile, "Could not flush buffer: no space left on device. %s ", strerror(errno));
				exit(1);
			case(EINTR):
				continue; // we are just interrupted
			default:
				tprintf(tracefile, "Unknown error during flushing of log: %s", strerror(errno));
			}
	  }
	  amount_to_write -= ret;
	  ram_pos += ret;
	}
}


static void hdT_LogWrite(TraceFileP tracefile, const char * message)
{
	if( !tracefile -> trace_enable )
		return;
	int len = strlen(message);
	if(tracefile -> buffer_pos + len >= HD_LOG_BUF_SIZE)
	{
		hdT_LogFlush(tracefile);
	}
	strncpy(tracefile -> buffer + tracefile -> buffer_pos, message, len);
	tracefile -> buffer_pos += len;
	if(tracefile -> always_flush)
	{
		hdT_LogFlush(tracefile);
	}
}

static void hdT_LogWriteFormatv(TraceFileP tracefile, const char * message, va_list valist)
{
	if( !tracefile -> trace_enable )
		return;
	char buffer[HD_TMP_BUF_SIZE];
	int written;
	written = vsnprintf(buffer, HD_TMP_BUF_SIZE, message, valist);
	if(written >= HD_TMP_BUF_SIZE)
	{
		tprintf(tracefile, "hdT_LogWriteFormat: buffer too small for string: %s", message);
	}
	hdT_LogWrite(tracefile, buffer);
}

static void hdT_LogWriteFormat(TraceFileP tracefile, const char * message, ...)
{
	if( !tracefile -> trace_enable )
		return;
	va_list valist;
	va_start(valist, message);
	hdT_LogWriteFormatv(tracefile, message, valist);
	va_end(valist);
}


static void hdT_LogWriteIndentation(TraceFileP tracefile, int count)
{
	int i;
	for(i = 0; i < count; ++i)
	{
		hdT_LogWrite(tracefile, HD_LOG_TAB_STRING);
	}
}


static const char * filePrefix = "";

void hdT_Init(const char * fp)
{
	filePrefix = fp;
}

int thread_counter = 0;
pthread_mutex_t thread_counter_mutex = PTHREAD_MUTEX_INITIALIZER;

TraceFileP hdT_Create(int rank)
{
	char filename[HD_LOG_BUF_SIZE];
	int written = 0;
	int fd;

	pthread_mutex_lock(&thread_counter_mutex);

	int thread = thread_counter;
	++thread_counter;

	pthread_mutex_unlock(&thread_counter_mutex);


	TraceFileP tracefile = (TraceFileP)malloc(sizeof(struct TraceFile));
	if(!tracefile) 
	{
		perror("Could not allocate memory");
		return NULL;
	}

	tracefile -> thread = thread;
	
	// open xml log
	written = snprintf(filename, HD_LOG_BUF_SIZE, "%s_%d_%d.xml", filePrefix, rank, thread);
	if(written >= HD_LOG_BUF_SIZE || written < 0) // buffer too small or error
	{
		perror("Could not generate output file path. File Prefix:");
		perror(filePrefix);
		return NULL;
	}
	tracefile -> trace_fd = open(filename, O_CREAT | O_WRONLY | O_TRUNC  | O_NONBLOCK, 0662);
	if(tracefile -> trace_fd == -1) 
	{
		perror("Could not open file:");
		perror(filename);
		return NULL;
	}

	// open info log
	written = snprintf(filename, HD_LOG_BUF_SIZE, "%s_%d_%d.info", filePrefix, rank, thread);
	if(written >= HD_LOG_BUF_SIZE || written < 0)  // buffer too small or error
	{
		perror("Could not generate output file path. File Prefix:");
		perror(filePrefix);
		return NULL;
	}
	tracefile -> info_fd = open(filename, O_CREAT | O_WRONLY | O_TRUNC  | O_NONBLOCK, 0662);
	if(tracefile -> info_fd == -1) 
	{
		perror("Could not open file:");
		perror(filename);
		return NULL;
	}

	// write program definition file
	if(rank == 0 && tracefile -> thread == 0) 
	{
		written = snprintf(filename, HD_LOG_BUF_SIZE, "%s_desc.info", filePrefix);
		if(written >= HD_LOG_BUF_SIZE || written < 0) // buffer too small or error
		{
			perror("Could not generate output file path. File Prefix:");
			perror(filePrefix);
			return NULL;
		}
		fd = open(filename, O_CREAT | O_WRONLY | O_TRUNC  | O_NONBLOCK, 0662);
		if(fd == -1) 
		{
			perror("Could not open file:");
			perror(filename);
			return NULL;
		}

		int size = 0;
		int i;
		MPI_Comm_size(MPI_COMM_WORLD,& size); 
    
		sprintf(filename, "%d\n", size);
		write(fd, filename, strlen(filename)); // TODO: check for write errors
		for (i=0; i < size; i++)
		{
			snprintf(filename, HD_LOG_BUF_SIZE, "%s_%d_%d\n", filePrefix, i, thread);
			write(fd, filename, strlen(filename)); // TODO: check for write errors
		}

		close(fd);
	}
	
	tracefile -> function_depth = -1;
	tracefile -> buffer_pos = 0;
	tracefile -> buffer[0] = '\0';
	tracefile -> rank = rank;
	tracefile -> thread = thread;

	tracefile -> always_flush = 0;
	tracefile -> trace_enable = 1;
	tracefile -> trace_nested_operations = 1;
	int i;
	for(i = 0; i < HD_LOG_MAX_DEPTH; ++i)
	{
		tracefile -> has_nested[i] = 0;
		tracefile -> state_name[i][0] = '\0';
	}

	return tracefile;
}

void hdT_Finalize(TraceFileP file)
{
	hdT_LogWrite(file, "</Program>\n");
	hdT_LogFlush(file);
	hdT_LogInfo(file, "\n\n");
	close(file -> info_fd);
	close(file -> trace_fd);
	free(file);
}

void hdT_LogElement(TraceFileP tracefile, const char * name, const char * valueFormat, ...)
{
	if(tracefile -> function_depth >= HD_LOG_MAX_DEPTH)
	{
		tprintf(tracefile, "maximum nesting depth exceeded. depth=%d", tracefile -> function_depth );
		return;
	}

	va_list valist;
	va_start(valist, valueFormat);
	int write;
	write = snprintf(tracefile -> elements[tracefile -> function_depth] + tracefile -> elements_pos[tracefile -> function_depth],
					 HD_LOG_COMMAND_BUF_SIZE - (tracefile -> elements_pos[tracefile -> function_depth]),
					 "<%s ", name);
	tracefile -> elements_pos[tracefile -> function_depth] = htT_min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	write = vsnprintf(tracefile -> elements[tracefile -> function_depth] + tracefile -> elements_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> elements_pos[tracefile -> function_depth],
					 valueFormat, valist);
	tracefile -> elements_pos[tracefile -> function_depth] = htT_min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);	
	write = snprintf(tracefile -> elements[tracefile -> function_depth] + tracefile -> elements_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> elements_pos[tracefile -> function_depth],
					 " />\n");
	tracefile -> elements_pos[tracefile -> function_depth] = htT_min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	va_end(valist);
}

void hdT_LogAttributes(TraceFileP tracefile, const char * valueFormat, ...)
{
	if(tracefile -> function_depth >= HD_LOG_MAX_DEPTH)
	{
		tprintf(tracefile, "maximum nesting depth exceeded. depth=%d", tracefile -> function_depth );
		return;
	}

	va_list valist;
	va_start(valist, valueFormat);
	int write;

	write = vsnprintf(tracefile -> attributes[tracefile -> function_depth] + tracefile -> attributes_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> attributes_pos[tracefile -> function_depth],
					 valueFormat, valist);
	tracefile -> attributes_pos[tracefile -> function_depth] = htT_min(tracefile -> attributes_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);	
	write = snprintf(tracefile -> attributes[tracefile -> function_depth] + tracefile -> attributes_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> attributes_pos[tracefile -> function_depth],
					 " ");
	tracefile -> attributes_pos[tracefile -> function_depth] = htT_min(tracefile -> attributes_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	va_end(valist);
}


static void writeState(TraceFileP tracefile)
{
	if(tracefile -> function_depth >= HD_LOG_MAX_DEPTH)
		return;

	hdT_LogWriteIndentation(tracefile, tracefile -> function_depth);
	hdT_LogWrite(tracefile, "<");
	hdT_LogWrite(tracefile, tracefile -> state_name[tracefile -> function_depth]);
	hdT_LogWrite(tracefile, " ");

	// write pending attributes
	if(tracefile -> attributes_pos[tracefile -> function_depth] != 0)
	{
		hdT_LogWrite(tracefile, tracefile -> attributes[tracefile -> function_depth]);
	}

	// write time information
	hdT_LogWriteFormat(tracefile, " time='%d.%.6d'", 
					   (unsigned)tracefile -> start_time[tracefile -> function_depth].tv_sec, 
					   (unsigned)tracefile -> start_time[tracefile -> function_depth].tv_usec);
	
	hdT_LogWriteFormat(tracefile, " end='%d.%.6d' ", 
					   (unsigned)tracefile -> end_time[tracefile -> function_depth].tv_sec, 
					   (unsigned)tracefile -> end_time[tracefile -> function_depth].tv_usec);

	// write pending elements
	if(tracefile -> elements_pos[tracefile -> function_depth] != 0)
	{
		hdT_LogWrite(tracefile, ">\n" HD_LOG_TAB_STRING);
		hdT_LogWriteIndentation(tracefile, tracefile -> function_depth);
		hdT_LogWrite(tracefile, tracefile -> elements[tracefile -> function_depth]);
		hdT_LogWriteIndentation(tracefile, tracefile -> function_depth);
		hdT_LogWrite(tracefile, "</");
		hdT_LogWrite(tracefile, tracefile -> state_name[tracefile -> function_depth]);
		hdT_LogWrite(tracefile, ">\n");
	}
	else
	{
		hdT_LogWrite(tracefile, " />\n");
	}
}

void hdT_StateStart(TraceFileP file, const char * stateName)
{
	file -> function_depth++;
	if(!file -> trace_nested_operations && file -> function_depth > 0)
	{
		return;
	}

	if(file -> function_depth > 0 && (file -> function_depth - 1 < HD_LOG_MAX_DEPTH))
	{
		if(file -> has_nested[file -> function_depth - 1] == 0)
		{
			hdT_LogWriteIndentation(file, file -> function_depth - 1);
			hdT_LogWrite(file, "<Nested>\n");
			file -> has_nested[file -> function_depth - 1] = 1;
		}
	}

	if(file -> function_depth < HD_LOG_MAX_DEPTH)
	{
		file -> elements_pos[file -> function_depth] = 0;
		file -> attributes_pos[file -> function_depth] = 0;

		gettimeofday(&file -> start_time[file -> function_depth], NULL);
		snprintf(file -> state_name[file -> function_depth],
				 HD_LOG_ELEMENT_NAME_BUF_SIZE,
				 "%s", stateName);
	}
}

void hdT_StateEnd(TraceFileP tracefile)
{
	if(tracefile -> function_depth > 0 && !tracefile -> trace_nested_operations)
	{
		tracefile -> function_depth--;
		return;
	}

	if(tracefile -> function_depth >= HD_LOG_MAX_DEPTH )
	{
		tracefile -> function_depth--;
		return;
	}
	gettimeofday(&tracefile->end_time[tracefile -> function_depth], NULL);
	
	if(tracefile -> has_nested[tracefile -> function_depth])
	{
		hdT_LogWriteIndentation(tracefile, tracefile -> function_depth);
		hdT_LogWrite(tracefile, "</Nested>\n");
		tracefile -> has_nested[tracefile -> function_depth] = 0;
	}

	writeState(tracefile);
		
	tracefile -> function_depth--;
}



