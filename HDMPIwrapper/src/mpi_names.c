/**
 * \file mpi_names.c
 *
 * This file contains functions, that resolve MPI constants
 * to constant strings.
 *
 * \author Paul Mueller <pmueller@ix.urz.uni-heidelberg.de>
 */

#include "common.h"

/**
 * This function returns a string corresponding to a combiner constant.
 *
 * \param combiner Combiner constant (as returned by \a MPI_Type_get_envelope)
 *
 * \return a constant string identifying the combiner constant, or the string "UNKNOWN"
 * if the constant can not be translated.
 */
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
	{
		printDebugMessage("%s: unknown combiner constant requested: %d", __FUNCTION__, combiner);
		return "UNKNOWN";
	}
}

/**
 * This function returns a constant string corresponding to the name
 * of an \a MPI_DISTRIBUTE_* variable.
 *
 * \param constant the MPI_DISTRIBUTE_* constant, as used to create an MPI
 *  DARRAY datatype.
 *
 * \returns a constant string corresponding to the name of the constant,
 * or the string "UNKNOWN" if the constant is not known.
 */
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
		// TODO: this is an error either in the wrapper or in the traced program
		printDebugMessage("%s: unknown constant constant requested: %d", __FUNCTION__, constant);
		return "UNKNOWN";
	}
}

static const char * getOrderConstantName(int constant)
{
	if(constant == MPI_ORDER_FORTRAN)
		return "MPI_ORDER_FORTRAN";
	else if(constant == MPI_ORDER_C)
		return "MPI_ORDER_C";
	else
	{
		printDebugMessage("%s: unknown constant constant requested: %d", __FUNCTION__, constant);
		return "UNKNOWN";
	}
}



/**
 * A static character to hold the result of \a getCommName(...)
 */
static char comm_name_buffer[MPI_MAX_OBJECT_NAME + 1];

/**
 * This function returns a string holding the name of the
 * communicator \a comm. If \a comm is identical to MPI_COMM_WORLD,
 * the string "WORLD" is returned. If \a comm is identical to MPI_COMM_SELF,
 * the string "SELF" is returned. Otherwise, it returns the result of
 * a call to MPI_Comm_get_name(...). The string is allocated statically and
 * must not be freed by the caller. The length of the string is limited
 * to \a MPI_MAX_OBJECT_NAME characters.
 *
 * The string is not valid after a consecutive call to \a getCommName(...)
 *
 * \param comm The MPI_Comm structure of which the name should be obtained
 *
 * \return a string holding the name of \a comm or \a NULL on error.
 */
static char * getCommName(MPI_Comm comm)
{
	// NOTE: the result becomes invalid after a consecutive
    // call to getCommName(...)

  int len = MPI_MAX_OBJECT_NAME + 1;
  int cmp = 0;
  int ret;
  ret = MPI_Comm_compare(comm, MPI_COMM_WORLD, & cmp);
  CHECK_MPI_ERROR(ret, NULL, "MPI_Comm_compare() failed");

  if(cmp == MPI_IDENT)
  {
	  return "WORLD";
  }
  ret = MPI_Comm_compare(comm, MPI_COMM_SELF, & cmp);
  CHECK_MPI_ERROR(ret, NULL, "MPI_Comm_compare() failed");

  if(cmp == MPI_IDENT)
  {
	  return "SELF";
  }
  ret = MPI_Comm_get_name(comm, comm_name_buffer, & len);
  CHECK_MPI_ERROR(ret, NULL, "MPI_Comm_get_name() failed")
  return comm_name_buffer;
}

/**
 * This function returns a constant string corresponding to
 * an update mode of an MPI_Seek* function.
 *
 * \param whence If \a whence is MPI_SEEK_SET, the string "Set" is returned.
 * If \a whence is MPI_SEEK_CUR the string "Cur" is returned
 * If \a whence is MPI_SEEK_END the string "End" is returned.
 * On any other value, the string "UNKNOWN" is returned.
 *
 * \return "Set", "Cur", "End" or "UNKNOWN", depending on \a whence
 */
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
		printDebugMessage("%s: unknown whence constant requested: %d", __FUNCTION__, whence);
		return "UNKNOWN";
	}
}
