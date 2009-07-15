/**
 * @file trace.h
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef TRACE_H_
#define TRACE_H_

#include "hdStats.h"

/**
 * Bitfield of supported values to measure
 */
typedef struct {
	int Utrms : 1;
	int Itrms : 1;
	int P : 1;
} ValueField;

/**
 * Structure for trace file description
 */
typedef struct trace_s {
	short num;
#if 0
	int bin : 1;
	int ascii : 1;
#endif
	int hdstats : 1;
	int channel;
	ValueField values;
	char *output;

    char *actn; /* action script part for this trace */
    int   size;    /* size of expected response in [byte] */

    hdTopoNode *tnode;   /* topology node, reference needed for destruction */
    hdStatsGroup *group; /* statistics group this belongs to */

    struct trace_s *prev;
} TraceStruct;

/**
 * List head (or actual foot) for list of traces
 */
typedef struct traceList_s {
	/** Last element of the linked list */
	TraceStruct *last;
} TraceListStruct;

/**
 * Add a trace to a trace linked list
 */
void addTraceToList(TraceStruct *trace, TraceListStruct *list);

/**
 * Macro to use for iterating over all traces in a configuration.
 * Use FOR_TRACES(tracelist) { ... }
 */
#define FOR_TRACES(trace, list) \
	for (TraceStruct *trace = (list).last; trace != NULL; trace = trace->prev)

/**
 * Free all traces in a trace linked list
 */
void freeAllTraces(TraceListStruct *list);

#endif /* TRACE_H_ */
