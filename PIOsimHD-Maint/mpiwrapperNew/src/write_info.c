static void writeFileInfo(const char * name, int size, gint id)
{
			hdT_LogInfo(tracefile, 
						"File name=\"%s\" Size=0 id=%d\n",
						name, id);

}

static void writeCommInfo(MPI_Comm comm, gint comm_id)
{
	int size;
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	int *ranks_world = malloc(size * sizeof(int));
	int i;

	for(i = 0; i < size; ++i)
	{
		ranks_world[i] = i;
	}
	int *ranks_comm = malloc(size * sizeof(int));

	MPI_Group world_group, comm_group;
	MPI_Comm_group(MPI_COMM_WORLD, &world_group);
	MPI_Comm_group(comm, &comm_group);
	
	MPI_Group_translate_ranks(world_group, size, ranks_world, comm_group, ranks_comm);
	
	char buffer[TMP_BUF_LEN];
	size_t position = 0;
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "Comm map='");
	position = hdT_min(position, TMP_BUF_LEN);

	for(i = 0; i < size; ++i)
	{
		if(ranks_comm[i] != MPI_UNDEFINED ) 
		{
			position += snprintf(buffer + position, TMP_BUF_LEN - position, "%d->%d;", i, ranks_comm[i]);
			position = hdT_min(position, TMP_BUF_LEN);
		}
	}
	position += snprintf(buffer + position, TMP_BUF_LEN - position, "' id=%d name='%s'\n", comm_id, getCommName(comm));
	position = hdT_min(position, TMP_BUF_LEN);

	hdT_LogInfo(tracefile, buffer);
}



static void writeTypeInfo(MPI_Datatype type, gint id)
{
	int max_integers, 
		max_addresses, 
		max_datatypes, 
		combiner;

	MPI_Type_get_envelope(type, &max_integers, &max_addresses, &max_datatypes, &combiner);

	char typename[MPI_MAX_OBJECT_NAME];
	int resultlen;
	MPI_Type_get_name(type, typename, &resultlen);

	if(combiner == MPI_COMBINER_NAMED) 	// cannot call get_contents on this
	{
		hdT_LogInfo(tracefile, "Type id='%d' combiner='%s' name='%s'\n",
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
		pos = hdT_min(pos, TMP_BUF_LEN);

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
				pos = hdT_min(pos, TMP_BUF_LEN);
			}

			for(i = integers[2] + 3; i < integers[2]*3 + 3; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
								"%s;", getDistributeConstantName(integers[i]) );
				pos = hdT_min(pos, TMP_BUF_LEN);
			}
			for(i = integers[2]*3 + 3; i < max_integers; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
								"%d;", integers[i] );
				pos = hdT_min(pos, TMP_BUF_LEN);
			}
		}
		else
		{
			for(i = 0; i < max_integers; ++i)
			{
				pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
								"%d;", integers[i] );
				pos = hdT_min(pos, TMP_BUF_LEN);
			}
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"' addresses='");
		pos = hdT_min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_addresses; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%lld;", (long long int)addresses[i] );
			pos = hdT_min(pos, TMP_BUF_LEN);
		}

		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"' types='");
		pos = hdT_min(pos, TMP_BUF_LEN);
		for(i = 0; i < max_datatypes; ++i)
		{
			pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
							"%d;", getTypeId(datatypes[i]) );
			pos = hdT_min(pos, TMP_BUF_LEN);
		}
		pos += snprintf(buffer + pos, TMP_BUF_LEN - pos, 
						"'\n");
		pos = hdT_min(pos, TMP_BUF_LEN);

		hdT_LogInfo(tracefile, buffer);

		free(integers);
		free(addresses);
		free(datatypes);
	}
}

