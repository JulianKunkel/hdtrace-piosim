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

char mpi_errstring[MPI_MAX_ERROR_STRING];
int ret;
int resultlen;
#define CHECK(f) ret = f; if(ret != MPI_SUCCESS) {MPI_Error_string(ret, mpi_errstring, &resultlen); printf("mpi error: %s\n", mpi_errstring); MPI_Abort(MPI_COMM_WORLD, ret);}

int main (int argc, char** argv)
{

	system("touch testfile_05.tmp");

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	if ( size < 3 )
	{
		printf("This test needs at least 3 processes\n");
		MPI_Abort(MPI_COMM_WORLD, 0);
	}

	MPI_Info info;
	MPI_File fh;
	CHECK(MPI_Info_create(&info));
	CHECK(MPI_File_open(MPI_COMM_WORLD, "testfile_05.tmp", MPI_MODE_WRONLY, info, &fh));

	char data = 'A' + rank;
	CHECK(MPI_File_write_at_all_begin(fh, rank, &data, 1, MPI_CHAR));
	MPI_Status status;
	CHECK(MPI_File_write_at_all_end(fh, &data, &status));


	CHECK(MPI_File_write_at_all_begin(fh, rank, &data, 1, MPI_CHAR));
	CHECK(MPI_File_write_at_all_end(fh, &data, &status));

	CHECK(MPI_File_write_at_all_begin(fh, rank, &data, 1, MPI_CHAR));
	CHECK(MPI_File_write_at_all_end(fh, &data, &status));

	CHECK(MPI_File_write_at_all_begin(fh, rank, &data, 1, MPI_CHAR));


	MPI_Request r1, r2;

	if(rank == 0)
	{
		CHECK(MPI_Isend(&data, 1, MPI_CHAR, 1, 0, MPI_COMM_WORLD, &r1));
		CHECK(MPI_Isend(&data, 1, MPI_CHAR, 2, 0, MPI_COMM_WORLD, &r2));

		CHECK(MPI_Wait(&r1, &status));
		CHECK(MPI_Wait(&r2, &status));
	}
	if(rank == 1)
	{
		CHECK(MPI_Irecv(&data, 1, MPI_CHAR, 0, 0, MPI_COMM_WORLD, &r1));
		CHECK(MPI_Wait(&r1, &status));
	}
	if(rank == 2)
	{
		CHECK(MPI_Irecv(&data, 1, MPI_CHAR, 0, 0, MPI_COMM_WORLD, &r2));
		CHECK(MPI_Wait(&r2, &status));
	}
	
	CHECK(MPI_File_write_at_all_end(fh, &data, &status));

	MPI_Barrier(MPI_COMM_WORLD);

	MPI_Finalize();

	return 0;
}
