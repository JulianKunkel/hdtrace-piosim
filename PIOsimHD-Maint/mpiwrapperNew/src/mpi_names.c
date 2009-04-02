

static const char * getCombinerName(int combiner)
{
	if(combiner == MPI_COMBINER_NAMED)
		return "MPI_COMBINER_NAMED";
	else if(combiner == MPI_COMBINER_DUP)
		return "MPI_COMBINER_DUP";
	else if(combiner == MPI_COMBINER_CONTIGUOUS)
		return "MPI_COMBINER_CONTIGUOUS";
	else if(combiner == MPI_COMBINER_VECTOR)
		return "MPI_COMBINER_VECTOR";
	else if(combiner == MPI_COMBINER_HVECTOR_INTEGER)
		return "MPI_COMBINER_HVECTOR_INTEGER";
	else if(combiner == MPI_COMBINER_HVECTOR)
		return "MPI_COMBINER_HVECTOR";
	else if(combiner == MPI_COMBINER_INDEXED)
		return "MPI_COMBINER_INDEXED";
	else if(combiner == MPI_COMBINER_HINDEXED_INTEGER)
		return "MPI_COMBINER_HINDEXED_INTEGER";
	else if(combiner == MPI_COMBINER_HINDEXED)
		return "MPI_COMBINER_HINDEXED";
	else if(combiner == MPI_COMBINER_INDEXED_BLOCK)
		return "MPI_COMBINER_INDEXED_BLOCK";
	else if(combiner == MPI_COMBINER_STRUCT_INTEGER)
		return "MPI_COMBINER_STRUCT_INTEGER";
	else if(combiner == MPI_COMBINER_STRUCT)
		return "MPI_COMBINER_STRUCT";
	else if(combiner == MPI_COMBINER_SUBARRAY)
		return "MPI_COMBINER_SUBARRAY";
	else if(combiner == MPI_COMBINER_DARRAY)
		return "MPI_COMBINER_DARRAY";
	else if(combiner == MPI_COMBINER_F90_REAL)
		return "MPI_COMBINER_F90_REAL";
	else if(combiner == MPI_COMBINER_F90_COMPLEX)
		return "MPI_COMBINER_F90_COMPLEX";
	else if(combiner == MPI_COMBINER_F90_INTEGER)
		return "MPI_COMBINER_F90_INTEGER";
	else if(combiner == MPI_COMBINER_RESIZED)
		return "MPI_COMBINER_RESIZED";
	else
		return "UNKNOWN";
}

static const char * getDistributeConstantName(int constant)
{
	if(constant == MPI_DISTRIBUTE_BLOCK)
		return "MPI_DISTRIBUTE_BLOCK";
	else if(constant == MPI_DISTRIBUTE_CYCLIC)
		return "MPI_DISTRIBUTE_CYCLIC";
	else if(constant == MPI_DISTRIBUTE_NONE)
		return "MPI_DISTRIBUTE_NONE";
	else if(constant == MPI_DISTRIBUTE_DFLT_DARG)
		return "MPI_DISTRIBUTE_DFLT_DARG";
	else
	{
		// TODO: this is an error
		return "???";
	}
}

static char * getCommName(MPI_Comm comm)
{
	// NOTE: the result becomes invalid after a consecutive
    // call to getCommName(...)

  int len = TMP_BUF_LEN;
  int cmp = 0;
  MPI_Comm_compare(comm, MPI_COMM_WORLD, & cmp);
  if(cmp == MPI_IDENT)
  {
	  return "WORLD";
  }
  MPI_Comm_compare(comm, MPI_COMM_SELF, & cmp);
  if(cmp == MPI_IDENT)
  {
	  return "SELF";
  }
  MPI_Comm_get_name(comm, cnbuff, & len);
  return cnbuff;
}


static const char * getWhenceString(int whence)
{
	switch(whence)
	{
	case MPI_SEEK_SET:
		return "Set";
	case MPI_SEEK_CUR:
		return "Cur";
	case MPI_SEEK_END:
		return "End";
	default:
		return "Invalid";
	}
}
