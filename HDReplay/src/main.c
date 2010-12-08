#include "main.h"

int main(int argc, char** argv)
{

  int err;
  int rank;
  int size;
  GSList * traceFile;
  GSList * comms;
  GSList * fileList;
  GSList * 
  
  struct Element* element;

  MPI_Init(&argc,&argv);
  
  MPI_Errhandler_set(MPI_COMM_WORLD, MPI_ERRORS_RETURN);
  
  if((err = MPI_Comm_rank(MPI_COMM_WORLD, &rank)) != MPI_SUCCESS)
  {
    crash(MPI_ERR, err, "",NULL);
  }
  
  if((err = MPI_Comm_size(MPI_COMM_WORLD, &size)) != MPI_SUCCESS)
  {
    crash(MPI_ERR, err, "", NULL);
  }
  
  init(&traceFile, &comms, &fileList, rank, size, argc, argv);
  
  element = (struct Element*) g_slist_nth_data(traceFile,0);
   
  GSList* last = g_slist_last(comms);
  struct Communicator* comm = (struct Communicator*) last->data;
  printf("%s\n", comm->name);
  printf("%i\n",element->type);
  struct FileList* file = (struct FileList*) fileList->data;
  printf("%s\n",file->name);
  
  MPI_Finalize();
  
  cleanUp(traceFile, comms);
  return 0;
}

void cleanUp(GSList* traceFile, GSList* comms)
{
  g_slist_foreach( traceFile, (GFunc)free, NULL);
  g_slist_free(traceFile);
  
  g_slist_foreach(comms, (GFunc)freeComms, NULL);
  g_slist_foreach(comms, (GFunc)free, NULL);
  g_slist_free(comms);
  
}

void freeComms(gpointer data)
{
  struct Communicator* element = (struct Communicator*) data;
  
  g_slist_foreach(element->ranks, (GFunc)free, NULL);
  g_slist_free(element->ranks);
}


