/**
 * @file hdPTL.c
 *
 * @date 11.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "PTL.h"

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


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

typedef struct PerfTrace_s
{
	GThread *tracingThread;
	tracingControlStruct *tracingControl;
} PerfTraceStruct;


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

static int changeEnableState(PerfTrace *trace, gboolean newstate);


/* ************************************************************************* */
/*                                                                           */
/* ************************************************************************* */

int ptl_verbosity;

/**
 * Create performance trace
 *
 * @param topology
 * @param topoNode
 * @param topoLevel
 * @param sources
 * @param interval
 *
 * @return
 */
PerfTrace * ptl_createTrace(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		ptlSources sources,  /* bit field of the sources to trace */
		int interval         /* interval of one tracing step in ms */
		)
{
	/*
	 * Set verbosity as requested by environment
	 */
	ptl_verbosity = 1;
	char *verbstr = getenv("PTL_VERBOSITY");
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
	ptl_malloc(tracingControl, 1, NULL);

	tracingControl->mutex = g_mutex_new();
	tracingControl->stateChanged = g_cond_new();
	tracingControl->enabled = FALSE;


	/*
	 * Create and fill tracing data structure
	 */

	tracingDataStruct *tracingData;
	ptl_malloc(tracingData, 1, NULL);

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

	PerfTraceStruct *myTrace;
	ptl_malloc(myTrace, 1, NULL);

	myTrace->tracingThread = tracingThread;
	myTrace->tracingControl = tracingControl;

	return myTrace;
}

/**
 * Start performance tracing
 *
 * @param trace Trace object to work on
 *
 * @return Indicate if tracing enable state changed
 *
 * @retval  0  Tracing is now enabled (was disabled before)
 * @retval  1  Tracing is enabled (as it was already before)
 */
int ptl_startTrace(PerfTrace *trace)
{
	PerfTraceStruct *myTrace = trace;
	return changeEnableState(myTrace, TRUE);
}

/**
 * Stop performance tracing
 *
 * @param trace Trace object to work on
 *
 * @return Indicate if tracing enable state changed
 *
 * @retval  0  Tracing is now disabled (was enabled before)
 * @retval  1  Tracing is disabled (as it was already before)
 */
int ptl_stopTrace(PerfTrace *trace)
{
	PerfTraceStruct *myTrace = trace;
	return changeEnableState(myTrace, FALSE);
}

/**
 * Destroy performance trace object
 */
void ptl_destroyTrace(PerfTrace *trace)
{
	PerfTraceStruct *myTrace = trace;

	/* request quitting of the tracing thread */
	g_mutex_lock(myTrace->tracingControl->mutex);
	myTrace->tracingControl->quit = TRUE;
	g_cond_broadcast(myTrace->tracingControl->stateChanged);
	g_mutex_unlock(myTrace->tracingControl->mutex);

	/* join the tracing thread to wait for its end */
	g_thread_join(myTrace->tracingThread);

	/* free all memory used for thread control */
	g_cond_free(myTrace->tracingControl->stateChanged);
	g_mutex_free(myTrace->tracingControl->mutex);
	ptl_free(myTrace->tracingControl);

	/* free all memory used for PerfTrace object */
	ptl_free(myTrace);


#if 0
	tracingDataStruct *tracingData;
	ptl_malloc(tracingData, 1, NULL);

	tracingData->sources = sources;
	tracingData->interval = interval;
	tracingData->control = tracingControl;

#endif

}


/**
 * Change tracing enable state
 *
 * @param trace     Trace object to work on
 * @param newstate  State to set
 *
 * @return Indicate if tracing enable state changed
 *
 * @retval  0  Tracing state changed
 * @retval  1  Tracing state was already the requested one
 */
static int changeEnableState(PerfTraceStruct *myTrace, gboolean newstate)
{
	int retval = 0;
	g_mutex_lock(myTrace->tracingControl->mutex);
	if (myTrace->tracingControl->enabled == newstate)
	{
		/* tracing is already enabled/disabled */
		retval = 1;
	}
	else
	{
		/* enable/disable tracing and notify tracing thread */
		myTrace->tracingControl->enabled = newstate;
		g_cond_broadcast(myTrace->tracingControl->stateChanged);
	}
	g_mutex_unlock(myTrace->tracingControl->mutex);
	return retval;
}
