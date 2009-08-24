/**
 * \file file.c
 * This program can be used to test the logging capabilities
 * of the MPI wrapper.
 * The programs creates, deletes and opens some files, for which
 * the logfile must show identical IDs for the same files and
 * different IDs for different files.
 *
 * The deletion of a file before opening it should be handled
 * correctly, even if the occurrence of such a sequence is rare.
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

char mpi_errstring[MPI_MAX_ERROR_STRING];
int resultlen;
int ret;
#define CHECK(f) ret = f; if(ret != MPI_SUCCESS) {MPI_Error_string(ret, mpi_errstring, &resultlen); printf("mpi error: %s\n", mpi_errstring); MPI_Abort(MPI_COMM_WORLD, ret);}

int MPI_hdT_Test_nested(int);

int main (int argc, char** argv)
{
	system("touch filetest_01.tmp");
	system("touch filetest_02.tmp");
	system("touch filetest_03.tmp");

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	MPI_Info info;
	CHECK(MPI_Info_create(&info));
	CHECK(MPI_File_delete("filetest_01.tmp", info));

	MPI_File fh;
	CHECK(MPI_File_open(MPI_COMM_WORLD, "filetest_02.tmp", MPI_MODE_WRONLY, info, &fh));

	MPI_Status status;
	char buf[] = "r";
	CHECK(MPI_File_write_at_all(fh, sizeof(char)*rank, buf, 1, MPI_CHAR, &status));
	CHECK(MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR));
	CHECK(MPI_File_write_at_all_end(fh, "r", &status));
	CHECK(MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR));
	CHECK(MPI_File_write_at_all_end(fh, "r", &status));
	CHECK(MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR));
	CHECK(MPI_File_write_at_all_end(fh, "r", &status));
	CHECK(MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR));
    CHECK(MPI_File_write_at_all_end(fh, "r", &status));


	CHECK(MPI_File_close(&fh));

	CHECK(MPI_File_open(MPI_COMM_WORLD, "filetest_03.tmp", MPI_MODE_RDONLY, info, &fh));
	CHECK(MPI_File_close(&fh));

	MPI_File fh2, fh3;
	CHECK(MPI_File_open(MPI_COMM_WORLD, "filetest_03.tmp", MPI_MODE_RDONLY, info, &fh2));
	CHECK(MPI_File_close(&fh2));

	CHECK(MPI_File_open(MPI_COMM_WORLD, "filetest_03.tmp", MPI_MODE_RDONLY, info, &fh3));
	CHECK(MPI_File_close(&fh3));

	CHECK(MPI_File_delete("filetest_02.tmp", info));
	CHECK(MPI_File_delete("filetest_03.tmp", info));


	CHECK(MPI_Barrier(MPI_COMM_WORLD));

	MPI_Finalize();

	return 0;
}
