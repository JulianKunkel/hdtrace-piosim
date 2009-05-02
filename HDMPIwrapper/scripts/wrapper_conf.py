#!/usr/bin/env python
# encoding: utf-8

"""
This file defines the variables noLog, beforeMpi, afterMpi, afterLog and 
logAttributes. These variables are used by create_sim-wrapper.py to generate
C functions.

noLog is a list of functions which are not logged. 

beforeMpi is a dictionary that maps the function name to a string that is 
    inserted in the C function before the call to the real MPI function.

afterMpi is a dictionary that maps the function name to a string. This string
    is inserted in the C function after the call to thre real MPI function.

afterLog is similar to afterMpi, but the content is inserted after the logging
    of the call is complete

logAttributes contains the information about the names and values of the logged
    attributes for every function call.

Further information is documented for each variable below.

Author: Paul Müller <pmueller@ix.urz.uni-heidelberg.de>
"""



##############################################################################
# """                                                                        #
# The following section contains commonly used code parts which are inserted #
# in the MPI_* wrapper functions.                                            #
# """                                                                        #
##############################################################################
def info_elements(arg):
  return """
  {
    int nkeys, i;
    char key[MPI_MAX_INFO_KEY];
    int MAX_INFO_VALUE_LEN = 1024;
    char value[MAX_INFO_VALUE_LEN];
    int flag;
    if(trace_file_info && !((___ARG___) == MPI_INFO_NULL))
    {
      PMPI_Info_get_nkeys((___ARG___), &nkeys);
      for(i = 0; i < nkeys; ++i)
      {
        PMPI_Info_get_nthkey((___ARG___), i, key);
        PMPI_Info_get((___ARG___), key, MAX_INFO_VALUE_LEN - 1, value, &flag);
        if(flag)
          hdT_logElement(tracefile, "Info", "key='%s' value='%s'", key, value);
        else
          hdT_logElement(tracefile, "EmptyInfo", "key='%s'", key);
      }
    }
  }
  """.replace("___ARG___", arg)

wait_elements = """
  {
    int i;
    for(i = 0; i < v1; ++i)
    {
      hdT_logElement(tracefile, "For", "rid='%d'", getRequestId(v2[i]));
    }
  }
"""

split_end_element = """
  {
    hdT_logElement(tracefile, "For", "rid='%d'", getRequestIdForSplit(v1));
  }
"""



####################################################################################
# """                                                                              #
# The following dictionary maps the MPI function name (without the "MPI_" prefix)  #
# to a string. This string is inserted in the MPI Wrapper function, before the     #
# corresponding PMPI call.                                                         #
#                                                                                  #
# The main purpose is to log information about the function by calling             #
# hdT_logElement(...)                                                              #
# """                                                                              #
####################################################################################
beforeMpi = {
  "Abort" : """hdT_logStateStart(tracefile, "Abort"); 
               hdT_logAttributes(tracefile, "cid='%d'", getCommId(v1)); 
               hdT_logStateEnd(tracefile); before_Abort()""",
  "File_delete" : info_elements("v2"),

  "File_set_info" : info_elements("v2"),

  "File_set_view" : info_elements("v6"),

  "File_read_all_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write_all_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_read_ordered_begin" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write_ordered_begin" : "  long long int byte_offset = getByteOffset(v1);",
  
  "File_read" : "  long long int byte_offset = getByteOffset(v1);",
  "File_read_all" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write" : "  long long int byte_offset = getByteOffset(v1);",
  "File_write_all" : "  long long int byte_offset = getByteOffset(v1);",
  "File_iread" : "  long long int byte_offset = getByteOffset(v1);",
  "File_iwrite" : "  long long int byte_offset = getByteOffset(v1);",

  "Wait": """hdT_logElement(tracefile, "For", "rid='%d'", getRequestId(*v1));""",
  "Waitany" : wait_elements,
  "Waitsome" : wait_elements,
  "Waitall" : wait_elements,

  "Test" : """hdT_logElement(tracefile, "For", "rid='%d'", getRequestId(*v1));""",
  "Testall" : wait_elements,
  "Testany" : wait_elements,
  "Testsome" : wait_elements,

  "File_close" : """
  gint pre_close_id = getFileId(*v1);
  removeFileHandle(*v1);
""",

  "Type_free" : """gint old_tid = getTypeId(*v1); removeType(*v1)""",

  "Comm_free" : """gint old_cid = getCommId(*v1); removeComm(*v1)""",
}



