#include <stdio.h>
#include <mpi.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>


int main (int argc, char ** argv)
{
  int rank, size;

  MPI_Init (&argc, &argv);	/* starts MPI */
  MPI_Comm_rank (MPI_COMM_WORLD, &rank);	/* get current process id */
  MPI_Comm_size (MPI_COMM_WORLD, &size);	/* get number of processes */

  char buff[102400];
  int f = open("test.txt", O_RDWR | O_CREAT, S_IRWXU);
  write(f, buff, 102400);
  close(f);
  
  MPI_Finalize();
  return 0;
}

