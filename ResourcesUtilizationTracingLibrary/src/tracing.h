/**
 * @file tracing.h
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef TRACING_H_
#define TRACING_H_

#include <glib/gthread.h>
#include <glibtop/cpu.h>
#include <glibtop/mem.h>
#include <glibtop/netlist.h>
#include <glibtop/netload.h>
#include <glibtop/fsusage.h>

#include "hdStats.h"
#include "PTL.h"

/* ************************************************************************* */
/*                            TYPE DEFINITIONS                               */
/* ************************************************************************* */

typedef struct {
	GMutex *mutex;
	GCond *stateChanged;
	gboolean enabled;
	gboolean quit;
} tracingControlStruct;

typedef struct {
	gboolean valid;
	glibtop_cpu cpu;
	glibtop_mem mem;
	glibtop_netload *netload;
	glibtop_fsusage fs;
} tracingValuesStruct;

typedef struct {
	gint cpu_num;
	glibtop_netlist netlist;
	char **netifs;
	char *hdd_mountpoint;
} tracingStaticDataStruct;

/**
 * TODO
 */
typedef struct {
	hdStatsGroup *group;
	ptlSources sources;
	int interval;
	tracingControlStruct *control;
	tracingStaticDataStruct staticData;
	tracingValuesStruct oldValues;
} tracingDataStruct;


/* ************************************************************************* */
/*                      PUBLIC FUNCTION DECLARATIONS                         */
/* ************************************************************************* */

/**
 * TODO
 */
/* tracingData->sources has to be already set *
 * tracingData->group is set by this function */
int initTracing(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		tracingDataStruct *tracingData  /* pointer to tracing Data */
		);

/**
 * TODO
 */
gpointer tracingThreadFunc(gpointer myPTLObject);

#endif /* TRACING_H_ */
