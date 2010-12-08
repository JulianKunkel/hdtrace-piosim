#include "readproject.h"

static void XMLCALL
  readTopology(struct UserData* data, const char* name,
    const char** attributes);

static void XMLCALL
  startElement(struct UserData* data, const char* name, 
    const char** attributes);
  
static void XMLCALL
  readFileList(struct UserData* data, const char*name,
    const char** attributes);
    
static void XMLCALL
  readCommunicator(struct UserData* data, char* name, char** attributes);
  
static void XMLCALL
  endElement(struct UserData* data, const char* name);
static void XMLCALL
  charhndl(struct UserData* data, const XML_Char* string, int len);

/*
static char* hostnameBuf;
static char* programNameBuf;
static GSList* commsBuf;
static int foundHostname = 0;
static int rankBuf;
static GSList* fileListBuf;
*/

XML_Parser parser;

/**
  The GSList represents the topology of the mpi program.  
  The indices of the list represents a rank of the program.
  The data field of the list contains the hostname of the node the rank was 
  executed with.
*/

// TODO: Implementation for reading the communicators


/**
  \brief startElement is called every time the XML-parser finds a new
  element in the project file.
  
  \param[in] depth Indicates the depth of the XML-element.
  \param[in] name The name of the XML-element.
  \param[in] attributes A list of the attributes of a element.
*/

void
readproject(char* programName, char* hostname, GSList** comms, GSList** fileList,
  int rank ,char* filename)
{
  parser = XML_ParserCreate(NULL);
  char buf[BUFSIZ];
  FILE* projectFile;
  int done;
    
  struct UserData* data = (struct UserData*) malloc(sizeof(struct UserData));  
  data->depth = 0;
  data->checkFlag = 0;
  data->programName = programName;
  data->hostname = hostname;
  data->rank = rank;
  data->comms = NULL;
  data->fileList = NULL;
  
  if((projectFile = fopen(filename, "r")) == NULL)
  {
    crash(SYS_ERR, errno, "%s", filename);
  }
  
  XML_SetUserData(parser, data);
  XML_SetElementHandler(parser,
    (XML_StartElementHandler) startElement, (XML_EndElementHandler) endElement);
  XML_SetCharacterDataHandler(parser,
    (XML_CharacterDataHandler) charhndl);


  do {
      int len = (int)fread(buf, 1, sizeof(buf), projectFile);
      done = len < sizeof(buf);
      if (XML_Parse(parser, buf, len, done) == XML_STATUS_ERROR) {
        crash(ERR, XML_PARSE, "%s in %s at line %"XML_FMT_INT_MOD"u",
                XML_ErrorString(XML_GetErrorCode(parser)),
                filename,
                XML_GetCurrentLineNumber(parser));
              break;
      }
    } while (!done);
    
  XML_ParserFree(parser);
  
  fileList[0] = data->fileList;
  comms[0] = data->comms;
}


static void XMLCALL
startElement(struct UserData* data, const char *name, const char** attributes)
{
  if(strcmp(name, "Application") == 0 && data->depth == 0)
  {
    strncpy(data->programName, attributes[1], PROGRAMM_NAME_LEN);
  }
  else if(strcmp(name,"FileList") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) readFileList, (XML_EndElementHandler) endElement);
  }
  else if(strcmp(name,"Topology") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) readTopology, (XML_EndElementHandler) endElement);
  }
  else if(strcmp(name, "CommunicatorList") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) readCommunicator, (XML_EndElementHandler) endElement);
  }
  data->depth++;
}

/**
  \brief Only decrease the depth if a XML-element is closed.
  
  \param[in] depth Indicates the depth of the XML-element.
  \param[in] name The name of the XML-element.
*/
static void XMLCALL
endElement(struct UserData* data, const char* name)
{
  if(strcmp(name,"FileList") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) startElement, (XML_EndElementHandler) endElement);
  }
  else if(strcmp(name,"Topology") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) startElement, (XML_EndElementHandler) endElement);
  }
  else if(strcmp(name, "CommunicatorList") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) startElement, (XML_EndElementHandler) endElement);
  }
  data->depth--;
}


static void XMLCALL
readTopology(struct UserData* data, const char* name, const char** attributes)
{
  // read the hostname
  if(strcmp(name,"Node") == 0 && data->depth == 2 && data->checkFlag == 0)
  {
    strncpy(data->hostname, attributes[1],HOST_NAME_LEN);
  }
  // read the rank(s) inside the hostname element
  else if
  (strcmp(name,"Node") == 0 && data->checkFlag == 0 && data->depth == 3)
  {
    if(data->rank == atoi(attributes[1]))
    {
      data->checkFlag = 1;
    }
  }
  data->depth++;
}

static void XMLCALL
readCommunicator(struct UserData* data, char* name, char** attributes)
{
  // reading the communicators
  
  if(strcmp(name,"Communicator") == 0 && data->depth == 2)
  {
    struct Communicator* com;
    com = (struct Communicator*) malloc(sizeof(struct Communicator));
    
    strncpy(com->name, attributes[1], COMM_NAME_LEN);
    
    data->comms = g_slist_append(data->comms, (gpointer) com);
  }
  else if(strcmp(name,"Rank") == 0)
  {
    struct Rank* comRank;
    comRank = (struct Rank*) malloc(sizeof(struct Rank));
    
    GSList* last;
    last =  g_slist_last(data->comms);
    
    struct Communicator* com;
    com = (struct Communicator*) last->data;
    com->ranks = NULL;
    
    for(int i = 0; attributes[i]; i+=2)
    {
      if(strcmp(attributes[i], "global") == 0)
      {
        comRank->global = atoi(attributes[i+1]);
      }
      else if(strcmp(attributes[i], "local") == 0)
      {
        comRank->local = atoi(attributes[i+1]);
      }
      else if(strcmp(attributes[i], "cid") == 0)
      {
        comRank->cid = atoi(attributes[i+1]);
      }
    }
    com->ranks = g_slist_append(com->ranks, (gpointer) comRank);
  }
  data->depth++;
}

static void XMLCALL
readFileList(struct UserData* data, const char* name, const char** attributes)
{
  if(strcmp(name, "File") == 0 && data->depth == 2)
  {
    struct FileList* file = (struct FileList*) malloc(sizeof(struct FileList));
   
    strncpy(file->name, attributes[1], PATH_LEN);
    
    data->fileList = g_slist_append(data->fileList, (gpointer) file);
  }
  else if(strcmp(name,"InitalSize") == 0 && data->depth == 3)
  {
    data->checkFlag = 2;
  }
  else if(strcmp(name, "Distribution") == 0 && data->depth == 3)
  {
    GSList* last;
    struct FileList* file;
    
    last = g_slist_last(data->fileList);
    file = (struct FileList*) last->data;
    
    strncpy(file->implementation, attributes[1], PATH_LEN);
  }
  data->depth++;
}

static void XMLCALL
charhndl(struct UserData* data, const XML_Char* string, int len)
{
  if(data->checkFlag == 2)
  {
    GSList* last;
    struct FileList* file;
    
    last = g_slist_last(data->fileList);
    file = (struct FileList*) last->data;
    
    file->initialSize = atoi(string);
  
    data->checkFlag = 0;
  }  
}


