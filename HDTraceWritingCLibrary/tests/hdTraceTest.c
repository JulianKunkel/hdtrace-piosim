/**
 * @file hdRelationTest.c
 *
 * @date 03.06.2009
 * @author Julian M. Kunkel
 * @version \$Id$
 */

#include <stdlib.h>
#include <stdio.h>

#include "hdTopo.h"
#include "hdTrace.h"

int main(void){
	hdTrace_init();
	
	const char *levels[] = {"Hostname", "Client", "Thread"};
	hdTopology *myTopology = hdT_createTopology("MyProject", levels, 3);


	const char *path[] = {"host0", "client0","thread0"};
	hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 3);

	hdTrace *trace = hdT_createTrace(myTopoNode);

	hdT_enableTrace(trace);
	//hdT_setNestedDepth(trace,3);
        int i;

	for(i=0; i < 100000;i++){

	hdT_logStateStart(trace, "a0");

	hdT_writeInfo(trace,"hello world");
	hdT_logAttributes(trace,"a0='A0'");
	hdT_logAttributes(trace,"a1='A1'");

	hdT_logStateStart(trace, "B");
	hdT_logAttributes(trace,"b0='B0'");
	hdT_logAttributes(trace,"b1='B1'");
	hdT_logAttributes(trace,"b2='B2'");
	hdT_logStateEnd(trace);

	hdT_logStateEnd(trace);

	hdT_logStateStart(trace, "C");
	hdT_logAttributes(trace,"c0='C0'");
	hdT_logElement(trace,"element","e0='E0'");
	hdT_logElement(trace,"element","e1='E1'");
	hdT_logAttributes(trace,"c1='C1'");

	hdT_logStateEnd(trace);
	}


	hdT_finalize(trace);

	return 0;
}
