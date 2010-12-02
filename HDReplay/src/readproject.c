#include "readproject.h"


static char* hostnameBuf;
static char* programNameBuf;
static GSList* commsBuf;
static int foundHostname = 0;
static int rankBuf;

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
static void XMLCALL
startElement(int* depth, const char *name, const char** attributes)
{
  // read the hostname form the file
  if(strcmp(name,"Node") == 0 && depth[0] == 2 && foundHostname == 0)
  {
    strncpy(hostnameBuf,attributes[1],HOST_NAME_LEN);
  }
  // read the rank(s) inside the hostname element
  else if(strcmp(name,"Node") == 0 && foundHostname == 0 && depth[0] == 3)
  {
    if(rankBuf == atoi(attributes[1]))
    {
      foundHostname = 1;
    }
  }
  else if(strcmp(name, "Application") == 0)
  {
    strncpy(programNameBuf, attributes[1], PROGRAMM_NAME_LEN);
  }
  
  // reading the communicators
  
  else if(strcmp(name,"Communicator") == 0)
  {
    struct Communicator* com;
    com = (struct Communicator*) malloc(sizeof(struct Communicator));
    
    strncpy(com->name, attributes[1], COMM_NAME_LEN);
    
    commsBuf = g_slist_append(commsBuf, (gpointer) com);
  }
  else if(strcmp(name,"Rank") == 0)
  {
    struct Rank* comRank;
    comRank = (struct Rank*) malloc(sizeof(struct Rank));
    
    GSList* last;
    last =  g_slist_last(commsBuf);
    
    struct Communicator* com;
    com = (struct Communicator*) last->data;
    
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
  depth[0]++;
}

/**
  \brief Only decrease the depth if a XML-element is closed.
  
  \param[in] depth Indicates the depth of the XML-element.
  \param[in] name The name of the XML-element.
*/
static void XMLCALL
endElement(int* depth, const char* name)
{
  depth[0]--;
}

void
readproject(char* programName, char* hostname, GSList** comms,int rank ,char* filename)
{
   XML_Parser parser = XML_ParserCreate(NULL);
  char buf[BUFSIZ];
  FILE* projectFile;
  int depth = 0;
  int done;  
  
  programNameBuf = programName;
  hostnameBuf = hostname;
  rankBuf = rank;
  
  if((projectFile = fopen(filename, "r")) == NULL)
  {
    crash(SYS_ERR, errno, "%s", filename);
  }
  
  XML_SetUserData(parser, &depth);
  XML_SetElementHandler(parser,
  (XML_StartElementHandler) startElement, (XML_EndElementHandler) endElement);

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
    
  comms[0] = commsBuf;
  XML_ParserFree(parser);
}
