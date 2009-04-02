

int MPI_hdT_Test_nested(int rec, int max);

static int PMPI_hdT_Test_nested(int rec, int max)
{
	if(rec < max)
	{
		MPI_hdT_Test_nested(rec + 1, max);
		MPI_hdT_Test_nested(rec + 1, max);
	}

	return 0;
}

