/**
 * @file ptInternal.h
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef PTINTERNAL_H_
#define PTINTERNAL_H_

#include <pthread.h>

#include "pt.h"
#include "conf.h"

typedef struct {
	int started : 1;
	int terminate : 1;
	pthread_cond_t cond;
	pthread_mutex_t mutex;
} threadControlStruct;


struct powertrace_s {
	ConfigStruct *config;
	int directOutput;
	pthread_t thread;
	threadControlStruct control;
};

/*
 * Define error states
 */
#define EOK            0
#define ESYNTAX       -1
#define ECONFNOTFOUND -2
#define ECONFINVALID  -3
#define ENOTRACES     -4
#define EMEMORY       -5
#define EHDLIB        -6
#define EDEVICE       -7
#define ETHREAD       -8

#define EOTHER      -100

#define ERROR_OUTPUT(msg, ...) \
	if (directOutput) \
		fprintf(stderr, "PowerTracer: " msg "\n", ## __VA_ARGS__)

int createTracingThread(ConfigStruct *config, int directOutput,
		PowerTrace **trace);

#endif /* PTINTERNAL_H_ */
