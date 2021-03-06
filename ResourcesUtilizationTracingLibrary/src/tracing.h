/**
 * @file tracing.h
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef TRACING_H_
#define TRACING_H_

#include <glib.h>
#include <glibtop/cpu.h>
#include <glibtop/mem.h>
#include <glibtop/netlist.h>
#include <glibtop/netload.h>
#include <glibtop/fsusage.h>

#include "hdStats.h"
#include "RUT.h"

/* ************************************************************************* */
/*                            TYPE DEFINITIONS                               */
/* ************************************************************************* */

/**
 * @internal
 * Structure for the control of the tracing thread
 */
typedef struct {
	GMutex *mutex;
	GCond *stateChanged;
	gboolean started;
	gboolean terminate;
} tracingControlStruct;

/**
 * @internal
 * Structure for storing old values e.g. to build differences
 */
typedef struct {
	gboolean valid;
	glibtop_cpu cpu;
#ifdef HAVE_PROCESSORSTATES
	guint64 *c_states;
	guint64 *p_states;
#endif
	glibtop_mem mem;
	glibtop_netload *netload;
	glibtop_fsusage fs;

	/* file system information */
	guint64 * io_completed [DISKSTAT_COUNT];

	guint64 contextSwitches;
} tracingValuesStruct;

/**
 * @internal
 * Strcuture for static data sourced out from \ref tracingDataStruct
 */
typedef struct {
	gint cpu_num;
#ifdef HAVE_PROCESSORSTATES
	gint c_states_num;
	gint p_states_num;
#endif
	glibtop_netlist netlist;
	char **netifs;
	char *hdd_mountpoint;

	gint block_devices_num;
	char ** block_devices;
	gint * block_devices_index; /* the linenumber of the block device in /proc/diskstats */
} tracingStaticDataStruct;

/**
 * @internal
 * Structure for the tracing data for managing the tracing iterations
 */
typedef struct {
	hdStatsGroup *group;
	rutSources sources;
	int interval;
	tracingControlStruct *control;
	tracingStaticDataStruct staticData;
	tracingValuesStruct oldValues;
} tracingDataStruct;


/* ************************************************************************* */
/*                      PUBLIC FUNCTION DECLARATIONS                         */
/* ************************************************************************* */

/**
 * Initialize the tracing stuff.
 */
/* tracingData->sources has to be already set *
 * tracingData->group is set by this function */
int initTracing(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		tracingDataStruct *tracingData  /* pointer to tracing Data */
		);

/**
 * Run function of the tracing thread.
 */
gpointer tracingThreadFunc(gpointer myRUTObject);

#endif /* TRACING_H_ */
