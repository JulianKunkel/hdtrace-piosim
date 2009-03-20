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

#include <sys/time.h>

void hdStartTracing();
void hdStopTracing();

#define HD_LOG_BUF_SIZE (1024 * 1024)
#define HD_LOG_TAB_STRING "\t"
#define HD_LOG_COMMAND_BUF_SIZE 1024 * 16
#define HD_TMP_BUF_SIZE 1024 * 16
#define HD_LOG_MAX_DEPTH 10

//ideas for a trace API:


struct TraceFile
{
	int trace_fd; // file descriptor
	int info_fd;
	char buffer[HD_LOG_BUF_SIZE];
	char elements[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];
	size_t elements_pos[HD_LOG_MAX_DEPTH];
	char attributes[HD_LOG_MAX_DEPTH][HD_LOG_COMMAND_BUF_SIZE];
	size_t attributes_pos[HD_LOG_MAX_DEPTH];

	struct timeval start_time[HD_LOG_MAX_DEPTH];
	
	size_t buffer_pos;
	int thread;
	int function_depth; // keeps track of the depth of nested function calls
	int nested_counter; // current depth of <Nested> tags in logfile
};

typedef struct TraceFile * TraceFileP;

/**
 * Defines information about a single statistic file group.
 */
struct StatisticFileGroup{
	int fd;
};

typedef struct StatisticFileGroup * StatisticFileGroupP;

void hdTraceInit(const char * filePrefix); 

// /tmp/test => /tmp/test.xml /tmp/test_<rank>_<thread>.xml .info

/**
 * create a new trace file and registers it.
 * Note that each thread must create its own trace file. However, the TraceFileP can be reused if it is
 * guaranteed that it is not used by several threads to write different states/events at the same time.
 *
 * This function is thread safe.
 *
 * @returns a TraceFile which must be used for further input or NULL on error
 */
TraceFileP hdTraceCreate(int rank, int thread);
/**
 * Closes an open trace file.
 * Must be called at the end of the program.
 */
void hdTraceFinalize(TraceFileP file);



/**
 * Can be called after LogStateStart or LogEventStart to write elements to the state/event.
 * Affects the last State for which hdLogStateEnd has not been called
 */
void hdLogElement    (TraceFileP file, const char * name, const char* valueFormat, ...);

/**
 * Can be called after LogStateStart or LogEventStart to write attributes to the state/event.
 * Affects the last State for which hdLogStateEnd has not been called
 */
void hdLogAttributes (TraceFileP file, const char* valueFormat, ...);
//"comm=\"%s\" name=\"%s\" flags=\"%d\" fh=\"%p\" ret='%d'",
//	  getCommName(comm), name, flags, getFileHandleName(fh), ret);

void hdLogStateStart    (TraceFileP file);
void hdLogStateEnd      (TraceFileP file, const char * stateName, const char* sprinhdStringForFurtherValues, ...);
//oder void hdLogStateEnd      (TraceFileP file, char* buff);


void hdLogEventStart    (TraceFileP file, char * eventName );
// Variable list of arguments: http://publications.gbdirect.co.uk/c_book/chapter9/stdarg.html
void hdLogEventEnd      (TraceFileP file, char* sprinhdStringForFurtherValues, ...);
//void hdLogEventEnd      (TraceFileP file, char* buff); falls so dann mergen mit hdLogEventStart.


enum hdStatisticType { INT, FLOAT, DOUBLE, LONG, STRING};

/**
 * Semantics of Statistic Groups: create the group first, then add all statistics,
 * then commit it, before it can be used.
 */
StatisticFileGroupP hdStatisticGroupCreate(int rank, char* groupName);

void hdStatisticGroupAddStatistic(StatisticFileGroupP group, char * name, enum hdStatisticType type,
		char * unit, long readOutMultiplier);

void hdStatisticGroupCommit(StatisticFileGroupP group);

void hdStatisticsNextTimestamp(StatisticFileGroupP group); // write timestamp, check all are written.

/**
 * Write order must be the same order as the statistics are registered!
 */
void hdStatisticsWriteNumeric(StatisticFileGroupP group, void * value, int valueLength);
void hdStatisticsWriteString (StatisticFileGroupP group, char * str);

/**
 * Closes a statistic group, must be called at the end of the program.
 */
void hdStatisticGroupFinalize(StatisticFileGroupP group);

/*
hi, denke im Grunde haben wir drei typen von Stat files, pro Prozess, pro Thread (geht ja schon), und pro Host.
das pro Prozess wär ins Namenschema leicht zu mappen => es fehlt einfach die Thread ID :)
pro Host ist ja schwieriger, denke mal da heißts dann <Projekt>_<Hostname>_stat_Energy.dat
welche maschinen auf welchem Host gestartet wurden kann man ins Projekt file aufnehmen
=> im MPI_init wird einfach der Hostname rausgeschrieben und der Rank...
wäre so lösbar.
*/



#define tprintf(format, ...) printf("[TRACER][%d] " format "\n", w_my_rank, __VA_ARGS__);
#define tsprintf(format)     printf("[TRACER][%d] " format "\n", w_my_rank);
