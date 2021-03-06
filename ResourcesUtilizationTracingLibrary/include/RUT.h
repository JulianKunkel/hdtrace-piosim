/**
 * @file RUT.h
 *
 * Main header file of the Resources Utilization Tracing Library.
 *
 * Declarations of all functions and types for doing utilization tracing,
 * representing the API of the RUT Library.
 *
 * @ifnot api_only
 *  @ingroup RUT
 * @endif
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
 * HDTrace format. The traces can be embedded in an HDTrace project and visualized in
 * Sunshot, the native viewer for HDTrace format.
 */

#ifndef HDRUT_H_
#define HDRUT_H_

#include <stdint.h>
#include <stdbool.h>

#include "hdTopo.h"

/* ************************************************************************* *
 *                        FIXED VALUES DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of CPUs supported */
/* not using GLIBTOP_NCPU here to avoid *
 * dependency from libgtop/cpu.h header */
#define RUT_MAX_NUM_CPUS 64

/** Maximum number of network interfaces supported */
#define RUT_MAX_NUM_NETIFS 8
/* TODO: Check this somewhere */

#define RUT_MAX_HDD_STATS 40

#ifdef HAVE_DBC

#define MULTIPLIER 1000

#include "DBConnector.h"

#endif

/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/** Type definition of utilization trace object */
typedef struct UtilTrace_s UtilTrace;

enum PROC_DISKSTATS{
	DISKSTAT_HDD_READ_COMPLETED = 0,
	DISKSTAT_HDD_READS_MERGED, /* F2 */
	DISKSTAT_HDD_SECTORS_READ, /* F3 */	
	DISKSTAT_HDD_MS_SPEND_READING, /* F4 */	
	DISKSTAT_HDD_WRITES_COMPLETED, /* F5 */	
	DISKSTAT_HDD_WRITES_MERGE, /* F6 */
	DISKSTAT_HDD_SECTORS_WRITTEN, /* F7 */	
	DISKSTAT_HDD_MS_SPEND_WRITING, /* F8 */	
	DISKSTAT_HDD_IOS_INPROGRESS, /* F9 */	
	DISKSTAT_HDD_IOS_MS_SPEND, /* F10 */	
	DISKSTAT_HDD_WEIGHTED_WAITTIME, /* F11 */
	DISKSTAT_COUNT
};

#ifdef HAVE_DBC
/** Type definition of power trace object */
typedef struct NodePowerTrace_s NodePowerTrace;

#endif

/** Bit field for sources to trace */
struct rutSources_s {
	/** aggregated utilization of all CPUs */
	unsigned int CPU_UTIL :1;
	/** CPU utilization for each single CPU */
	unsigned int CPU_UTIL_X :1;
#ifdef HAVE_PROCESSORSTATES
	/** CPU frequency for each single CPU */
	unsigned int CPU_FREQ_X : 1;
	/** CPU c-states 1,2 and 3 for each single CPU */
	unsigned int CPU_IDLE_X : 1;
#endif
	/** amount of main memory used */
	unsigned int MEM_USED :1;
	/** amount of free main memory */
	unsigned int MEM_FREE :1;
	/** amount of shared main memory */
	unsigned int MEM_SHARED :1;
	/** amount of main memory used as buffer */
	unsigned int MEM_BUFFER :1;
	/** amount of main memory cached */
	unsigned int MEM_CACHED :1;
	/** incoming traffic of each single network interface */
	unsigned int NET_IN_X :1;
	/** outgoing traffic of each single network interface */
	unsigned int NET_OUT_X :1;
	/** aggregated incoming traffic of external network interfaces */
	unsigned int NET_IN_EXT :1;
	/** aggregated outgoing traffic of external network interfaces */
	unsigned int NET_OUT_EXT :1;
	/** aggregated incoming traffic of all network interfaces */
	unsigned int NET_IN :1;
	/** aggregated outgoing traffic of all network interfaces */
	unsigned int NET_OUT :1;
	/** amount of data read from hard disk drives */
	unsigned int HDD_READ :1;
	/** amount of data written to hard disk drives */
	unsigned int HDD_WRITE :1;
	/** HDD stats from /proc/diskstats **/
	unsigned int PROC_HDD_STATS[DISKSTAT_COUNT]; 	
};

/** Type definition of tracing sources bit field */
typedef struct rutSources_s rutSources;

/* ************************************************************************* *
 *                        PUBLIC MACRO DEFINITIONS                           *
 * ************************************************************************* */

