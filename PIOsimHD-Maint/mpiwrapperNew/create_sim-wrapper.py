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

beforeMpi = {
  "Abort" : "before_Abort(v1, v2)",
}

# the right place to call hdLogElement and hdLogAttribute...
afterMpi = {
}

afterLog = {
  "Init" : "after_Init(v1, v2)",
  "Finalize" : "after_Finalize()",
}



# maps function basename to the list (format_string, arguments)
logAttributes = {
  "Send" : ("size='%lld' toRank='%d' tag='%d' comm='%s'", 
            "getTypeSize(v2, v3), v4, v5, getCommName(v6)")


}



noLog = ["Abort", "Init"]


for f in funcs:
  if len(f) > 5 :
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
    argString = argString.rstrip(", ")
    callString = callString.rstrip(", ")

    print "int MPI_" + fkt + "(" + argString + "){"
    print "  int ret;"
    print

    if fkt in beforeMpi:
      print '  ' + beforeMpi[fkt] + ';'

    if not fkt in noLog:
      print '  hdLogStateStart(tracefile);'
      print

    if fkt in beforeLog:
      print '  ' + beforeLog[fkt] + ';'

    print "  ret = PMPI_" + fkt + "(" + callString + ");"
    print

    if fkt in afterMpi:
      print '  ' + afterMpi[fkt] + ';'

    if not fkt in noLog:
      #print '  hdLogElement(tracefile, "int", "fuenf=\'%d\'", 5);'
      #print '  hdLogAttributes(tracefile, "fuenf=\'%d\'", 5);'
      if fkt in logAttributes:
        print '  hdLogAttributes(tracefile, "' + logAttributes[fkt][0] + '", ' + logAttributes[fkt][1] + ');'
      print '  hdLogStateEnd(tracefile, "' + fkt + '", "");'
      print

    if fkt in afterLog:
      print '  ' + afterLog[fkt] + ';'

    print "  return ret;"
    print "}"
    print
