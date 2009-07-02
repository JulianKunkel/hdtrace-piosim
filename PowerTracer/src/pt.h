/**
 * @file pt.h
 *
 * @date 02.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef PT_H_
#define PT_H_

typedef struct powertrace_s PowerTrace;

/**
 * Create a power trace using the passed configuration file
 */
int pt_createTrace(const char* configfile, PowerTrace **trace);

/**
 * Return the hostname with the measuring device connected
 */
char *pt_getHostname(PowerTrace *trace);

/**
 * Start the power tracing
 */
int pt_startTracing(PowerTrace *trace);

/**
 * Stop the power tracing
 */
int pt_stopTracing(PowerTrace *trace);

/**
 * Finalize and free a power trace
 */
int pt_finalizeTrace(PowerTrace *trace);

#endif /* PT_H_ */