/** Maximum number of statistics values traceable */
#ifdef HAVE_PROCESSORSTATES
#define RUT_MAX_STATS_VALUES (12 + (3 * RUT_MAX_NUM_CPUS) + 2 * RUT_MAX_NUM_NETIFS + RUT_MAX_HDD_STATS * DISKSTAT_COUNT)
#else
#define RUT_MAX_STATS_VALUES (12 + RUT_MAX_NUM_CPUS + 2 * RUT_MAX_NUM_NETIFS + RUT_MAX_HDD_STATS * DISKSTAT_COUNT)
#endif

/** Macro for cleaning all available sources */
#define RUTSRC_UNSET_ALL(sources) \
		do { \
		RUTSRC_UNSET_CPU(sources); \
		RUTSRC_UNSET_MEM(sources); \
		RUTSRC_UNSET_NET(sources); \
		RUTSRC_UNSET_HDD(sources); \
	} while (0)

/** Macro for setting all available sources */
#define RUTSRC_SET_ALL(sources) \
	do { \
		RUTSRC_SET_CPU(sources); \
		RUTSRC_SET_MEM(sources); \
		RUTSRC_SET_NET(sources); \
		RUTSRC_SET_HDD(sources); \
	} while (0)

/** Macro for setting/cleaning all CPU statistics at once */
#ifdef HAVE_PROCESSORSTATES
#define RUTSRC_SET_CPU__(sources, enabled) \
	do { \
		(sources).CPU_UTIL = enabled; \
		(sources).CPU_UTIL_X = enabled; \
		(sources).CPU_FREQ_X = enabled; \
		(sources).CPU_IDLE_X = enabled; \
	} while (0)
#else
#define RUTSRC_SET_CPU__(sources, enabled) \
	do { \
		(sources).CPU_UTIL = enabled; \
		(sources).CPU_UTIL_X = enabled; \
	} while (0)
#endif

