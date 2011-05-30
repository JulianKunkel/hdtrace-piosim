/**
 * @file RUT.c
 *
 * @date 11.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "RUT.h"

#include <stdio.h>
#include <glib.h>
#include <errno.h>
#include <assert.h>
#include <unistd.h> /* for sleep */

#include "common.h"
#include "tracing.h"

/* ************************************************************************* */
/*               COMPILE TIME ERROR AND WARNING DEFINITIONS                  */
/* ************************************************************************* */

__errordecl(ptl_cte_no_glib_thread_support,
		"ERROR: Glib used is not compiled with threads support!");
__errordecl(ptl_cte_no_glib_thread_implementation,
		"ERROR: Glib has no thread implementation!");

/* ************************************************************************* *
 *                        DOCUMENTATION STRUCTURING                          *
 * ************************************************************************* */

/** @cond api_only */
/**
 * @typedef UtilTrace
 * @ingroup RUT
 */
/**
 * @struct rutSources_s
 * @ingroup RUT
 */
/**
 * @typedef rutSources
 * @ingroup RUT
 */
/**
 * @def RUT_MAX_NUM_CPUS
 * @ingroup RUT
 */
/**
 * @def RUT_MAX_NUM_NETIFS
 * @ingroup RUT
 */
/**
 * @def RUT_MAX_STATS_VALUES
 * @ingroup RUT
 */
/**
 * @def RUTSRC_UNSET_ALL
 * @ingroup RUT
 */
/**
 * @def RUTSRC_SET_ALL
 * @ingroup RUT
 */
/**
 * @def RUTSRC_SET_CPU
 * @ingroup RUT
 */
/**
 * @def RUTSRC_SET_MEM
 * @ingroup RUT
 */
/**
 * @def RUTSRC_SET_NET
 * @ingroup RUT
 */
/**
 * @def RUTSRC_SET_HDD
 * @ingroup RUT
 */
/**
 * @def RUT_SUCCESS
 * @ingroup RUT
 */
/**
 * @def RUT_EMEMORY
 * @ingroup RUT
 */
/**
 * @def RUT_EHDLIB
 * @ingroup RUT
 */
/**
 * @def RUT_ETHREAD
 * @ingroup RUT
 */
/** @endcond */


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

#ifdef HAVE_DBC

typedef struct NodePowerTrace_s
{
	/** name of the node (must NOT be different to name in database!) */
	const char *node;
	/** hdStatsGroup energy trace belongs to */
	hdStatsGroup *group;
	/** start of energy trace (19 digits -- nanoseconds)  */
	uint64_t from_timestamp;
	/** end of energy trace (19 digits -- nanoseconds)  */
	uint64_t to_timestamp;
} NodePowerTraceStruct;

#endif

typedef struct UtilTrace_s
{
	GThread *tracingThread;
	tracingControlStruct *tracingControl;

#ifdef HAVE_DBC

	NodePowerTrace *node_pt;

#endif

} UtilTraceStruct;


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

static int changeTracingState(UtilTrace *trace, gboolean newstate);


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

int ptl_verbosity;

/**
 * Create performance trace
 *
 * @if api_only
 *  @ingroup RUT
 * @endif
 *
 * Use this function to create a new utilization trace. It will setup a
 * new statistics trace with the values specified in \a sources to trace.
 *
 * The tracing will not start until \ref rut_startTracing is called for the
 * UtilTrace object returned by this function.
 *
 * @param topoNode  The new trace will we associated to this node or one of
 *                  its ancestor nodes (those on the nodes path)
 * @param topoLevel level of the node to associate the new trace to
 * @param sources   Sources to trace
 * @param interval  Interval to trace in milliseconds. Should be at least
 *                  100ms for acceptable performance impact.
 * @param trace      Location to store the UtilTrace pointer (OUTPUT)
 *
 * @return  Error state
 *
 * @retval RUT_SUCCESS        Success
 * @retval RUT_EMEMORY        Out of memory
 * @retval RUT_EHDLIB         Error in HDTrace library
 * @retval RUT_ETHREAD        Cannot create tracing thread
 */