####################################################################################
# """                                                                              #
# The following dictionary maps the MPI function name (without the "MPI_" prefix)  #
# to a string. This string is inserted in the MPI Wrapper function, after the      #
# corresponding PMPI call.                                                         #
#                                                                                  #
# The main purpose is to log information about the function by calling             #
# hdT_logElement(...)                                                              #
# """                                                                              #
####################################################################################
afterMpi = {
  "File_open" : """
  {
    MPI_Offset fileSize;
    PMPI_File_get_size(*v5, &fileSize);
  }
""" + info_elements("v4"),

  "File_read_all_begin" : """
      hdT_logElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' tid='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,

  "File_read_all_end" : split_end_element,
  "File_read_at_all_end" : split_end_element,
  "File_write_at_all_end" : split_end_element,

  "File_write_all_begin" : """
      hdT_logElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' tid='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,

  "File_write_all_end" : split_end_element,

  "File_read_ordered_begin" : """
      hdT_logElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' tid='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,

  "File_read_ordered_end" : split_end_element,

  "File_write_ordered_begin" : """
      hdT_logElement(tracefile, "Data", "offset='%lld' size='%lld' count='%d' tid='%d'", 
      byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4));
  """,

  "File_write_ordered_end" : split_end_element,

  "Pcontrol" : """
  {
    if(v1 == 0) {
      hdT_disableTrace(tracefile);
    }
    else if(v1 == 1) {
      hdT_enableTrace(tracefile);
      hdT_setForceFlush(tracefile, 0);
    }
    else {
      hdT_enableTrace(tracefile);
      hdT_setForceFlush(tracefile, 1);
    }
  }
""",

  "Alltoallv" : """
  {
    int size, i;
    MPI_Comm_size(v9, &size);
    for(i = 0; i < size; ++i)
    {
      hdT_logElement(tracefile, "Send", "rank='%d' size='%lld' count='%d' tid='%d'",
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
      hdT_logElement(tracefile, "Recv", "rank='%d' size='%lld' count='%d' tid='%d'",
                   getWorldRank(i, v6), getTypeSize(v3[i], v4), v3[i], v4);
    }             
  }
""",

#    "Init_thread" : """
#    {
#      if(v3 != MPI_THREAD_SINGLE )
#      {
#        printDebugMessage("Init_thread: multithreading currently not supported by HDTrace\\n");
#        return -1;
#      }
#    }
# """,


}


####################################################################################
# """                                                                              #
# The following dictionary maps the MPI function name (without the "MPI_" prefix)  #
# to a string. This string is inserted in the MPI Wrapper function, after the      #
# corresponding PMPI call and after the logging of this function is completed.     #
# """                                                                              #
####################################################################################
afterLog = {
  "Init" : "after_Init(v1, v2); hdT_logStateStart(tracefile, \"Init\"); hdT_logStateEnd(tracefile)",

  "Init_thread" : "after_Init(v1, v2); hdT_logStateStart(tracefile, \"Init_thread\"); hdT_logStateEnd(tracefile)",

  "Finalize" : "after_Finalize()",
}


####################################################################################
# """                                                                              #
# Attribute - value pairs, which are shared by all send / isend - style functions. #
# """                                                                              #
####################################################################################
send_attributes = ("size='%lld' count='%d' tid='%d' toRank='%d' toTag='%d' cid='%d'", 
                   "getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6)")
