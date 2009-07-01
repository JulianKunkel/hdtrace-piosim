
#ifndef __PINT_EVENT_CLIENT_HD_H
#define __PINT_EVENT_CLIENT_HD_H

#include "pvfs2-types.h"

/** Makros zum schreiben */

#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "pint-event.h"
#include "hdRelation.h"

/**
 * Traceable facilities
 */
typedef enum {
	BMI, 
	FLOW,
	CLIENT,
	STATISTIC_END,
	NET, MEM, CPU, DISC,
	ALL_FACILITIES
} HD_Trace_Facility;

#define HD_CLIENT_RELATION(facility, stmt) \
	do{ if(topoTokenArray[facility]){ stmt } } while(0);

extern hdR_topoToken topoTokenArray[STATISTIC_END];

int PVFS_HD_client_trace_initialize(hdTopology topo, hdTopoNode parentNode);

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENT_CLIENT_HD_H */
