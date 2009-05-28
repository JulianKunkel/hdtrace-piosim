
#ifndef __PINT_EVENTHD_H
#define __PINT_EVENTHD_H

#include "pvfs2-types.h"

/** Makros zum schreiben */

#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"


int PINT_eventHD_finalize(void);
int PINT_eventHD_initalize(char * traceWhat);

/**
 * Traceable facilities
 */
typedef enum {
	BMI, TROVE, FLOW, SM, ALL_FACILITIES
} HD_Trace_Facility;

/**
 * Array: State of each facility i.e. is it enabled or not
 */

extern hdStatsGroup hd_facilityTrace[ALL_FACILITIES]; 

extern int hd_facilityTraceStatus[ALL_FACILITIES]; 

#define PINT_HD_UPDATE_COUNTER(facility, value) if (hd_facilityTraceStatus[facility]) hdS_writeInt32Value(hd_facilityTrace[facility], value);

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */

