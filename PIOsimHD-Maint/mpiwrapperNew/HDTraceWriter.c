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



// Traces all functions, even those without a custom logging routine
int trace_all_functions = 1;
int trace_compute_time = 1;
int trace_nested_operations = 1;
int trace_file_info = 1;


const char * control_vars[] = { "HDTRACE_ALL_FUNCTIONS", 
                                     "HDTRACE_COMPUTE_TIME", 
                                     "HDTRACE_NESTED",
                                     "HDTRACE_FILE_INFO",
                                     NULL };
int * controlled_vars[] = { &trace_all_functions,
								 &trace_compute_time,
								 &trace_nested_operations, 
								 &trace_file_info,
								 NULL };

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

void hdT_LogFlush(TraceFileP tracefile)
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


void hdLogWrite(TraceFileP tracefile, const char * message)
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

void hdLogWriteFormatv(TraceFileP tracefile, const char * message, va_list valist)
{
	char buffer[HD_TMP_BUF_SIZE];
	int written;
	written = vsnprintf(buffer, HD_TMP_BUF_SIZE, message, valist);
	if(written >= HD_TMP_BUF_SIZE)
	{
		tprintf(tracefile, "hdLogWriteFormat: buffer too small for string: %s", message);
	}
	hdLogWrite(tracefile, buffer);
}

void hdLogWriteFormat(TraceFileP tracefile, const char * message, ...)
{
	va_list valist;
	va_start(valist, message);
	hdLogWriteFormatv(tracefile, message, valist);
	va_end(valist);
}



void hdLogWriteIndentation(TraceFileP tracefile)
{
	int i;
	for(i = 0; i < tracefile -> nested_counter; ++i)
	{
		hdLogWrite(tracefile, HD_LOG_TAB_STRING);
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
	written = snprintf(filename, HD_LOG_BUF_SIZE, "%s-%d-%d.xml", filePrefix, rank, thread);
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
	written = snprintf(filename, HD_LOG_BUF_SIZE, "%s-%d-%d.info", filePrefix, rank, thread);
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
		written = snprintf(filename, HD_LOG_BUF_SIZE, "%s-desc.info", filePrefix);
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
			snprintf(filename, HD_LOG_BUF_SIZE, "%s-%d-%d\n", filePrefix, i, thread);
			write(fd, filename, strlen(filename)); // TODO: check for write errors
		}

		close(fd);

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
	}
	
	tracefile -> function_depth = -1;
	tracefile -> nested_counter = 0;
	tracefile -> buffer_pos = 0;
	tracefile -> buffer[0] = '\0';
	tracefile -> rank = rank;
	tracefile -> thread = thread;

	tracefile -> always_flush = 0;
	tracefile -> trace_enable = 1;

	//
	hdLogWriteFormat(tracefile, "<Rank number='%d' thread='%d'>\n<Program>\n", rank, thread);

	return tracefile;
}

void hdT_Finalize(TraceFileP file)
{
	hdLogWrite(file, "</Program>\n</Rank>\n\n");
	hdT_LogFlush(file);
	hdT_LogInfo(file, "\n\n");
	close(file -> info_fd);
	close(file -> trace_fd);
	free(file);
}

/*
    MPI_Write
	hdLogStateStart()
		Prüfe ob nested Tag geschrieben / nötig für gegenwärtige Tiefe
	PMPI_Send
	hdLogAttributes
	GENERATE_WRITE_NESTED_TAGS in Buffer
	hdLogStateEnd(buffer)
		Prüfe ob END nested Tag nötig
		Eigentliche Daten speichern
*/

// writes as many <Nested> or </Nested>-Tags as needed to arrive at function_depth
void writeNestedTags(TraceFileP file)
{
	if(trace_nested_operations == 0)
	{
		return;
	}
	while(file -> nested_counter > file -> function_depth) 
	{
		hdLogWriteIndentation(file);
		hdLogWrite(file, "</Nested>\n");
		file -> nested_counter--;
	}
	while(file -> nested_counter < file -> function_depth)
	{
		hdLogWriteIndentation(file);
		hdLogWrite(file, "<Nested>\n");
		file -> nested_counter++;
	}
}

void hdT_StateStart(TraceFileP file)
{
	file -> function_depth++;
	writeNestedTags(file);
	file -> elements_pos[file -> function_depth] = 0;
	file -> attributes_pos[file -> function_depth] = 0;

	if(file -> function_depth < HD_LOG_MAX_DEPTH)
	{
		gettimeofday(&file -> start_time[file -> function_depth], NULL);
	}
}

size_t min(size_t a, size_t b)
{
	if(a < b)
		return a;
	return b;
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
	tracefile -> elements_pos[tracefile -> function_depth] = min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	write = vsnprintf(tracefile -> elements[tracefile -> function_depth] + tracefile -> elements_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> elements_pos[tracefile -> function_depth],
					 valueFormat, valist);
	tracefile -> elements_pos[tracefile -> function_depth] = min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);	
	write = snprintf(tracefile -> elements[tracefile -> function_depth] + tracefile -> elements_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> elements_pos[tracefile -> function_depth],
					 " />\n");
	tracefile -> elements_pos[tracefile -> function_depth] = min(tracefile -> elements_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
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
	/*
	write = snprintf(tracefile -> attributes[tracefile -> function_depth] + tracefile -> attributes_pos[tracefile -> function_depth],
					 HD_LOG_COMMAND_BUF_SIZE - (tracefile -> attributes_pos[tracefile -> function_depth]),
					 "%s='", name);
	tracefile -> attributes_pos[tracefile -> function_depth] = min(tracefile -> attributes_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	*/
	write = vsnprintf(tracefile -> attributes[tracefile -> function_depth] + tracefile -> attributes_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> attributes_pos[tracefile -> function_depth],
					 valueFormat, valist);
	tracefile -> attributes_pos[tracefile -> function_depth] = min(tracefile -> attributes_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);	
	write = snprintf(tracefile -> attributes[tracefile -> function_depth] + tracefile -> attributes_pos[tracefile -> function_depth],
					  HD_LOG_COMMAND_BUF_SIZE - tracefile -> attributes_pos[tracefile -> function_depth],
					 " ");
	tracefile -> attributes_pos[tracefile -> function_depth] = min(tracefile -> attributes_pos[tracefile -> function_depth] + write, HD_LOG_COMMAND_BUF_SIZE);
	va_end(valist);
}



void hdT_StateEnd(TraceFileP tracefile, const char * stateName, const char* format, ...)
{
	va_list valist;
	struct timeval end_time;
	gettimeofday(&end_time, NULL);

	hdLogWriteIndentation(tracefile);

	hdLogWrite(tracefile, "<");
	hdLogWrite(tracefile, stateName);
	hdLogWrite(tracefile, " ");

	// write pending attributes
	if(tracefile -> function_depth < HD_LOG_MAX_DEPTH 
	   && tracefile -> attributes_pos[tracefile -> function_depth] != 0)
	{
		hdLogWrite(tracefile, tracefile -> attributes[tracefile -> function_depth]);
	}

	//hdLogWrite(tracefile, " ");
	va_start(valist, format);
	hdLogWriteFormatv(tracefile, format, valist);
	va_end(valist);

	// write start and end times
	if(tracefile -> function_depth < HD_LOG_MAX_DEPTH )
	{
		hdLogWriteFormat(tracefile, " time='%d.%6d'", 
						 (unsigned)tracefile -> start_time[tracefile -> function_depth].tv_sec, 
						 (unsigned)tracefile -> start_time[tracefile -> function_depth].tv_usec);
	}
	hdLogWriteFormat(tracefile, " end='%d.%6d' ", 
						 (unsigned)end_time.tv_sec, 
						 (unsigned)end_time.tv_usec);

	// write pending elements
	if(tracefile -> function_depth < HD_LOG_MAX_DEPTH 
	   && tracefile -> elements_pos[tracefile -> function_depth] != 0)
	{
		hdLogWrite(tracefile, ">\n" HD_LOG_TAB_STRING);
		hdLogWriteIndentation(tracefile);
		hdLogWrite(tracefile, tracefile -> elements[tracefile -> function_depth]);
		hdLogWriteIndentation(tracefile);
		hdLogWrite(tracefile, "</");
		hdLogWrite(tracefile, stateName);
		hdLogWrite(tracefile, ">\n");
	}
	else
	{
		hdLogWrite(tracefile, " />\n");
	}


			   

	tracefile -> function_depth--;
}
//oder void hdLogStateEnd      (TraceFileP file, char* buff);


