
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

int PINT_HD_event_finalize(void); /* client also uses this function */

#ifdef __PVFS2_SERVER__

int PINT_HD_event_initalize(char * traceWhat);

/**
 * Traceable facilities
 */
typedef enum {
	BMI, TROVE, 
	FLOW, REQ, BREQ,
	SERVER, JOB,
	STATISTIC_END, //facility end
	NET, CPU, MEM, DISC,
	ALL_FACILITIES
} HD_Trace_Facility;

struct _hdHintRelationStructure{
	pthread_mutex_t mutex;
	hdR_token token;
};

#define HD_SERVER_RELATION(facility, stmt) \
	do{ if(topoTokenArray[facility]){ stmt } } while(0);

typedef struct _hdHintRelationStructure hdHintRelation_t;
typedef struct _hdHintRelationStructure * hdHintRelation_p;

extern hdR_topoToken topoTokenArray[STATISTIC_END];

#define PINT_HD_UPDATE_COUNTER_INC_SERVER(facility) PINT_HD_update_counter_inc(facility);
#define PINT_HD_UPDATE_COUNTER_DEC_SERVER(facility) PINT_HD_update_counter_dec(facility);
#define PINT_HD_UPDATE_COUNTER_INC_MULTIPLE_SERVER(facility, count) PINT_HD_update_counter_inc_multiple(facility, count);
#define PINT_HD_UPDATE_COUNTER_DEC_MULTIPLE_SERVER(facility, count) PINT_HD_update_counter_dec_multiple(facility, count);

#else /* CLIENT inside PVFS2 OR external client application */
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

#define PINT_HD_UPDATE_COUNTER_INC_SERVER(facility);
#define PINT_HD_UPDATE_COUNTER_DEC_SERVER(facility);
#define PINT_HD_UPDATE_COUNTER_INC_MULTIPLE_SERVER(facility, count);
#define PINT_HD_UPDATE_COUNTER_DEC_MULTIPLE_SERVER(facility, count);

#endif /* __PVFS2_SERVER__ */

#define PINT_HD_UPDATE_COUNTER_INC(facility) PINT_HD_update_counter_inc(facility);
#define PINT_HD_UPDATE_COUNTER_DEC(facility) PINT_HD_update_counter_dec(facility);
#define PINT_HD_UPDATE_COUNTER_INC_MULTIPLE(facility, count) PINT_HD_update_counter_dec_multiple(facility, count);
#define PINT_HD_UPDATE_COUNTER_DEC_MULTIPLE(facility, count) PINT_HD_update_counter_dec_multiple(facility, count);

int PINT_HD_update_counter_inc(HD_Trace_Facility facility);
int PINT_HD_update_counter_dec(HD_Trace_Facility facility);
int PINT_HD_update_counter_inc_multiple(HD_Trace_Facility facility, int count);
int PINT_HD_update_counter_dec_multiple(HD_Trace_Facility facility, int count);

#else /* __HAVE_HDTRACE__ */

#define PINT_HD_UPDATE_COUNTER_INC(facility);
#define PINT_HD_UPDATE_COUNTER_DEC(facility);
#define PINT_HD_UPDATE_COUNTER_INC_MULTIPLE(facility, count);
#define PINT_HD_UPDATE_COUNTER_DEC_MULTIPLE(facility, count);

#define HD_SERVER_RELATION (facility stmt)
#define HD_DESTROY_RELATION(facility, token) 

#endif /* __HAVE_HDTRACE__ */

#endif /* __PINT_EVENTHD_H */
