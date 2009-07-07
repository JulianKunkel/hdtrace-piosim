/**
 * @file tracing.c
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "tracing.h"

#include <unistd.h>
#include <glib.h>
#include <glibtop.h>
#include <errno.h>
#include <assert.h>

#include "config.h"
#include "common.h"
#include "hdStats.h"
#include "hdError.h"

/* ************************************************************************* */
/*               COMPILE TIME ERROR AND WARNING DEFINITIONS                  */
/* ************************************************************************* */

__warndecl(ptl_cte_too_few_hds_values_per_group,
		"WARNING: HDS_MAX_VALUES_PER_GROUP < PTL_MAX_STATS_VALUES");


/* ************************************************************************* */
/*                      STATIC FUNCTION DECLARATIONS                         */
/* ************************************************************************* */

static void doTracingStep(tracingDataStruct *tracingData);
static void doTracingStepCPU(tracingDataStruct *tracingData);
static void doTracingStepMEM(tracingDataStruct *tracingData);
static void doTracingStepNET(tracingDataStruct *tracingData);
static void doTracingStepHDD(tracingDataStruct *tracingData);


/* ************************************************************************* */
/*                    PUBLIC FUNCTION IMPLEMENTATIONS                        */
/* ************************************************************************* */

int initTracing(
		hdTopoNode topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		tracingDataStruct *tracingData  /* pointer to tracing Data */
		)
{
	/*
	 * Create statistics group for tracing
	 */

	/* generate statistics group */
	hdStatsGroup group = hdS_createGroup("Performance", topoNode, topoLevel);
	/* TODO Error handling */

	tracingData->group = group;

	/*
	 * Initialize static tracing data
	 */

	/* get number of CPUs */
	tracingData->staticData.cpu_num = (gint) sysconf(_SC_NPROCESSORS_CONF);

	/* get available network interfaces */
    tracingData->staticData.netifs =
    	glibtop_get_netlist(&(tracingData->staticData.netlist));


	/*
	 * Specify tracing entry format
	 */

	ptlSources sources = tracingData->sources;


#define PTL_STRING_BUFFER_LENGTH 50

	/* allocate string buffer */
	char strbuf[PTL_STRING_BUFFER_LENGTH];
	strbuf[0] = '\0';

#if HDS_MAX_VALUES_PER_GROUP < PTL_MAX_STATS_VALUES
	ptl_cte_too_few_hds_values_per_group();
#endif

	int ret = 0;

#define ADD_VALUE(group, string, type, unit, grouping) \
	do { \
		ret = hdS_addValue(group, string, type, unit, grouping); \
		if(ret < 0)	{ \
			g_assert(errno != HD_ERR_INVALID_ARGUMENT); \
			g_assert(errno != HD_ERR_BUFFER_OVERFLOW); \
			g_assert(errno != HDS_ERR_GROUP_COMMIT_STATE); \
		} \
	} while (0)


	/* specify entry format */
	if (sources.PTLSRC_CPU_LOAD)
		ADD_VALUE(group, "CPU_TOTAL", FLOAT, "%", "CPU");

	if (sources.PTLSRC_CPU_LOAD_X)
		for (int i = 0; i < tracingData->staticData.cpu_num; ++i)
		{
			ret = snprintf(strbuf, PTL_STRING_BUFFER_LENGTH, "CPU_TOTAL_%d", i);
			g_assert(ret < PTL_STRING_BUFFER_LENGTH);
			g_assert(ret > 0);
			ADD_VALUE(group, strbuf, FLOAT, "%", "CPU");
		}

#define MEM_UNIT "B"

	if (sources.PTLSRC_MEM_USED)
		ADD_VALUE(group,"MEM_USED", INT64, MEM_UNIT, "MEM");

	if (sources.PTLSRC_MEM_FREE)
		ADD_VALUE(group,"MEM_FREE", INT64, MEM_UNIT, "MEM");

	if (sources.PTLSRC_MEM_SHARED)
		ADD_VALUE(group,"MEM_SHARED", INT64, MEM_UNIT, "MEM");

	if (sources.PTLSRC_MEM_BUFFER)
		ADD_VALUE(group,"MEM_BUFFER", INT64, MEM_UNIT, "MEM");

	if (sources.PTLSRC_MEM_CACHED)
		ADD_VALUE(group,"MEM_CACHED", INT64, MEM_UNIT, "MEM");


#define NET_UNIT "B"

	for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i)
	{
		char name[255];

		if (sources.PTLSRC_NET_IN_X)
		{
			ret = snprintf(name, 255, "NET_IN_%s",
					tracingData->staticData.netifs[i]);
			assert(ret < 255);
			ADD_VALUE(group, name, INT64, NET_UNIT, "NET");
		}

		if (sources.PTLSRC_NET_OUT_X)
		{
			ret = snprintf(name, 255, "NET_OUT_%s",
					tracingData->staticData.netifs[i]);
			assert(ret < 255);
			ADD_VALUE(group, name, INT64, NET_UNIT, "NET");
		}

	}

	if (sources.PTLSRC_NET_IN_EXT)
		ADD_VALUE(group, "NET_IN_EXT", INT64, NET_UNIT, "NET");

	if (sources.PTLSRC_NET_OUT_EXT)
		ADD_VALUE(group, "NET_OUT_EXT", INT64, NET_UNIT, "NET");

	if (sources.PTLSRC_NET_IN)
		ADD_VALUE(group, "NET_IN", INT64, NET_UNIT, "NET");

	if (sources.PTLSRC_NET_OUT)
		ADD_VALUE(group, "NET_OUT", INT64, NET_UNIT, "NET");


