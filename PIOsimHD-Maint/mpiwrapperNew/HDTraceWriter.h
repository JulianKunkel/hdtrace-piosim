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

void SimStartTracing();
void SimStopTracing();


//ideas for a trace API:

struct TraceFile{
	int fd; // file descriptor
};

typedef struct TraceFile * TraceFileP;


/**
 * Defines information about a single statistic file group.
 */
struct StatisticFileGroup{
	int fd;
};

typedef struct StatisticFileGroup * StatisticFileGroupP;

/**
 * create a new trace file and registers it.
 * Note that each thread must create its own trace file. However, the TraceFileP can be reused if it is
 * guaranteed that it is not used by several threads to write different states/events at the same time.
 *
 * This function is thread safe.
 *
 * @returns a TraceFile which must be used for further input
 */
TraceFileP hdTraceCreate(int rank);

/**
 * Closes an open trace file.
 * Must be called at the end of the program.
 */
void hdTraceFinalize(TraceFileP file);

void hdLogEventStart    (TraceFileP file, char * eventName );
// Variable list of arguments: http://publications.gbdirect.co.uk/c_book/chapter9/stdarg.html
void hdLogEventEnd      (TraceFileP file, char* sprinhdStringForFurtherValues, ...);

/**
 * Can be called after LogStateStart or LogEventStart to write attributes to the state/event.
 */
void hdLogAttributes    (TraceFileP file, char* sprinhdAttributes, ...);

void hdLogStateStart    (TraceFileP file, char * stateName);
void hdLogStateEnd      (TraceFileP file, char* sprinhdStringForFurtherValues, ...);



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




#define tprintf(format, ...) printf("[TRACER][%d] " format "\n", w_my_rank, __VA_ARGS__);
#define tsprintf(format)     printf("[TRACER][%d] " format "\n", w_my_rank);
