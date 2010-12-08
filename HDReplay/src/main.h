/**
  \author Johann Weging
  
  \brief The main function.
  
  No serious work is done here, it only calls the important functions.
  
*/

#ifndef _MAIN_H_
#define _MAIN_H_

#include <stdio.h>
#include <stdlib.h>

#include "mpi.h"
#include "readtrace.h"
#include "readproject.h"
#include "error.h"
#include "init.h"
#include "glib.h"

/**
  \brief Mian function.
*/
int main(int, char**);
void cleanUp(GSList* traceFile, GSList* comms);
void freeComms(gpointer data);

#endif
