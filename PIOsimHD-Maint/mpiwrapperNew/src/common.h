#ifndef COMMON_H_
#define COMMON_H_

#define DEBUG_MESSAGE_PREFIX "[MPIWrapper]"

static void printDebugMessage(const char * format, ...);

/**
 * macro to handle errors from MPI functions.
 * check, if the value of \a mpi_return equals to \a MPI_SUCCESS;
 * if not, print the function name (of the calling function) and
 * \a message, and return \a return_value.
 */
#define CHECK_MPI_ERROR(mpi_return, return_value, message) \
	if(mpi_return != MPI_SUCCESS) \
	{\
		printDebugMessage("%s: " message, __FUNCTION__ ); \
		return return_value; \
	}


#endif
