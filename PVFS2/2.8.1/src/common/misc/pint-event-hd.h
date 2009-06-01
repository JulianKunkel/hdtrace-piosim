
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
	BMI, TROVE, FLOW, REQ, BLOCK_REQ, ALL_FACILITIES
} HD_Trace_Facility;

int PINT_HD_update_counter_inc(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec(HD_Trace_Facility facility);

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */

