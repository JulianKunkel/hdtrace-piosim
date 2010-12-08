/**
* \author Johann Weging
* This files contains all function for reading the *trc files.
*/

#include "readtrace.h"

static GSList* elements = NULL;
static int isNested = 0;


#define PARSE(varName, parseFunc) \
if (strcmp(attributes[i], #varName) == 0) { \
  tmp->varName = parseFunc; \
}

#define PARSEINT(varName) PARSE(varName, atoi(value))

#define COMMAND(TYPE, content)  \
 if ( strcmp(name, #TYPE) == 0 ){ \
  struct Element* newElement = calloc(1,sizeof(struct Element));\
   newElement->type = Mpi##TYPE; \
   struct Mpi##TYPE * tmp = & newElement->u.mpi##TYPE; \
   tmp->cLine = -1; \
   for(int i=0; attributes[i]; i+=2){ \
     const char * value = attributes[i+1]; \
     content \
     if(strcmp(attributes[i], "time") == 0) newElement->start = atof(value);\
     if(strcmp(attributes[i], "end") == 0) newElement->end = atof(value); \
  } \
  if(isNested >1) tmp->cLine = -1;\
  elements = g_slist_append(elements, (struct Element*) newElement);\
}

static void XMLCALL
startElement(int* depth, const char *name, const char** attributes)
{
  if(strcmp(name, "Program")!=0 && depth == 0)
  {
    // TODO: errorhandling for a invalid tracefile
  }
  if(strcmp(name, "Nested") == 0)
  {
    isNested++;
  }
  else
  {
    if((isNested == 0) || (isNested > 1))
    {
      
      COMMAND(Barrier,
        PARSEINT(cid)
        PARSEINT(cLine)
      )
      COMMAND(Comm_create,
        PARSEINT(cid)
        PARSEINT(cLine)
        PARSEINT(newcid)
      )
      /*COMMAND(Comm_free,
        PARSEINT(cid)
        PARSEINT(cLine)
      )*/
      COMMAND(Finalize,
        PARSEINT(cLine)
      )
      COMMAND(Recv,
        PARSEINT(cid)
        PARSEINT(fromTag)
        PARSEINT(fromRank)
        PARSEINT(cLine)
      )
      COMMAND(Send,
        PARSEINT(cid)
        PARSEINT(toTag)
        PARSEINT(toRank)
        PARSEINT(size)
        PARSEINT(count)
        PARSEINT(tid)
        PARSEINT(cLine)
      ) 
      COMMAND(Sendrecv,
      PARSEINT(cid)
      PARSEINT(toRank)
      PARSEINT(toTag)
      PARSEINT(fromRank)
      PARSEINT(fromTag)
      PARSEINT(size)
      PARSEINT(count)
      PARSEINT(sendTid)
      PARSEINT(recvTid)
      PARSEINT(cLine)
      ) 
    }
    
  }
  depth++;
}

static void XMLCALL
endElement(int* depth, const char* name)
{

  if(strcmp(name, "Nested") == 0)
  {
    isNested = 0;
  }
  
  depth--;
}

void readTrace(GSList** list, char * filename)
{


  XML_Parser parser = XML_ParserCreate(NULL);
  char buf[BUFSIZ];
  FILE* traceFile;
  int done;
  int depth;
  
  if((traceFile = fopen(filename, "r")) == NULL)
  {
    crash(SYS_ERR, errno, "%s", filename);
  }
  
  XML_SetUserData(parser, &depth);
  XML_SetElementHandler(parser,
  (XML_StartElementHandler)startElement, (XML_EndElementHandler)endElement);
   
  do {
      int len = (int)fread(buf, 1, sizeof(buf), traceFile);
      done = len < sizeof(buf);
      if (XML_Parse(parser, buf, len, done) == XML_STATUS_ERROR) {
        crash(ERR, errno, "%s in %s at line %"XML_FMT_INT_MOD"u",
                XML_ErrorString(XML_GetErrorCode(parser)),
                filename,
                XML_GetCurrentLineNumber(parser));
              break;
      }
    } while (!done);
    list[0] = elements;
}
