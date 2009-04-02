#include "write_info.h"

/** Hash function for MPI_File. This function is needed for 
 * file handle to id hashtables
 */
guint hash_MPI_File(gconstpointer key)
{
	return (guint)*(MPI_File*)key;
}

/** Compare two MPI_File objects. This function is needed for
 * file handle to id hashtables
 */
gboolean equal_MPI_File(gconstpointer a, gconstpointer b)
{
	if((*(MPI_File*)a) == (*(MPI_File*)b))
		return TRUE;
	return FALSE;
}


static __thread GHashTable *comm_to_id = NULL;
static __thread gint comm_id_counter = 0;

static gint getCommId(MPI_Comm comm)
{
	if(comm_to_id == NULL)
	{
		comm_to_id = g_hash_table_new_full(g_int_hash, g_int_equal, free, free);
	}	
	gpointer result = g_hash_table_lookup(comm_to_id, &comm);
	if(result == NULL)
	{
		gint * g_comm = malloc(sizeof(gint));
		*g_comm = (gint)comm;
		gint * g_id = malloc(sizeof(gint));
		*g_id = (gint)comm_id_counter;
		g_hash_table_insert(comm_to_id, g_comm, g_id);
		comm_id_counter++;

		writeCommInfo(comm, *g_id);
		return *g_id;
	}
	return *(gint*)result;
}

__thread GHashTable *file_handle_to_id = NULL;
__thread GHashTable *file_name_to_id = NULL;
__thread gint file_id_counter = 0;

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




static gint getFileIdFromName(const char * name)
{
	if(file_name_to_id == NULL)
	{
		file_name_to_id = g_hash_table_new_full(g_str_hash, g_str_equal, free, free);
	}

	/* NOTE: it would be nice to canonicalize the file name, e.g. with 
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

	/* NOTE: it would be nice to canonicalize the file name, e.g. with 
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
			return -1;
		}
		else
		{
			// this is a new file
			gint *g_id = malloc(sizeof(gint)); 
			gint *g_id2 = malloc(sizeof(gint)); // this makes it easier to automatically free the memory
			MPI_File *g_fh = malloc(sizeof(MPI_File));
			assert(sizeof(gint) >= sizeof(MPI_File));
			
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

__thread GHashTable *type_table = NULL;
__thread gint type_table_result = 1; // what is returned when the type is found in type_table

static gint getTypeId(MPI_Datatype type)
{
	if(type_table == NULL)
	{
		type_table = g_hash_table_new_full(g_int_hash, g_int_equal, free, NULL);
	}
	gpointer result = g_hash_table_lookup(type_table, &type);
	if(result == NULL)
	{
		gint * g_type = malloc(sizeof(gint));
		*g_type = (gint)type;
		g_hash_table_insert(type_table, g_type, &type_table_result);

		writeTypeInfo(type, (gint)type);
	}
	return type;
}

__thread GHashTable *request_to_id = NULL;
__thread GHashTable *fh_to_request_id = NULL; // used to log split-collective-calls
__thread gint request_counter = 0;

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

static void removeFileHandle(MPI_File fh)
{
	g_hash_table_remove(file_handle_to_id, &fh);
}
