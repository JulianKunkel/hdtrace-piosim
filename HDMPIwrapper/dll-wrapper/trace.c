#include <unistd.h>
#include <sys/types.h>
#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>

#include <glib.h>

#ifndef GLIBCLIB
#define GLIBCLIB "/lib/libc.so.6"
#endif

#ifdef NETCDFLIB
#warning Will use NETCDFLIB
#endif

#ifdef HDF5LIB
#warning Will use HDF5LIB
#endif

// maps memory positions of the functions i.e. & write to the corresponding dlsym openend.
static GHashTable * loadedSymbols = NULL;

PYTHON_ADD_FUNCTIONS

void initDynamicLoader(void){
  loadedSymbols = g_hash_table_new(g_direct_hash, g_direct_equal);

  void * dllFile;
  void * symbol;

#define OPEN_DLL(file)  dllFile = dlopen(file, RTLD_LAZY); \
  if (dllFile == NULL){ \
    printf("[Error] trace wrapper - dll not found %s\n", file); \
    exit(1); \
  }

#define ADD_SYMBOL(name) \
  symbol = dlsym(dllFile, #name);\
  if (symbol == NULL){ \
     printf("[Error] trace wrapper - symbol not found %s\n", #name); \
  } \
  g_hash_table_insert(loadedSymbols, & name, symbol);


  PYTHON_ADD_DLL_OPEN

  /**
  OPEN_DLL(GLIBCLIB);
  ADD_SYMBOL(write);
  **/
#undef ADD_SYMBOL
#undef OPEN_DLL
}

void closeDynamicLoader(){
  // TODO
}
