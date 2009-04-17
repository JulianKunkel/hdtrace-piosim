#ifndef MPI_NAMES_H_
#define MPI_NAMES_H_

#include <mpi.h>

static const char * getCombinerName(int combiner);
static const char * getDistributeConstantName(int constant);
static char * getCommName(MPI_Comm comm);
static const char * getWhenceString(int whence);


#endif