isend_attributes = ("size='%lld' count='%d' tid='%d' toRank='%d' toTag='%d' cid='%d' rid='%d'", 
                    "getTypeSize(v2, v3), v2, getTypeId(v3), getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7)")


###################################################################################################
# """                                                                                             #
# logAttributes is a dictionary which maps the MPI function name (without 'MPI_' prefix)          #
# to a tuple (<format string>, <format parameters>, <tag name>).                                  #
#                                                                                                 #
# <format string> and <format parameters> are passed to a printf-like function, similar to        #
#                                                                                                 #
#     printf(<format string>, <format parameters>);                                               #
#                                                                                                 #
# <format string> should contain expressions of the form                                          #
#                                                                                                 #
#     'name="%d" name2="%d"...'                                                                   #
#                                                                                                 #
# (single or double quotes must be used around the value)                                         #
# and <format parameters> a comma separated list of c expressions. The arguments that are passed  #
# to the MPI function can be accessed via the variables v1, v2... . All functions that are        #
# declared in or included by HDTraceMPIWrapper.src.c may be called.                               #
#                                                                                                 #
# The attributes are logged after the call to the PMPI_ function. This must be considered, when   #
# MPI changes the values of the parameters.                                                       #
#                                                                                                 #
# <tag name> (optional) is the name which is used in the xml file to log the MPI function.        #
# If <tag name> is omitted, the name of the MPI function without "MPI_" prefix is used.           #
#                                                                                                 #
# Example: the entry                                                                              #
#                                                                                                 #
#     "Barrier" : ("cid='%d'", "getCommId(v1)", "BARR")                                          #
#                                                                                                 #
# may produce the output                                                                          #
#                                                                                                 #
#     <BARR cid='0' time='0.230000' end='0.240000' />                                            #
#                                                                                                 #
# in the xml log. The time=... and end=... attributes are added automatically by the              #
# log writer library.                                                                             #
#                                                                                                 #
# It is discouraged to use custom tag names without a good reason.                                #
# """                                                                                             #
###################################################################################################
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

  "Bcast" : ("size='%lld' rootRank='%d' cid='%d' count='%d' tid='%d'", 
             "getTypeSize(v2, v3), getWorldRank(v4, v5), getCommId(v5), v2, getTypeId(v3)"),

  "Gather" : ("size='%lld' recvSize='%lld' root='%d' cid='%d' count='%d' tid='%d' recvCount='%d' recvtid='%d'",
              "getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6)" ),

  "Gatherv" : ("size='%lld' root='%d' cid='%d' count='%d' tid='%d'",
               "getTypeSize(v2, v3), getWorldRank(v8, v9), getCommId(v9), v2, getTypeId(v3)"), 
  
  "Scatter" : ("size='%lld' recvSize='%lld' root='%d' cid='%d' count='%d' tid='%d' recvCount'%d' recvtid='%d'",
               "getTypeSize(v2, v3), getTypeSize(v5, v6), getWorldRank(v7, v8), getCommId(v8), v2, getTypeId(v3), v5, getTypeId(v6)"),

  "Scatterv" : ("recvSize='%lld' root='%d' cid='%d' recvCount='%d' recvtid='%d'",
                "getTypeSize(v6, v7), getWorldRank(v8, v9), getCommId(v9), v6, getTypeId(v7)"),

  "Allgather" : ("size='%lld' recvSize='%lld' cid='%d' count='%d' tid='%d' recvCount='%d' recvtid='%d'",
                 "getTypeSize(v2, v3), getTypeSize(v5, v6), getCommId(v7), v2, getTypeId(v3), v5, getTypeId(v6)"),

  "Allgatherv" : ("size='%lld' cid='%d' count='%d' tid='%d'",
                  "getTypeSize(v2, v3), getCommId(v8), v2, getTypeId(v3)"),
  
  "Alltoall" : ("size='%lld' cid='%d' count='%d' tid='%d'",
                "getTypeSize(v2, v3), v7, v2, getTypeId(v3)"),

  "Alltoallv" : ("cid='%d'", 
                 "getCommId(v9)"), 

  "Reduce" : ("size='%lld' rootRank='%d' cid='%d' count='%d' tid='%d'",
              "getTypeSize(v3, v4), getWorldRank(v6, v7), getCommId(v7), v3, getTypeId(v4)"),

  "Reduce_scatter" : ("cid='%d'", 
                      "getCommId(v6)"),

  "Scan" : ("size='%lld' cid='%d' count='%d' tid='%d'",
            "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "Exscan" : ("size='%lld' cid='%d' count='%d' tid='%d'",
            "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "Recv" : ("fromRank='%d' fromTag='%d' cid='%d'", 
            "getWorldRank(v4, v6), v5, getCommId(v6)"),

  "Irecv" : ("fromRank='%d' fromTag='%d' cid='%d' rid='%d'", 
             "getWorldRank(v4, v6), v5, getCommId(v6), getRequestId(*v7)"),

  "Barrier" : ("cid='%d'", 
               "getCommId(v1)"),

  "Sendrecv" : ("size='%lld' toRank='%d' toTag='%d' fromRank='%d' fromTag='%d' cid='%d' count='%d' tid='%d'", 
                "getTypeSize(v2, v3), getWorldRank(v4, v11), v5, getWorldRank(v9, v11), v10, getCommId(v11), v2, getTypeId(v3)"),

  "Sendrecv_replace" : ("sendSize='%lld' toRank='%d' toTag='%d' fromRank='%d' fromTag='%d' cid='%d' count='%d' tid='%d'", 
                        "getTypeSize(v2, v3), getWorldRank(v4, v8), v5, getWorldRank(v6, v8), v7, getCommId(v8), v2, getTypeId(v3)"),

  "Allreduce" : ("size='%lld' cid='%d' count='%d' tid='%d'", 
                 "getTypeSize(v3, v4), getCommId(v6), v3, getTypeId(v4)"),

  "File_open" : ("cid='%d' name='%s' flags='%d' fid='%d'", 
                 "getCommId(v1), v2, v3, getFileIdEx(*v5, v2)"),

  "File_close" : ("fid='%d'", 
                  "pre_close_id"),

   
  "File_delete" : ("fid='%d' name='%s'", 
                   "getFileIdFromName(v1), v1"),

  "File_write" :        ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                         "getFileId(v1), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_write_all" :    ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                         "getFileId(v1), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_write_all_begin" : ("fid='%d' rid='%d'", 
                            "getFileId(v1), getRequestIdForSplit(v1)"),

  "File_write_all_end" : ("", 
                          "", 
                          "Wait"),

  "File_write_at" :     ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                         "getFileId(v1), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_write_at_all" : ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                         "getFileId(v1), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_write_at_all_begin" : ("fid='%d' rid='%d'", 
                               "getFileId(v1), getRequestIdForSplit(v1)"),
  "File_write_at_all_end" : ("", 
                             "",
                             "Wait"),

  "File_write_ordered" : ("fid='%d' size='%lld' count='%d' tid='%d' ",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_write_ordered_begin" : ("fid='%d' rid='%d'", 
                                "getFileId(v1), getRequestIdForSplit(v1)"),
  "File_write_ordered_end" : ("", 
                              "",
                              "Wait"),

  "File_write_shared" : ("fid='%d' size='%lld' count='%d' tid='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_read" :        ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                        "getFileId(v1), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_read_all" :    ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                        "getFileId(v1), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_read_all_begin" : ("fid='%d' rid='%d'", 
                           "getFileId(v1), getRequestIdForSplit(v1)"),

  "File_read_all_end" : ("", 
                         "", 
                         "Wait"),

  "File_read_at" :     ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                        "getFileId(v1), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_read_at_all" : ("fid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                        "getFileId(v1), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_read_at_all_begin" : ("fid='%d' rid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                              "getFileId(v1), getRequestIdForSplit(v1), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_read_at_all_end" : ("", 
                            "", 
                            "Wait"),

  "File_read_ordered" : ("fid='%d' size='%lld' count='%d' tid='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),
  
  "File_read_ordered_begin" : ("fid='%d' rid='%d'", 
                               "getFileId(v1), getRequestIdForSplit(v1)"),

  "File_read_ordered_end" : ("", 
                             "", 
                             "Wait"),

  "File_read_shared" : ("fid='%d' size='%lld' count='%d' tid='%d'",
                        "getFileId(v1), getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_iread" : ("fid='%d' rid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                  "getFileId(v1), getRequestId(*v5), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_iread_at" : ("fid='%d' rid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                     "getFileId(v1), getRequestId(*v6)"),

  "File_iread_shared" : ("fid='%d' rid='%d'", 
                         "getFileId(v1), getRequestId(*v5)"),

  "File_iwrite" : ("fid='%d' rid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                   "getFileId(v1), getRequestId(*v5), byte_offset, getTypeSize(v3, v4), v3, getTypeId(v4)"),

  "File_iwrite_at" : ("fid='%d' rid='%d' offset='%lld' size='%lld' count='%d' tid='%d'", 
                      "getFileId(v1), getRequestId(*v6), (long long int)v2, getTypeSize(v4, v5), v4, getTypeId(v5)"),

  "File_iwrite_shared" : ("fid='%d' rid='%d'", 
                          "getFileId(v1), getRequestId(*v5)"),

  "File_set_size" : ("fid='%d' size='%lld'", 
                     "getFileId(v1), (long long int)v2"),

  "File_preallocate" : ("fid='%d' size='%lld'", 
                        "getFileId(v1), (long long int)v2"),

  "File_get_size" : ("fid='%d' size='%lld'", 
                     "getFileId(v1), (long long int)*v2"),

  "File_set_info" : ("fid='%d'", 
                     "getFileId(v1)"),

  "File_set_atomicity" : ("fid='%d' flag='%d'", 
                          "getFileId(v1), v2"),
  
  "File_sync" : ("fid='%d'", 
                 "getFileId(v1)"),

  "File_seek_shared" : ("fid='%d' relative-offset='%lld' whence='%s' offset='%lld'",
                        "getFileId(v1), (long long int)v2, getWhenceString(v3), getByteOffset(v1)"),

  "File_seek" : ("fid='%d' relative-offset='%lld' whence='%s' offset='%lld'",
                        "getFileId(v1), (long long int)v2, getWhenceString(v3), getByteOffset(v1)"),

  "File_set_view" : ("fid='%d' offset='%lld' etid='%d' filetid='%d' representation='%s'", 
                     "getFileId(v1), (long long int)v2, getTypeId(v3), getTypeId(v4), v5"),

  "Iprobe" : ("source='%d' tag='%d' cid='%d'", 
              "getWorldRank(v1, v3), v2, getCommId(v3)"),

  "Type_vector" : ("oldTid='%d'", 
                   "getTypeId(v4)"),

  "hdT_Test_nested" : ("depth='%d'", 
                       "v1"),

  "Type_commit" : ("tid='%d'", 
                   "getTypeId(*v1)"),

  "Type_free" : ("tid='%d'", 
                 "old_tid"),

  "Comm_free" : ("cid='%d'",
                 "old_cid")
}


##################################################################################
# """                                                                            #
# No logging is performed for the functions in noLog.                            #
# Reason: the log context is created in Init and destroyed in Abort, so          #
# writing to it would be erroneous.                                              #
#                                                                                #
# Please note: A wrapper function is still created and if the function is listed #
# in beforeMpi or afterMpi, the corresponding code is inserted. Only the logging #
# is omitted.                                                                    #
# """                                                                            #
##################################################################################
noLog = ["Abort", 
         "Init"
         ]

