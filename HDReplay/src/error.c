#include "error.h"
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include "mpi.h"
#include <signal.h>
#include <execinfo.h>

/**
 * \author Johann Weging
 * \date 2010-09-29
**/

char* getErrorString(int errorCode)
{
  char* errorString = (char*) malloc(sizeof(char)*MAX_ERROR_STRING);
  switch(errorCode)
  {
    case TRACE_FILE_PATH:
      errorString = "No trace file path given.";
      break;
    case PROJECT_FILE_PATH:
      errorString = "No project file given.";
      break;
    case XML_PARSE:
      errorString = "XML parsing error.";
      break;
    case PROGRAMM_NAME:
      errorString = "No programm name given.";
  }
  return errorString;
}

void crash(enum ErrorMode errorMode, int errorNo, const char* fmt, ...)
{
  va_list al;
  va_start(al, fmt);
  
  int errorLen = MPI_MAX_ERROR_STRING + MAX_ERROR_STRING;
  char buffer[errorLen];
  char mpiErr[MPI_MAX_ERROR_STRING];
  
  int mpiErrLen;
  
  
   
  switch(errorMode)
  {
    case WARN:
    // TODO: Warining for a wrong oder of timestamps and or missing elements (no recv for a send)
    break;
    
    case ERR:
      strncat(buffer,"PROGRAMM ERROR: ", errorLen-1);
      vsprintf(buffer+strlen(buffer), fmt, al);
      strncat(buffer, getErrorString(errorNo), errorLen-1);
    break;
    
    case SYS_WARN:
    // TODO System Warining if one is needed
    break;
    
    case SYS_ERR:
      strncat(buffer,"SYSTEM ERROR: ", errorLen-1);
      vsprintf(buffer+strlen(buffer), fmt, al);
      sprintf(buffer, strerror(errorNo), errorLen-1); 
    break;
    
    case MPI_WARN:
      // TODO catch mpi warnings
    break;
    
    case MPI_ERR:
      strncat(buffer, "MPI ERROR: ",errorLen-1);
      vsprintf(buffer+strlen(buffer), fmt,al);
      MPI_Error_string(errorNo, mpiErr, &mpiErrLen);
      strncat(buffer, mpiErr, errorLen-1);
    
  }
  
  fflush(stderr);
  show_stackframe();
  fprintf(stderr, "%s\n", buffer);
  fflush(NULL);
  
  if(errorMode == WARN || errorMode == SYS_WARN || errorMode == MPI_WARN)
  {
    return;
  }
  MPI_Abort(MPI_COMM_WORLD, 0);
  
}


void show_stackframe() {
  void *trace[16];
  char **messages = (char **)NULL;
  int i, trace_size = 0;

  trace_size = backtrace(trace, 16);
  messages = backtrace_symbols(trace, trace_size);
  fprintf(stderr, "[bt] Execution path:\n");
  for (i=3; i<trace_size; ++i)
  {
    
    fprintf(stderr, "[bt] %s\n", messages[i]);
  }
}


