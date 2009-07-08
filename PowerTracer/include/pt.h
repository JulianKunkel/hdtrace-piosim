/**
 * @file pt.h
 *
 * @date 02.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef PT_H_
#define PT_H_

typedef struct powertrace_s PowerTrace;

/*
 * Define error states
 */
#define PT_EOK            0
#define PT_ESYNTAX       -1
#define PT_ECONFNOTFOUND -2
#define PT_ECONFINVALID  -3
#define PT_ENOTRACES     -4
#define PT_EMEMORY       -5
#define PT_EHDLIB        -6
#define PT_EDEVICE       -7
#define PT_ETHREAD       -8


/**
 * Create a power trace using the passed configuration file
 */
int pt_createTrace(const char* configfile, PowerTrace **trace);

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
