#include "main.h"

int main(int argc, char** argv)
{

  int err;
  int rank;
  int size;
  GSList * traceFile;
  GSList * comms;
  

  MPI_Init(&argc,&argv);
  
  MPI_Errhandler_set(MPI_COMM_WORLD, MPI_ERRORS_RETURN);
  
  if((err = MPI_Comm_rank(MPI_COMM_WORLD, &rank)) != MPI_SUCCESS)
  {
    crash(MPI_ERR, err, "",NULL);
  }
  
  if((err = MPI_Comm_size(MPI_COMM_WORLD, &size)) != MPI_SUCCESS)
  {
    crash(MPI_ERR, err, "", NULL);
  }
  
  init(&traceFile, &comms, rank, size, argc, argv);

  //printf("Hostname: %s\n", topo->hostname);
  MPI_Finalize();
  return 0;
}
