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

int main (int argc, char** argv)
{
	int i, j, ret;

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	printf("%3d/%3d: Hello world!\n", rank + 1, size);
	
	if(size < 5)
	{
		perror("This test requires a group size of at least 5\n");
		MPI_Abort(MPI_COMM_WORLD, -1);
	}


	MPI_Group  group;
	MPI_Group  worldGroup;

	int ranksInOUT[20];
	int ranksOut[20];
	for(i=0; i < size; i++){
		ranksInOUT[i] = i;
	}

	MPI_Comm_group(MPI_COMM_WORLD, & worldGroup);

	/* for testing */
	int testRanks [] = {2, 4};
	MPI_Group_incl( worldGroup, 2, testRanks, & group ) ;

	ret = MPI_Group_translate_ranks( worldGroup, size, ranksInOUT, group, ranksOut ) ;

	printf("ret: %d\n", ret);

	for(i=0; i < size; i++){
		printf(" %d %d \n", i, ranksOut[i]);
	}
		
	MPI_Comm newcomm;
	MPI_Comm_create(MPI_COMM_WORLD, group, &newcomm);

	if( rank == 2 || rank == 4)
		MPI_Barrier(newcomm);


	MPI_Barrier(MPI_COMM_WORLD);
	
	MPI_Finalize();

	return 0;
}
