
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <signal.h>
#include <execinfo.h>

#include "error.h"
#include "error-strings.h"
#include "mpi.h"

/**
 * \author Johann Weging
**/



#define ERROR_SWITCH(code) \
	case HD_##code:\
  errorString = g_strdup_printf("%s\n", code);\
	break;



gchar* get_error_string(int errorCode)
{
  gchar* errorString; 

			
	switch(errorCode)
	{
		ERROR_SWITCH(CLI_PARSE)
    ERROR_SWITCH(PROGRAM_NAME);
		ERROR_SWITCH(PROJECT_FILE_PATH);
		ERROR_SWITCH(TRACE_FILE_PATH);
		ERROR_SWITCH(XML_PARSE);

    default:
      errorString = g_strdup_printf("%s", "");
  }

  return errorString;
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


void error(enum ErrorMode errorMode, int errorNo, const char* fmt, ...)
{
  va_list al;
  va_start(al, fmt);
	
	/*
	 *resulting error message that will be printed with out the stack trace
	 */
	GString* errorMessage = g_string_new(NULL); 
	gchar* tmpErrMsg = NULL; 
	int mpiErrLen;
  
	/*
	 *switch over the error mode and choose the method of how to handle the occurred error
	 */
  switch(errorMode)
	{
		/*warnings of HDRplay*/
		case WARN:
		/*
		 *are there any warings jet?
		 */
		break;

		/*error of HDRplay*/
    case ERR:
			errorMessage = g_string_append(errorMessage, (const gchar*) "Program ERROR:");
			tmpErrMsg = get_error_string(errorNo);

      errorMessage = g_string_append(errorMessage, (const gchar*) tmpErrMsg); 
    break;
    
    case SYS_WARN:
		/*
     *TODO System Warining if one is needed
		 */
    break;
    
		/*system error like missing files or missing rights*/
    case SYS_ERR:
			errorMessage = g_string_append(errorMessage,
											(const gchar*) "System ERROR:");

      errorMessage = g_string_append((GString*) errorMessage,
											(const gchar*) strerror(errorNo));
    break;
    
    case MPI_WARN:
			 /*
        *TODO catch mpi warnings
				*/
    break;
    
    case MPI_ERR:
			errorMessage = g_string_append(errorMessage, (const gchar*) "MPI ERROR:");
			
      MPI_Error_string(errorNo, (char*) tmpErrMsg, &mpiErrLen);
			errorMessage = g_string_append((GString*) errorMessage, (const gchar*) tmpErrMsg); 
  }
  
  g_string_append_vprintf(errorMessage, (const gchar*) fmt, al);

  fflush(stderr);
  show_stackframe();
  fprintf(stderr, "%s\n", (char*) errorMessage->str);
  fflush(NULL);
	
	if(tmpErrMsg )
	{
		g_free(tmpErrMsg);
	}

	/*
	*if the error mode is a warning, don't kill the program
	*FIXME: change the if statement to kill the program not return
	*/
  if(errorMode == ERR || errorMode == SYS_ERR || errorMode == MPI_ERR)
  {
		MPI_Abort(MPI_COMM_WORLD, 1);
  }
  
	return;
}


