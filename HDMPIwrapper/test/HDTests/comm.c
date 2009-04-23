/**
 * @file comm.c
 * This program can be used to perform a test of the logging capabilities
 * of the MPI wrapper. \a MPI_Sendrecv(...) is attempted with
 * MPI_COMM_WORLD and a manually created MPI communicator.
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define DATATYPE MPI_BYTE

#define MPI_Sleep(c, n) MPI_Barrier(c); sleep(n); MPI_Barrier(c);

#define DEFAULT_ELEMENTS (10 * 1024 * 1024)
#define DEFAULT_FILENAME "foobar"
#define DEFAULT_ITERATIONS 10
#define DEFAULT_OUTER_ITERATIONS 1

#define BOOL(n) (((n) == 0) ? "false" : "true")

int rank;
int size;

/*
 * create and use a comm with the members p1 and p2 relative to 
 * world rank.
 */ 
void test_comm(int p1, int p2)
{
	int i;
	int ranksInOUT[size];
	int ranksOut[size];
	int ret;

	MPI_Group  group;
	MPI_Group  worldGroup;

	for(i=0; i < size; i++){
		ranksInOUT[i] = i;
	}

	MPI_Comm_group(MPI_COMM_WORLD, & worldGroup);

	int testRanks [] = {p1, p2};
	MPI_Group_incl( worldGroup, 2, testRanks, & group ) ;

	ret = MPI_Group_translate_ranks( worldGroup, size, ranksInOUT, group, ranksOut ) ;

	printf("ret: %d\n", ret);

	for(i=0; i < size; i++){
		printf(" %d %d \n", i, ranksOut[i]);
	}

	MPI_Comm newcomm;
	MPI_Comm_create(MPI_COMM_WORLD, group, &newcomm);

	if( rank == p1 || rank == p2)
	{
		int sendbuf, recvbuf;
		MPI_Status status;

		MPI_Sendrecv(&sendbuf, 1, MPI_INT, rank==p2 ? p1 : p2, 0, 
					 &recvbuf, 1, MPI_INT, rank==p2 ? p1 : p2, 0, 
					 MPI_COMM_WORLD, &status);

		MPI_Sendrecv(&sendbuf, 1, MPI_INT, rank==p2 ? 1 : 0, 0,
					 &recvbuf, 1, MPI_INT, rank==p2 ? 1 : 0, 0,
					 newcomm, &status);

		MPI_Barrier(newcomm);
	}

}

int main (int argc, char** argv)
{
	int i, j, ret;

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	printf("%3d/%3d: Hello world!\n", rank + 1, size);

	if(size < 5)
	{
		MPI_Barrier(MPI_COMM_WORLD); // ensure that all ranks are started, so each produces a log 
		perror("This test requires a group size of at least 5\n");
		MPI_Abort(MPI_COMM_WORLD, -1);
	}


	test_comm(2, 4);
	test_comm(2, 1);
	test_comm(3, 1);


	MPI_Barrier(MPI_COMM_WORLD);

	MPI_Finalize();

	return 0;
}
