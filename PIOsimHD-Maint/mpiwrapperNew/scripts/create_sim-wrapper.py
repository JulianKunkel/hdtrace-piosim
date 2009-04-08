#!/usr/bin/env python
# encoding: utf-8

"""
/*
* Copyright (c) 2008 Julian M. Kunkel
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
* OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
*/
"""

"""
This script uses the configuration from wrapper_conf to produce C functions 
which can then be compiled to an MPI wrapper. The used variables from wrapper_conf 
are:

wrapper_conf.noLog is a list of functions which are not logged. 

wrapper_conf.beforeMpi is a dictionary that maps the function name to a string that is 
    inserted in the C function before the call to the real MPI function.

wrapper_conf.afterMpi is a dictionary that maps the function name to a string. This string
    is inserted in the C function after the call to thre real MPI function.

wrapper_conf.afterLog is similar to afterMpi, but the content is inserted after the logging
    of the call is complete

wrapper_conf.logAttributes contains the information about the names and values of the logged
    attributes for every function call.

Illustration: The produced C functions have the following form:
--------------------------------------------------------------------------------
int MPI_<Function>(<type 1> v1, <type 2> v2, ..., <type n> vn)
{
    <Begin logging using hdT_StateStart, unless <Function> is in wrapper_conf.noLog>

    <Insert code from wrapper_conf.beforeMpi>

    PMPI_<Function>(v1, ..., vn)

    <log Attributes using wrapper_conf.logAttributes>

    <Insert code from wrapper_conf.afterMpi>

    <End logging using hdT_StateStart, unless <Function> is in wrapper_conf.noLog>

    <Insert code from wrapper_conf.afterLog>
}
--------------------------------------------------------------------------------
"""




import re
import sys
from wrapper_conf import noLog, beforeMpi, afterMpi, afterLog, logAttributes

if len(sys.argv) != 2:
  print "usage: %s <function_declarations.h>"
  print "\tthe file <function_declarations.h> must contain c-style function"
  print "\tdeclarations."
  print
  sys.exit()

funcs = open(sys.argv[1]).readlines()

# print all function definitions
for i in xrange(0, len(funcs)):
  f = funcs[i]

  #ignore comments
  if len(f) >= 1 and f[0] == "/":  
    continue
  if len(f) > 5 :
    regex = re.match("int MPI_([^(]*)\(([^)]*)", f)
    if not regex:
      sys.stderr.write("[WARNING] in file '%s', line %d:\n" % (sys.argv[1], i))
      sys.stderr.write("\tline ignored.\n")
      sys.stderr.write(f + "\n")
      continue

    fkt = regex.group(1)
        
    args = regex.group(2).split(",")
    argString = ""
    callString = ""

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
