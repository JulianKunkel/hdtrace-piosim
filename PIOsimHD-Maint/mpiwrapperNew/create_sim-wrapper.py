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

funcs = open('interesting_funcs.h').read().splitlines()

beforeLog = {
}



def info_elements(arg):
  return """
  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    if(trace_file_info && !((___ARG___) == MPI_INFO_NULL))
    {
    PMPI_Info_get_nkeys((___ARG___), &nkeys);
    for(i = 0; i < nkeys; ++i)
    {
      MPI_Info_get_nthkey((___ARG___), i, key);
      hdT_LogElement(tracefile, "Info", "value='%s'", key);
    }
    }
  }
  """.replace("___ARG___", arg)

write_at_elements = """
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
                 (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5));
"""
write_elements = """
  {
    hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
                 (long long int)getByteOffset(v1), getTypeSize(v3, v4), v3, getTypeId(v4));
  }
"""
wait_elements = """
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(v2[i]));
    }
  }
"""
split_end_element = """
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestIdForSplit(v1));
  }
"""


beforeMpi = {
  "Abort" : "before_Abort(v1, v2)",
  "File_delete" : info_elements("v2"),
  "File_write_at" : write_at_elements,
  "File_read_at" : write_at_elements,
  "File_read_at_all" : write_at_elements,
  "File_write_at_all" : write_at_elements,
  "File_iread_at" : write_at_elements,
  "File_iwrite_at" : write_at_elements, 

  "File_write_ordered" : write_elements,
  
  "File_write" : write_elements,
  "File_write_all" : write_elements,
  "File_read" : write_elements,
  "File_read_all" : write_elements,
  "File_iread" : write_elements,
  "File_iwrite" : write_elements, 

  "File_set_info" : info_elements("v2"),

  "File_read_all_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write_all_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_read_ordered_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write_ordered_begin" : "  long long int byte_offset = getByteOffset(v1);",

  "Waitany" : wait_elements,
  "Waitsome" : wait_elements,
  "Waitall" : wait_elements,

  "Wait": """
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(*v1));
  }
""",

  "Test" : """
  {
    hdT_LogElement(tracefile, "For", "request='%d'", getRequestId(*v1));
  }
""",
  "Testall" : wait_elements,
  "Testany" : wait_elements,
  "Testsome" : wait_elements,

  "File_close" : """
  gint pre_close_id = getFileId(*v1);
  removeHandle(*v1);
""",
}


afterMpi = {
  "File_open" : """
  {
    MPI_Offset fileSize;
    PMPI_File_get_size(*v5, &fileSize);
//    hdT_LogInfo(tracefile, 
//	      "File_open name=\\"%s\\" comm=\\"%d\\" InitialSize=%lld id=%d flags=%d \\n", 
//	      v2, getCommId(v1), v3, (long long int)fileSize, getFileIdEx(*v5, v2));
  }
""" + info_elements("v4"),

  "File_read_all_begin" : """
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,

  "File_read_all_end" : split_end_element,
  "File_read_at_all_begin" : write_at_elements,
  "File_read_at_all_end" : split_end_element,

  "File_write_at_all_begin" : write_at_elements, 
  "File_write_at_all_end" : split_end_element,
  "File_write_all_begin" : """
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,
  "File_write_all_end" : split_end_element,

  "File_read_ordered_begin" : """
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,
  "File_read_ordered_end" : split_end_element,

  "File_write_ordered_begin" : """
      hdT_LogElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' datatype='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,
  "File_write_ordered_end" : split_end_element,

  "Pcontrol" : """
  {
    if(v1 == 0) {
      hdT_Enable(tracefile, 0);
    }
    else if(v1 == 1) {
      hdT_Enable(tracefile, 1);
      hdT_ForceFlush(tracefile, 0);
    }
    else {
      hdT_Enable(tracefile, 1);
      hdT_ForceFlush(tracefile, 1);
    }
  }
""",

  "Alltoallv" : """
  {
    int size, i;
    MPI_Comm_size(v9, &size);
    for(i = 0; i < size; ++i)
    {
      hdT_LogElement(tracefile, "Send", "rank='%d' size='%lld' count='%d' type='%d'",
                   getWorldRank(i, v9), getTypeSize(v2[i], v4), v2[i], getTypeId(v4));
    }
  }
""",

  "Reduce_scatter" : """
  {
    int size, i;
    MPI_Comm_size(v6, &size);
    for(i = 0; i < size; ++i)
    {
      hdT_LogElement(tracefile, "Recv", "rank='%d' size='%lld' count='%d' type='%d'",
                   getWorldRank(i, v6), getTypeSize(v3[i], v4), v3[i], v4);
    }             
  }
""",

  "Init_thread" : """
  {
    if(v3 != MPI_THREAD_SINGLE )
    {
      tsprintf("Init_thread: multithreading currently not supported by HDTrace");
      return -1;
    }
  }
"""
}

