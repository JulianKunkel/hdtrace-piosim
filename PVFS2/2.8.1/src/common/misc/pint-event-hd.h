
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

extern gen_mutex_t trove_relation;

int PINT_HD_event_initalize(const char * traceWhat, const char * projectFile);

/**
 * Traceable facilities
 */
typedef enum {
	BMI, TROVE, 
	FLOW, REQ, BREQ,
	SERVER, JOB,
	STATISTIC_END, //facility end
	NET, MEM, CPU, DISK,
	ALL_FACILITIES
} HD_Trace_Facility;

#define IO_TROVE_RELATION(hints, name, CMD, p_size, size, p_offset, offset) \
	const char * io_keys[] = {"size","offset"}; \
	char attr1[15],attr2[15]; \
	const char * io_values[] = {attr1, attr2}; \
	int run = 1; \
	HD_SERVER_RELATION(SERVER, \
			hdR_token relateToken = NULL; \
				hdR_token parentToken = *(hdR_token*) PINT_hint_get_value_by_name(hints, PVFS_HINT_RELATION_TOKEN_NAME, NULL); \
			\
			if (parentToken && topoTokenArray[TROVE]) \
			{ \
				gen_mutex_lock(&trove_relation); \
				relateToken = hdR_relateProcessLocalToken(topoTokenArray[TROVE], parentToken); \
				gen_mutex_unlock(&trove_relation); \
			} \
			\
			if (relateToken) \
			{ \
				gen_mutex_lock(&trove_relation); \
				hdR_startS(relateToken,name); \
				gen_mutex_unlock(&trove_relation); \
				run = 0; \
				\
				CMD \
				\
				snprintf(io_values[0], 15, p_size, size); \
				snprintf(io_values[1], 15, p_offset, offset); \
				\
				gen_mutex_lock(&trove_relation); \
				hdR_end(relateToken,2,io_keys,io_values); \
				hdR_destroyRelation(&relateToken); \
				gen_mutex_unlock(&trove_relation); \
			} \
	) \
	if(run){ \
		CMD \
	} \

#define HD_SERVER_RELATION(facility, stmt) \
	do{ if(topoTokenArray[facility]){ stmt } } while(0);

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
