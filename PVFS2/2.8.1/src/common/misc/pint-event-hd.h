
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
	BMI = 0, TROVE, 
	FLOW, REQ, BREQ,
	NET, CPU, MEM, DISK,
	ALL_FACILITIES
} HD_Trace_Facility;

int PINT_HD_update_counter_inc(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec_multiple(HD_Trace_Facility facility, int count);

int PINT_HD_update_counter_get(HD_Trace_Facility facility); 

//struct hintRelationToken{
//	gen_mutex_t mutex;
//	hdR_token token;
//};

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */
