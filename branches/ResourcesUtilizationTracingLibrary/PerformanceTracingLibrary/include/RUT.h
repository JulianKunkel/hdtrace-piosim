/**
 * @file RUT.h
 *
 * Main header file of the Resources Utilization Tracing Library.
 *
 * Declarations of all functions and types for doing performance tracing,
 * representing the API of the RUT Library.
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

/**
 * @addtogroup RUT Resources Utilization Tracing Library
 *
 * @details
 * The Resources Utilization Tracing Library (libRUT) provides an easy way to produce traces
 * of several utilization statistics available in a common UNIX system.
 * It utilizes the Gtop library to get the statistics and the
 * HDTraceWritingCLibrary to produce statistics traces as specified by the
 * HDTrace format. The traces can be shown directly in Sunshot, the native
 * viewer for HDTrace format. The produced statistics traces can also be
 * combined with each other and with other HDTrace parts into an HDTrace
 * project.
 */

#ifndef HDRUT_H_
#define HDRUT_H_

#include <stdint.h>

#include "hdTopo.h"

/* ************************************************************************* *
 *                        FIXED VALUES DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of CPUs supported */
/* not using GLIBTOP_NCPU here to avoid *
 * dependency from libgtop/cpu.h header */
#define RUT_MAX_NUM_CPUS 32

/** Maximum number of network interfaces supported */
#define RUT_MAX_NUM_NETIFS 8
/* TODO: Check this somewhere */


/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/** Type definition of performance trace object */
typedef struct UtilTrace_s UtilTrace;

/** Bit field for sources to trace */
struct rutSources_s {
    /** aggregated load of all CPUs */
    unsigned int CPU_LOAD : 1;
	/** CPU load for each single CPU */
    unsigned int CPU_LOAD_X : 1;
    /** amount of main memory used */
    unsigned int MEM_USED : 1;
    /** amount of free main memory */
    unsigned int MEM_FREE  : 1;
    /** amount of shared main memory */
    unsigned int MEM_SHARED  : 1;
    /** amount of main memory used as buffer */
    unsigned int MEM_BUFFER : 1;
    /** amount of main memory cached */
    unsigned int MEM_CACHED : 1;
    /** incoming traffic of each single network interface */
    unsigned int NET_IN_X : 1;
    /** outgoing traffic of each single network interface */
    unsigned int NET_OUT_X : 1;
    /** aggregated incoming traffic of external network interfaces */
    unsigned int NET_IN_EXT : 1;
    /** aggregated outgoing traffic of external network interfaces */
    unsigned int NET_OUT_EXT : 1;
    /** aggregated incoming traffic of all network interfaces */
    unsigned int NET_IN : 1;
    /** aggregated outgoing traffic of all network interfaces */
    unsigned int NET_OUT : 1;
    /** amount of data read from hard disk drives */
    unsigned int HDD_READ : 1;
    /** amount of data written to hard disk drives */
    unsigned int HDD_WRITE : 1;
};

/** Type definition of tracing sources bit field */
typedef struct rutSources_s rutSources;

/* ************************************************************************* *
 *                        PUBLIC MACRO DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of statistics values traceable */
#define RUT_MAX_STATS_VALUES (8 + RUT_MAX_NUM_CPUS + RUT_MAX_NUM_NETIFS)

/** Macro for cleaning all available sources */
#define RUTSRC_UNSET_ALL(sources) \
	bzero(&(sources), sizeof(sources))

/** Macro for setting all available sources */
#define RUTSRC_SET_ALL(sources) \
	do { \
		RUTSRC_SET_CPU(sources); \
		RUTSRC_SET_MEM(sources); \
		RUTSRC_SET_NET(sources); \
		RUTSRC_SET_HDD(sources); \
	} while (0)

/** Macro for setting/cleaning all CPU statistics at once */
#define RUTSRC_SET_CPU__(sources, bool) \
	do { \
		(sources).CPU_LOAD = bool; \
		(sources).CPU_LOAD_X = bool; \
	} while (0)

/** Macro for setting/cleaning all memory statistics at once */
#define RUTSRC_SET_MEM__(sources, bool) \
	do { \
		(sources).MEM_USED = bool; \
		(sources).MEM_FREE = bool; \
		(sources).MEM_SHARED = bool; \
		(sources).MEM_BUFFER = bool; \
		(sources).MEM_CACHED = bool; \
	} while (0)

/** Macro for setting/cleaning all NET statistics at once */
#define RUTSRC_SET_NET__(sources, bool) \
	do { \
		(sources).NET_IN_X = bool; \
		(sources).NET_OUT_X = bool; \
		(sources).NET_IN_EXT = bool; \
		(sources).NET_OUT_EXT = bool; \
		(sources).NET_IN = bool; \
		(sources).NET_OUT = bool; \
	} while (0)

/** Macro for setting/cleaning all HDD statistics at once */
#define RUTSRC_SET_HDD__(sources, bool) \
	do { \
		(sources).HDD_READ = bool; \
		(sources).HDD_WRITE = bool; \
	} while (0)

/** Macro for enabling tracing of all CPU statistics at once */
#define RUTSRC_SET_CPU(sources) RUTSRC_SET_CPU__(sources, 1)

/** Macro for enabling tracing of all memory statistics at once */
#define RUTSRC_SET_MEM(sources) RUTSRC_SET_MEM__(sources, 1)

/** Macro for enabling tracing of all NET statistics at once */
#define RUTSRC_SET_NET(sources) RUTSRC_SET_NET__(sources, 1)

/** Macro for enabling tracing of all hard disk statistics at once */
#define RUTSRC_SET_HDD(sources) RUTSRC_SET_HDD__(sources, 1)


/* ************************************************************************* *
 *                      PUBLIC FUNCTION DECLARATIONS                         *
 * ************************************************************************* */

/**
 * @addtogroup RUT
 *
 * @section seclu Library Usage
 * <b>Outline of creating Resources Utilization Traces using this library:</b>
 *  -# Define sources to trace (@ref rutSources, RUTSRC_SET_* macros)
 *  -# Create a @ref UtilTrace object (@ref rut_createTrace)
 *  -# Start tracing (@ref rut_startTrace)
 *  -# Stop tracing (@ref rut_stopTrace)
 *  -# Use the group to trace data (@b hdS_write..., called many times)
 *  -# Destroy the @ref UtilTrace object (@ref rut_finalizeTrace)
 *
 * @subsection ssecds Defining sources
 * After creating a @ref rutSources object
 * @code
 * rutSources sources
 * @endcode
 * you can either set every source you want to trace by hand
 * @code
 * sources.CPU_LOAD = 1;
 * @endcode
 * or you can use the provided macros to set a whole source group at once
 * @code
 * RUTSRC_SET_CPU(sources)
 * @endcode
 * or to simply set all sources available
 * @code
 * RUTSRC_SET_ALL(sources)
 * @endcode
 *
 * @subsection sseccpto Creating a UtilTrace object
 * To create a UtilTrace object, you first need to create a hdTopology and a
 * hdTopoNode object to define the place of your trace in a larger project.
 * Refer to hdTopology Section in HDTraceWritingCLibrary documentation for
 * further information about this topic.
 * In the simplest case, when you do not plan to integrate your trace into a
 * HDTrace project, you can create the simplest hdTopology and hdTopoNode
 * objects possible using the provided macros:
 * @code
 * RUT_GET_SIMPLEST_TOPOLOGY(myTopology, myProject)
 * RUT_GET_SIMPLEST_TOPONODE(myTopoNode)
 * @endcode
 *
 * @note rut_finalizeTrace will not destroy the hdTopology and hdTopoNode object
 *  for you, so if you don't need it for other purposes, you have to do this by
 *  calling hdT_destroyTopoNode and hdT_destroyTopology.
 *
 * Now that you have a hdTopology object, a hdTopoNode object and a rutSources
 *  object with all sources set that you want to be included in the trace, you
 *  can create the UtilTrace object:
 * @code
 * UtilTrace *myUtilTrace rut_createTrace(myTopology, myTopoNode, 1,
 * 		mySources, 500);
 * @endcode
 *
 * @note You cannot change the sources of an already created UtilTrace object
 *
 * @subsection ssecsst Start and stop tracing
 * The tracing does not start before you tell it to by calling
 *  @ref rut_startTrace.
 *
 * You can start and stop tracing multiple times by calling @ref rut_startTrace
 * and @ref rut_stopTrace. When calling rut_stopTrace the current tracing period
 * is finished and no new period is started but the period timer is not reset.
 * So after a subsequent rut_startTrace, the tracing will first wait until the
 * current (virtual) period would end and take the next values at the beginning
 * of the next period.
 */

/**
 * Create performance trace
 */
UtilTrace * rut_createTrace(
		hdTopoNode *topoNode, /* topoNode the trace belongs to */
		int topoLevel,       /* level of topology the trace take place */
		rutSources sources,  /* bit field of the sources to trace */
		int interval         /* interval of one tracing step in ms */
		);

/**
 * Start performance tracing
 */
int rut_startTrace(UtilTrace *trace);

/**
 * Stop performance tracing
 */
int rut_stopTrace(UtilTrace *trace);

/**
 * Destroy performance trace object
 */
void rut_finalizeTrace(UtilTrace *trace);

#endif /* HDRUT_H_ */