afterLog = {
  "Init" : "after_Init(v1, v2)",
  "Finalize" : "after_Finalize()",
}

send_attributes = ("size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d'", 
                   "getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6)")
isend_attributes = ("size='%lld' count='%d' type='%d' toRank='%d' tag='%d' comm='%d' request='%d'", 
                    "getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7)")

# maps function basename to the list (format_string, arguments, [tag name])
# [tag name] may be omitted; the name of the mpi function without the 'MPI_'
# prefix is then used
# expressions may not contain double quotes 

logAttributes = {
  "Send" :  send_attributes,
  "Bsend" : send_attributes, 
  "Ssend" : send_attributes,
  "Ssend" : send_attributes,
  "Rsend" : send_attributes,

  "Isend" :  isend_attributes,
  "Ibsend" : isend_attributes, 
  "Issend" : isend_attributes, 
  "Irsend" : isend_attributes, 

  "Bcast" : ("size='%lld' rootRank='%d' comm='%d' count='%d' type='%d'", 
             "getTypeSize(v2, v3), getWorldRank(v4, v5), getCommId(v5), v2, getTypeId(v3)"),

  "Gather" : ("size='%lld' recvSize='%lld' root='%d' comm='%d' count='%d' type='%d' recvCount='%d' recvType='%d'",
              "getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6)" ),

  "Gatherv" : ("size='%lld' root='%d' comm='%d' size='%d' type='%d'",
               "getTypeSize(v2, v3), getWorldRank(v8, v9), getCommId(v9), v2, getTypeId(v3)"), 
  
  "Scatter" : ("size='%lld' recvSize='%lld' root='%d' comm='%d' count='%d' type='%d' recvCount'%d' recvType='%d'",
               "getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6)"),

  "Scatterv" : ("recvSize='%lld' root='%d' comm='%d' recvCount='%d' recvType='%d'",
                "getTypeSize(v6, v7), getWorldRank(v8, v9), getCommId(v9), v6, getTypeId(v7)"),

  "Allgather" : ("size='%lld' recvSize='%lld' comm='%d' count='%d' type='%d' recvCount='%d' recvType='%d'",
                 "getTypeSize(v2, v3), getTypeSize(v5, v6), getCommId(v7), v2, getTypeId(v3), v5, getTypeId(v6)"),

  "Allgatherv" : ("size='%lld' comm='%d' count='%d' type='%d'",
                  "getTypeSize(v2, v3), getCommId(v8), v2, getTypeId(v3)"),
  
  "Alltoall" : ("size='%lld' comm='%d' count='%d' type='%d'",
                "getTypeSize(v2, v3), v7, v2, getTypeId(v3)"),

  "Alltoallv" : ("comm='%d'", "getCommId(v9)"), 

  "Reduce" : ("size='%lld' rootRank='%d' comm='%d' count='%d' type='%d'",
              "getTypeSize(v3, v4), getWorldRank(v6, v7), getCommId(v7), v3, getTypeId(v4)"),

  "Reduce_scatter" : ("comm='%d'", "getCommId(v6)"),

  "Scan" : ("size='%lld' comm='%d' count='%d' type='%d'",
            "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "Exscan" : ("size='%lld' comm='%d' count='%d' type='%d'",
            "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "Recv" : ("fromRank='%d' tag='%d' comm='%d'", "getWorldRank(v4, v6), v5, getCommId(v6)"),

  "Irecv" : ("fromRank='%d' tag='%d' comm='%d'", "getWorldRank(v4, v6), v5, getCommId(v6)"),

  "Barrier" : ("comm='%d'", "getCommId(v1)"),

  "Sendrecv" : ("size='%lld' toRank='%d' to-tag='%d' fromRank='%d' fromTag='%d' comm='%d'", 
                "getTypeSize(v2, v3), getWorldRank(v4, v11), v5, getWorldRank(v9, v11), v10, getCommId(v11)"),

  "Sendrecv_replace" : ("sendSize='%lld' toRank='%d' to-tag='%d' fromRank='%d' fromTag='%d' comm='%d'", 
                        "getTypeSize(v2, v3), getWorldRank(v4, v8), v5, getWorldRank(v6, v8), v7, getCommId(v8)"),

  "Allreduce" : ("size='%lld' comm='%d' count='%d' type='%d'", 
                 "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "File_open" : ("comm='%d' name='%s' flags='%d' file='%d'", 
                 "getCommId(v1), v2, v3, getFileIdEx(*v5, v2)"),

  "File_close" : ("file='%d'", 
                  "pre_close_id"),

   
  "File_delete" : ("file='%d'", 
                   "getFileIdFromName(v1)"),

  "File_write" :        ("file='%d'", "getFileId(v1)", "File_write"),

  "File_write_all" :    ("file='%d'", "getFileId(v1)", "File_write_all"),
  "File_write_all_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "File_write_all"),
  "File_write_all_end" : ("", "", "Wait"),

  "File_write_at" :     ("file='%d'", "getFileId(v1)", "File_write"),
  "File_write_at_all" : ("file='%d'", "getFileId(v1)", "File_write_all"),
  "File_write_at_all_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "File_write"),
  "File_write_at_all_end" : ("", "", "Wait"),

  "File_write_ordered" : ("file='%d' size='%lld' count='%d' type='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_write_ordered_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "FileWrite"),
  "File_write_ordered_end" : ("", "", "Wait"),

  "File_write_shared" : ("file='%d' size='%lld' count='%d' type='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_read" :        ("file='%d'", "getFileId(v1)", "FileRead"),
  "File_read_all" :    ("file='%d'", "getFileId(v1)", "FileRead"),
  "File_read_all_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "FileRead"),
  "File_read_all_end" : ("", "", "Wait"),
  "File_read_at" :     ("file='%d'", "getFileId(v1)", "FileRead"),
  "File_read_at_all" : ("file='%d'", "getFileId(v1)", "FileRead"),
  "File_read_at_all_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "FileRead"),
  "File_read_at_all_end" : ("", "", "Wait"),

  "File_read_ordered" : ("file='%d' size='%lld' count='%d' type='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),
  "File_read_ordered_begin" : ("file='%d' aid='%d'", "getFileId(v1), getRequestIdForSplit(v1)", "FileWrite"),
  "File_read_ordered_end" : ("", "", "Wait"),

  "File_read_shared" : ("file='%d' size='%lld' count='%d' type='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),


  "File_iread" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v5)", "FileIread"),
  "File_iread_at" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v6)", "FileIread"),
  "File_iread_shared" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v5)", "FileIread"),

  "File_iwrite" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v5)", "FileIwrite"),
  "File_iwrite_at" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v6)", "FileIwrite"),
  "File_iwrite_shared" : ("file='%d' request='%d'", "getFileId(v1), getRequestId(*v5)", "FileIwrite"),



  "File_set_size" : ("file='%d' size='%lld'", "getFileId(v1), (long long int)v2"),
  "File_preallocate" : ("file='%d' size='%lld'", "getFileId(v1), (long long int)v2"),
  "File_get_size" : ("file='%d' size='%lld'", "getFileId(v1), (long long int)*v2"),

  "File_set_info" : ("file='%d'", "getFileId(v1)"),

  "File_set_atomicity" : ("file='%d' flag='%d'", "getFileId(v1), v2"),
  
  "File_sync" : ("file='%d'", "getFileId(v1)"),

  "File_seek_shared" : ("file='%d' relative-offset='%lld' whence='%s' offset='%lld'",
                        "getFileId(v1), (long long int)v2, getWhenceString(v3), getByteOffset(v1)"),


  "Iprobe" : ("source='%d' tag='%d' comm='%d'", "getWorldRank(v1, v3), v2, getCommId(v3)"),

  "Type_vector" : ("from_type='%d'", "getTypeId(v4)"),

  "hdT_Test_nested" : ("depth='%d'", "v1"),

  "Type_commit" : ("type='%d'", "getTypeId(*v1)"),
}