#define HDD_UNIT "Blocks"

	// right now read harddisk partition to use from environment
	char * mountpoint = getenv("PTL_HDD_MOUNTPOINT");
	if(mountpoint == NULL){
		mountpoint = "/";
		WARNMSG("Use environment variable PTL_HDD_MOUNTPOINT to set mount point to trace.\n"
				"Right now '%s' is used by default", mountpoint);
	}
	ptl_malloc(tracingData->staticData.hdd_mountpoint, strlen(mountpoint) + 1, -1)
	strcpy(tracingData->staticData.hdd_mountpoint, mountpoint);

	if (sources.PTLSRC_HDD_READ)
		ADD_VALUE(group, "HDD_READ", INT64, HDD_UNIT, "HDD");

	if (sources.PTLSRC_HDD_WRITE)
		ADD_VALUE(group, "HDD_WRITE", INT64, HDD_UNIT, "HDD");

	/*
	 * Commit statistics group
	 */
	hdS_commitGroup(tracingData->group);


	/*
	 * Initialize internal tracing data
	 */

	/* allocate memory for saving network interfaces statistics values */
	ptl_malloc(tracingData->oldValues.netload,
			tracingData->staticData.netlist.number, -1);

    /* mark old values invalid */
	tracingData->oldValues.valid = FALSE;

	return 0;
}

/**
 * Run function of the tracing thread.
 *
 * This function and so the thread is running the tracing loop all
 * the time when \a tracingData->control->enabled is \a TRUE.
 *
 * @param tracingDataPointer  Pointer to the tracing data structure
 *
 * @return Always returns NULL when the tread is terminated
 */
