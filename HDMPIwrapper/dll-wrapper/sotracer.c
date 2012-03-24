#include <unistd.h>
#include <sys/types.h>
#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>

#include <glib.h>

#ifdef HDTRACE
#include <mpi.h>
#include <hdTrace.h>
#include <hdMPITracer.h>

#else

#warning Not using hdTrace
#define hdMPI_threadLogAttributes(...)
#define hdMPI_threadLogStateStart(...)
#define hdMPI_threadLogStateEnd(...)
#endif

#ifndef GLIBCLIB
#define GLIBCLIB "/lib/libc.so.6"
#endif

#ifdef NETCDFLIB
#warning Will use NETCDFLIB as a default
#include <netcdf.h>
#endif

#ifdef HDF5LIB
#warning Will use HDF5LIB as a default
#include <hdf5.h>
#endif

#ifdef CDILIB
#warning Will use CDILIB as a default
#include <cdi.h>
#endif


// maps memory positions of the functions i.e. & write to the corresponding dlsym openend.

static __thread int started_tracing = 0;
static int initalized_tracing = 0;

// ensures that we will never nest calls.
static __thread int isNested = 0;

static void sotracer_initalize(void);

PYTHON_ADD_FUNCTIONS

static void sotracer_initalize(void){
  void * dllFile;
  void * symbol;

 /* allow only one initialization */
  if (initalized_tracing)
    return;

  initalized_tracing = 1;


#ifdef DEBUG
  printf("Initalizing!\n");
#endif

#define OPEN_DLL(defaultfile, libname) \
  { \
   char * file = getenv(libname); \
  if (file == NULL)\
	file = defaultfile;\
  printf("[SOTRACE] use %s for %s (env variable)\n", file, libname); \
  dllFile = dlopen(file, RTLD_LAZY); \
  if (dllFile == NULL){ \
    printf("[Error] trace wrapper - dll not found %s\n", file); \
    exit(1); \
  } \
  }

#define ADD_SYMBOL(name) \
  symbol = dlsym(dllFile, #name);\
  if (symbol == NULL){ \
     printf("[Error] trace wrapper - symbol not found %s\n", #name); \
  }


  PYTHON_ADD_DLL_OPEN

  /**
  OPEN_DLL(GLIBCLIB);
  ADD_SYMBOL(write);
  **/

  started_tracing = 1;
#undef ADD_SYMBOL
#undef OPEN_DLL
}

