/**
 * @file pt.h
 *
 * @ifnot api_only
 *  @ingroup PT
 * @endif
 *
 * @date 02.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

/**
 * @addtogroup PT Power Tracer Library
 *
 * @details
 * The Power Tracer Library (libPT) is the part of the Power Tracer providing
 * the power tracing functionality directly to other programs. It is able to
 * trace the power measured by a power analyzer connected.
 *
 * Currently only the ZES ZIMMER LMG450 power analyzer connected via RS-232
 *  serial port is supported.
 *
 * The library uses the HDTraceWritingCLibrary to produce statistics traces as
 *  specified by the HDTrace format. The traces can be embedded in an HDTrace
 *  project and visualized in Sunshot, the native viewer for HDTrace format.
 */

#ifndef PT_H_
#define PT_H_

#include "hdTopo.h"

/** Type definition of power trace object */
typedef struct powertrace_s PowerTrace;

/*
 * Define error states
 */
/** Success */
#define PT_SUCCESS        0
/** TODO */
#define PT_ESYNTAX       -1
/** Could not find configuration file */
#define PT_ECONFNOTFOUND -2
/** Configuration read from file is invalid */
#define PT_ECONFINVALID  -3
/** TODO */
#define PT_EWRONGHOST    -4
/** No traces found in configuration */
#define PT_ENOTRACES     -5
/** Out of memory */
#define PT_EMEMORY       -6
/** Error in HDTrace library */
#define PT_EHDLIB        -7
/** Problem during communication with measurement device */
#define PT_EDEVICE       -8
/** Cannot create tracing thread */
#define PT_ETHREAD       -9

/**
 * @addtogroup PT
 *
 * @section seclu Library Usage
 * <b>Outline of creating Power Traces using this library:</b>
 *  -# Create a configuration file matching the setup of the power analyser and
 *     containing the traces you want to create.
 *  -# Create a \ref PowerTrace object (\ref pt_createTrace)
 *  -# Start tracing (\ref pt_startTracing)
 *  -# Stop tracing (\ref pt_stopTracing)
 *  -# Finalize the trace and destroy the \ref PowerTrace object (\ref pt_finalizeTrace)
 *
 * @subsection ssecds Creating the configuration file
 *
 * The configuration file has to contain the general hardware setup of the
 *  power analyzer and all traces to create with all relevant information.
 *  Therefore the file is divided into sections, one for the general setup
 *  and one for each trace to create.
 *
 * In the general section we need the following values:
 *  - \p device specifying the type of the power analyzer in use.
 *  - \p port specifying where the power analyzer is connected.
 *  - \p cycle specifying the period time for the tracing.
 *  - \p project specifying the project name.
 *  - \p topology specifying the topology for the project.
 *
 * In each trace section we need the following values:
 *  - \p type specifying the type of the trace.
 *  - \p node specifying the node to associate with the trace.
 *  - \p channel specifying the channel of the power analyzer to use for the trace.
 *  - \p values specifying which values to trace
 *
 * Example:
 * @code
 * [General]
 * device=LMG450
 * port=node06:/dev/ttyUSB0
 * cycle=100
 * project=MeinProjekt
 * topology=Cluster_Host_Process_Thread
 *
 * [Trace]
 * type=HDSTATS
 * node=pvs_node06
 * channel=1
 * values=Utrms,Itrms,P
 *
 * [Trace]
 * type=HDSTATS
 * node=pvs_node07
 * channel=2
 * values=Utrms,Itrms,P
 * @endcode
 *
 * - \p port has the format NODE:DEVICE. NODE is the node which has the power
 *    analyzer connected via the RS-232 port. DEVICE is the name of the serial
 *    device file to use.
 * - \p topology is built using the topology level types concatenated with
 *    underscores in the same way as the topology node path string is created.
 * - \p node is given as the topology node path string and has to match
 *    the topology defined. Of course, as in the example, the topology can
 *    have more levels than the node uses.
 * - \p values is a comma separated list of strings, defining the values to
 *    trace. The strings can be specific to the used power analyzer.
 *    Currently the three shown here are the only supported values.
 *
 * @subsection sseccpto Creating a PowerTrace object
 *
 * With the configuration file created you are ready to create a PowerTrace object:
 * @code
 * PowerTrace *myPowerTrace;
 * pt_createTrace("pt.cfg", NULL, &myUtilTrace);
 * @endcode
 *
 * @note Passing NULL to the second argument will use the topology defined in
 *       the configuration file.
 *
 * @subsection ssecsst Start and stop tracing
 * The tracing does not start before you tell it to by calling \ref pt_startTracing.
 *
 * You can start and stop tracing multiple times by calling \ref pt_startTracing
 * and \ref pt_stopTracing. When calling rut_stopTrace the current tracing period
 * is finished and no new period is started. Calling rut_startTrace immediately
 *  tells power analyzer to send date, so the exact start time depend on the
 *  arrival time of the new data.
 *
 * @section secenv Environment Variables
 *
 * There in an environment variables used by libPT: <tt>PT_VERBOSITY</tt>
 *
 * <tt>PT_VERBOSITY</tt> can be set to a number in the range -1 to 3.
 *  The default is 0 only showing error messaged. 1 enables warnings,
 *  2 enables info messages and 3 enable all debugging output.
 *  -1 makes the library absolutely silence, even in case of a fatal error.
 *  This value affects only the messages printed to stderr not the behavior
 *   of the functions.
 */

/**
 * Create a power trace using the passed configuration file
 */
int pt_createTrace(const char* configfile, hdTopology *topology,
		PowerTrace **trace);

/**
 * Return the hostname with the measuring device connected
 *  if specified in config file.
 */
char * pt_getHostname(PowerTrace *trace);

/**
 * Start the power tracing
 */
void pt_startTracing(PowerTrace *trace);

/**
 * Stop the power tracing
 */
void pt_stopTracing(PowerTrace *trace);

/**
 * Finalize and free a power trace
 */
int pt_finalizeTrace(PowerTrace *trace);

#endif /* PT_H_ */