gpointer tracingThreadFunc(gpointer tracingDataPointer)
{
	tracingDataStruct *tracingData = (tracingDataStruct *) tracingDataPointer;

	/* enable used statistics group
	 * (we handle trace enabling/disabling by ourself for performance reasons) */
	// TODO Change this, since start/stop tracing will be handled special in hdStats
	hdS_enableGroup(tracingData->group);

	/* create timer */
	gulong currentTime, waitTime;
	GTimer *timer = g_timer_new();

	gboolean quit = FALSE;
	while(1)
	{
		VERBMSG("Entering tracing loop");
		/* wait until tracing is enabled and check quit condition */
		g_mutex_lock(tracingData->control->mutex);

		while (!tracingData->control->enabled && !tracingData->control->quit)
		{
			VERBMSG("Waiting for tracing becomes enabled");
			/* mark old statistics data invalid */
			tracingData->oldValues.valid = FALSE;
			/* wait for tracing becomes enabled */
			g_cond_wait (tracingData->control->stateChanged,
					tracingData->control->mutex);
		}
		quit = tracingData->control->quit;

		g_mutex_unlock(tracingData->control->mutex);

		/* quit loop if requested to do */
		if (quit)
			break;

		/* wait for next multiple of interval time */
		currentTime = (gulong) (1000.0 * g_timer_elapsed(timer, NULL));

		waitTime = (gulong) tracingData->interval
				- (currentTime % (gulong) tracingData->interval);

		DEBUGMSG("Wait %ld ms", waitTime);
		g_usleep (waitTime * 1000);

		/*  do tracing step */
		doTracingStep(tracingData);
	}
	VERBMSG("Loop exited");

	/* disable statistics group */
	hdS_disableGroup(tracingData->group);

	/* finalize trace */
	hdS_finalize(tracingData->group);

	/* free timer */
	g_timer_destroy(timer);

	/* free memory used for thread data */
	ptl_free(tracingData->oldValues.netload);
	for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i)
		ptl_free(tracingData->staticData.netifs[i]);
	ptl_free(tracingData->staticData.netifs);
	ptl_free(tracingData->staticData.hdd_mountpoint);
	ptl_free(tracingData);

	g_free(NULL);

	return NULL;
}


/* ************************************************************************* */
/*                    STATIC FUNCTION IMPLEMENTATIONS                        */
/* ************************************************************************* */

static void doTracingStep(tracingDataStruct *tracingData)
{

	VERBMSG("Step!");

	doTracingStepCPU(tracingData);

	doTracingStepMEM(tracingData);

	doTracingStepNET(tracingData);

	doTracingStepHDD(tracingData);

	/* mark old statistics values saved as valid */
	tracingData->oldValues.valid = TRUE;
}

#define CHECK_WRITE_VALUE_ERROR \
	do { \
		if (ret < 0) \
			switch (errno)	{ \
			case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT"); \
			case HD_ERR_TRACE_DISABLED: assert(!"HD_ERR_TRACE_DISABLED"); \
			case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE"); \
			case HDS_ERR_ENTRY_STATE: assert(!"HDS_ERR_ENTRY_STATE"); \
			default: assert(ret == 0); \
			} \
	} while (0)

#define WRITE_I32_VALUE(value) \
	do { \
		int ret = hdS_writeInt32Value(tracingData->group, value); \
		CHECK_WRITE_VALUE_ERROR; \
	} while (0)

#define WRITE_I64_VALUE(value) \
	do { \
		int ret = hdS_writeInt64Value(tracingData->group, value); \
		CHECK_WRITE_VALUE_ERROR; \
	} while (0)

#define WRITE_FLOAT_VALUE(value) \
	do { \
		int ret = hdS_writeFloatValue(tracingData->group, value); \
		CHECK_WRITE_VALUE_ERROR; \
	} while (0)


/* ************************************************************************
 * CPU
 */
static void doTracingStepCPU(tracingDataStruct *tracingData) {

	if (! (tracingData->sources.PTLSRC_CPU_LOAD
			|| tracingData->sources.PTLSRC_CPU_LOAD_X))
		return;

#define CPUDIFF(val) \
	((gdouble) (cpu.val - tracingData->oldValues.cpu.val))

	gfloat valuef;
	glibtop_cpu cpu;

	glibtop_get_cpu(&cpu);

	if (tracingData->oldValues.valid)
	{
		if (tracingData->sources.PTLSRC_CPU_LOAD)
		{
			valuef = (gfloat) (1.0 - (CPUDIFF(idle) / CPUDIFF(total)));
			WRITE_FLOAT_VALUE(valuef * 100);
			DEBUGMSG("CPU_TOTAL = %f%%", valuef * 100);
		}

		if (tracingData->sources.PTLSRC_CPU_LOAD_X)
		{
			for (int i = 0; i < tracingData->staticData.cpu_num; ++i)
			{
				/* TODO: Check CPU enable state (flags) */
				valuef = (gfloat)(1.0 - (CPUDIFF(xcpu_idle[i])
						/ CPUDIFF(xcpu_total[i])));
				WRITE_FLOAT_VALUE(valuef * 100);
				DEBUGMSG("CPU_TOTAL_%d = %f%%", i, valuef * 100);
			}
		}
	}
	/* save current CPU statistics for next step */
	tracingData->oldValues.cpu = cpu;

#undef CPUDIFF

}


