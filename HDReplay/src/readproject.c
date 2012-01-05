#include "readproject.h"


	/*
	 *now its getting a bit tricky 
	 */
  #define PARSE(varName, parseFunc)\
	\
	if(strcmp(attributes[i], #varName) == 0)\
  {\
    tmp->varName = parseFunc;\
  }\
  
  #define PARSEINT(varName) PARSE(varName, atoi(value))

  #define PARSECHAR(varName)\
	PARSE (varName, g_strdup_printf("%s", value))
  
  #define COMMAND(TYPE, content)\
  \
  if( strcmp(name, #TYPE) == 0)\
  {\
		\
		struct DataType* dataType = (struct DataType*) malloc(sizeof(struct DataType));\
    dataType->type = Data##TYPE;\
		\
		struct Data##TYPE *tmp = &dataType->u.data##TYPE;\
    \
    for(int i = 0; attributes[i]; ++i)\
    {\
		\
			const char* value = attributes[i+1];\
      content\
    }\
		data->dataTypes = g_slist_append(data->dataTypes,(gpointer) dataType);\
  }\



XML_Parser parser;

/**
  The GSList represents the topology of the mpi program.  
  The indices of the list represents a rank of the program.
  The data field of the list contains the hostname of the node the rank was 
  executed with.
*/



/**
  \brief startElement is called every time the XML-parser finds a new
  element in the project file.
  
  \param[in] depth Indicates the depth of the XML-element.
  \param[in] name The name of the XML-element.
  \param[in] attributes A list of the attributes of a element.
*/

void
readproject(gchar** programName, gchar** hostname, GSList** comms, 
					GSList** fileList, GSList** dataTypes, int rank ,gchar* filename)
{
  parser = XML_ParserCreate(NULL);
  char buf[BUFSIZ];
  FILE* projectFile;
  int done;
    
  struct UserData* data = (struct UserData*) malloc(sizeof(struct UserData));  
  data->depth = 0;
  data->checkFlag = 0;
  data->rank = rank;
  data->comms = NULL;
  data->fileList = NULL;
  data->dataTypes = NULL;
	data->hostname = NULL;
  
  if((projectFile = fopen(filename, "r")) == NULL)
  {
    error(SYS_ERR, errno, "%s", filename);
  }
  
  XML_SetUserData(parser, data);
  XML_SetElementHandler(parser,
    (XML_StartElementHandler) start_element, (XML_EndElementHandler) end_element);
  XML_SetCharacterDataHandler(parser,
    (XML_CharacterDataHandler) char_handler);


  do {
      unsigned int len = (int)fread(buf, 1, sizeof(buf), projectFile);
      done = len < sizeof(buf);
      if (XML_Parse(parser, buf, len, done) == XML_STATUS_ERROR) {
        error(ERR, HD_XML_PARSE, "%s in %s at line %"XML_FMT_INT_MOD"u",
                XML_ErrorString(XML_GetErrorCode(parser)),
                filename,
                XML_GetCurrentLineNumber(parser));
              break;
      }
    } while (!done);
    
  XML_ParserFree(parser);
  
  fileList[0] = data->fileList;
  comms[0] = data->comms;
  dataTypes[0] = data->dataTypes; 
	programName[0] = data->programName;
	hostname[0] = data->hostname;
	
}


void XMLCALL
start_element(struct UserData* data, const char *name, const char** attributes)
{
	/*
	 *writes the program name into the user data 
	 */
  if(strcmp(name, "Application") == 0 && data->depth == 0)
  {
     data->programName = g_strdup_printf("%s", attributes[1]);
  }
	/*
	 *if the file list is found set new xml handler functions to parse
	 *  the file list
	 */
  else if(strcmp(name,"FileList") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser, (XML_StartElementHandler) read_file_list,
				(XML_EndElementHandler) end_element);
  }
	/*
	 *set the xml handler functions for the Topology
	 */
  else if(strcmp(name,"Topology") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) read_topology, (XML_EndElementHandler) end_element);
  }
	/*
	 *set the xml handler function for the communicator list 
	 */
  else if(strcmp(name, "CommunicatorList") == 0 && data->depth == 1)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) read_communicator, (XML_EndElementHandler) end_element);
  }
	/*
	 *set the xml handler function for the data types 
	 */
  else if(strcmp(name, "Datatypes") == 0 && data->depth ==1)
  {
    XML_SetElementHandler(parser, (XML_StartElementHandler) read_data_types,
			(XML_EndElementHandler) end_element);
  }
  data->depth++;
}


/**
 * @brief This function handles the end of a XML tag.  
 * 
 * This function handles the end of a XML tag. When the end of a tag is read
 * that manipulated the element handler the handler is set back to
 * start_element.
 *
 * @param data User data passed to the XML callback function.
 * @param name The name of the XML tag.
 */
void XMLCALL
end_element(struct UserData* data, const char* name)
{
	/*
	 *this resets the function handlers after a closing XML tags
	 */
  if(strcmp(name,"FileList") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser, (XML_StartElementHandler) start_element,
				(XML_EndElementHandler) end_element);
  }
  else if(strcmp(name,"Topology") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser, (XML_StartElementHandler) start_element,
				(XML_EndElementHandler) end_element);
  }
  else if(strcmp(name, "CommunicatorList") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser, (XML_StartElementHandler) start_element,
				(XML_EndElementHandler) end_element);
  }
  else if(strcmp(name, "Datatypes") == 0 && data->depth == 2)
  {
    XML_SetElementHandler(parser,
    (XML_StartElementHandler) start_element, (XML_EndElementHandler) end_element);
  }
  data->depth--;
}


