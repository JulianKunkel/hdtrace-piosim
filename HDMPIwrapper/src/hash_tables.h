#ifndef HASH_TABLE_H_
#define HASH_TABLE_H_


guint hash_MPI_File(gconstpointer key);
gboolean equal_MPI_File(gconstpointer a, gconstpointer b);
static void destroyHashTables();
static void removeFileHandle(MPI_File fh);

#endif

