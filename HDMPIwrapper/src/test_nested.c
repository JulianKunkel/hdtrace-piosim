/**
 * @file test_nested.c
 *
 * This file provides the functions \a PMPI_hdT_Test_nested(...)
 * and \a MPI_hdT_Test_nested(...) that are used to test the logging
 * of nested function calls. This means that if an MPI function
 * calls another MPI function, the resulting log should contain
 * the following:
 * \code
 * <Nested>
 *   <Inner_function_call time="0.12" end="0.13" />
 * </Nested>
 * <Outer_function_call time="0.11" end="0.23" />
 * \endcode
 *
 * If the logging of nested calls is disabled (default),
 * only the outer call will be logged:
 * \code
 * <Outer_function_call time="0.11" end="0.23" />
 * \endcode
 *
 * The logging of nested calls can be enabled by setting the environment
 * variable
 * \code HDTRACE_NESTED=1
 * \endcode
 */

/* this unit is optional */
#ifdef HDTRACE_INCLUDE_NESTED_TEST

int MPI_hdT_Test_nested(int rec, int max);

/**
 * This function calls itself recursively with the
 * recursion depth \a max.
 * It is used to test the logging of nested tags.
 */
static int PMPI_hdT_Test_nested(int rec, int max)
{
	if(rec < max)
	{
		MPI_hdT_Test_nested(rec + 1, max);
		MPI_hdT_Test_nested(rec + 1, max);
	}

	return 0;
}

/**
 * The wrapper function for \a PMPI_hdT_Test_nested().
 */
int MPI_hdT_Test_nested(int v1,  int v2){
  int ret;

  hdMPI_threadLogStateStart( "hdT_Test_nested");

  ret = PMPI_hdT_Test_nested( v1,  v2);

  hdMPI_threadLogAttributes("depth='%d'", v1);
  hdMPI_threadLogStateEnd();

  return ret;
}

#endif
