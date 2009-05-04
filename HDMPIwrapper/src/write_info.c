/**
 * @file write_info.c
 *
 * Each trace consists of a log file and an info file. The log file
 * contains the information that can be used directly by other
 * tools (like HDJumpshot).
 * Because each thread writes its own trace, it may contain
 * redundant information. For example, collectively used files
 * or communicators need to appear only once in the global trace.
 * This is achieved by writing the redundant information to an info file and
 * processing it later by a script (e.g. \a scripts/project-description-merger.py)
 *
 * \a write_info.c contains functions that write the information to
 * the info file.
 *
 * \author Paul Mueller <pmueller@ix.urz.uni-heidelberg.de>
 */


#include "hash_tables.h"
#include "write_info.h"

/**
 * Returns the smaller value of the two parameters.
 */
size_t min(size_t a, size_t b)
{
	if(a > b)
		return b;
	return a;
}

/**
 * This function writes information about a file to the trace
 * info file. The global variable \a tracefile is used for this purpose
 * and must already be initialized.
 *
 * Typical output:
 * \code
 * File name="test.txt" size=0 id=3
 * \endcode
 *
 * \param name The name of the file.
 * \param size The size of the file at first access
 * \param id   The id that has been assigned to the file by the wrapper
 *
 *
 */
static void writeFileInfo(const char * name, int size, gint id)
{
	hdT_writeInfo(tracefile,
			"File name=\"%s\" Size=%d id=%lld\n",
			name, size, (long long int)id);
}

/**
 * This function writes information about an MPI communicator
 * \a comm to * the trace info file. The global variable \a tracefile is used for this purpose.
 * It must already be initialized.
 *
 * Typical output (info file):
 * \code
 * Comm map='0->0;1->1;2->2;3->3;4->4;' id=0 name='WORLD'
 * Comm map='2->0;4->1;' id=1 name=''
 * \endcode
 *
 * \param comm The communicator, for which the info is being written
 * \param comm_id The id that has been assigned to \a comm by the mpi wrapper
 *
 */
static void writeCommInfo(MPI_Comm comm, gint comm_id)
{
	int size;
	PMPI_Comm_size(MPI_COMM_WORLD, &size);
	int *ranks_world = malloc(size * sizeof(int));
	int i;

	for(i = 0; i < size; ++i)
	{
		ranks_world[i] = i;
	}
	int *ranks_comm = malloc(size * sizeof(int));

	MPI_Group world_group, comm_group;
	PMPI_Comm_group(MPI_COMM_WORLD, &world_group);
	PMPI_Comm_group(comm, &comm_group);

	PMPI_Group_translate_ranks(world_group, size, ranks_world, comm_group, ranks_comm);

	char buffer[TMP_BUF_LEN];
	size_t position = 0;
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "Comm map='");
	position = min(position, TMP_BUF_LEN);

	for(i = 0; i < size; ++i)
	{
		if(ranks_comm[i] != MPI_UNDEFINED )
		{
			position += snprintf(buffer + position, TMP_BUF_LEN - position, "%d->%d;", i, ranks_comm[i]);
			position = min(position, TMP_BUF_LEN);
		}
	}
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "' id=%d name='%s'\n", comm_id, getCommName(comm));
	position = min(position, TMP_BUF_LEN);

	hdT_writeInfo(tracefile, "%s", buffer);
}


/**
 * This function writes information about the MPI datatype
 * \a type to the trace info file. The global variable
 * \a tracefile is used for this purpose
 * and must already be initialized.
 */
static void writeTypeInfo(MPI_Datatype type, gint id)
{
	int max_integers,
		max_addresses,
		max_datatypes,
		combiner;

	PMPI_Type_get_envelope(type, &max_integers, &max_addresses, &max_datatypes, &combiner);

	char typename[MPI_MAX_OBJECT_NAME];
	int resultlen;
	PMPI_Type_get_name(type, typename, &resultlen);

	if(combiner == MPI_COMBINER_NAMED) 	// cannot call get_contents on this
	{
		hdT_writeInfo(tracefile, "Type id='%d' combiner='%s' name='%s'\n",
				  (int)id, getCombinerName(combiner), typename);
	}
	else
	{
		int *integers = malloc(sizeof(int) * max_integers);
		MPI_Aint *addresses = malloc(sizeof(MPI_Aint) * max_addresses);
		MPI_Datatype *datatypes = malloc(sizeof(MPI_Datatype) * max_datatypes);
		MPI_Type_get_contents(type, max_integers, max_addresses, max_datatypes,
							  integers, addresses, datatypes);


		char buffer[TMP_BUF_LEN];
		int pos = 0;
		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
						"Type id='%d' combiner='%s' name='%s' integers='", (int)id, getCombinerName(combiner), typename);
		pos = min(pos, TMP_BUF_LEN);

		int i;
		if(combiner == MPI_COMBINER_DARRAY)
		{
			// this is not nice, but we need to log the names of the constantes
			// used by mpi_create_darray, not the numerical values
			assert(max_integers > 2);
			for(i = 0; i < integers[2] + 3; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
								"%d;", integers[i] );
				pos = min(pos, TMP_BUF_LEN);
			}

			for(i = integers[2] + 3; i < integers[2]*3 + 3; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
								"%s;", getDistributeConstantName(integers[i]) );
				pos = min(pos, TMP_BUF_LEN);
			}
			for(i = integers[2]*3 + 3; i < max_integers-1; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
								"%d;", integers[i] );
				pos = min(pos, TMP_BUF_LEN);
			}
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%s;", getOrderConstantName(integers[max_integers - 1]));
		}
		else if(combiner == MPI_COMBINER_SUBARRAY)
		{
			for(i = 0; i < max_integers - 1; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
								"%d;", integers[i] );
				pos = min(pos, TMP_BUF_LEN);
			}
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%s;", getOrderConstantName(integers[max_integers - 1]));
		}
		else
		{
			for(i = 0; i < max_integers; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
								"%d;", integers[i] );
				pos = min(pos, TMP_BUF_LEN);
			}
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
						"' addresses='");
		pos = min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_addresses; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
							"%lld;", (long long int)addresses[i] );
			pos = min(pos, TMP_BUF_LEN);
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
						"' types='");
		pos = min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_datatypes; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
							"%d;", getTypeId(datatypes[i]) );
			pos = min(pos, TMP_BUF_LEN);
		}
		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos,
						"'\n");
		pos = min(pos, TMP_BUF_LEN);

		hdT_writeInfo(tracefile, "%s", buffer);

		free(integers);
		free(addresses);
		free(datatypes);
	}
}

