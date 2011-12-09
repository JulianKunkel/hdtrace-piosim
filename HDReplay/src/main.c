#include "main.h"

int main(int argc, char** argv)
{

  int err;

  int rank;
  int size;

	/*
	 *a list containing the single instructions read from a trace file
	 */
  GSList *traceFile;
  GSList *comms;
  GSList *fileList;
	GSList *dataTypes;

	/*
   *setup MPI
	 */
  MPI_Init(&argc,&argv);

	/*
   *set the MPI errorhandler
	 */
  MPI_Errhandler_set(MPI_COMM_WORLD, MPI_ERRORS_RETURN);

	/*
   *get the MPI rank
	 */
  if((err = MPI_Comm_rank(MPI_COMM_WORLD, &rank)) != MPI_SUCCESS)
  {
    error(MPI_ERR, err, "",NULL);
  }

	/*
   *get the count of MPI processes
	 */
  if((err = MPI_Comm_size(MPI_COMM_WORLD, &size)) != MPI_SUCCESS)
  {
    error(MPI_ERR, err, "", NULL);
  }

	/*
   *call the init function to
	 */
  init(&traceFile, &comms, &fileList, &dataTypes,rank, size, argc, argv);

	printf("trace flie length: %i\n",g_slist_length(traceFile));
	printf("communicator length: %i\n", g_slist_length(comms));
	printf("file list length: %i\n",g_slist_length(fileList));
	printf("data types length: %i\n",g_slist_length(dataTypes));

  MPI_Finalize();

  return 0;
}

