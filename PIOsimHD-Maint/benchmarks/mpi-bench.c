/*
 Author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>


typedef struct{
  int repeats;
} Options;

static Options options = {3};

static int rank, nproc;

/* returns the test time */
typedef double (*testFunc)(long param1, double * totalTime);


typedef struct{
   const testFunc func;
   char const * name;
   long param1;
}Test;

static Test * currentTest = NULL;

#define getTime MPI_Wtime

#define CHECKRET if(ret != 0) printf("Error in function %s in line %d\n", __FUNCTION__, __LINE__);

#define STARTTIME   MPI_Barrier(MPI_COMM_WORLD);  double t = getTime(); 
#define GETTIME   double e = getTime() - t;  MPI_Barrier(MPI_COMM_WORLD); *totalTime = getTime() -t;

void * mallocWithCheck(long size){
  void * ret = malloc(size);
  if (ret == NULL){
    printf("Error could not malloc %lld of bytes\n", (long long int) size);
    printf("Current test %s with param %ld\n", currentTest->name, currentTest->param1);
    MPI_Abort(1, MPI_COMM_WORLD);
  }
  return ret;
}

double reduceTo0(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  memset(sendbuf, 0, size*8);
  
  STARTTIME
  int ret = MPI_Reduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  

  free(sendbuf);
  free(recvbuf);
  
  return e;
}

