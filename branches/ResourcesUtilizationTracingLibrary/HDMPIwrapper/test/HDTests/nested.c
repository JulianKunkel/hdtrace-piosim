/**
 * \file nested.c
 *
 * This program can be used to test, if the logging of nested
 * function calls is handled correctly by the MPI wrapper.
 *
 * Please note, that this program can only be used, if the
 * wrapper has been compiled with the macro \a HDTRACE_INCLUDE_NESTED_TEST
 * defined.
 *
 * \author Paul Mueller <pmueller@ix.urz.uni-heidelberg.de>
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int rank;
int size;

int MPI_hdT_Test_nested(int, int);

int main (int argc, char** argv)
{

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	MPI_hdT_Test_nested(0, 6);

	MPI_Barrier(MPI_COMM_WORLD);

	MPI_hdT_Test_nested(0, 3);

	MPI_Finalize();

	return 0;
}
