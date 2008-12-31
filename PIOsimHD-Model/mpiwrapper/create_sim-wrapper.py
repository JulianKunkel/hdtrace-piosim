#!/usr/bin/env python
# encoding: utf-8

#/*
# * Copyright (c) 2008 Julian M. Kunkel
# * All rights reserved.
# *
# * Redistribution and use in source and binary forms, with or without
# * modification, are permitted provided that the following conditions
# * are met:
# *
# * 1. Redistributions of source code must retain the above copyright
# *    notice, this list of conditions and the following disclaimer.
# * 2. Redistributions in binary form must reproduce the above copyright
# *    notice, this list of conditions and the following disclaimer in the
# *    documentation and/or other materials provided with the distribution.
# *
# * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
# * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
# * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
# * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
# * SUCH DAMAGE.
# */

import re

#inhalt = open('wrapper.src').read()

funcs = open('interesting_funcs.h').read().splitlines()

startFuncs = { 
"Finalize" : "priorFinalize()",
"Send" : "Send(v2,v3,v4,v5,v6)", 
"Recv" : "Receive(v2,v3,v4,v5,v6)", 
"Sendrecv" : "Sendrecv(v2 , v3 , v4, v5, v9, v10, v11)",
"Barrier" : "Barrier(v1)",
"Allreduce" : "Allreduce(v3, v4,v6)",
"Reduce" : "Reduce(v3,v4,v6,v7)",
"Bcast" : "Bcast(v2,v3,v4,v5)",
"File_write" : "File_write(v1,v3,v4)",
"File_read" : "File_read(v1,v3,v4)",
"File_write_at" : "File_write_at(v1,v2, v4,v5)",
"File_read_at" : "File_read_at(v1,v2, v4,v5)",
}

endFuncs   = {
"File_open" : "File_open(v1,v2,v3,v4,v5)",
}

noTimerEndFuncs   = { 
"Init" : "Init(v2)", 
"Finalize" : "Finalize()",
}

startEndFuncs = { }


#prepare start / end funcs out of startEndFuncs
for f in startEndFuncs:
  val = startEndFuncs[f]
  regex = re.match("([^(]*\()(.*)", val)
  prep = regex.group(1)
  past = regex.group(2)  
  startFuncs[f] = prep + "1," + past;
  endFuncs[f]   = prep + "0," + past;

for f in funcs:
  if len(f) > 5 :
    #int MPI_Send(void*, int, MPI_Datatype, int, int, MPI_Comm);
    regex = re.match("int MPI_([^(]*)\(([^)]*)", f)
    #print regex.group(1) + " " + regex.group(2)
    fkt = regex.group(1)
        
    args = regex.group(2).split(",")
    argString = ""
    callString = ""
    #print args
    if regex.group(2) == "void":
      argString = "void, "
      callString = ", "
    else:
      count = 1
      for arg in args:
          var = " v" + str(count)  + ", "
          argString += arg + var
          count = count +1
          callString += var
      
    print "int MPI_" + fkt + "(" + argString.rstrip(", ") + "){"
    print "  int ret;"
    
    if fkt in startFuncs:
      print "  w_timeStamp();\n"
      print "  w_" + startFuncs[fkt] + ";\n"      
    elif fkt in endFuncs:
      print "  w_timeStamp();\n"
    elif fkt in noTimerEndFuncs:
      print ""
    else:
      print "  tsprintf(\"" + fkt + "\")"
    
    print "  ret = PMPI_" + fkt + "(" + callString.rstrip(", ") + ");"
    
    if fkt in endFuncs:
      print "  w_" + endFuncs[fkt] + ";\n"      
    if fkt in noTimerEndFuncs:
      print "  w_" + noTimerEndFuncs[fkt] + ";\n"      

    if fkt in startFuncs:
      if fkt != "Finalize":
        print "  w_createNewtimeStamp();\n"
      
    print "  return ret;"
    print "}"
