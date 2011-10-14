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
	MPI_Barrier(MPI_COMM_WORLD);  \
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


void sendRecvRightNeighbour(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  const int dest = rank + 1 == nproc ? 0 : rank + 1 ;
  const int source = rank - 1 >= 0 ? rank - 1 : nproc -1;

  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711, recvbuf, size, MPI_DOUBLE, source, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    CHECKRET
    UPDATETIME
  }
  printTiming("sendRecvRightNeighbour", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void sendRecvPaired(int count, int multi, double * timeArray){
  if (nproc % 2 == 1)   return;

  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);  

  const int dest = rank % 2 == 0 ? rank + 1 : rank -1;
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
      int ret =  MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, dest, 4711,
      recvbuf, size, MPI_DOUBLE, dest, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    CHECKRET
    UPDATETIME
  }
  printTiming("sendRecvPaired", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void sendToRoot(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret;
    if(rank == 0){
	for(int p=1; p < nproc; p++)
		ret = MPI_Recv( sendbuf, size, MPI_DOUBLE, p, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }else{
	ret = MPI_Send( sendbuf, size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD);
    }
    CHECKRET
    UPDATETIME
  }
  printTiming("sendToRoot", curTimeIndex, multi, timeArray);

  free(sendbuf);
  free(recvbuf);
}

void sendRecvToRoot(int count, int multi, double * timeArray){
  double * sendbuf = mallocWithCheck(size*8);
  double * recvbuf = mallocWithCheck(size*8);
  
  STARTTIME
  for(int i= 0 ; i < count ; i++){
    int ret;
    if(rank == 0){
	for(int p=1; p < nproc; p++)
		ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, p, 4711, recvbuf, size, MPI_DOUBLE, p, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }else{
	ret = MPI_Sendrecv( sendbuf, size, MPI_DOUBLE, 0, 4711, recvbuf,  size, MPI_DOUBLE, 0, 4711, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }
    CHECKRET
    UPDATETIME
  }
  printTiming("sendRecvToRoot", curTimeIndex, multi, timeArray);

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
	  printf("%s measures the wall-clock timing with a barrier", argv[0]);
	  printf("# count: %d multi: %d size: %ld\n", count, multi, size);
  }

  if (nproc == 1) return;

  sendRecvRightNeighbour(count, multi, timeArray);
  sendRecvPaired(count, multi, timeArray);
  sendToRoot(count, multi, timeArray);
  sendRecvToRoot(count, multi, timeArray);


  MPI_Finalize();
  return 0;
}
