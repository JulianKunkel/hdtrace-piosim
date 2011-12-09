/**
* \author Johann Weging
* This files contains all function for reading the *trc files.
*/

#include <stdio.h>
#include <string.h>

#include "readtrace.h"
#include "structures.h"
#include "error.h"
#include "readxml.h"

/*
 * Now it's getting a bit tricky. These macros generate the trace file 
 * parsing at compile time. The initial macro that is called, is the COMMAND
 * macro. The COMMAND macro takes two arguments. The first one is the name
 * of the MPI-command inside the trace file. The second parameter are the 
 * arguments of the MPI-function stored in the trace file. 
 * 
 * Every parameter is a macro which defines the name of the value and its
 * type. For example PARSEINT(cid). PARSEINT is a macro which calls the PARSE
 * macro with the parsing function atoi(). The variable name is cid which is
 * the communicator id. 
 * 
 * Here is a example of the generated code for a MPI-Barrier:
 *
 * if ( strcmp(name, "Barrier") == 0 )
 * {
 *  //allocate the new element and set its type
 *	struct Element* newElement = calloc(1,sizeof(struct Element)); 
 *	newElement->type = MpiBarrier; 
 *  
 *  //store the union in a temporary variable for better access
 *	struct  MpiBarrier* tmp = & newElement->u.mpiBarrier; 
 *  // set the source code line to -1 if there is one stored in the trace file
 *  the -1 is overwritten
 *  tmp->cLine = -1; 
 *
 *  // iterate over the attributes of the xml node
 *	for(int i=0; attributes[i]; i+=2)
 *	{
 *		const char * value = attributes[i+1]; 
 *		
 *    //check for the attributes defined as content in the COMMAND macro
 *		if(strcmp(attributes[i], "cid") == 0)
 *		{ 
 *			tmp->cid = atoi(value);
 *    }
 *
 *		if (strcmp(attributes[i], "cLine") == 0)
 *    {
 *			tmp->cLine = atoi(value);
 *    }
 *    
 *    //read the start and end timestamps of the command 
 *    if(strcmp(attributes[i], "time") == 0)
 *			newElement->start = atof(value);
 *    
 *    if(strcmp(attributes[i], "end") == 0) 
 *    newElement->end = atof(value); 
 *
 *    }
 *    //if the command was nested there is no cLine 
 *    if(isNested >1) tmp->cLine = -1;
 *    
 *    //FIXME use prepend instad of append to save time 
 *    //prepend the command to the elment list
 *    elements = g_slist_append(elements, (struct Element*) newElement);
 *  }
 *
 *
 */
#define PARSE(varName, parseFunc) \
	else if (strcmp(attributes[i], #varName) == 0) { \
  tmp->varName = parseFunc; \
}

#define PARSEINT(varName) PARSE(varName, atoi(value))

#define COMMAND(TYPE, content)  \
 if ( strcmp(name, #TYPE) == 0 ){ \
\
	GSList* elements = userData->elements;\
  struct Element* newElement = calloc(1,sizeof(struct Element));\
   newElement->type = Mpi##TYPE; \
   struct Mpi##TYPE * tmp = & newElement->u.mpi##TYPE; \
   tmp->cLine = -1; \
   for(int i=0; attributes[i]; i+=2){ \
     const gchar * value = attributes[i+1]; \
     if(strcmp(attributes[i], "time") == 0) newElement->start = atof(value);\
     content \
		 else if(strcmp(attributes[i], "end") == 0) newElement->end = atof(value); \
		 else if (strcmp(attributes[i], "cLine") == 0) newElement->cLine = atoi(value);\
  } \
  if(isNested >1) tmp->cLine = -1;\
  userData->elements = g_slist_append(elements, (gpointer) newElement);\
}

void XMLCALL
start_trace_element(struct UserDataTrace* userData, const char *name, 
		const char** attributes)
{
	
	int isNested = userData->isNested;
	int depth = userData->depth;

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
      )
      COMMAND(Comm_create,
        PARSEINT(cid)
        
        PARSEINT(newcid)
      )
      COMMAND(Comm_free,
        PARSEINT(cid)
        
      )
      COMMAND(Finalize,
      )
      COMMAND(Recv,
        PARSEINT(cid)
        PARSEINT(fromTag)
        PARSEINT(fromRank)
      )
      COMMAND(Send,
        PARSEINT(cid)
        PARSEINT(toTag)
        PARSEINT(toRank)
        PARSEINT(size)
        PARSEINT(count)
        PARSEINT(tid)
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
      ) 
    }
    
  }
  depth++;
}

void XMLCALL
end_trace_element(struct UserDataTrace *userData, const char* name)
{
	int isNested = userData->isNested;
	int depth = userData->depth;
  if(strcmp(name, "Nested") == 0)
  {
    isNested = 0;
  }
  
  depth--;
}

void read_trace(GSList** list, char * filename)
{


  XML_Parser parser = XML_ParserCreate(NULL);
  char buf[BUFSIZ];
  FILE* traceFile;
  int done;
	
	struct UserDataTrace  userData[1];

	userData->isNested = 0;
	userData->depth = 0;
	userData->elements = NULL;

  if((traceFile = fopen(filename, "r")) == NULL)
  {
    error(SYS_ERR, errno, "%s", filename);
  }
  
  XML_SetUserData(parser, userData);
  XML_SetElementHandler(parser,
  (XML_StartElementHandler) start_trace_element,
	(XML_EndElementHandler) end_trace_element);
   
  do {
      unsigned int len = (int)fread(buf, 1, sizeof(buf), traceFile);
      done = len < sizeof(buf);
      if (XML_Parse(parser, buf, len, done) == XML_STATUS_ERROR) {
        error(ERR, errno, "%s in %s at line %"XML_FMT_INT_MOD"u",
                XML_ErrorString(XML_GetErrorCode(parser)),
                filename,
                XML_GetCurrentLineNumber(parser));
              break;
      }
    } while (!done);

		printf("type %i\n", g_slist_length(userData->elements));
    list[0] = userData->elements;
}
