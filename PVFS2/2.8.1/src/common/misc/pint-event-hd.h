
#ifndef __PINT_EVENTHD_H
#define __PINT_EVENTHD_H

#include "pvfs2-types.h"

/** Makros zum schreiben */

#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"


int PINT_HD_event_initalize(char * traceWhat);

int PINT_HD_event_finalize(void);

/**
 * Traceable facilities
 */
typedef enum {
	TROVE, 
	BMI, 
	FLOW, REQ, BREQ,
	statisticsNET, statisticsCPU, statisticsMEM, statisticsDISK,
	ALL_FACILITIES
} HD_Trace_Facility;

int PINT_HD_update_counter_inc(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec_multiple(HD_Trace_Facility facility, int count);

int PINT_HD_update_counter_get(HD_Trace_Facility facility); 

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */

