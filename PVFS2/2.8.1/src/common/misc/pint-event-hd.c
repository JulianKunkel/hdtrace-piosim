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
#include <unistd.h>
#include <string.h>
#include "hdTopo.h"

#ifdef HAVE_HDPTL
#include "PTL.h"
static ptlSources statistics;
static PerfTrace pStatistics;
#endif

static hdStatsGroup hd_facilityTrace[ALL_FACILITIES];
static int hd_facilityTraceStatus[ALL_FACILITIES];
static int hdStatsGroupValue[ALL_FACILITIES];
static gen_mutex_t hdStatsGroupMutex[ALL_FACILITIES] ;
hdR_topoToken topoTokenArray[STATISTIC_END];
static hdTopoNode topoNodeArray[STATISTIC_END];
static hdTopology topology;

#ifdef __PVFS2_SERVER__
const char * hdFacilityNames[] = {"BMI", "TROVE", "FLOW", "REQ", "BREQ", "SERVER", "JOB", "STATISTIC_END"};
#endif

#ifdef __PVFS2_CLIENT__

const char * hdFacilityNames[] = {"BMI", "", "CLIENT"};
hdR_topoToken topoTokenArray[STATISTIC_END];

static hdTopoNode topoNode;

int PVFS_HD_client_trace_initialize(void)
{
	char hostname[255];
	int ret;
	ret = gethostname(hostname, 255);

	if (ret != 0)
	{
		fprintf(stderr, "Problem with hostname !\n");
		return 1;
	}
	
	// im MPI program:
	const char * levels[]={"Host", "Rank", "Thread"};
	topology = hdT_createTopology("/tmp/Client", levels, 3);
	const char * nodeP[]={hostname, "BMI", "0"};
	topoNode = hdT_createTopoNode(topology, nodeP , 3);
	// ende MPI program
	
	/* statistic trace for client BMI*/
	hd_facilityTraceStatus[BMI] = 1;
	hd_facilityTrace[BMI] = hdS_createGroup("BMI", topoNode, 1);
	hdS_addValue(hd_facilityTrace[BMI], "BMI", INT32, "#", NULL);
	hdS_commitGroup(hd_facilityTrace[BMI]);
	hdS_enableGroup(hd_facilityTrace[BMI]);
	hdR_initTopology(topoNode, & topoTokenArray[CLIENT]);
	
	set_hd_sm_trace_enabled(1);
	return 0;
}

int PVFS_HD_client_trace_finalize(void)
{
	if(topoNode != NULL)
	{	
		set_hd_sm_trace_enabled(0);
		hd_facilityTraceStatus[BMI] = 0;
		hdR_finalize(topoNode);
		topoNode = NULL;
	}
	
	if(hd_facilityTraceStatus[BMI] && hd_facilityTrace[BMI] != NULL)
	{
		hdS_writeInt32Value(hd_facilityTrace[BMI], hdStatsGroupValue[BMI]);
		hdS_finalize(hd_facilityTrace[BMI]);
		hd_facilityTraceStatus[BMI] = 0;
		hd_facilityTrace[BMI] = NULL; 
	}
	
	if(topoNode != NULL)
	{	
		hdR_finalize(topoNode);
		topoNode = NULL;
		topoTokenArray[CLIENT] = NULL;
	}
	
	return 0;
}

#endif /* __PVFS2_CLIENT__ */

static void testInitFacilityStatisticTrace(hdTopoNode topoNode , HD_Trace_Facility facilityNum)
{	
	hd_facilityTraceStatus[facilityNum] = 1;
	hd_facilityTrace[facilityNum] = hdS_createGroup(hdFacilityNames[facilityNum], topoNode, 1);
	hdS_addValue(hd_facilityTrace[facilityNum], hdFacilityNames[facilityNum], INT32, "#", NULL);
	hdS_commitGroup(hd_facilityTrace[facilityNum]);
	hdS_enableGroup(hd_facilityTrace[facilityNum]);
}

int PINT_HD_event_initalize(char * traceWhat)
{	
	char hostname[255];
	int ret;
	ret = gethostname(hostname, 255);

	if (ret != 0)
	{
		fprintf(stderr, "Problem with hostname !\n");
		return 1;
	}

	char **event_list;
	int i, count;
	count = PINT_split_string_list(&event_list, traceWhat);

	const char *levels[] = {"Hostname", "Layer", "Client"};
	topology = hdT_createTopology("/tmp/MyProject", levels, 3);

	for(i=0; i < count; i++)
	{
		printf("Enable: %s\n", event_list[i]);
		int facilityNum;
		for (facilityNum = 0; facilityNum < STATISTIC_END; facilityNum++)
		{
			if((strcasecmp(event_list[i], hdFacilityNames[facilityNum]) == 0) && !hd_facilityTrace[facilityNum])
			{
				const char *path[] = {hostname,hdFacilityNames[facilityNum] , "0"};
				hdTopoNode topoNode = hdT_createTopoNode(topology, path, 3);

				testInitFacilityStatisticTrace(topoNode, facilityNum);
				topoNodeArray[facilityNum] = topoNode;
				hdR_initTopology(topoNodeArray[facilityNum], & topoTokenArray[facilityNum]);
				
				break;
			}
		}

#ifdef HAVE_HDPTL

		if (strcasecmp(event_list[i],"NET") == 0)
		{	
			hd_facilityTraceStatus[NET] = 1;
			statistics.PTLSRC_NET_IN = 1;
			statistics.PTLSRC_NET_OUT = 1;
		}

		if (strcasecmp(event_list[i],"MEM") == 0)
		{	
			hd_facilityTraceStatus[MEM] = 1;
			statistics.PTLSRC_MEM_USED = 1;
			statistics.PTLSRC_MEM_FREE = 1;
			statistics.PTLSRC_MEM_BUFFER = 1;
		}

		if (strcasecmp(event_list[i],"CPU") == 0)
		{	
			hd_facilityTraceStatus[CPU] = 1;
			statistics.PTLSRC_CPU_LOAD = 1;
		}

		if (hd_facilityTraceStatus[NET] || hd_facilityTraceStatus[MEM] || hd_facilityTraceStatus[CPU])
		{
			pStatistics = ptl_createTrace(topology, topoNode, 1, statistics, 700);
			ptl_startTrace(pStatistics);
		}

#endif /* __HAVE_HDPTL__ */

	}

	set_hd_sm_trace_enabled(1);

	return 0;
}

int PINT_HD_event_finalize(void)
{
	// called by SM thread => no mutex needed.
	set_hd_sm_trace_enabled(0);

	int i;
	for (i = 0 ; i < ALL_FACILITIES; i++)
	{
		if(hd_facilityTraceStatus[i] && hd_facilityTrace[i] != NULL)
		{
			hdS_writeInt32Value(hd_facilityTrace[i], hdStatsGroupValue[i]);
			hdS_finalize(hd_facilityTrace[i]);
			hd_facilityTraceStatus[i] = 0;
			hd_facilityTrace[i] = NULL; 
		}
	}
	for (i = 0 ; i < STATISTIC_END; i++){
		if(topoNodeArray[i] != NULL)
		{	
			hdR_finalize(topoNodeArray[i]);
			topoNodeArray[i] = NULL;
			topoTokenArray[i] = NULL;
		}
	}
#ifdef HAVE_HDPTL
	if (pStatistics != NULL)	
	{
		ptl_stopTrace(pStatistics);
		ptl_destroyTrace(pStatistics);
	}
#endif
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
