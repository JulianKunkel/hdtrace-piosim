#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int rank;
int size;

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
	MPI_Info_create(&info);
	MPI_File_delete("filetest_01.tmp", info);
	
	MPI_File fh;
	MPI_File_open(MPI_COMM_WORLD, "filetest_02.tmp", MPI_MODE_RDONLY, info, &fh);
	

	MPI_Status status;
	char buf[] = "r";
	MPI_File_write_at_all(fh, sizeof(char)*rank, buf, 1, MPI_CHAR);
	MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR);
	MPI_File_write_at_all_end(fh, "r", &status);
	MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR);
	MPI_File_write_at_all_end(fh, "r", &status);
	MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR);
	MPI_File_write_at_all_end(fh, "r", &status);
	MPI_File_write_at_all_begin(fh, sizeof(char)*rank, buf, 1, MPI_CHAR);
	MPI_File_write_at_all_end(fh, "r", &status);


	MPI_File_close(&fh);

	MPI_File_open(MPI_COMM_WORLD, "filetest_02.tmp", MPI_MODE_RDONLY, info, &fh);
	MPI_File_close(&fh);
	
	MPI_File_delete("filetest_02.tmp", info);
	MPI_File_delete("filetest_03.tmp", info);
	
	
	MPI_Barrier(MPI_COMM_WORLD);
	
	MPI_Finalize();

	return 0;
}
