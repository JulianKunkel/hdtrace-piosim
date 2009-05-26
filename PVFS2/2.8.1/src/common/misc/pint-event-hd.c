#include "pvfs2-config.h"

#ifdef HAVE_HDTRACE

#include "hdStats.h"
#include "hdError.h"
#include "pint-event.h"
#include "pint-event-hd.h"

#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

hdStatsGroup hd_facilityTrace[ALL_FACILITIES];

int PINT_eventHD_initalize(char * traceWhat){	
	char hostname[255];
	int len;
	int ret;
	ret = gethostname(hostname, 255);
	if (ret != 0){
		// error handling
		fprintf(stderr, "Problem with hostname\n");
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
		
		if (strcasecmp(event_list[i],"bmi") == 0)
		{	
			if (!hd_facilityTrace[BMI])
			{	
				hd_facilityTrace[BMI] = hdS_createGroup("BMI", topology, topoNode, 1);
				hdS_addValue(hd_facilityTrace[BMI],"Concurrent ops", INT32, "#", NULL);
				hdS_commitGroup(hd_facilityTrace[BMI]);
				hdS_enableGroup(hd_facilityTrace[BMI]);
			}
			else
				printf("no BMI\n");
		}
		
		if (strcasecmp(event_list[i],"trove") == 0)
		{	
			if (!hd_facilityTrace[TROVE])
			{	
				hd_facilityTrace[TROVE] = hdS_createGroup("TROVE", topology, topoNode, 1);
				hdS_addValue(hd_facilityTrace[TROVE],"Concurrent ops", INT32, "#", NULL);
				hdS_commitGroup(hd_facilityTrace[TROVE]);
				hdS_enableGroup(hd_facilityTrace[TROVE]);
			}
		}
	
		if (strcasecmp(event_list[i],"flow") == 0)
		{	
			if (!hd_facilityTrace[FLOW])
			{	
				hd_facilityTrace[FLOW] = hdS_createGroup("FLOW", topology, topoNode, 1);
				hdS_addValue(hd_facilityTrace[FLOW],"Concurrent ops", INT32, "#", NULL);
				hdS_commitGroup(hd_facilityTrace[FLOW]);
				hdS_enableGroup(hd_facilityTrace[FLOW]);
			}
		}
	
		if (strcasecmp(event_list[i],"sm") == 0)
		{	
			if (!hd_facilityTrace[SM])
			{	
				hd_facilityTrace[SM] = hdS_createGroup("SM", topology, topoNode, 1);
				hdS_addValue(hd_facilityTrace[SM],"Concurrent ops", INT32, "#", NULL);
				hdS_commitGroup(hd_facilityTrace[SM]);
				hdS_enableGroup(hd_facilityTrace[SM]);
			}
		}
	}
	
	return 0;
}

int PINT_eventHD_finalize(void)
{
//	hdS_finalize(hd_facilityTrace[BMI]);
//	hdS_finalize(hd_facilityTrace[TROVE]);
//	hdS_finalize(hd_facilityTrace[FLOW]);
//	hdS_finalize(hd_facilityTrace[SM]);
	return 0;
}



int PINT_hdS_writeInt32Value(hdStatsGroup name, int value)
{	
	if (name)
	{
		hdS_writeInt32Value(name, value);
	}
   	else
   	{
   		printf("count = %d\n",value);
   		printf("BMI instance not existed\n");
   	}
	return 0;
}


#else

/* NO HDTRACE AVAILABLE */

int PINT_eventHD_initalize(void){
	return 0;
}

int PINT_eventHD_initalize(void){
	return 0;
}


#endif /* __HAVE_HDTRACE__ */

