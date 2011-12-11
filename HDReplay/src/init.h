#ifndef _INIT_H_
#define _INIT_H_

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <glib.h>

#include "structures.h"
#include "readproject.h"
#include "readtrace.h"
#include "error.h"
#include "constant.h"


/**
 * @brief 
 *
 * @param projectFile
 * @param traceFileFolderPath
 * @param argc
 * @param argv
 *
 * @return 
 */
void read_cli_args(gchar **projectFile, gchar **traceFileFolderPath, int argc, char** argv);

/**
 * @brief 
 *
 * @param traceFile
 * @param comms
 * @param fileList
 * @param rank
 * @param size
 * @param argc
 * @param argv
 */
void init
(GSList** traceFile, GSList** comms, GSList** fileList, GSList **dataTypes,
 int rank, int size, int argc, char** argv);

/**
 * @brief 
 *
 * @param traceFilePath
 * @param traceFileFolderPath
 * @param programName
 * @param hostname
 * @param rank
 */
gchar *gen_trace_file_name
(gchar *traceFileFolderPath, gchar *programName, gchar *hostname, int rank);

void gen_communicator_hash(struct Communicator* communicators);

#endif