int rut_createTrace(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		rutSources sources,  /* sources to trace */
		int interval,         /* interval of one tracing step in ms */
		UtilTrace **trace     /* OUTPUT: the trace created */
		)
{

	/* TODO: Implement error report as in PT */

	/*
	 * Set verbosity as requested by environment
	 */
	ptl_verbosity = 1;
	char *verbstr = getenv("RUT_VERBOSITY");
	if (verbstr != NULL)
		sscanf(verbstr, "%d", &ptl_verbosity);

	DEBUGMSG("Verbosity: %d", ptl_verbosity);

	/*
	 * Take care about Glibs thread capabilities
	 */

	/* Make sure Glib is compiled with threads support */
#ifndef G_THREADS_ENABLED
	ptl_cte_no_glib_thread_support();
#endif
#ifdef G_THREADS_IMPL_NONE
	ptl_cte_no_glib_thread_implementation();
#endif

	/* Make sure Glib's threads support is initialized */
	if (!g_thread_supported ()) g_thread_init (NULL);


	/*
	 * Create and fill tracing control structure
	 */

	tracingControlStruct *tracingControl;
	rut_malloc(tracingControl, 1, RUT_EMEMORY);

	tracingControl->mutex = g_mutex_new();
	tracingControl->stateChanged = g_cond_new();
	tracingControl->started = FALSE;


	/*
	 * Create and fill tracing data structure
	 */

	tracingDataStruct *tracingData;
	rut_malloc(tracingData, 1, RUT_EMEMORY);

	tracingData->sources = sources;
	tracingData->interval = interval;
	tracingData->control = tracingControl;

	/* initialize tracing and fill tracingData->group */
	int ret = initTracing(topoNode, topoLevel, tracingData);
	if (ret < 0)
	{
		assert(ret == 0);
		/* TODO: Error handling */
	}

	/*
	 * Create tracing thread
	 */

	GThread *tracingThread;
	GThreadFunc tracingFunc = tracingThreadFunc;
	GError *tracingThreadError = NULL;

	tracingThread = g_thread_create(tracingFunc,
			(gpointer) tracingData, TRUE, &tracingThreadError);
	if (tracingThread == NULL)
	{
		ERRORMSG("%s", tracingThreadError->message);
		g_error_free (tracingThreadError);
	}


#ifdef HAVE_DBC

	/*
	 * Initialize power tracing
	 */

	/* get current system time and convert it to nanoseconds */
	uint64_t timestamp = getSystemTimestamp();

	/* create a new group (power is not synced with other resources */
	hdStatsGroup *power_group = hdS_createGroup("Energy", topoNode, 1);
	/* TODO Error handling */
	if(power_group == NULL) {
		assert(errno != HD_ERR_INVALID_ARGUMENT);
		assert(errno != HD_ERR_MALLOC);
		assert(errno != HD_ERR_BUFFER_OVERFLOW);
		assert(errno != HD_ERR_CREATE_FILE);
	}

	ret = hdS_addValue(power_group, "POWER_CONSUMPTION", DOUBLE, "Watt", "Power");
	if(ret < 0) { 
		g_assert(errno != HD_ERR_INVALID_ARGUMENT);
		g_assert(errno != HD_ERR_BUFFER_OVERFLOW);
		g_assert(errno != HDS_ERR_GROUP_COMMIT_STATE);
	} 

	/* 
	 * the group won't be committed yet because the actual start time is
	 * different (afterwards) to the previously taken time -- that timestamp
	 * is only used to obtain the relevant timestamps out of the database
	 */

	NodePowerTrace *node_pt;
	rut_malloc(node_pt, 1, RUT_EMEMORY);
	
	/* get node name depending on the topology path */
	node_pt->node = hdT_getTopoPathLabel(topoNode, 1);
	node_pt->group = power_group;
	node_pt->from_timestamp = timestamp;
	node_pt->to_timestamp = 0;

	DEBUGMSG("timestamp: %lu (started power tracing on node %s)\n",timestamp, node_pt->node);

#endif

	/* !!! We must not access tracingData after this point. !!! */

	rut_malloc(*trace, 1, RUT_EMEMORY);

	(*trace)->tracingThread = tracingThread;
	(*trace)->tracingControl = tracingControl;

#ifdef HAVE_DBC

	(*trace)->node_pt = node_pt;
	
#endif

	return RUT_SUCCESS;
}

/**
 * Start performance tracing
 *
 * @if api_only
 *  @ingroup RUT
 * @endif
 *
 * @param trace Trace object to work on
 *
 * @return Indicate if tracing state changed
 *
 * @retval  0  Tracing is now started (was stopped before)
 * @retval  1  Tracing is started (as it was already before)
 * @retval  2  \a trace is NULL
 */
int rut_startTracing(UtilTrace *trace)
{
	UtilTraceStruct *myTrace = trace;
	return changeTracingState(myTrace, TRUE);
}

/**
 * Stop performance tracing
 *
 * @if api_only
 *  @ingroup RUT
 * @endif
 *
 * @param trace Trace object to work on
 *
 * @return Indicate if tracing state changed
 *
 * @retval  0  Tracing is now stopped (was started before)
 * @retval  1  Tracing is stopped (as it was already before)
 * @retval  2  \a trace is NULL
 */
