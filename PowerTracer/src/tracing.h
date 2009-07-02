/**
 * @file tracing.h
 *
 * @date 02.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef TRACING_H_
#define TRACING_H_

#include "pt.h"

/**
 * Main tracing loop
 */
int traceLoop(int serial_fd, PowerTrace *trace);


#endif /* TRACING_H_ */
