/**
 * @file ptInternal.h
 *
 * This header is shared by pt.c and ptmain.c
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef PTINTERNAL_H_
#define PTINTERNAL_H_

#include <pthread.h>

#include "pt.h"
#include "conf.h"
#include "common.h"

typedef struct {
	int started : 1;
	int terminate : 1;
	pthread_cond_t cond;
	pthread_mutex_t mutex;
} threadControlStruct;


struct powertrace_s {
	ConfigStruct *config;
	pthread_t thread;
	threadControlStruct control;
};

extern int pt_directOutput;

#define ERROR_OUTPUT(msg, ...) \
	if (pt_directOutput) \
		ERRORMSG("PowerTracer: " msg "\n", ## __VA_ARGS__)

int createTracingThread(ConfigStruct *config, PowerTrace **trace);

#endif /* PTINTERNAL_H_ */
