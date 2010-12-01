#ifndef _INIT_H_
#define _INIT_H_

#include <string.h>
#include <stdio.h>
#include "structures.h"
#include "readproject.h"
#include "stdlib.h"
#include "error.h"
#include "constant.h"

/**
  \brief Reads the command line parameter.
  
  \param[in] argc Count of the parameters.
  \param[in] argv The parameters it self.
  \param[out] projectFile Path to the project file.
  \param[out] traceFileFolderPath Path to the folder containing the traces files.
  
   The function can't be called with out a proproject file. If no path to the folder
   of the trace files is given, the function uses the folder of the project file.
  
*/
int readCliArgs(char* projectFile, char* traceFileFolderPath, int argc, char** argv);


void init
(GSList** traceFile, GSList** comms, int rank, int size,
int argc, char** argv);

void genTraceFileName
(char* traceFilePath, char* traceFileFolderPath, char* programName,
char* hostname, int rank);

#endif
