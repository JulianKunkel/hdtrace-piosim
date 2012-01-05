/*
 Author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <string.h>

#define getTime MPI_Wtime

#define SIZE (1024*10/8)

#define CHECKRET if(ret != 0) printf("Error in function %s in line %d\n", __FUNCTION__, __LINE__);
#define STARTTIME   MPI_Barrier(MPI_COMM_WORLD); int curTimeIndex = 0; double t = getTime(); 
#define UPDATETIME       if(i % multi == (multi - 1) ) { \
        double now = getTime(); \
  	timeArray[curTimeIndex] = now - t; \
	curTimeIndex++; \
        t = now; \
     }

int rank;
int nproc;
long size = SIZE;

void * mallocWithCheck(long size){
  void * ret = malloc(size);
  if (ret == NULL){
    printf("Error could not malloc %lld of bytes\n", (long long int) size);
    MPI_Abort(1, MPI_COMM_WORLD);
  }
  memset(ret, 0, size);
  return ret;
}

void printTiming(char * testname, int count, int multi, double * timeArray){
  MPI_Barrier(MPI_COMM_WORLD);
  if(rank == 0){	  
	  printf("%s: %.10f", testname, timeArray[0] / multi);
	  for(int i= 1 ; i < count ; i++){
		printf(",%.10f", timeArray[i] / multi);
	  }
	  printf("\n");
  }
  MPI_Barrier(MPI_COMM_WORLD);
}

void barrier(int count, int multi, double * timeArray){
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Barrier(MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("barrier", curTimeIndex, multi, timeArray);
}

void reduce(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Reduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("reduce", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void allreduce(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);

  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Allreduce(sendbuf, recvbuf, size, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("allreduce", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void bcast(int count, int multi, double * timeArray){
  double * buffer = mallocWithCheck(size*8);

  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Bcast(buffer, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("bcast", curTimeIndex, multi, timeArray);

  free(buffer);
}

void gather(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8 * nproc);
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Gather(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("gather", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void allgather(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8 * nproc);
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Allgather(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("allgather", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void scatter(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8 * nproc);
  double * recvbuf = mallocWithCheck(size*8);
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Scatter(sendbuf, size, MPI_DOUBLE, recvbuf, size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    CHECKRET
    UPDATETIME
  }
  printTiming("scatter", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

  

int main (int argc, char *argv[])
{
  MPI_Init (&argc, &argv);	/* starts MPI */
  MPI_Comm_rank (MPI_COMM_WORLD, &rank);
  MPI_Comm_size (MPI_COMM_WORLD, &nproc);

  if(argc < 3 ){
	printf("Syntax: %s <count> <multi>\n", argv[0]);
	exit(1);
  }
  int count = atoi(argv[1]);
  int multi = atoi(argv[2]);

  double * timeArray = malloc(sizeof(double) * count);

  if(rank == 0){	  
	  printf("%s measures the timing from process 0", argv[0]);
	  printf("# count: %d multi: %d size: %ld\n", count, multi, size);
  }

  barrier(count, multi, timeArray);
  reduce(count, multi, timeArray);
  allreduce(count, multi, timeArray);
  bcast(count, multi, timeArray);
  gather(count, multi, timeArray);
  scatter(count, multi, timeArray);
  allgather(count, multi, timeArray);

  MPI_Finalize();
  return 0;
}
