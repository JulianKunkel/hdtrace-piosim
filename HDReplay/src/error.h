#ifndef _ERROR_H_
#define _ERROR_H_

#include <errno.h>
#include "constant.h"


enum ErrorMode{WARN=0, ERR, SYS_WARN, SYS_ERR, MPI_ERR, MPI_WARN};
void crash(enum ErrorMode, int, const char* fmt, ...);

void show_stackframe();

enum ErrorCode{
  SUCCESS=0,
  TRACE_FILE_PATH,
  PROJECT_FILE_PATH,
  XML_PARSE,
  PROGRAMM_NAME,
};
#endif

