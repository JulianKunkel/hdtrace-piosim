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
  
int (*MpiFinalize)
  ();

int (*MPI_Gather)
  (void* , int, MPI_Datatype, void*, int, MPI_Datatype, int, MPI_Comm); 

int (*MPI_Gatherv)
  (void* , int, MPI_Datatype, void*, int *, int *, MPI_Datatype, int, MPI_Comm); 
int (*MpiInit)
  (int*, char***);
  
int (*MpiRecv)
  (void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Status*);
  
int (*MpiSend)
  (void*, int, MPI_Datatype, int, int, MPI_Comm);
  
int (*MpiSendrecv)
  (void *, int, MPI_Datatype,int, int, void *, int, MPI_Datatype, 
  int, int, MPI_Comm, MPI_Status *);
  
#endif
