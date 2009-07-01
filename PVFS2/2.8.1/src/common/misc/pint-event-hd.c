#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"
#include  "str-utils.h"
#include "state-machine.h"
#include "gen-locks.h"
#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "hdTopo.h"
#include "hdRelation.h"

#ifdef HAVE_HDPTL
#include "PTL.h"
#endif

static hdStatsGroup hd_facilityTrace[ALL_FACILITIES];
static int hd_facilityTraceStatus[ALL_FACILITIES];
static int hdStatsGroupValue[ALL_FACILITIES];
static gen_mutex_t hdStatsGroupMutex[ALL_FACILITIES] ;
hdR_topoToken topoTokenArray[STATISTIC_END];
static hdTopology topology;
static char hostname[255];
static hdTopoNode topoNodeArray[ALL_FACILITIES];

#ifdef __PVFS2_CLIENT__
const char * hdFacilityNames[] = {"BMI", "FLOW", "CLIENT", "STATISTIC_END",
		"NET", "MEM", "CPU", "DISC", "ALL_FACILITIES"};
#endif

#ifdef __PVFS2_SERVER__
static ptlSources statistics;
static PerfTrace pStatistics[ALL_FACILITIES];
const char * hdFacilityNames[] = {"BMI", "TROVE", "FLOW", "REQ", "BREQ", "SERVER", "JOB", "STATISTIC_END", 
		"NET", "CPU", "MEM", "DISC", "ALL_FACILITIES"};
#endif

static int checkHostname(void)
{
	if (gethostname(hostname, 255) != 0)
	{
		fprintf(stderr, "Problem with hostname !\n");
		return 1;
	}
	return 0;
}

static void testInitFacilityStatisticTrace(hdTopoNode topoNode , HD_Trace_Facility facilityNum)
{	
	hd_facilityTraceStatus[facilityNum] = 1;
	hd_facilityTrace[facilityNum] = hdS_createGroup(hdFacilityNames[facilityNum], topoNode, 1);
	hdS_addValue(hd_facilityTrace[facilityNum], hdFacilityNames[facilityNum], INT32, "#", NULL);
	hdS_commitGroup(hd_facilityTrace[facilityNum]);
	hdS_enableGroup(hd_facilityTrace[facilityNum]);
}

/*
 * CLIENT
 * */

#ifdef __PVFS2_CLIENT__

inline static void createTopologyNode(HD_Trace_Facility facility, int depth, hdTopoNode parentNode){
	const char ** labels = malloc(sizeof(char*) * (depth + 1) );
	int i;
	if(labels == NULL){
		fprintf(stderr,"ERROR %p!\n",labels);
		exit(1);
	}

	for(i=1; i <= depth; i++){
		labels[i-1] = hdT_getTopoPathLabel(parentNode, i);
	}
	labels[depth] = hdFacilityNames[facility];
	topoNodeArray[facility] = hdT_createTopoNode(topology, labels, depth+1);
	free(labels);
}

int PVFS_HD_client_trace_initialize(hdTopology topo, hdTopoNode parentNode)
{
	checkHostname();

	topology = topo;

	const int depth = hdT_getTopoNodeLevel(parentNode);

	if(hdT_getTopoDepth(topo) <= depth)
	{
		/* this is invalid, we need at least a deeper topology */
		return -1;
	}

	// getenv, PVFS2_HD_TRACE_CLIENT=BMI,JOB,STATEMACHINE ...
	const char * clientTraceNames = getenv("PVFS2_HD_TRACE_CLIENT");
	
	if (clientTraceNames == NULL){
		printf("the enviroment variable PVFS2_HD_TRACE_CLIENT not set in %s!\n",hostname);
	}
	else
	{
		char **event_list;
		int i, count;
		count = PINT_split_string_list(&event_list, clientTraceNames);

		for (i = 0; i < count; i++){
			
			if (!strcasecmp(event_list[i],"CLIENT"))
			{
			createTopologyNode(CLIENT, depth, parentNode);
			hdR_initTopology(topoNodeArray[CLIENT], & topoTokenArray[CLIENT]);
			}
			
			if (!strcasecmp(event_list[i],"BMI"))
			{
				createTopologyNode(BMI, depth, parentNode);
				testInitFacilityStatisticTrace(topoNodeArray[BMI], BMI);
			}
			
			
			if (!strcasecmp(event_list[i],"FLOW"))
			{
				createTopologyNode(FLOW, depth, parentNode);
				testInitFacilityStatisticTrace(topoNodeArray[FLOW], FLOW);
			}
		}
		free(event_list);
	}

	set_hd_sm_trace_enabled(1);
	return 0;
}
#endif /* __PVFS2_CLIENT__ */

/*
 * SERVER
 * */

#ifdef __PVFS2_SERVER__

