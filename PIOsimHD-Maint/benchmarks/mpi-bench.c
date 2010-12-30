/*
 Author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <string.h>

typedef struct{
  int repeats;
} Options;

static Options options = {3};

static int rank, nproc;

/* returns the test time */
typedef double (*testFunc)(long param1);

typedef struct{
   const testFunc func;
   char const * name;
   long param1;
}Test;


#define getTime MPI_Wtime

#define CHECKRET if(ret != 0) printf("Error in function %s in line %d\n", __FUNCTION__, __LINE__);

double reduceTo0(long size){
  double * sendbuf = malloc(size*8);
  double * recvbuf = malloc(size*8);
  
  memset(sendbuf, 0, size*8);
  
  double t = getTime();   
  int ret = MPI_Reduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}

double allreduce(long size){
  double * sendbuf = malloc(size*8);
  double * recvbuf = malloc(size*8);
  
  memset(sendbuf, 0, size*8);
  
  double t = getTime();   
  int ret = MPI_Allreduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double bcast(long size){
  double * buffer = malloc(size*8);
  
  memset(buffer, 0, size*8);
  
  double t = getTime();   
  int ret = MPI_Bcast(buffer, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t;
  free(buffer);
  
  return e;
}


double barrier(long unused){
 
  double t = getTime();   
  int ret = MPI_Barrier(MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t; 
  return e;
}


double gather(long size){
  double * sendbuf = malloc(size*8);
  double * recvbuf = malloc(size*8 * nproc);
  
  memset(sendbuf, 0, size*8);
  
  double t = getTime();   
  int ret =     MPI_Gather(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double scatter(long size){
  double * sendbuf = malloc(size*8 * nproc);
  double * recvbuf = malloc(size*8);
  
  memset(sendbuf, 0, size*8);
  
  double t = getTime();   
  int ret =     MPI_Scatter(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double sendRecvRightNeighbour(long size){
  double * sendbuf = malloc(size*8);
  double * recvbuf = malloc(size*8);
  
  memset(sendbuf, 0, size*8);
  
  const int dest = rank + 1 == size ? 0 : rank +1 ;
  const int source = rank - 1 > 0 ? rank - 1 : size -1;
  
  double t = getTime();
  int ret =       MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711,
      recvbuf, size, MPI_DOUBLE, source, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double sendRecvPaired(long size){
  
  if(size % 2 == 1){
    /* test is run only with equal number of processes */
    return 0;
  }
  
  double * sendbuf = malloc(size*8);
  double * recvbuf = malloc(size*8);
  
  memset(sendbuf, 0, size*8);
  
  
  const int dest = rank % 2 == 0 ? rank + 1 : rank -1;
  
  double t = getTime();
  int ret =  MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711,
      recvbuf, size, MPI_DOUBLE, dest, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  CHECKRET
  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


const Test tests[] = { 
{reduceTo0, "Reduce10K", 10*1024}, 
{reduceTo0, "Reduce1M", 1024*1024}, 
{reduceTo0, "Reduce100M", 100*1024*1024}, 
{reduceTo0, "Reduce1000M", 1000*1024*1024},
{allreduce, "Allreduce10K", 10*1024}, 
{allreduce, "Allreduce1M",  1024*1024}, 
{allreduce, "Allreduce100M", 100*1024*1024}, 
{allreduce, "Allreduce1000M", 1000*1024*1024},
{bcast, "Broadcast10K", 10*1024},
{bcast, "Broadcast1M",   1*1024*1024},
{bcast, "Broadcast100M", 100*1024*1024},
{bcast, "Broadcast1000M", 1000*1024*1024},

{barrier, "Barrier\t", 0},

{gather, "Gather10K", 10*1024},
{gather, "Gather1M",   1*1024*1024},
{gather, "Gather100M", 100*1024*1024},

{scatter, "Scatter10K", 10*1024},
{scatter, "Scatter1M",   1*1024*1024},
{scatter, "Scatter100M", 100*1024*1024},

{sendRecvRightNeighbour, "sendRecvRightNeighbour10K", 10*1024},
{sendRecvRightNeighbour, "sendRecvRightNeighbour1M", 1*1024*1024},
{sendRecvRightNeighbour, "sendRecvRightNeighbour100M", 100*1024*1024},
{sendRecvRightNeighbour, "sendRecvRightNeighbour1000M", 1000*1024*1024},

{sendRecvPaired, "sendRecvPaired10K", 10*1024},
{sendRecvPaired, "sendRecvPaired1M", 1*1024*1024},
{sendRecvPaired, "sendRecvPaired100M", 100*1024*1024},
{sendRecvPaired, "sendRecvPaired1000M", 1000*1024*1024},

};


const int  testCount = 4+4+4+1+3+3+4+4;

int main (argc, argv)
     int argc;
     char *argv[];
{
  MPI_Init (&argc, &argv);	/* starts MPI */
  MPI_Comm_rank (MPI_COMM_WORLD, &rank);
  MPI_Comm_size (MPI_COMM_WORLD, &nproc);
 
  
  double resultsTime[testCount][options.repeats];
  
  double t =  getTime();
  
  printf("Running testsuite of %d tests with %d process(es) \n", testCount, nproc);
  
  /* perform tests */
  for(int r = 0 ; r < options.repeats; r++){
    for(int t = 0; t < testCount; t++){
      MPI_Barrier(MPI_COMM_WORLD);
      resultsTime[t][r] = tests->func(tests->param1);   
    }
  }
  
  /* output results */
  printf("Total runtime: %fs\n", getTime() - t);
  printf("Testname\tMin\t\tMax\t\tAverage\n");
  for(int t = 0; t < testCount; t++){
    double sum = 0;
    double min = 10e300;
    double max = 0;
    for(int r = 0 ; r < options.repeats; r++){
      double cur = resultsTime[t][r];
      sum += cur;
      
      min = cur < min ? cur : min;
      max = cur > max ? cur : max;
    }
    
    printf("%s\t%10.9f\t%10.9f\t%10.9f\n", tests[t].name, min, max, sum / options.repeats);
  }
  
  MPI_Finalize();
  return 0;
}
