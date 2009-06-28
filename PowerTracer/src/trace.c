/**
 * @file trace.c
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include "trace.h"

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
	TraceStruct *this = list->last;
	while (this != NULL) {
		TraceStruct *next = this->prev;
		free(this);
		this = next;
	}
}

