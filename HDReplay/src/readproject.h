#ifndef _READPROJECT_H_
#define _READPROJECT_H_

#include <stdio.h>
#include <string.h>

#include "readxml.h"
#include "structures.h"
#include "error.h"
#include "constant.h"
#include "readxml.h"



struct UserData
{
   gchar* hostname;
   gchar* programName;
   GSList* comms;
   int checkFlag;
   int rank;
   int depth;
   GSList* fileList;
   GSList* dataTypes;
};
/**
 * @brief 
 *
 * @param programName
 * @param hostname
 * @param com
 * @param files
 * @param rank
 * @param filename
 */
void
readproject(gchar** programName, gchar** hostname, GSList** com,
  GSList** files, GSList **dataTypes , int rank, gchar* filename);


void XMLCALL
  read_topology(struct UserData* data, const char* name,
    const char** attributes);

void XMLCALL
  start_element(struct UserData* data, const char* name, 
    const char** attributes);
  
void XMLCALL
  read_file_list(struct UserData* data, const char*name,
    const char** attributes);
    
void XMLCALL
  read_communicator(struct UserData* data, char* name, char** attributes);
  
void XMLCALL
  read_data_types(struct UserData* data, const char* name, 
			const char** attributes);

void XMLCALL
  end_element(struct UserData* data, const char* name);

void XMLCALL
  char_handler(struct UserData* data, const XML_Char* string, int len);

#endif