noLog = ["Abort", "Init"]


for f in funcs:
  if len(f) > 5 :
    regex = re.match("int MPI_([^(]*)\(([^)]*)", f)
    #print regex.group(1) + " " + regex.group(2)
    if not regex:
      continue

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
        if arg.strip() == "...":
          argString += " ...,"
        else:
          var = " v" + str(count)  + ", "
          argString += arg + var
          count = count +1
          callString += var
    argString = argString.rstrip(", ")
    callString = callString.rstrip(", ")

    print "int MPI_" + fkt + "(" + argString + "){"
    print "  int ret;"
    print

    if fkt in beforeLog:
      print '  ' + beforeLog[fkt] + ';'

    if not fkt in noLog:
      logname = ""
      if fkt in logAttributes and len(logAttributes[fkt]) > 2:
        logname = logAttributes[fkt][2]
      else:
        logname = fkt
      print '  hdT_StateStart(tracefile, "%s");' % logname
      print

    if fkt in beforeMpi:
      print '  ' + beforeMpi[fkt] + ';'

    print "  ret = PMPI_" + fkt + "(" + callString + ");"
    print

    if fkt in afterMpi:
      print '  ' + afterMpi[fkt] + ';'

    if not fkt in noLog:
      if fkt in logAttributes:
        if logAttributes[fkt][1] == "":
          print '  hdT_LogAttributes(tracefile, "' + logAttributes[fkt][0] + '");'
        else:
          print '  hdT_LogAttributes(tracefile, "' + logAttributes[fkt][0] + '", ' + logAttributes[fkt][1] + ');'
      
      print '  hdT_StateEnd(tracefile);'
      print

    if fkt in afterLog:
      print '  ' + afterLog[fkt] + ';'

    print "  return ret;"
    print "}"
    print
