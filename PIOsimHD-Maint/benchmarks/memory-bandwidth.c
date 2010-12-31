/*
 Author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <string.h>

#define getTime MPI_Wtime

#define ITERATIONS 10

int main (int argc, char *argv[])
{
  long size = 10*1024*1024;
  if (argc > 1){
    size = atol(argv[1]);
  }
  
  MPI_Init(& argc, & argv);
  printf("%s run with %ld of data\n", argv[0], size);
  
  char * buffer = malloc(size);
  
  double t = getTime();   
  for(int i=0 ; i < ITERATIONS; i++){
    memset(buffer, i, size);
  }
  double e = getTime() - t;
  
  printf("%d iterations, time:%fs MB/s:%f\n", ITERATIONS, e, ITERATIONS * size / 1024 / 1024 / e);
  
  MPI_Finalize();
  return 0;
}
