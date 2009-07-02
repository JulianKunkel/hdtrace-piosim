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

#endif /* PTINTERNAL_H_ */
