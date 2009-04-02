#ifndef WRITE_INFO_H_
#define WRITE_INFO_H_

static void writeFileInfo(const char * name, int size, gint id);
static void writeCommInfo(MPI_Comm comm, gint comm_id);
static void writeTypeInfo(MPI_Datatype type, gint id);

#endif