double allreduce(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  memset(sendbuf, 0, size*8);
  
  STARTTIME
  int ret = MPI_Allreduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
  CHECKRET
  GETTIME

  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double bcast(long size, double * totalTime){
  double * buffer = mallocWithCheck(size*8);
  
  memset(buffer, 0, size*8);
  
  STARTTIME  
  int ret = MPI_Bcast(buffer, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  free(buffer);
  
  return e;
}


double barrier(long unused, double * totalTime){
 
  STARTTIME
  int ret = MPI_Barrier(MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  return e;
}


double gather(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8 * nproc);
  
  memset(sendbuf, 0, size*8);
  
  STARTTIME
  int ret =     MPI_Gather(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  free(sendbuf);
  free(recvbuf);
  
  return e;
}

double allgather(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8 * nproc);
  
  memset(sendbuf, 0, size*8);
  
  STARTTIME
  int ret =     MPI_Allgather(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  free(sendbuf);
  free(recvbuf);
  
  return e;
}



double scatter(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8 * nproc);
  double * recvbuf = mallocWithCheck(size*8);
  
  memset(sendbuf, 0, size*8);
  
  STARTTIME
  int ret =     MPI_Scatter(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  CHECKRET
  GETTIME
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double sendRecvRightNeighbour(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  memset(sendbuf, 0, size*8);
  
  const int dest = rank + 1 == nproc ? 0 : rank + 1 ;
  const int source = rank - 1 >= 0 ? rank - 1 : nproc -1;
  
  STARTTIME
  int ret =       MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711,
      recvbuf, size, MPI_DOUBLE, source, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  CHECKRET
  GETTIME
  free(sendbuf);
  free(recvbuf);
  
  return e;
}


double sendRecvPaired(long size, double * totalTime){
  
  if(nproc % 2 == 1){
    /* test is run only with equal number of processes */
    return 0;
  }
  
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  memset(sendbuf, 0, size*8);
  
  
  const int dest = rank % 2 == 0 ? rank + 1 : rank -1;
  
  STARTTIME
  int ret =  MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711,
      recvbuf, size, MPI_DOUBLE, dest, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  CHECKRET
  GETTIME
  free(sendbuf);
  free(recvbuf);
  
  return e;
}



double sendToRoot(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  memset(sendbuf, 0, size*8);

  STARTTIME
  if(rank == 0){
    for(int i=1; i < nproc; i++){
      int ret = MPI_Recv( sendbuf, size, MPI_DOUBLE, i, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      CHECKRET
    }
  }else{
    int ret = MPI_Send( sendbuf, size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD);
    CHECKRET
  }

  GETTIME
  free(sendbuf);
  free(recvbuf);

  return e;
}

double sendRecvToRoot(long size, double * totalTime){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  memset(sendbuf, 0, size*8);

  STARTTIME
  if(rank == 0){
    for(int i=1; i < nproc; i++){
      int ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, i, 4711, recvbuf, size, MPI_DOUBLE, i, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      CHECKRET
    }
  }else{
    int ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, 0, 4711, recvbuf,  size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    CHECKRET
  }

  GETTIME
  free(sendbuf);
  free(recvbuf);

  return e;
}



const Test tests[] = { 
{reduceTo0, "Reduce10K", 10*1024 / 8}, 
{reduceTo0, "Reduce1M", 1024*1024 / 8}, 
{reduceTo0, "Reduce10M", 10*1024*1024 / 8}, 
{reduceTo0, "Reduce100M", 100*1024*1024 / 8}, 
#ifdef GByte
{reduceTo0, "Reduce1000M", 1000*1024*1024 / 8},
#endif
{allreduce, "Allreduce10K", 10*1024 / 8}, 
{allreduce, "Allreduce1M",  1024*1024 / 8}, 
{allreduce, "Allreduce10M",  10*1024*1024 / 8}, 
{allreduce, "Allreduce100M", 100*1024*1024 / 8}, 
#ifdef GByte
{allreduce, "Allreduce1000M", 1000*1024*1024 / 8},
#endif
{bcast, "Broadcast10K", 10*1024 / 8},
{bcast, "Broadcast1M",   1*1024*1024 / 8},
{bcast, "Broadcast10M",   10*1024*1024 / 8},
{bcast, "Broadcast100M", 100*1024*1024 / 8},
#ifdef GByte
{bcast, "Broadcast1000M", 1000*1024*1024 / 8},
#endif

{barrier, "Barrier\t", 0},

{allgather, "Allgather10K", 10*1024 / 8},
{allgather, "Allgather1M",   1*1024*1024 / 8},
{allgather, "Allgather10M",   10*1024*1024 / 8},
#ifdef GByte
{allgather, "Allgather100M", 100*1024*1024 / 8},
#endif

{gather, "Gather10K", 10*1024 / 8},
{gather, "Gather1M",   1*1024*1024 / 8},
{gather, "Gather10M",   10*1024*1024 / 8},
{gather, "Gather100M", 100*1024*1024 / 8},

{scatter, "Scatter10K", 10*1024 / 8},
{scatter, "Scatter1M",   1*1024*1024 / 8},
{scatter, "Scatter10M",   10*1024*1024 / 8},
{scatter, "Scatter100M", 100*1024*1024 / 8},

{sendRecvRightNeighbour, "sendRecvRightNeighbour10K", 10*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour1M", 1*1024*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour10M", 10*1024*1024 / 8},
{sendRecvRightNeighbour, "sendRecvRightNeighbour100M", 100*1024*1024 / 8},
#ifdef GByte
{sendRecvRightNeighbour, "sendRecvRightNeighbour1000M", 1000*1024*1024 / 8},
#endif
{sendRecvPaired, "sendRecvPaired10K", 10*1024 / 8},
{sendRecvPaired, "sendRecvPaired1M", 1*1024*1024 / 8},
{sendRecvPaired, "sendRecvPaired10M", 10*1024*1024 / 8},
{sendRecvPaired, "sendRecvPaired100M", 100*1024*1024 / 8},
#ifdef GByte
{sendRecvPaired, "sendRecvPaired1000M", 1000*1024*1024 / 8},
#endif
{sendToRoot, "sendToRoot10K", 10*1024 / 8},
{sendToRoot, "sendToRoot100K", 100*1024 / 8},
{sendToRoot, "sendToRoot1M", 1*1024*1024 / 8},
{sendToRoot, "sendToRoot10M", 10*1024*1024 / 8},
{sendToRoot, "sendToRoot100M", 100*1024*1024 / 8},
#ifdef GByte
{sendToRoot, "sendToRoot1000M", 1000*1024*1024 / 8},
#endif
{sendRecvToRoot, "sendRecvToRoot10K", 10*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot100K", 100*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot1M", 1*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot10M", 10*1024*1024 / 8},
{sendRecvToRoot, "sendRecvToRoot100M", 100*1024*1024 / 8},
#ifdef GByte
{sendRecvToRoot, "sendRecvToRoot1000M", 1000*1024*1024 / 8},
#endif
{NULL, NULL, 0}
};

int getTestcount(){
  int i=0; 
  while(1){
    if(tests[i].name == NULL){
      return i;
    }
    i++;
  }
}

int main (int argc, char *argv[])
{
  MPI_Init (&argc, &argv);	/* starts MPI */
  MPI_Comm_rank (MPI_COMM_WORLD, &rank);
  MPI_Comm_size (MPI_COMM_WORLD, &nproc);
  
  int testCount = getTestcount();
  
  double resultsTime[testCount][options.repeats];
  double totalTime[testCount][options.repeats];
  
  double t =  getTime();
  
  if(rank == 0){
    printf("Running testsuite of %d tests with %d process(es) \n", testCount, nproc);
  }
  char hostname[256];
  hostname[255] = '\0';    
  gethostname(hostname, 255);
  printf("%d on host %s CPU-ID %d\n", rank, hostname, sched_getcpu());

  
  /* perform tests */
  for(int r = 0 ; r < options.repeats; r++){
    for(int t = 0; t < testCount; t++){
      currentTest = & tests[t];
      
      if(rank == 0){
	printf("Running %s\n", currentTest->name);
      }
      MPI_Barrier(MPI_COMM_WORLD);
      
      resultsTime[t][r] = tests[t].func(tests[t].param1, & totalTime[t][r]);   
    }
  }
  
  /* output results */
  printf("Total runtime: %fs\n", getTime() - t);
  if(rank == 0 ){
	printf("Values are provided for P0 and aggregated for all other processes\n");
	printf("Testname\tMinBarrier\tMaxBarrier\tAverageBarrier\tMinP0\t\tMaxP0\t\tAverage\t\tMinAll\t\tMaxAll\t\tAverageAll\tMinAll/AvgAll\tAvgAll/MaxAll\n");
  }

  MPI_Barrier(MPI_COMM_WORLD);
  sleep(5);
  MPI_Barrier(MPI_COMM_WORLD);

  for(int t = 0; t < testCount; t++){
    double sum = 0;
    double min = 10e300;
    double max = 0;

    double sumT = 0;
    double minT = 10e300;
    double maxT = 0;
    for(int r = 0 ; r < options.repeats; r++){
      double cur = resultsTime[t][r];
      sum += cur;
      
      min = cur < min ? cur : min;
      max = cur > max ? cur : max;

      cur = totalTime[t][r];
      minT = cur < minT ? cur : minT;
      maxT = cur > maxT ? cur : maxT;      
      sumT += cur;
    }
    
    /* Now gather results */
    double minTotal, maxTotal, sumTotal;
    MPI_Reduce(&min, & minTotal, 1, MPI_DOUBLE, MPI_MIN, 0, MPI_COMM_WORLD);
    MPI_Reduce(&max, & maxTotal, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
    MPI_Reduce(&sum, & sumTotal, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
    
    if(rank == 0){
      const double avgTotal = sumTotal / options.repeats / nproc;
      printf("%s\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%10.7f\t%2.2f%%\t%2.2f%%\n", tests[t].name, minT, maxT, sumT / options.repeats, min, max, sum / options.repeats, minTotal, maxTotal, avgTotal , minTotal/avgTotal*100, avgTotal / maxTotal * 100);
    }
  }
  
  MPI_Finalize();
  return 0;
}