/* ************************************************************************
 * Memory
 */
static void doTracingStepMEM(tracingDataStruct *tracingData) {

	if (! (tracingData->sources.PTLSRC_MEM_USED
			|| tracingData->sources.PTLSRC_MEM_FREE
			|| tracingData->sources.PTLSRC_MEM_SHARED
			|| tracingData->sources.PTLSRC_MEM_BUFFER
			|| tracingData->sources.PTLSRC_MEM_CACHED))
		return;

	gint64 valuei64;
	glibtop_mem mem;

	glibtop_get_mem(&mem);

	if (tracingData->oldValues.valid)
	{

#define MEM_WRITE_VALUE(PART,part) \
	if (tracingData->sources.PTLSRC_MEM_##PART) { \
		valuei64 = (gint64) (mem.part); \
		WRITE_I64_VALUE(valuei64); \
		DEBUGMSG("MEM_" #PART " = %" G_GINT64_FORMAT " " MEM_UNIT, valuei64); \
	}

		MEM_WRITE_VALUE(USED, used)
		MEM_WRITE_VALUE(FREE, free)
		MEM_WRITE_VALUE(SHARED, shared)
		MEM_WRITE_VALUE(BUFFER, buffer)
		MEM_WRITE_VALUE(CACHED, cached)

#undef MEM_WRITE_VALUE

	}
	/* save current memory statistics for next step */
	tracingData->oldValues.mem = mem;

}

/* ************************************************************************
 * Network
 */
static void doTracingStepNET(tracingDataStruct *tracingData) {

	if (! (tracingData->sources.PTLSRC_NET_IN_X
			|| tracingData->sources.PTLSRC_NET_OUT_X
			|| tracingData->sources.PTLSRC_NET_IN_EXT
			|| tracingData->sources.PTLSRC_NET_OUT_EXT
			|| tracingData->sources.PTLSRC_NET_IN
			|| tracingData->sources.PTLSRC_NET_OUT))
		return;

	gint64 valuei64;

	/* variables to aggregate in and out bytes */
	guint64 net_all_in = 0;
	guint64 net_all_out = 0;
	guint64 net_ext_in = 0;
	guint64 net_ext_out = 0;

	/* handle statistics for each network interface */
    for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i)
    {
    	glibtop_netload netload;
    	glibtop_get_netload (&netload, tracingData->staticData.netifs[i]);

		guint64 new_in, old_in, new_out, old_out;
		new_in = netload.bytes_in;
		old_in = tracingData->oldValues.netload[i].bytes_in;
		new_out = netload.bytes_out;
		old_out = tracingData->oldValues.netload[i].bytes_out;

		DEBUGMSG("Got network traffic for %s:"
				" IN = %" G_GUINT64_FORMAT " " NET_UNIT ", "
				" OUT = %" G_GUINT64_FORMAT " " NET_UNIT,
				tracingData->staticData.netifs[i], new_in, new_out);

		/* handle system counter overflows */
		if (tracingData->oldValues.valid)
		{
			/* ATTENTION: Some assumptions take place here:
			 * - OS counter for network traffic is either 32bit or 64bit
			 *   unsigned integer.
			 * - Only 32bit will overflow in the near future, since unsigned
			 *   64bit won't until reaching 16 Exbibytes of network traffic.
			 *
			 * So (old < new) means, that an unsigned 32bit counter is used
			 * and overflowed once since last cycle.
			 */
			new_in += (new_in < old_in) ? G_GUINT64_CONSTANT(0xFFFFFFFF) : 0;
			new_out += (new_out < old_out) ? G_GUINT64_CONSTANT(0xFFFFFFFF) : 0;
		}

    	/* count aggregated traffic of all interfaces */
    	net_all_in += new_in;
    	net_all_out += new_out;

    	/* count aggregated traffic of external interfaces */
    	if (! (netload.if_flags	& (1 << GLIBTOP_IF_FLAGS_LOOPBACK)))
    	{
    		net_ext_in += new_in;
    		net_ext_out += new_out;
    	}

		if (tracingData->oldValues.valid)
		{
			/* trace single interfaces */

			if (tracingData->sources.PTLSRC_NET_IN_X)
			{
				valuei64 = (gint64) (new_in - old_in);
				WRITE_I64_VALUE(valuei64);
				DEBUGMSG("NET_IN_%s = %" G_GINT64_FORMAT " " NET_UNIT,
						tracingData->staticData.netifs[i], valuei64);
			}

			if (tracingData->sources.PTLSRC_NET_OUT_X)
			{
				valuei64 = (gint64) (new_out - old_out);
				WRITE_I64_VALUE(valuei64);
				DEBUGMSG("NET_OUT_%s = %" G_GINT64_FORMAT " " NET_UNIT,
						tracingData->staticData.netifs[i], valuei64);
			}
		}

		/* save current network interface statistics for next step */
		tracingData->oldValues.netload[i] = netload;
	}

    /* handle aggregated statistics of all interfaces */
    if (tracingData->oldValues.valid)
    {
    	if (tracingData->sources.PTLSRC_NET_IN_EXT)
		{
			valuei64 = (gint64)	(net_ext_in
					- tracingData->oldValues.net_ext_in);
			WRITE_I64_VALUE(valuei64);
			DEBUGMSG("NET_IN_EXT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

    	if (tracingData->sources.PTLSRC_NET_OUT_EXT)
		{
			valuei64 = (gint64)	(net_ext_out
					- tracingData->oldValues.net_ext_out);
			WRITE_I64_VALUE(valuei64);
			DEBUGMSG("NET_OUT_EXT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

    	if (tracingData->sources.PTLSRC_NET_IN)
		{
			valuei64 = (gint64)	(net_all_in
					- tracingData->oldValues.net_all_in);
			WRITE_I64_VALUE(valuei64);
			DEBUGMSG("NET_IN = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

    	if (tracingData->sources.PTLSRC_NET_OUT)
		{
			valuei64 = (gint64)	(net_all_out
					- tracingData->oldValues.net_all_out);
			WRITE_I64_VALUE(valuei64);
			DEBUGMSG("NET_OUT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}
    }

	/* save current network statistics for next step */
	tracingData->oldValues.net_ext_in = net_ext_in;
	tracingData->oldValues.net_ext_out = net_ext_out;
	tracingData->oldValues.net_all_in = net_all_in;
	tracingData->oldValues.net_all_out = net_all_out;

}



/* *************************************************************************
 * HDD
 */
static void doTracingStepHDD(tracingDataStruct *tracingData) {

	if(! (tracingData->sources.PTLSRC_HDD_READ
			|| tracingData->sources.PTLSRC_HDD_WRITE))
		return;

	gint64 valuei64;
	glibtop_fsusage fs;

	glibtop_get_fsusage (&fs, tracingData->staticData.hdd_mountpoint);

	if (tracingData->oldValues.valid)
	{
		if (tracingData->sources.PTLSRC_HDD_READ)
		{
			valuei64 = (gint64) (fs.read - tracingData->oldValues.fs.read);
			WRITE_I64_VALUE(valuei64);
			DEBUGMSG("DISK_READ = %" G_GINT64_FORMAT, valuei64);
	  }

	  if (tracingData->sources.PTLSRC_HDD_WRITE)
	  {
		  valuei64 = (gint64) (fs.write - tracingData->oldValues.fs.write);
		  WRITE_I64_VALUE(valuei64);
		  DEBUGMSG("DISK_WRITE = %" G_GINT64_FORMAT, valuei64);
	  }
	}
	tracingData->oldValues.fs = fs;
}

#undef WRITE_I64_VALUE
#undef CHECK_WRITE_VALUE_ERROR
