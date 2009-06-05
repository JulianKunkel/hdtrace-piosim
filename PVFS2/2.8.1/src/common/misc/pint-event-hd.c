#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"
#include "pint-event-hd.h"
#include  "str-utils.h"
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

int PINT_HD_event_initalize(char * traceWhat)
{	
	char hostname[255];
	int len;
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
	
	for(i=0; i < count; i++)
	{
		printf("Enable: %s\n", event_list[i]);
		hdTopoNode topoNode = NULL;
		hdTopology topology = NULL;
		
		if (!topoNode || !topology)
		{
			const char *path[] = {hostname, event_list[i], "0"};
			len = sizeof(path)/sizeof(int);
			topoNode = hdT_createTopoNode(path, len);
		
			const char *levels[] = {"Hostname", "Layer", "Client"};
			topology = hdT_createTopology("/tmp/MyProject", levels, len);
		}
		
		if ((strcasecmp(event_list[i],"bmi") == 0) && !hd_facilityTrace[BMI])
		{	
			hd_facilityTraceStatus[BMI] = 1;
			hd_facilityTrace[BMI] = hdS_createGroup("BMI", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[BMI],"BMI", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[BMI]);
			hdS_enableGroup(hd_facilityTrace[BMI]);
		}
		
		if ((strcasecmp(event_list[i],"trove") == 0) && !hd_facilityTrace[TROVE])
		{	
			hd_facilityTraceStatus[TROVE] = 1;
			hd_facilityTrace[TROVE] = hdS_createGroup("TROVE", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[TROVE],"TROVE", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[TROVE]);
			hdS_enableGroup(hd_facilityTrace[TROVE]);
		}
	
		if ((strcasecmp(event_list[i],"flow") == 0) && !hd_facilityTrace[FLOW])
		{	
			hd_facilityTraceStatus[FLOW] = 1;
			hd_facilityTrace[FLOW] = hdS_createGroup("FLOW", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[FLOW],"FLOW", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[FLOW]);
			hdS_enableGroup(hd_facilityTrace[FLOW]);
		}
		
		if ((strcasecmp(event_list[i],"req") == 0) && !hd_facilityTrace[REQ])
		{	
			hd_facilityTraceStatus[REQ] = 1;
			hd_facilityTrace[REQ] = hdS_createGroup("REQ", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[REQ],"REQ", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[REQ]);
			hdS_enableGroup(hd_facilityTrace[REQ]);
		}
		
		if ((strcasecmp(event_list[i],"breq") == 0) && !hd_facilityTrace[BREQ])
		{	
			hd_facilityTraceStatus[BREQ] = 1;
			hd_facilityTrace[BREQ] = hdS_createGroup("BREQ", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[BREQ],"BREQ", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[BREQ]);
			hdS_enableGroup(hd_facilityTrace[BREQ]);
		}
		
		#ifdef HAVE_HDPTL
		
		if (strcasecmp(event_list[i],"statisticsNET") == 0)
		{	
			statistics.PTLSRC_NET_IN = 1;
			statistics.PTLSRC_NET_OUT = 1;
		}
		
		if (strcasecmp(event_list[i],"statisticsMEM") == 0)
		{	
			statistics.PTLSRC_MEM_USED = 1;
			statistics.PTLSRC_MEM_FREE = 1;
			statistics.PTLSRC_MEM_BUFFER = 1;
		}
		
		if (strcasecmp(event_list[i],"statisticsCPU") == 0)
		{	
			statistics.PTLSRC_CPU_LOAD = 1;
		}
		
//		if (strcasecmp(event_list[i],"statisticsCPU") == 0)
//		{	
//			statistics.PTLSRC_HDD_WRITE = 1;
//			statistics.PTLSRC_HDD_READ = 1;
//		}
		
		pStatistics = ptl_createTrace(topology, topoNode, 1, statistics, 700);
		ptl_startTrace(pStatistics);
		
		#endif
	}

	
	return 0;
}

int PINT_HD_event_finalize(void)
{
	int i;
	for (i= 0 ; i < ALL_FACILITIES; i++){
		if(hd_facilityTraceStatus[i])
		{
			hdS_finalize(hd_facilityTrace[i]);
			if (hdStatsGroupValue[i] > 0)
				hdS_writeInt32Value(hd_facilityTrace[i], 0);
			hd_facilityTraceStatus[i] = 0;
			hd_facilityTrace[i] = NULL; 
		#ifdef HAVE_HDPTL
			ptl_stopTrace(pStatistics);
			ptl_destroyTrace(pStatistics);
		#endif
		}
	}
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

int PINT_HD_update_counter_get(HD_Trace_Facility facility) 
{
	int ret = 0;
	if (hd_facilityTraceStatus[facility]) 
	{	
		gen_mutex_lock(&hdStatsGroupMutex[facility]);
		ret = hdStatsGroupValue[facility];
		gen_mutex_unlock(&hdStatsGroupMutex[facility]);
	}
	return ret;
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

