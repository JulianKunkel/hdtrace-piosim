/**
 * @file trace.c
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include <stdlib.h>

#include "trace.h"

#include "common.h"

/* ************************************************************************* *
 *                            Trace List Handling                            *
 * ************************************************************************* */

/**
 * Add a trace to a trace linked list
 *
 * @param trace  Trace to add
 * @param list   Trace list
 */
void addTraceToList(TraceStruct *trace, TraceListStruct *list) {
	trace->prev = list->last;
	trace->num = (list->last == NULL) ? 0 : list->last->num + 1;
	list->last = trace;
}

/**
 * Free all traces in a trace linked list
 *
 * @param list Trace list
 */
void freeAllTraces(TraceListStruct *list) {
	/*
	 * Free all memory allocated in each trace
	 */
	FOR_TRACES(*list) {
		if (trace->hdstats) {
			hdS_finalize(trace->group);
			hdT_destroyTopoNode(trace->tnode);
		}
		if (trace->actn)
			pt_free(trace->actn); // allocated in createTraces()
	}

	/*
	 * Free memory for traces
	 */
	TraceStruct *this = list->last;
	while (this != NULL) {
		TraceStruct *next = this->prev;
		pt_free(this);
		this = next;
	}

	list->last = NULL;
}