int PINT_HD_event_initalize(char * traceWhat)
{	
	checkHostname();

	char **event_list;
	int i, count;
	count = PINT_split_string_list(&event_list, traceWhat);

	const char *levels[] = {"Hostname", "Layer"};
	topology = hdT_createTopology("/tmp/MyProject", levels, 2);

	for(i=0; i < count; i++)
	{
		printf("Enable: %s\n", event_list[i]);
		int facilityNum;
		for (facilityNum = 0; facilityNum < ALL_FACILITIES; facilityNum++)
		{
			if((!strcasecmp(event_list[i], hdFacilityNames[facilityNum])) && !hd_facilityTrace[facilityNum])
			{
				const char *path[] = {hostname, hdFacilityNames[facilityNum]};
				topoNodeArray[facilityNum] = hdT_createTopoNode(topology, path, 2);
				//				if (facilityNum != SERVER)
				testInitFacilityStatisticTrace(topoNodeArray[facilityNum], facilityNum);
				//				if (facilityNum == SERVER)
				hdR_initTopology(topoNodeArray[facilityNum], & topoTokenArray[facilityNum]);
				break;
			}
		}

#ifdef HAVE_HDPTL

		if (!strcasecmp(event_list[i],"NET"))
		{	
			hd_facilityTraceStatus[NET] = 1;
			statistics.PTLSRC_NET_IN = 1;
			statistics.PTLSRC_NET_OUT = 1;
			pStatistics[NET] = ptl_createTrace(topoNodeArray[NET], 1, statistics, 700);
			ptl_startTrace(pStatistics[NET]);
		}

		if (!strcasecmp(event_list[i],"MEM"))
		{	
			hd_facilityTraceStatus[MEM] = 1;
			statistics.PTLSRC_MEM_USED = 1;
			statistics.PTLSRC_MEM_FREE = 1;
			statistics.PTLSRC_MEM_BUFFER = 1;
			pStatistics[MEM] = ptl_createTrace(topoNodeArray[MEM], 1, statistics, 700);
			ptl_startTrace(pStatistics[MEM]);
		}

		if (!strcasecmp(event_list[i],"CPU"))
		{	
			hd_facilityTraceStatus[CPU] = 1;
			statistics.PTLSRC_CPU_LOAD = 1;
			pStatistics[CPU] = ptl_createTrace(topoNodeArray[CPU], 1, statistics, 700);
			ptl_startTrace(pStatistics[CPU]);
		}

		if (!strcasecmp(event_list[i],"DISK"))
		{	
			/* to do */
		}

#endif /* __HAVE_HDPTL__ */

	}

	set_hd_sm_trace_enabled(1);
	
	free(event_list);

	return 0;
}

#endif /* __PVFS2_SERVER__ */

int PINT_HD_event_finalize(void)
{
	// called by SM thread => no mutex needed.
	set_hd_sm_trace_enabled(0);

	int i;
	for (i = 0; i < ALL_FACILITIES; i++)
	{
		if(hd_facilityTraceStatus[i] && hd_facilityTrace[i] != NULL)
		{
			hdS_writeInt32Value(hd_facilityTrace[i], hdStatsGroupValue[i]);
			hdS_finalize(hd_facilityTrace[i]);
			hd_facilityTraceStatus[i] = 0;
			hd_facilityTrace[i] = NULL; 
		}
		if(topoNodeArray[i] != NULL)
			topoNodeArray[i] = NULL;
	}
	for (i = 0; i < STATISTIC_END; i++)
	{
		if(topoTokenArray[i] != NULL)
		{
			hdR_finalize(&topoTokenArray[i]);
			topoTokenArray[i] = NULL;
		}
	}
#ifdef __PVFS2_SERVER__
#ifdef HAVE_HDPTL
	int ptl = NET;
	while(ptl <= MEM){
		if (pStatistics[ptl] != NULL)	
		{
			ptl_stopTrace(pStatistics[ptl]);
			ptl_destroyTrace(pStatistics[ptl]);
		}
		ptl++;
	}
#endif /* __HAVE_HDPTL__*/
#endif /* __PVFS2_SERVER__ */

	return 0;
}

int PINT_HD_update_counter_inc(HD_Trace_Facility facility) 
{
	gen_mutex_lock(&hdStatsGroupMutex[facility]);

	if (hd_facilityTraceStatus[facility]) 
	{	
		hdS_writeInt32Value(hd_facilityTrace[facility], hdStatsGroupValue[facility]);
	}

	++hdStatsGroupValue[facility];
	gen_mutex_unlock(&hdStatsGroupMutex[facility]);

	return 0;
}

int PINT_HD_update_counter_dec(HD_Trace_Facility facility) 
{
	gen_mutex_lock(&hdStatsGroupMutex[facility]);

	if (hd_facilityTraceStatus[facility]) 
	{	
		hdS_writeInt32Value(hd_facilityTrace[facility], hdStatsGroupValue[facility]);	
	}

	--hdStatsGroupValue[facility];
	gen_mutex_unlock(&hdStatsGroupMutex[facility]);
	return 0;
}

int PINT_HD_update_counter_inc_multiple(HD_Trace_Facility facility, int count) 
{
	if(count <= 0)
		return 0;

	gen_mutex_lock(&hdStatsGroupMutex[facility]);

	if (hd_facilityTraceStatus[facility]) 
	{	
		hdS_writeInt32Value(hd_facilityTrace[facility], hdStatsGroupValue[facility]);
	}
	hdStatsGroupValue[facility] += count;

	gen_mutex_unlock(&hdStatsGroupMutex[facility]);
	return 0;
}

int PINT_HD_update_counter_dec_multiple(HD_Trace_Facility facility, int count) 
{
	if(count <= 0)
		return 0;

	gen_mutex_lock(&hdStatsGroupMutex[facility]);

	if (hd_facilityTraceStatus[facility]) 
	{	
		hdS_writeInt32Value(hd_facilityTrace[facility], hdStatsGroupValue[facility]);
	}
	hdStatsGroupValue[facility] -= count;

	gen_mutex_unlock(&hdStatsGroupMutex[facility]);
	return 0;
}

#else

/* NO HDTRACE AVAILABLE */

int PINT_HD_event_initalize(void){
	return 0;
}

int PINT_HD_event_finalize(void){
	return 0;
}


#endif /* __HAVE_HDTRACE__ */
