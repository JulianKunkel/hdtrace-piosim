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
#define hdTrace_isWritingToFile(...) 0
#endif

#ifndef GLIBCLIB
#define GLIBCLIB "/lib/libc.so.6"
#endif

#ifdef NETCDFLIB
#warning Will use NETCDFLIB
#include <netcdf.h>
#endif

#ifdef HDF5LIB
#warning Will use HDF5LIB
#include <hdf5.h>
#endif

// maps memory positions of the functions i.e. & write to the corresponding dlsym openend.

static __thread int started_tracing = 0;
static int initalized_tracing = 0;

// ensures that we will never nest calls.
static __thread int isNested = 0;

PYTHON_ADD_FUNCTIONS

void sotracer_initalize(void){
  void * dllFile;
  void * symbol;

 /* allow only one initialization */
  if (initalized_tracing)
    return;

  initalized_tracing = 1;


#ifdef DEBUG
  printf("Initalizing!\n");
#endif

#define OPEN_DLL(file)  dllFile = dlopen(file, RTLD_LAZY); \
  if (dllFile == NULL){ \
    printf("[Error] trace wrapper - dll not found %s\n", file); \
    exit(1); \
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
#undef ADD_SYMBOL
#undef OPEN_DLL
}

void sotracer_disable(){
  started_tracing = 0;
}

void sotracer_enable(){
  started_tracing = 1;
}

void sotracer_finalize(){
  sotracer_disable();
}

void sotracer_initalize_ () __attribute__ ((weak, alias ("sotracer_initalize")));
void sotracer_initalize__ () __attribute__ ((weak, alias ("sotracer_initalize")));
void sotracer_finalize_ () __attribute__ ((weak, alias ("sotracer_initalize")));
void sotracer_finalize__ () __attribute__ ((weak, alias ("sotracer_finalize")));

