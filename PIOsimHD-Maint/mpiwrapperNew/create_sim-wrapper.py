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
"Allreduce" : "Allreduce(v3, v4,v6)",
"Barrier" : "Barrier(v1)",
"Bcast" : "Bcast(v2,v3,v4,v5)",
"Bsend" : "Bsend(v2,v3,v4,v5,v6)", 
"File_read" : "File_read(v1,v3,v4)",
"File_read_at" : "File_read_at(v1,v2, v4,v5)",
"File_write" : "File_write(v1,v3,v4)",
"File_write_all" : "File_write_all(v1,v3,v4)",
"File_write_at" : "File_write_at(v1,v2, v4,v5)",
"File_write_at_all" : "File_write_at_all(v1,v2, v4,v5)",
"Finalize" : "priorFinalize()",
"Recv" : "Receive(v2,v3,v4,v5,v6)", 
"Reduce" : "Reduce(v3,v4,v6,v7)",
"Send" : "Send(v2,v3,v4,v5,v6)", 
"Ssend" : "Ssend(v2,v3,v4,v5,v6)", 
"Sendrecv" : "Sendrecv(v2 , v3 , v4, v5, v9, v10, v11)",
#"Isend" : "Isend(v2,v3,v4,v5,v6)", 
"Sendrecv_replace" : "Sendrecv_replace(v2, v3, v4, v5, v6, v7, v8)",
"Gather" : "Gather(v2, v3, v5, v6, v7, v8)",
"Gatherv" : "Gatherv(v2, v3, v5, v6, v7, v8, v9)",
"Scatter" : "Scatter(v2, v3, v5, v6, v7, v8)",
"Scatterv" : "Scatterv(v2, v4, v6, v7, v8, v9)", 
"Allgather" : "Allgather(v2, v3, v5, v6, v7)",
"Allgatherv" : "Allgatherv(v2, v3, v5, v7, v8)",
"Alltoall" : "Alltoall(v2, v3, v5, v6, v7)",
"Alltoallv" : "Alltoallv(v2, v4, v6, v8, v9)", 
"Reduce_scatter" : "Reduce_scatter(v3, v4, v5, v6)",
"Scan" : "Scan(v3, v4, v5, v6)",
"Exscan" : "Exscan(v3, v4, v5, v6)",
"Abort" : "Abort(v1, v2)", 

"File_close" : "File_close(v1)",
"File_delete" : "File_delete(v1)",
"File_set_size" : "File_set_size(v1, v2)",
"File_preallocate" : "File_preallocate(v1, v2)",
"File_set_info" : "File_set_info(v1, v2)",
"File_read_at_all" : "File_read_at_all(v1, v2, v4, v5)",

"File_set_atomicity" : "File_set_atomicity(v1, v2)",
"File_read_shared" : "File_read_shared(v1, v3, v4)", 
"File_write_shared" : "File_write_shared(v1, v3, v4)", 

"File_read_ordered" : "File_read_ordered(v1, v3, v4)",
"File_write_ordered" : "File_write_ordered(v1, v3, v4)",
"File_seek_shared" : "File_seek_shared(v1, v2, v3)", 

"Isend" : "Isend(v2, v3, v4, v5, v6, v7)", 
"Waitall" : "Waitall(v1, v2)",
"Wait" : "Wait(v1)",
"Waitany" : "Waitany(v1, v2)",
"Waitsome" : "Waitsome(v1, v2)",
"Iprobe" : "Iprobe(v1, v2, v3)",
"Irecv" : "Irecv(v2, v3, v4, v5, v6, v7)",
"Ibsend" : "Ibsend(v2, v3, v4, v5, v6, v7)", 
"Issend" : "Issend(v2, v3, v4, v5, v6, v7)", 
"Irsend" : "Irsend(v2, v3, v4, v5, v6, v7)", 

"File_iread" : "File_iread(v1, v3, v4, v5)",
"File_iread_at" : "File_iread_at(v1, v2, v4, v5, v6)", 
"File_iwrite" : "File_iwrite(v1, v3, v4, v5)", 
"File_iwrite_at" : "File_iwrite_at(v1, v2, v4, v5, v6)", 

"File_read_at_all_begin" : "File_read_all_begin(v1, v2, v4, v5)",
"File_read_at_all_end" : "end_split(v1)",

"File_read_all_begin" : "File_read_all_begin(v1, v3, v4)",
"File_read_all_end" : "end_split(v1)",

"File_write_at_all_begin" : "File_write_at_all_begin(v1, v2, v4, v5)", 
"File_write_at_all_end" : "end_split(v1)",

"File_write_all_begin" : "File_write_all_begin(v1, v3, v4)",
"File_write_all_end" : "end_split(v1)",

"File_read_ordered_begin" : "File_read_ordered_begin(v1, v3, v4)", 
"File_read_ordered_end" : "end_split(v1)",

"File_write_ordered_begin" : "File_write_ordered_begin(v1, v3, v4)", 
"File_write_ordered_end" : "end_split(v1)",
}

endFuncs   = {
"File_open" : "File_open(v1,v2,v3,v4,v5,ret)",
"File_get_size" : "File_get_size(v1, v2)", 
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
      print '  if(trace_all_functions) {'
      print '  log("<' + fkt + " time='%f' />\\n\", MPI_Wtime() - startTime); }"
    
    print "  ret = PMPI_" + fkt + "(" + callString.rstrip(", ") + ");"
    
    if fkt in endFuncs:
      print "  w_" + endFuncs[fkt] + ";\n"      
    if fkt in noTimerEndFuncs:
      print "  w_" + noTimerEndFuncs[fkt] + ";\n"      

    if fkt in startFuncs or fkt in endFuncs and fkt != "Finalize":
        print "  w_createNewtimeStamp();\n"
      
    print "  return ret;"
    print "}"
