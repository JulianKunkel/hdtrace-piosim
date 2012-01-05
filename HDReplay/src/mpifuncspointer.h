/**
* \author Johann Weging
*/
#ifndef _MPIFUNCSPOINTER_H_
#define _MPIFUNCSPOINTER_H_

#include "mpi.h"


int (*MpiBarrierPointer)
  (MPI_Comm);
  
int (*MpiComm_CreatePointer)
  (MPI_Comm, MPI_Group, &MPI_Comm);
  
int (*MPISendPointer)
  (void*, int, MPI_Datatype, int, int, MPI_Comm);
  
#endif
