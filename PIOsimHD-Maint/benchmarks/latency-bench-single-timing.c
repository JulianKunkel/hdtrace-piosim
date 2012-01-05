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

#ifndef MULTI
#define MULTI 10
#endif

double sendRecvPaired(long count){
  int sendbuf, recvbuf;

  if(nproc % 2 == 1){
    /* test is run only with equal number of processes */
    return 0;
  }
  
  const int dest = rank % 2 == 0 ? rank + 1 : rank -1;
  
  double * curTime = malloc(sizeof(double) * count);

  double t = getTime();
  for(int i= 0 ; i < count ; i++){
    curTime[i] = 0;
    int ret =       MPI_Sendrecv( & sendbuf, 0, MPI_INT, dest, 4711,
	& recvbuf, 0, MPI_INT, dest, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);      
     if(i % MULTI == (MULTI - 1) ) {
        double now = getTime();
 	curTime[i] = now - t;
        t = now;
     }
  }

  for(int i= 0 ; i < count ; i++){
    if(curTime[i] != 0)
	printf("%.10f,", curTime[i] / MULTI);
  }
  printf("\n");
  printf("MULTIPLIER %d\n", MULTI);
  
  return 0;
}


const Test tests[] = { 
{sendRecvPaired, "sendRecvPaired10Million", 10000000},
};


const int  testCount = 1 ;

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
