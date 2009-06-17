
#ifndef __PINT_EVENT_CLIENT_HD_H
#define __PINT_EVENT_CLIENT_HD_H

#include "pvfs2-types.h"

/** Makros zum schreiben */

#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#define HD_CLIENT_RELATION(facility, stmt) \
	do{ if(topoClientTokenArray[facility]){ stmt } } while(0);	

#include "pint-event.h"
#include "hdRelation.h"


/**
 * Traceable facilities
 */
typedef enum {
	CLIENT,
	ALL_CLIENT_FACILITIES
} HD_Trace_Client_Facility;

int PVFS_hd_client_trace_initialize(hdTopoNode topoNode);

extern hdR_topoToken topoClientTokenArray[ALL_CLIENT_FACILITIES];

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENT_CLIENT_HD_H */
