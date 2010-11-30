#include "init.h"


char numbuf[10];

void init
(GSList** traceFile, GSList** comms, int rank, int size,
int argc, char** argv)
{
  char* projectFile;
  char* traceFileFolderPath;
  char* traceFilePath;
  char* hostname;
  char* programName;
  
  int err;
  
  projectFile = (char*) malloc(sizeof(char)*PATH_LEN);
  traceFileFolderPath = (char*) malloc(sizeof(char)*PATH_LEN);
  programName = (char*) malloc(sizeof(char)*PROGRAMM_NAME_LEN);
  hostname = (char*) malloc(sizeof(char)*HOST_NAME_LEN);
  traceFilePath = (char*)malloc(sizeof(char)*PATH_LEN);
  
  if((err = readCliArgs(projectFile, traceFileFolderPath, argc, argv))
  != SUCCESS)
  {
    crash(ERR, err,"");
  }
  
  readproject(programName ,hostname, comms,rank ,projectFile);
  
  printf("%s\n",programName);

  genTraceFileName(traceFilePath ,traceFileFolderPath, programName,hostname,rank);
  
  printf("trc: %s\n", traceFilePath);
  
  readTrace(traceFile, traceFilePath);

}

int readCliArgs
(char* projectFile, char* traceFileFolderPath, int argc, char** argv)
{
  int i;
  int err=0;
  
  for(i=1; i < argc; i+=2)
  {
    if(strcmp(argv[i], "-t")==0)
    {
      strncpy(traceFileFolderPath, argv[i+1], PATH_LEN);
    }
    else if(strcmp(argv[i], "-p")==0)
    {
      strncpy(projectFile, argv[i+1], PATH_LEN);
    }
    
  }
  
  if(projectFile == NULL)
  {
    err = PROJECT_FILE_PATH;
  }
  if(traceFileFolderPath == NULL)
  {
    err = TRACE_FILE_PATH;
  }
  
  return err;
}

void genTraceFileName
(char* traceFilePath, char* traceFileFolderPath, char* programName,
char* hostname, int rank)
{  
  
  strncpy(traceFilePath, traceFileFolderPath, PATH_LEN);
  
  if(traceFilePath[(strlen(traceFilePath)-1)] != '/')
  {
    strncat(traceFilePath, "/", 1);
  }
  sprintf(numbuf,"%i",rank);
  
  strncat(traceFilePath,programName, PATH_LEN);
  strncat(traceFilePath,"_", 1);
  strncat(traceFilePath,hostname, PATH_LEN);
  strncat(traceFilePath,"_", 1);
  strncat(traceFilePath,numbuf, PATH_LEN);
  strncat(traceFilePath,"_0.trc", 6);
  
}