void XMLCALL
read_topology(struct UserData* data, const char* name, const char** attributes)
{
	/*
	 * read the hostname
	 * the host name is read every time until the matching rank is found
	 * if the matching rank is found the checkFlag is set so the hostname 
	 * won't be overwritten any more, this is not bautifull but is needed
	 * because in the topology the rank depends on the hostname. 
	 * HDReplay reads the trace file with the same rank but needs the hostname
	 * for the trace file name.
	 *
	 */
  if(strcmp(name,"Node") == 0 && data->depth == 2 && data->checkFlag == 0)
  {
		if(data->hostname != NULL)
		{
			g_free((gpointer) data->hostname);
		}

    data->hostname = g_strdup_printf("%s", attributes[1]);
  }
	/*
   * read the rank(s) inside the hostname element
	 */
  else if
  (strcmp(name,"Node") == 0 && data->checkFlag == 0 && data->depth == 3)
  {
		/*
		 *if the rank is the same as the rank inside the project file 
		 *set the check flag, so the hostname won't be overwritten any more
		 */
    if(data->rank == atoi(attributes[1]))
    {
      data->checkFlag = 1;
    }
  }
  data->depth++;
}

/**
 * @brief This function parses the communicators in the project file.
 *
 * This function parses the communicators in the project file. The XML 
 * handler is set by the start_element function when parsing the
 * <CommunicatorList> tag.
 *
 * @param data The user data passed to the function.
 * @param name The name of the XML element.
 * @param attributes The attributes inside the XML element.
 *
 * @return 
 */
void XMLCALL
read_communicator(struct UserData* data, char* name, char** attributes)
{
  
  if(strcmp(name,"Communicator") == 0 && data->depth == 2)
  {
    struct Communicator* com;
    com = (struct Communicator*) malloc(sizeof(struct Communicator));
		
		/*
		 *read the name of the communicator
		 */
    com->name = g_strdup_printf("%s", attributes[1]);
		
		/*
		 *append the communicator to the communicator list
		 */
    data->comms = g_slist_append(data->comms, (gpointer) com);
  }
	/*
	 *read the ranks inside the communicator
	 */
  else if(strcmp(name,"Rank") == 0 && data->depth == 3)
  {
    struct Rank* comRank;
    comRank = (struct Rank*) malloc(sizeof(struct Rank));
		
		/*
		 *get the last communicator in the list 
		 */
    GSList* last;
    last =  g_slist_last(data->comms);
    
    struct Communicator* com;
    com = (struct Communicator*) last->data;
    com->ranks = NULL;
		
		/*
		 * iterate over the attributes in the rank element
		 */
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
		/*
		 *append the new rank to the communicator
		 */
    com->ranks = g_slist_append(com->ranks, (gpointer) comRank);
  }
  data->depth++;
}

/**
 * @brief This function reads the file list. 
 *
 * @param data User data passed to the XML callback.
 * @param name Name of the XML tag.
 * @param attributes Attributes inside the XML tag. 
 *
 * @return 
 */
void XMLCALL
read_file_list(struct UserData* data, const char* name, const char** attributes)
{
/*
 * if a file is found... THIS IS PARSA!!
 */
  if(strcmp(name, "File") == 0 && data->depth == 2)
  {
		struct FileList* file = (struct FileList*) malloc(sizeof(struct FileList));
		
		/*
		 *read the path of the file
		 */
    file->path= g_strdup_printf("%s", attributes[1]);
		
		/*
		 *append the the new file to the file list	
		 */
    data->fileList = g_slist_append(data->fileList, (gpointer) file);
  }
	
	/*
	 *set the checkFalg so the charhandler function will read the size
	 */
  else if(strcmp(name,"InitalSize") == 0 && data->depth == 3)
  {
    data->checkFlag = 2;
  }

	/*
	 *read the distribution, this seems to be the implementation of the filesystem ?!
	 */
  else if(strcmp(name, "Distribution") == 0 && data->depth == 3)
  {
    GSList* last;
    struct FileList* file;
    
    last = g_slist_last(data->fileList);
    file = (struct FileList*) last->data;
    
    file->implementation = g_strdup_printf("%s", attributes[1]);
  }
  data->depth++;
}

void XMLCALL
read_data_types(struct UserData* data, const char* name, const char** attributes)
{


COMMAND(NAMED,
				PARSECHAR(name)
				PARSEINT(id)
			)
}

/**
 * @brief This function parses the text between XML tags.
 *
 * This function is needed for the file list in the project file to read the
 * initial size of the file 
 *
 * @param data The user defined data passed to the callback function
 * @param string the String containing the file size (not '\0' escaped)
 * @param len the length of the string 
 *
 * @return 
 */
void XMLCALL
char_handler(struct UserData* data, const XML_Char* string, int len)
{
  if(data->checkFlag == 2)
  {
    GSList* last;
    struct FileList* file;
    
    last = g_slist_last(data->fileList);
    file = (struct FileList*) last->data;
    
		/*
		 *the XML_Char* is not \0 escaped so lets escape it
		 */
		gchar* initialSize = g_strndup((gchar*) string, (gsize) len);
    file->initialSize = atoi(initialSize);
		
		/*
		 *the generated string by g_strdup must be freed 
	   */
		g_free(initialSize);
		
		/*
		 *FIXME: if the order in the project file of the topology and the file list changes this will defiantly crash
		 * maybe... not sure \(o_O)/
		 */
    data->checkFlag = 0;

		/*
		 *TODO: read the chunck size
		 */
  }  
}



