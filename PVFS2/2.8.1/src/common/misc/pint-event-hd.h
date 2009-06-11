
#ifndef __PINT_EVENTHD_H
#define __PINT_EVENTHD_H

#include "pvfs2-types.h"

/** Makros zum schreiben */

#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"
#include "hdRelation.h"
#include "gen-locks.h"


int PINT_HD_event_initalize(char * traceWhat);

int PINT_HD_event_finalize(void);

/**
 * Traceable facilities
 */
typedef enum {
	BMI, TROVE, 
	FLOW, REQ, BREQ,
	SERVER, JOB,
	STATISTIC_END, //facility end
	NET, CPU, MEM,
	REL,
	ALL_FACILITIES
} HD_Trace_Facility;

struct _hdHintRelationStructure{
	pthread_mutex_t mutex;
	hdR_token token;
};


#ifdef HAVE_HDTRACE
	#define HD_RELATION(facility, stmt) \
	do{        if(topoTokenArray[facility]){ stmt }  }while(0);

	#define HD_DESTROY_RELATION(facility, token) \
	if(topoNodeArray[facility]){ \
		hdR_destroyRelation(& token); \
	} else { \
		free(token); \
	}

#else
	#define HD_RELATION (facility stmt)
	#define HD_DESTROY_RELATION(facility, token) 
#endif

typedef struct _hdHintRelationStructure hdHintRelation_t;
typedef struct _hdHintRelationStructure * hdHintRelation_p;

extern const char *hdFacilityNames[];

extern hdR_topoToken topoTokenArray[STATISTIC_END] ;
extern hdTopoNode topoNodeArray[STATISTIC_END];

int PINT_HD_update_counter_inc(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec(HD_Trace_Facility facility);

int PINT_HD_update_counter_dec_multiple(HD_Trace_Facility facility, int count);

int PINT_HD_update_counter_get(HD_Trace_Facility facility); 

extern hdR_topoToken topoToken;

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */
