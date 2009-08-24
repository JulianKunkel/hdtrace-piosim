/**
 * @file hash_tables.c
 *
 * The MPI wrapper maps file handles, file names, communicators
 * and types to integers for easier logging. This file provides
 * an interface to these mappings.
 *
 * Example: The first call of getCommId(comm) creates a new id
 * (an integer) and associates it with \a comm. Any following call
 * with the same argument will return the same id.
 *
 *
 */

#include "write_info.h"

/**
 * Hash function for MPI_File.
 *
 * This function is used to create a GHashTable of MPI_File
 * objects.
 *
 * @return  Key hash, which is a simple cast to int.
 */
guint hash_MPI_File(gconstpointer key)
{
	long f = (long) (*(MPI_File*)key);
	return (guint) f;
}

/**
 * Comparison function for MPI_File objects.
 *
 * Compare two MPI_File objects. This function is needed for
 * file handle to id hashtables
 *
 * @return \a TRUE if the file handles are equal and \a FALSE
 * otherwise.
 */
gboolean equal_MPI_File(gconstpointer a, gconstpointer b)
{
	if((*(MPI_File*)a) == (*(MPI_File*)b))
		return TRUE;
	return FALSE;
}


/**
 * This thread global variable holds a pointer to a hash table that maps
 * an MPI communicator to an ID number. The ID is assigned to
 * the communicator on the first use.
 *
 * The hash table is created at first use of this variable and
 * destroyed, when \a destroyHashTables() is called.
 */
static __thread GHashTable *comm_to_id = NULL;

/**
 * This thread global variable is used to assign unique IDs to
 * the used communicators. On the first use of an MPI communicator,
 * the value of \a comm_id_counter is used as ID. \a comm_id_counter
 * is incremented afterwards.
 */
static __thread gint comm_id_counter = 0;

/**
 * This function returns the ID that has been assigned to \a comm.
 * If \a comm does not have an ID, a new one will be assigned.
 *
 * \param comm an MPI communicator
 *
 * \return the ID that is associated with the communicator.
 */
static gint getCommId(MPI_Comm comm)
{
	if(comm_to_id == NULL)
	{
		comm_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}
	gpointer result = g_hash_table_lookup(comm_to_id, &comm);
	if(result == NULL)
	{
		void * g_comm = malloc(sizeof(MPI_Comm));
		memcpy(g_comm, & comm, sizeof(MPI_Comm));

		gint * g_id = malloc(sizeof(gint));
		*g_id = (gint)comm_id_counter;
		g_hash_table_insert(comm_to_id, g_comm, g_id);
		comm_id_counter++;

		writeCommInfo(comm, *g_id);
		return *g_id;
	}
	return *(gint*)result;
}

/**
 * remove \a comm from the communicator lookup table
 */
static void removeComm(MPI_Comm comm)
{
	g_hash_table_remove(comm_to_id, &comm);
}

/**
 * This variable holds a pointer to a hash table that maps
 * an MPI file handle to an ID that has been assigned to the
 * file handle.
 *
 * The hash table is created in \a getFileId(...) or \a getFileIdEx(...)
 * and destroyed in \a destroyHashTables()
 */
__thread GHashTable *file_handle_to_id = NULL;

/**
 * This variable holds a pointer to a hash table that maps the
 * name of a file to an ID. If a file is opened, the corresponding
 * file handle is mapped to the same ID by the hash table
 * \a file_handle_to_id.
 *
 * The hash table is created in \a getFileIdFromName(...)
 *  or \a getFileIdEx(...) and destroyed in \a destroyHashTables()
 */
__thread GHashTable *file_name_to_id = NULL;

/**
 * This variable is used to assign unique IDs to file handles and
 * file names. Whenever a new ID is needed, the value of
 * \a file_id_counter is taken as id and \a file_id_counter is incremented.
 */
__thread gint file_id_counter = 0;



/**
 * This function returns the unique ID that has been assigned to the
 * MPI file handle \a fh. A new ID is assigned, if needed.
 *
 * \param fh an MPI file handle.
 *
 * \return the ID assigned to \a fh.
 */
static gint getFileId(MPI_File fh)
{
	if(file_handle_to_id == NULL)
	{
		file_handle_to_id = g_hash_table_new_full(hash_MPI_File, equal_MPI_File, free, free);
	}
	gpointer result = g_hash_table_lookup(file_handle_to_id, &fh);
	if(result == NULL)
	{
		MPI_File *g_handle = malloc(sizeof(MPI_File));
		*g_handle = fh;
		gint *g_id = malloc(sizeof(gint));
		*g_id = (gint)file_id_counter;
		g_hash_table_insert(file_handle_to_id, g_handle, g_id);
		file_id_counter++;

		//tprintf(tracefile, "getFileId: got File handle without associated id. new id=%d, fh=%llx", (*g_id), fh);

		return *g_id;
	}
	return *(gint*)result;
}

/**
 * This function can be used to remove a file handle from the
 * \a file_handle_to_id map. It should be called whenever the
 * file handle becomes invalid (i.e. on \aMPI_File_close(...) )
 *
 * \param fh The file handle to be removed
 */
static void removeFileHandle(MPI_File fh)
{
	g_hash_table_remove(file_handle_to_id, &fh);
}

/**
 * This function returns the file ID that has been associated
 * with \a name.
 *
 * \param name the name of the file
 *
 * \return ID associated with \a name
 */
static gint getFileIdFromName(const char * name)
{
	if(file_name_to_id == NULL)
	{
		file_name_to_id = g_hash_table_new_full(g_str_hash, g_str_equal, free, free);
	}

	/* TODO
     * NOTE: it would be nice to canonicalize the file name, e.g. with
	 * realpath(name, NULL);. Unfortunately, this doesn't work with names
     * beginning with "pvfs://"
	 */
	char * real_path = g_strdup(name);

	gpointer result =  g_hash_table_lookup(file_name_to_id, real_path);
	if(result == NULL)
	{
		gint *g_id = malloc(sizeof(gint));
		*g_id = file_id_counter;
		g_hash_table_insert(file_name_to_id, real_path, g_id);
		file_id_counter++;

		writeFileInfo(name, 0, *g_id);

		return *g_id;
	}
	return *(gint*)result;
}

 /**
 * This function should be used, whenever a file is acessed by name and its
 * file pointer is known (File_open...)
 * If the file has been accessed before (by name), the same id can
 * be associated again.
 *
 * If the file has not yet been opened, an entry is written to the trace file
 *
 */
static gint getFileIdEx(MPI_File fh, const char * name)
{
	if(file_name_to_id == NULL)
	{
		file_name_to_id = g_hash_table_new_full(g_str_hash, g_str_equal, free, free);
	}
	if(file_handle_to_id == NULL)
	{
		file_handle_to_id = g_hash_table_new_full(hash_MPI_File, equal_MPI_File, free, free);
	}

	/* TODO
     * NOTE: it would be nice to canonicalize the file name, e.g. with
	 * nrealpath(name, NULL);. Unfortunately, this doesn't work with names
     * beginning with "pvfs://"
	 */
	gpointer name_result = g_hash_table_lookup(file_name_to_id, name);
	gpointer handle_result = g_hash_table_lookup(file_handle_to_id, &fh);

	if(name_result == NULL) //don't know the file
	{
		if(handle_result != NULL)
		{
			//the file has a handle without us knowing the name. this is bad
			//hdt_debug(tracefile, "getFileIdEx(...): file handle without matching filename");
			printDebugMessage("%s: broken file handle hash map.", __FUNCTION__);
			return -1;
		}
		else
		{
			// this is a new file
			gint *g_id = malloc(sizeof(gint));
			gint *g_id2 = malloc(sizeof(gint)); // this makes it easier to automatically free the memory
			MPI_File *g_fh = malloc(sizeof(MPI_File));

			assert(sizeof(void *) == sizeof(MPI_File));



			*g_fh = fh;
			*g_id = file_id_counter;
			*g_id2 = file_id_counter;

			g_hash_table_insert(file_name_to_id, g_strdup(name), g_id);
			g_hash_table_insert(file_handle_to_id, g_fh, g_id2);

			long long int fileSize;
			PMPI_File_get_size(fh, &fileSize);

			writeFileInfo(name, fileSize, *g_id);
			/*
			hdT_LogInfo(tracefile,
						"File name=\"%s\" Size=%lld id=%d\n",
						name, fileSize, *g_id);
			*/

			file_id_counter++;

			return *g_id;
		}
	}
	else
	{
		// know the file
		if(handle_result != NULL)
		{
			// file probably has been opened before...
			if( *(gint*)name_result == *(gint*)handle_result )
			{
				return *(gint*)name_result;
			}
			else
			{
				// fix the fh -> id mapping and warn
				gint *g_id = malloc(sizeof(gint));
				MPI_File *g_fh = malloc(sizeof(MPI_File));
				*g_id = *(gint*)name_result;
				*g_fh = fh;

				//hdt_debug(tracefile, "file handle map and file name map do not match. id=%d", *g_id);

				g_hash_table_insert(file_handle_to_id, g_fh, g_id);
				return *g_id;
			}
		}
		else
		{
			// know the file, don't know the handle
			// insert the fh -> id mapping
			gint *g_id = malloc(sizeof(gint));
			MPI_File *g_fh = malloc(sizeof(MPI_File));
			*g_id = *(gint*)name_result;
			*g_fh = fh;

			g_hash_table_insert(file_handle_to_id, g_fh, g_id);
			return *g_id;
		}
	}
}