/** Macro for setting/cleaning all memory statistics at once */
#define RUTSRC_SET_MEM__(sources, enabled) \
	do { \
		(sources).MEM_USED = enabled; \
		(sources).MEM_FREE = enabled; \
		(sources).MEM_SHARED = enabled; \
		(sources).MEM_BUFFER = enabled; \
		(sources).MEM_CACHED = enabled; \
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


#define RUTSRC_SET_HDD__(sources, bool) \
	do { \
		int my_i; \
		(sources).HDD_READ = bool; \
		(sources).HDD_WRITE = bool; \
		for(my_i = 0 ; my_i < DISKSTAT_COUNT ; my_i++){\
			(sources).PROC_HDD_STATS[my_i] = bool;\
		}\
	} while (0)

/** Macro for enabling tracing of all CPU statistics at once */
#define RUTSRC_SET_CPU(sources) RUTSRC_SET_CPU__(sources, true)

/** Macro for enabling tracing of all memory statistics at once */
#define RUTSRC_SET_MEM(sources) RUTSRC_SET_MEM__(sources, true)

/** Macro for enabling tracing of all NET statistics at once */
#define RUTSRC_SET_NET(sources) RUTSRC_SET_NET__(sources, true)

/** Macro for enabling tracing of all hard disk statistics at once */
#define RUTSRC_SET_HDD(sources) RUTSRC_SET_HDD__(sources, true)

/** Macro for disabling tracing of all CPU statistics at once */
#define RUTSRC_UNSET_CPU(sources) RUTSRC_SET_CPU__(sources, false)

/** Macro for disabling tracing of all memory statistics at once */
#define RUTSRC_UNSET_MEM(sources) RUTSRC_SET_MEM__(sources, false)

/** Macro for disabling tracing of all NET statistics at once */
#define RUTSRC_UNSET_NET(sources) RUTSRC_SET_NET__(sources, false)

/** Macro for disabling tracing of all hard disk statistics at once */
#define RUTSRC_UNSET_HDD(sources) RUTSRC_SET_HDD__(sources, false)

/* ************************************************************************* *
 *                       PUBLIC ERROR VALUE DEFINITIONS                      *
 * ************************************************************************* */

/** Success */
#define RUT_SUCCESS        0
/** Out of memory */
#define RUT_EMEMORY       -6
/** Error in HDTrace library */
#define RUT_EHDLIB        -7
/** Cannot create tracing thread */
#define RUT_ETHREAD       -9

#ifdef HAVE_DBC

/** Error in DBConnector */
#define RUT_EDBCONNECTOR  -10

#endif

/* ************************************************************************* *
 *                      PUBLIC FUNCTION DECLARATIONS                         *
 * ************************************************************************* */

/**
 * @addtogroup RUT
 *
 * @section seclu Library Usage
 * <b>Outline of creating Resources Utilization Traces using this library:</b>
 *  -# Define sources to trace (\ref rutSources, \b RUTSRC_SET_* macros)
 *  -# Create a \ref UtilTrace object (\ref rut_createTrace)
 *  -# Start tracing (\ref rut_startTracing)
 *  -# Stop tracing (\ref rut_stopTracing)
 *  -# Finalize the trace and destroy the \ref UtilTrace object (\ref rut_finalizeTrace)
 *
 * @subsection ssecds Defining sources
 * After creating a @ref rutSources object
 * @code
 * rutSources sources
 * @endcode
 * you can either set every source you want to trace by hand
 * @code
 * sources.CPU_UTIL = 1;
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
 * To create a \ref UtilTrace object, you first need to create an \a hdTopology and an
 * \a hdTopoNode object to define the place of your trace in a larger project.
 * Refer to \a hdTopology section in <i>HDTraceWritingCLibrary</i> documentation for
 * further information about this topic.
 * For the simplest case, you can use the following code fragment:
 * @code
 * const char *levels[] = {"Host"};
 * hdTopology *myTopology = hdT_createTopology("MyProject", levels, 1);
 * const char *path[] = {"host0"};
 * hdTopoNode *myTopoNode = hdT_createTopoNode(myTopology, path, 1);
 * @endcode
 *
 * @note \ref rut_finalizeTrace will not destroy the \a hdTopology and \a hdTopoNode objects
 *  for you, so if you don't need it for other purposes, you have to do this by
 *  calling \a hdT_destroyTopoNode and \a hdT_destroyTopology.
 *
 * Now that you have an \a hdTopology object, an \a hdTopoNode object and a \ref rutSources
 *  object with all sources set that you want to be included in the trace, you
 *  can create the \ref UtilTrace object:
 * @code
 * UtilTrace *myUtilTrace;
 * rut_createTrace(myTopology, myTopoNode, 1, mySources, 500, &myUtilTrace);
 * @endcode
 *
 * @note You cannot change the sources of an already created \ref UtilTrace object
 *
 * @subsection ssecsst Start and stop tracing
 * The tracing does not start before you tell it to by calling \ref rut_startTracing.
 *
 * You can start and stop tracing multiple times by calling \ref rut_startTracing
 * and \ref rut_stopTracing. When calling rut_stopTracing the current tracing period
 * is finished and no new period is started but the period timer is not reset.
 * So after a subsequent rut_startTracing, the tracing will first wait until the
 * current (virtual) period would end and take the next values at the beginning
 * of the next period.
 *
 * @section secenv Environment Variables
 *
 * There are two environment variables used by libRUT:<br>
 *  <center><tt>RUT_VERBOSITY</tt> and <tt>RUT_HDD_MOUNTPOINT</tt></center>
 *
 * <tt>RUT_VERBOSITY</tt> can be set to a number in the range -1 to 3.
 *  The default is 0 only showing error messaged. 1 enables warnings,
 *  2 enables info messages and 3 enable all debugging output.
 *  -1 makes the library absolutely silence, even in case of a fatal error.
 *  This value affects only the messages printed to stderr not the behavior
 *   of the functions.
 *
 * <tt>RUT_HDD_MOUNTPOINT</tt> specifies the mountpoint of the partition to be traces
 *  when \ref rutSources.HDD_READ or \ref rutSources.HDD_WRITE is enabled.
 *  Currently you can trace only one partition at the same time.<br>
 *   <b>Note:</b> The mount mountpoint must be specified without a trailing '/'
 */

/**
 * Create performance trace
 */
int rut_createTrace(hdTopoNode *topoNode, /* topoNode the trace belongs to */
int topoLevel, /* level of topology the trace take place */
rutSources sources, /* bit field of the sources to trace */
int interval, /* interval of one tracing step in ms */
UtilTrace **trace /* OUTPUT: the trace created */
);

/**
 * Start performance tracing
 */
int rut_startTracing(UtilTrace *trace);

/**
 * Stop performance tracing
 */
int rut_stopTracing(UtilTrace *trace);

/**
 * Finalize utilization trace object
 */
int rut_finalizeTrace(UtilTrace *trace);

#endif /* HDRUT_H_ */
