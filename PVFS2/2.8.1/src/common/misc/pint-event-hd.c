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

static hdStatsGroup hd_facilityTrace[ALL_FACILITIES];
static int hd_facilityTraceStatus[ALL_FACILITIES];
static int hdStatsGroupValue[ALL_FACILITIES];
static gen_mutex_t hdStatsGroupMutex[ALL_FACILITIES];
//hdStatsGroupMutex[ALL_FACILITIES] = GEN_MUTEX_INITIALIZER;
//{ GEN_MUTEX_INITIALIZER, GEN_MUTEX_INITIALIZER, GEN_MUTEX_INITIALIZER,
//  GEN_MUTEX_INITIALIZER, GEN_MUTEX_INITIALIZER, GEN_MUTEX_INITIALIZER };

int PINT_HD_event_initalize(char * traceWhat)
{	
	char hostname[255];
	int len;
	int ret;
	ret = gethostname(hostname, 255);
	
	if (ret != 0)
	{
		/* error handling */
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
			printf("new topo \"%s\"\n",event_list[i]);
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
			hdS_addValue(hd_facilityTrace[BMI],"Concurrent ops", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[BMI]);
			hdS_enableGroup(hd_facilityTrace[BMI]);
			printf("hd_facilityTrace[BMI] = %p\n",&hd_facilityTrace[BMI]);
		}
		
		if ((strcasecmp(event_list[i],"trove") == 0) && !hd_facilityTrace[TROVE])
		{	
			hd_facilityTraceStatus[TROVE] = 1;
			hd_facilityTrace[TROVE] = hdS_createGroup("TROVE", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[TROVE],"Concurrent ops", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[TROVE]);
			hdS_enableGroup(hd_facilityTrace[TROVE]);
		}
	
		if ((strcasecmp(event_list[i],"flow") == 0) && !hd_facilityTrace[FLOW])
		{	
			hd_facilityTraceStatus[FLOW] = 1;
			hd_facilityTrace[FLOW] = hdS_createGroup("FLOW", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[FLOW],"Concurrent ops", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[FLOW]);
			hdS_enableGroup(hd_facilityTrace[FLOW]);
		}
		
		if ((strcasecmp(event_list[i],"req") == 0) && !hd_facilityTrace[REQ])
		{	
			hd_facilityTraceStatus[REQ] = 1;
			hd_facilityTrace[REQ] = hdS_createGroup("REQ", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[REQ],"Concurrent ops", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[REQ]);
			hdS_enableGroup(hd_facilityTrace[REQ]);
		}
		
		if ((strcasecmp(event_list[i],"breq") == 0) && !hd_facilityTrace[BLOCK_REQ])
		{	
			hd_facilityTraceStatus[BLOCK_REQ] = 1;
			hd_facilityTrace[BLOCK_REQ] = hdS_createGroup("BLOCK_REQ", topology, topoNode, 1);
			hdS_addValue(hd_facilityTrace[BLOCK_REQ],"Concurrent ops", INT32, "#", NULL);
			hdS_commitGroup(hd_facilityTrace[BLOCK_REQ]);
			hdS_enableGroup(hd_facilityTrace[BLOCK_REQ]);
		}
	}
	
	return 0;
}

int PINT_HD_event_finalize(void)
{
	int i;
	for (i= 0 ; i < ALL_FACILITIES; i++){
		if(hd_facilityTraceStatus[i]){
			hdS_finalize(hd_facilityTrace[i]);
			hd_facilityTraceStatus[i] = 0;
		}
	}
	return 0;
}

int PINT_HD_update_counter(HD_Trace_Facility facility, char * sign) 
{
	if (hd_facilityTraceStatus[facility]) 
	{	
		if (strcasecmp(sign,"+") == 0)
		{
			hdStatsGroupValue[facility]++;
		}
		else if (strcasecmp(sign,"-") == 0)
		{
			hdStatsGroupValue[facility]--;
		}
		else
		{	
			fprintf(stderr, "Problem with sign [+|-] !\n");
			return 1;
		}
		gen_mutex_lock(&hdStatsGroupMutex[facility]);
		hdS_writeInt32Value(hd_facilityTrace[facility], hdStatsGroupValue[facility]);
		gen_mutex_unlock(&hdStatsGroupMutex[facility]);
	}
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