/**
 * This variable holds a pointer to the hash table that maps MPI types
 * to IDs
 */
__thread GHashTable *type_table = NULL;

/**
 * counter for type IDs. Incremented on each ID assignment in
 * \a getTypeId()
 */
__thread gint type_id_counter = 0;

guint hash_MPI_Type(gconstpointer key)
{
	return (guint)*(MPI_Datatype*)key;
}

gboolean equal_MPI_Type(gconstpointer a, gconstpointer b)
{
	return *(MPI_Datatype*)a == *(MPI_Datatype*)b;
}

/**
 * This function returns the ID that is associated with the
 * MPI datatype \a type. If no ID is associated, a new one is created
 * and returned.
 */
static gint getTypeId(MPI_Datatype type)
{
	assert(sizeof(gint) >= sizeof(MPI_Datatype));

	if(type_table == NULL)
	{
		// TODO: use datatype hash, implement == for datatypes
		type_table = g_hash_table_new_full(hash_MPI_Type, equal_MPI_Type, free, free);
	}
	gpointer result = g_hash_table_lookup(type_table, &type);
	if(result == NULL)
	{
		MPI_Datatype * m_type = malloc(sizeof(MPI_Datatype));
		*m_type = type;
		gint * g_id = malloc(sizeof(gint));
		*g_id = type_id_counter;
		type_id_counter++;

		g_hash_table_insert(type_table, m_type, g_id);

		writeTypeInfo(type, *g_id);

		return *g_id;
	}
	return *(gint*)result;
}

static void removeType(MPI_Datatype type)
{
	g_hash_table_remove(type_table, &type);
}

/**
 * \a request_to_id holds a pointer to a hash table that maps an
 * MPI_Request to an ID.
 *
 * Because we use requests to keep track of split collective calls
 * as well as for nonblocking calls, we need to map MPI_Requests
 * and file handles to unique request IDs
 */
__thread GHashTable *request_to_id = NULL;

/**
 * \a request_to_id holds a pointer to a hash table that maps an
 * MPI file handle to an ID.
 *
 * Because we use requests to keep track of split collective calls
 * as well as for nonblocking calls, we need to map MPI_Requests
 * and file handles to unique request IDs
 */
__thread GHashTable *fh_to_request_id = NULL; // used to log split-collective-calls

/**
 * This counter provides the request IDs that are assigned
 * to MPI_Requests (from nonblocking calls) and file handles
 * (from split collective calls).
 */
__thread gint request_counter = 0;

/**
 * This function returns the ID that has been assigned to \a request.
 */
static gint getRequestId(MPI_Request request)
{
	if(request_to_id == NULL)
	{
		request_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}
	gpointer result = g_hash_table_lookup(request_to_id, &request);
	if(result == NULL)
	{
		gint *g_request = malloc(sizeof(gint));
		*g_request = (gint)request;
		gint *g_id = malloc(sizeof(gint));
		*g_id = (gint)request_counter;
		g_hash_table_insert(request_to_id, g_request, g_id);
	    request_counter++;

		return *g_id;
	}
	return *(gint*)result;
}

/**
 * This function returns the request ID that has been assigned to
 * the \a file for a split collective call.
 */
static gint getRequestIdForSplit(MPI_File file)
{
	if(fh_to_request_id == NULL)
	{
		fh_to_request_id = g_hash_table_new_full(hash_MPI_File, equal_MPI_File, free, free);
	}
	gpointer result = g_hash_table_lookup(fh_to_request_id, &file);
	if(result == NULL)
	{
		MPI_File *g_fh = malloc(sizeof(MPI_File));
		gint *g_id = malloc(sizeof(gint));
		*g_fh = file;
		*g_id = request_counter;
		request_counter++;
		g_hash_table_insert(fh_to_request_id, g_fh, g_id);

		return *g_id;
	}
	return *(gint*)result;
}

/**
 * This functions destroys all hash tables that have been created.
 */
static void destroyHashTables()
{
	if(file_handle_to_id)
	{
		g_hash_table_destroy(file_handle_to_id);
		file_handle_to_id = NULL;
	}
	if(file_name_to_id)
	{
		g_hash_table_destroy(file_name_to_id);
		file_name_to_id = NULL;
	}
	if(comm_to_id)
	{
		g_hash_table_destroy(comm_to_id);
		comm_to_id = NULL;
	}
	if(type_table)
	{
		g_hash_table_destroy(type_table);
		type_table = NULL;
	}
	if(request_to_id)
	{
		g_hash_table_destroy(request_to_id);
		request_to_id = NULL;
	}
}


