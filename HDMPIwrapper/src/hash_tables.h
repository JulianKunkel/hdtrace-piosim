#ifndef HASH_TABLE_H_
#define HASH_TABLE_H_

guint hash_MPI_File(gconstpointer key);
gboolean equal_MPI_File(gconstpointer a, gconstpointer b);
static gint getCommId(MPI_Comm comm);
static gint getFileId(MPI_File fh);
static gint getFileIdFromName(const char * name);
static gint getFileIdEx(MPI_File fh, const char * name);
static gint getTypeId(MPI_Datatype type);
static gint getRequestId(MPI_Request request);
static gint getRequestIdForSplit(MPI_File file);
static void destroyHashTables();
static void removeFileHandle(MPI_File fh);

#endif

