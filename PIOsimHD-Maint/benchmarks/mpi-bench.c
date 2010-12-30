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

static Test * currentTest = NULL;

#define getTime MPI_Wtime

#define CHECKRET if(ret != 0) printf("Error in function %s in line %d\n", __FUNCTION__, __LINE__);

void * mallocWithCheck(long size){
  void * ret = malloc(size);
  if (ret == NULL){
    printf("Error could not malloc %lld of bytes\n", (long long int) size);
    printf("Current test %s with param %ld\n", currentTest->name, currentTest->param1);
    MPI_Abort(1, MPI_COMM_WORLD);
  }
  return ret;
}

double reduceTo0(long size){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
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
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
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
  double * buffer = mallocWithCheck(size*8);
  
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
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8 * nproc);
  
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
  double * sendbuf = mallocWithCheck(size*8 * nproc);
  double * recvbuf = mallocWithCheck(size*8);
  
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
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
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
  
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
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
{reduceTo0, "Reduce10K", 10*1024 / 8}, 
{reduceTo0, "Reduce1M", 1024*1024 / 8}, 
{reduceTo0, "Reduce100M", 100*1024*1024 / 8}, 
{reduceTo0, "Reduce1000M", 1000*1024*1024 / 8},
{allreduce, "Allreduce10K", 10*1024 / 8}, 
{allreduce, "Allreduce1M",  1024*1024 / 8}, 
{allreduce, "Allreduce100M", 100*1024*1024 / 8}, 
{allreduce, "Allreduce1000M", 1000*1024*1024 / 8},
{bcast, "Broadcast10K", 10*1024 / 8},
{bcast, "Broadcast1M",   1*1024*1024 / 8},
{bcast, "Broadcast100M", 100*1024*1024 / 8},
{bcast, "Broadcast1000M", 1000*1024*1024 / 8},

{barrier, "Barrier\t", 0},

{gather, "Gather10K", 10*1024 / 8},
{gather, "Gather1M",   1*1024*1024 / 8},
{gather, "Gather100M", 100*1024*1024 / 8},

{scatter, "Scatter10K", 10*1024 / 8},
{scatter, "Scatter1M",   1*1024*1024 / 8},
{scatter, "Scatter100M", 100*1024*1024 / 8},

{sendRecvRightNeighbour, "sendRecvRightNeighbour10K", 10*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour1M", 1*1024*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour100M", 100*1024*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour1000M", 1000*1024*1024 / 8},

{sendRecvPaired, "sendRecvPaired10K", 10*1024 / 8},
{sendRecvPaired, "sendRecvPaired1M", 1*1024*1024 / 8},
{sendRecvPaired, "sendRecvPaired100M", 100*1024*1024 / 8},
{sendRecvPaired, "sendRecvPaired1000M", 1000*1024*1024 / 8},

};


const int  testCount = 4+4+4+1+3+3+4+4;

int main (int argc, char *argv[])
{
  MPI_Init (&argc, &argv);	/* starts MPI */
  MPI_Comm_rank (MPI_COMM_WORLD, &rank);
  MPI_Comm_size (MPI_COMM_WORLD, &nproc);
 
  
  double resultsTime[testCount][options.repeats];
  
  double t =  getTime();
  
  if(rank == 0){
    printf("Running testsuite of %d tests with %d process(es) \n", testCount, nproc);
  }
  
  /* perform tests */
  for(int r = 0 ; r < options.repeats; r++){
    for(int t = 0; t < testCount; t++){
      currentTest = & tests[t];
      
      if(rank == 0){
	printf("Running %s\n", currentTest->name);
      }
      MPI_Barrier(MPI_COMM_WORLD);
      
      resultsTime[t][r] = tests[t].func(tests[t].param1);   
    }
  }
  
  /* output results */
  printf("Total runtime: %fs\n", getTime() - t);
  printf("Values are provided for P0 and aggregated for all other processes\n");
  printf("Testname\tMin\t\tMax\t\tAverage\t\tMinAll\t\tMaxAll\t\tAverageAll\tMinAll/AvgAll\tAvgAll/MaxAll\n");
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
    
    /* Now gather results */
    double minTotal, maxTotal, sumTotal;
    MPI_Reduce(&min, & minTotal, 1, MPI_DOUBLE, MPI_MIN, 0, MPI_COMM_WORLD);
    MPI_Reduce(&max, & maxTotal, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
    MPI_Reduce(&sum, & sumTotal, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
    
    if(rank == 0){
      const double avgTotal = sumTotal / options.repeats / nproc;
      printf("%s\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%2.2f%%\t%2.2f%%\n", tests[t].name, min, max, sum / options.repeats, minTotal, maxTotal, avgTotal , minTotal/avgTotal*100, avgTotal / maxTotal * 100);
    }
  }
  
  MPI_Finalize();
  return 0;
}
