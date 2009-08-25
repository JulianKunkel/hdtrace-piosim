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
/** @endcond */


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

typedef struct UtilTrace_s
{
	GThread *tracingThread;
	tracingControlStruct *tracingControl;
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
 * @param topoNode
 * @param topoLevel
 * @param sources
 * @param interval
 *
 * @return
 */
UtilTrace * rut_createTrace(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		rutSources sources,  /* bit field of the sources to trace */
		int interval         /* interval of one tracing step in ms */
		)
{
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
	rut_malloc(tracingControl, 1, NULL);

	tracingControl->mutex = g_mutex_new();
	tracingControl->stateChanged = g_cond_new();
	tracingControl->started = FALSE;


	/*
	 * Create and fill tracing data structure
	 */

	tracingDataStruct *tracingData;
	rut_malloc(tracingData, 1, NULL);

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

	/* !!! We must not access tracingData after this point. !!! */

	UtilTraceStruct *myTrace;
	rut_malloc(myTrace, 1, NULL);

	myTrace->tracingThread = tracingThread;
	myTrace->tracingControl = tracingControl;

	return myTrace;
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
 */
int rut_startTrace(UtilTrace *trace)
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
 */
int rut_stopTrace(UtilTrace *trace)
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
 * @param trace
 */
void rut_finalizeTrace(UtilTrace *trace)
{
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

	/* free all memory used for UtilTrace object */
	rut_free(myTrace);


#if 0
	tracingDataStruct *tracingData;
	rut_malloc(tracingData, 1, NULL);

	tracingData->sources = sources;
	tracingData->interval = interval;
	tracingData->control = tracingControl;

#endif

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
 */
static int changeTracingState(UtilTraceStruct *trace, gboolean newstate)
{
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
