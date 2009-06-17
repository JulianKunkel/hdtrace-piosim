#include "pvfs2-config.h"

#ifdef __PVFS2_SERVER__
#warning "Shall not be included"
#endif

#ifdef HAVE_HDTRACE

#include "pint-event.h"
#include  "str-utils.h"
#include "state-machine.h"
#include "gen-locks.h"
#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include "hdTopo.h"

hdR_topoToken topoClientTokenArray[ALL_CLIENT_FACILITIES];

hdTopoNode clientTopoNode;

int PVFS_hd_client_trace_initialize(hdTopoNode topoNode){
	hdR_initTopology(topoNode, & topoClientTokenArray[CLIENT]);
	clientTopoNode = topoNode;
	set_hd_sm_trace_enabled(1);

	return 0;
}

int PVFS_hd_client_trace_finalize(void){
	if(clientTopoNode != NULL)
	{	
		set_hd_sm_trace_enabled(0);

		hdR_finalize(clientTopoNode);
		clientTopoNode = NULL;
	}
	return 0;
}

#else
/* NO HDTRACE AVAILABLE */

int PVFS_hd_client_trace_initialize(hdTopoNode topoNode){
	return 0;
}

#endif /* __HAVE_HDTRACE__ */
