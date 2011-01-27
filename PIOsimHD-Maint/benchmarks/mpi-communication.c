/*
 Author Julian M. Kunkel
 2010/2011
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
    MPI_Abort(MPI_COMM_WORLD, 1);
  }
  return ret;
}

double sendToRoot(long size){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  memset(sendbuf, 0, size*8);

  double t = getTime();
  if(rank == 0){
    for(int i=1; i < nproc; i++){
      int ret = MPI_Recv( sendbuf, size, MPI_DOUBLE, i, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      CHECKRET
    }
  }else{
    int ret = MPI_Send( sendbuf, size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD);
    CHECKRET
  }

  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);

  return e;
}

double sendRecvToRoot(long size){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  memset(sendbuf, 0, size*8);

  double t = getTime();
  if(rank == 0){
    for(int i=1; i < nproc; i++){
      int ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, i, 4711, recvbuf, size, MPI_DOUBLE, i, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      CHECKRET
    }
  }else{
    int ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, 0, 4711, recvbuf,  size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    CHECKRET
  }

  double e = getTime() - t;
  free(sendbuf);
  free(recvbuf);

  return e;
}


const Test tests[] = {
{sendToRoot, "sendToRoot10K", 10*1024 / 8},
{sendToRoot, "sendToRoot100K", 100*1024 / 8},
{sendToRoot, "sendToRoot1M", 1*1024*1024 / 8},
{sendToRoot, "sendToRoot10M", 10*1024*1024 / 8},
{sendToRoot, "sendToRoot100M", 100*1024*1024 / 8},
{sendToRoot, "sendToRoot1000M", 1000*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot10K", 10*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot100K", 100*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot1M", 1*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot10M", 10*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot100M", 100*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot1000M", 1000*1024*1024 / 8},

};


const int  testCount = 6+6;

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