int rut_stopTracing(UtilTrace *trace)
{
	UtilTraceStruct *myTrace = trace;
	return changeTracingState(myTrace, FALSE);
}

/**
 * Finalize utilization trace object
 *
 * @if api_only
 *  @ingroup RUT
 * @endif
 *
 * Finalizes the trace. This destroys the UtilTrace object an frees all memory.
 *
 * @param trace  Utilization trace to finalize
 *
 * @return  Error state
 *
 * @retval RUT_SUCCESS  Success
 * @retval RUT_EMEMORY  Out of memory
 * @retval RUT_EDBCONNECTOR Error while connecting to database / getting data from database
 */
int rut_finalizeTrace(UtilTrace *trace)
{
	/* TODO: Implement Error report as in PT */
	UtilTraceStruct *myTrace = trace;

	/* request quitting of the tracing thread */
	g_mutex_lock(myTrace->tracingControl->mutex);
	myTrace->tracingControl->terminate = TRUE;
	g_cond_broadcast(myTrace->tracingControl->stateChanged);
	g_mutex_unlock(myTrace->tracingControl->mutex);

	/* join the tracing thread to wait for its end */
	g_thread_join(myTrace->tracingThread);

	/* free all memory used for thread control */
	g_cond_free(myTrace->tracingControl->stateChanged);
	g_mutex_free(myTrace->tracingControl->mutex);
	rut_free(myTrace->tracingControl);

#ifdef HAVE_DBC

	/* get current system time and convert it to nanoseconds */
	uint64_t timestamp = getSystemTimestamp();

	DEBUGMSG("timestamp: %lu (stopped power tracing on node %s)\n",timestamp, myTrace->node_pt->node);

	myTrace->node_pt->to_timestamp = timestamp;

	// sleep one second, because only every 200ms power values are written into the database
	sleep(1);

	row_struct *rows = NULL;
	int crows = getNodeData(myTrace->node_pt->node,
				myTrace->node_pt->from_timestamp,
				myTrace->node_pt->to_timestamp,
				&rows);

	if (crows > 0) {
		DEBUGMSG("timestamp: %lu (first db timestamp for node %s)\n",
						rows[0].timestamp, myTrace->node_pt->node);
		DEBUGMSG("timestamp: %lu (last db timestamp for node %s)\n",
						rows[crows - 1].timestamp, myTrace->node_pt->node);
	} else {
		return RUT_EDBCONNECTOR;
	}

	/* 
	 * the group will be commited at the first timestamp of the database 
	 * which is most likely somewhat later than the actual start whereas
	 * the last entry is a bit earlier than the actual end...
	 */

	hdS_commitGroupAtTimestamp(myTrace->node_pt->group, rows[0].timestamp);
	hdS_enableGroup(myTrace->node_pt->group);

	for (int i = 0; i < crows; i++) {
		hdS_writeDoubleValueAtTimestamp(myTrace->node_pt->group,
						rows[i].value,
						rows[i].timestamp * MULTIPLIER);
	}
	
	free(rows);
	hdS_disableGroup(myTrace->node_pt->group);
	hdS_finalize(myTrace->node_pt->group);

	/* free memory used for node_pt object */
	rut_free(myTrace->node_pt);

#endif

	/* free all memory used for UtilTrace object */
	rut_free(myTrace);


#if 0
	tracingDataStruct *tracingData;
	rut_malloc(tracingData, 1, NULL);

	tracingData->sources = sources;
	tracingData->interval = interval;
	tracingData->control = tracingControl;

#endif

	return RUT_SUCCESS;
}


/**
 * Change tracing state
 *
 * @param trace     Trace object to work on
 * @param newstate  State to set
 *
 * @return Indicate if tracing state changed
 *
 * @retval  0  Tracing state changed
 * @retval  1  Tracing state was already the requested one
 * @retval  2  \a trace is NULL
 */
static int changeTracingState(UtilTraceStruct *trace, gboolean newstate)
{
	if (trace == NULL)
		return 2;

	int retval = 0;
	g_mutex_lock(trace->tracingControl->mutex);
	if (trace->tracingControl->started == newstate)
	{
		/* tracing is already enabled/disabled */
		retval = 1;
	}
	else
	{
		/* enable/disable tracing and notify tracing thread */
		trace->tracingControl->started = newstate;
		g_cond_broadcast(trace->tracingControl->stateChanged);
	}
	g_mutex_unlock(trace->tracingControl->mutex);
	return retval;
}
