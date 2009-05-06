/*
 * HDMPITracer.h
 * Contains the API which is visible within MPI programs
 *
 *  Created on: 05.05.2009
 *      Author: julian
 */

#ifndef HDMPITRACER_H_
#define HDMPITRACER_H_

#include "hdTrace.h"

/**
 * Return the tracefile used to store the threads values in the current topology
 */
hdTrace hdMPI_getThreadTracefile();

/**
 * Return the topology for the current thread
 */
hdTopology hdMPI_getThreadTopology();

/**
 * Enable the tracing for this thread (automatically invoked upon creation)
 */
int hdMPI_threadEnableTracing();

/**
 * Disable the tracing for this thread (can be reactivated via enable)
 */
int hdMPI_threadDisableTracing();


/**
 * Initalize tracing, to allow thread to use tracing.
 */
void hdMPI_threadInitTracing();

/**
 * Finalize trace file for the thread.
 */
void hdMPI_threadFinalizeTracing();

/**
 * Log Attributes
 */
#define hdMPI_threadLogAttributes(valueFormat, ...) hdT_logAttributes(hdMPI_getThreadTracefile(), valueFormat, __VA_ARGS__)

/**
 * Mark the start of a new state
 */
#define hdMPI_threadLogStateStart(stateName ) hdT_logStateStart(hdMPI_getThreadTracefile(), stateName)

/* Log Element
*
* Logs an element for the latest open state. A state is open
* if \a hdT_logStateStart without a corresponding \a hdT_logStateEnd
* has been called.
*/
#define hdMPI_threadLogElement(name, valueFormat, ... ) hdT_logElement(hdMPI_getThreadTracefile(), name, valueFormat, __VA_ARGS__)

/**
 * Mark the end of a state and write it
 */
#define hdMPI_threadLogStateEnd() hdT_logStateEnd(hdMPI_getThreadTracefile())

#define hdMPI_threadWriteInfo(format, ...) hdT_writeInfo(hdMPI_getThreadTracefile(), format, __VA_ARGS__)


void hdMPI_logInternalsMPI_Send(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6);
void hdMPI_logInternalsMPI_Recv(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Status * v7);
void hdMPI_logInternalsMPI_Isend(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7);
void hdMPI_logInternalsMPI_Irecv(void* v1,  int v2,  MPI_Datatype v3,  int v4,  int v5,  MPI_Comm v6,  MPI_Request * v7);
void hdMPI_logInternalsMPI_Sendrecv(void * v1,  int v2,  MPI_Datatype v3, int v4,  int v5,  void * v6,  int v7,  MPI_Datatype v8,  int v9,  int v10,  MPI_Comm v11,  MPI_Status * v12);
#endif

