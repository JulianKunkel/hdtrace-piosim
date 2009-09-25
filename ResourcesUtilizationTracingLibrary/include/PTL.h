/**
 * @file PTL.h
 *
 * Main header file of the Performance Tracing Library.
 *
 * Declarations of all functions and types for doing performance tracing,
 * representing the API of the PTL.
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

/**
 * @addtogroup PTL Performance Tracing Library
 *
 * @details
 * The Performance Tracing Library (PTL) provides an easy way to produce traces
 * of several performance statistics available in a common UNIX system.
 * It utilizes the Gtop library to get the statistics and the
 * HDTraceWritingCLibrary to produce statistics traces as specified by the
 * HDTrace format. The traces can be shown directly in HDJumpshot, the native
 * viewer for HDTrace format. The produced statistics traces can also be
 * combined with each other and with other HDTrace parts into an HDTrace
 * project.
 */

#ifndef HDPTL_H_
#define HDPTL_H_

#include <stdint.h>

#include "hdTopo.h"

/* ************************************************************************* *
 *                        FIXED VALUES DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of CPUs supported */
/* not using GLIBTOP_NCPU here to avoid *
 * dependency from libgtop/cpu.h header */
#define PTL_MAX_NUM_CPUS 32

/** Maximum number of network interfaces supported */
#define PTL_MAX_NUM_NETIFS 8
/* TODO: Check this somewhere */


/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/** Type definition of performance trace object */
typedef struct PerfTrace_s PerfTrace;

/** Bit field for sources to trace */
struct ptlSources_s {
    /** aggregated load of all CPUs */
    unsigned int PTLSRC_CPU_LOAD : 1;
	/** CPU load for each single CPU */
    unsigned int PTLSRC_CPU_LOAD_X : 1;
    /** amount of main memory used */
    unsigned int PTLSRC_MEM_USED : 1;
    /** amount of free main memory */
    unsigned int PTLSRC_MEM_FREE  : 1;
    /** amount of shared main memory */
    unsigned int PTLSRC_MEM_SHARED  : 1;
    /** amount of main memory used as buffer */
    unsigned int PTLSRC_MEM_BUFFER : 1;
    /** amount of main memory cached */
    unsigned int PTLSRC_MEM_CACHED : 1;
    /** incoming traffic of each single network interface */
    unsigned int PTLSRC_NET_IN_X : 1;
    /** outgoing traffic of each single network interface */
    unsigned int PTLSRC_NET_OUT_X : 1;
    /** aggregated incoming traffic of external network interfaces */
    unsigned int PTLSRC_NET_IN_EXT : 1;
    /** aggregated outgoing traffic of external network interfaces */
    unsigned int PTLSRC_NET_OUT_EXT : 1;
    /** aggregated incoming traffic of all network interfaces */
    unsigned int PTLSRC_NET_IN : 1;
    /** aggregated outgoing traffic of all network interfaces */
    unsigned int PTLSRC_NET_OUT : 1;
    /** amount of data read from hard disk drives */
    unsigned int PTLSRC_HDD_READ : 1;
    /** amount of data written to hard disk drives */
    unsigned int PTLSRC_HDD_WRITE : 1;
};

/** Type definition of tracing sources bit field */
typedef struct ptlSources_s ptlSources;

/* ************************************************************************* *
 *                        PUBLIC MACRO DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of statistics values traceable */
#define PTL_MAX_STATS_VALUES (8 + PTL_MAX_NUM_CPUS + PTL_MAX_NUM_NETIFS)

/** Macro for cleaning all available sources */
#define PTLSRC_UNSET_ALL(sources) \
	bzero(&(sources), sizeof(sources))

/** Macro for setting all available sources */
#define PTLSRC_SET_ALL(sources) \
	do { \
		PTLSRC_SET_CPU(sources); \
		PTLSRC_SET_MEM(sources); \
		PTLSRC_SET_NET(sources); \
		PTLSRC_SET_HDD(sources); \
	} while (0)

/** Macro for setting/cleaning all CPU statistics at once */
#define PTLSRC_SET_CPU__(sources, bool) \
	do { \
		(sources).PTLSRC_CPU_LOAD = bool; \
		(sources).PTLSRC_CPU_LOAD_X = bool; \
	} while (0)

/** Macro for setting/cleaning all memory statistics at once */
#define PTLSRC_SET_MEM__(sources, bool) \
	do { \
		(sources).PTLSRC_MEM_USED = bool; \
		(sources).PTLSRC_MEM_FREE = bool; \
		(sources).PTLSRC_MEM_SHARED = bool; \
		(sources).PTLSRC_MEM_BUFFER = bool; \
		(sources).PTLSRC_MEM_CACHED = bool; \
	} while (0)

/** Macro for setting/cleaning all NET statistics at once */
#define PTLSRC_SET_NET__(sources, bool) \
	do { \
		(sources).PTLSRC_NET_IN_X = bool; \
		(sources).PTLSRC_NET_OUT_X = bool; \
		(sources).PTLSRC_NET_IN_EXT = bool; \
		(sources).PTLSRC_NET_OUT_EXT = bool; \
		(sources).PTLSRC_NET_IN = bool; \
		(sources).PTLSRC_NET_OUT = bool; \
	} while (0)

/** Macro for setting/cleaning all HDD statistics at once */
#define PTLSRC_SET_HDD__(sources, bool) \
	do { \
		(sources).PTLSRC_HDD_READ = bool; \
		(sources).PTLSRC_HDD_WRITE = bool; \
	} while (0)

/** Macro for enabling tracing of all CPU statistics at once */
#define PTLSRC_SET_CPU(sources) PTLSRC_SET_CPU__(sources, 1)

/** Macro for enabling tracing of all memory statistics at once */
#define PTLSRC_SET_MEM(sources) PTLSRC_SET_MEM__(sources, 1)

/** Macro for enabling tracing of all NET statistics at once */
#define PTLSRC_SET_NET(sources) PTLSRC_SET_NET__(sources, 1)

/** Macro for enabling tracing of all hard disk statistics at once */
#define PTLSRC_SET_HDD(sources) PTLSRC_SET_HDD__(sources, 1)


/* ************************************************************************* *
 *                      PUBLIC FUNCTION DECLARATIONS                         *
 * ************************************************************************* */

/**
 * @addtogroup PTL
 *
 * @section seclu Library Usage
 * <b>Outline of creating Performance Traces using this library:</b>
 *  -# Define sources to trace (@ref ptlSources, PTLSRC_SET_* macros)
 *  -# Create a @ref PerfTrace object (@ref ptl_createTrace)
 *  -# Start tracing (@ref ptl_startTrace)
 *  -# Stop tracing (@ref ptl_stopTrace)
 *  -# Use the group to trace data (@b hdS_write..., called many times)
 *  -# Destroy the @ref PerfTrace object (@ref ptl_destroyTrace)
 *
 * @subsection ssecds Defining sources
 * After creating a @ref ptlSources object
 * @code
 * ptlSources sources
 * @endcode
 * you can either set every source you want to trace by hand
 * @code
 * sources.PTLSRC_CPU_LOAD = 1;
 * @endcode
 * or you can use the provided macros to set a whole source group at once
 * @code
 * PTLSRC_SET_CPU(sources)
 * @endcode
 * or to simply set all sources available
 * @code
 * PTLSRC_SET_ALL(sources)
 * @endcode
 *
 * @subsection sseccpto Creating a PerfTrace object
 * To create a PerfTrace object, you first need to create a hdTopology and a
 * hdTopoNode object to define the place of your trace in a larger project.
 * Refer to hdTopology Section in HDTraceWritingCLibrary documentation for
 * further information about this topic.
 * In the simplest case, when you do not plan to integrate your trace into a
 * HDTrace project, you can create the simplest hdTopology and hdTopoNode
 * objects possible using the provided macros:
 * @code
 * PTL_GET_SIMPLEST_TOPOLOGY(myTopology, myProject)
 * PTL_GET_SIMPLEST_TOPONODE(myTopoNode)
 * @endcode
 *
 * @note ptl_destroyTrace will not destroy the hdTopology and hdTopoNode object
 *  for you, so if you don't need it for other purposes, you have to do this by
 *  calling hdT_destroyTopoNode and hdT_destroyTopology.
 *
 * Now that you have a hdTopology object, a hdTopoNode object and a ptlSources
 *  object with all sources set that you want to be included in the trace, you
 *  can create the PerfTrace object:
 * @code
 * PerfTrace *myPerfTrace ptl_createTrace(myTopology, myTopoNode, 1,
 * 		mySources, 500);
 * @endcode
 *
 * @note You cannot change the sources of an already created PerfTrace object
 *
 * @subsection ssecsst Start and stop tracing
 * The tracing does not start before you tell it to by calling
 *  @ref ptl_startTrace.
 *
 * You can start and stop tracing multiple times by calling @ref ptl_startTrace
 * and @ref ptl_stopTrace. When calling ptl_stopTrace the current tracing period
 * is finished and no new period is started but the period timer is not reset.
 * So after a subsequent ptl_startTrace, the tracing will first wait until the
 * current (virtual) period would end and take the next values at the beginning
 * of the next period.
 */

/**
 * Create performance trace
 */
PerfTrace * ptl_createTrace(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		ptlSources sources,  /* bit field of the sources to trace */
		int interval         /* interval of one tracing step in ms */
		);

/**
 * Start performance tracing
 */
int ptl_startTrace(PerfTrace *trace);

/**
 * Stop performance tracing
 */
int ptl_stopTrace(PerfTrace *trace);

/**
 * Destroy performance trace object
 */
void ptl_destroyTrace(PerfTrace *trace);

#endif /* HDPTL_H_ */
