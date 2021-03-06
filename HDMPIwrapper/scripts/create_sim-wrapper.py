#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
    <Begin logging using hdT_logStateStart, unless <Function> is in wrapper_conf.noLog>

    <Insert code from wrapper_conf.beforeMpi>

    PMPI_<Function>(v1, ..., vn)

    <log Attributes using wrapper_conf.logAttributes>

    <Insert code from wrapper_conf.afterMpi>

    <End logging using hdT_logStateEnd, unless <Function> is in wrapper_conf.noLog>

    <Insert code from wrapper_conf.afterLog>
}
--------------------------------------------------------------------------------
"""




import re
import sys
import StringIO

from wrapper_conf import noLog, beforeMpi, afterMpi, afterLog, logAttributes, createFktHeaders

if len(sys.argv) != 5:
  print "usage: %s <function_declarations.h> <output_c_file> <output_header_file> <line_generator_header>"
  print "\tthe file <function_declarations.h> must contain c-style function"
  print "\tdeclarations."
  print
  sys.exit()

funcs = open(sys.argv[1]).readlines()

# open for append:
outputC = open(sys.argv[2], "a")
outputHeader = open(sys.argv[3] ,"a")

# Output header and scripts to incorporate line numbers into trace:
codeLocatorHeader = open(sys.argv[4] ,"w")
codeLocatorHeader.write("/* include this file to add source file and line to the trace */ \n#ifndef HDMPITRACER_CODELOCATOR_H_\n#define HDMPITRACER_CODELOCATOR_H_\n#if defined(__cplusplus)\n extern \"C\" {\n \n#endif \n\n")

# MPI function decls with file & name
extendedMPIDecl = StringIO.StringIO()

outputHeader.write("/* The following functions just log the attributes of the corresponding MPI calls */\n")

outputC.write("#ifdef ENABLE_FUNCTION_WRAPPER\n")

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

    #buffer which contains the normal MPI func implementation (without line & file)
    mpiFunc = StringIO.StringIO()
    eMpiFunc = StringIO.StringIO()
    internalFunc = StringIO.StringIO()
    
    createeMPIFunc = True
    
    # take MPI funcs into account which have any number of arguments ("..."), PControl for MPICH2
    if argString.find("...") >= 0:
      createeMPIFunc = False
      
    mpiFunc.write("int MPI_" + fkt + "(" + argString + "){\n")
    mpiFunc.write("  int ret;\n\n")
    
    if argString == "void":
      eMpiFuncDecl = "int eMPI_" + fkt + "(const char * file, unsigned int line)";
    else:
      eMpiFuncDecl = "int eMPI_" + fkt + "(" + argString + ", const char * file, unsigned int line)";
    eMpiFunc.write(eMpiFuncDecl + "{\n")
    eMpiFunc.write("  int ret;\n\n")

    # write extended MPI function declaration to header file
    if createeMPIFunc:
      extendedMPIDecl.write(eMpiFuncDecl + ";\n")
      comma=""
      if len(callString) > 1:
        comma=", "
      codeLocatorHeader.write("#define MPI_" + fkt + "(" + callString + ") eMPI_" + fkt + "(" + callString + comma + " __FILE__, __LINE__)\n")

    if not fkt in noLog:
      logname = ""
      if fkt in logAttributes and len(logAttributes[fkt]) > 2:
        logname = logAttributes[fkt][2]
      else:
        logname = fkt
      eMpiFunc.write('  hdMPI_threadLogStateStart("' + logname + '");\n\n')
      mpiFunc.write('  hdMPI_threadLogStateStart("' + logname + '");\n\n')

    if fkt in beforeMpi:
      eMpiFunc.write( '  ' + beforeMpi[fkt] + ';\n')
      mpiFunc.write( '  ' + beforeMpi[fkt] + ';\n')

    eMpiFunc.write("  ret = PMPI_" + fkt + "(" + callString + ");\n\n")
    mpiFunc.write("  ret = PMPI_" + fkt + "(" + callString + ");\n\n")

    if fkt in afterMpi:
      eMpiFunc.write( '  ' + afterMpi[fkt] + ';\n\n')
      mpiFunc.write( '  ' + afterMpi[fkt] + ';\n\n')

    # buffer for delayed writting to C file:
    delayedBuffer = StringIO.StringIO()
    if not fkt in noLog:
      fktName = "hdMPI_logInternalsMPI_" + fkt;
      if fkt in createFktHeaders:
          internalFunc.write("void " + fktName + "(" + argString + "){\n")

      # log attribute string
      logString = ""
      
      if fkt in logAttributes:
        if logAttributes[fkt][0] == "":
          pass
        elif logAttributes[fkt][1] == "":
          logString = '  hdMPI_threadLogAttributes("' + logAttributes[fkt][0] + '");\n'
        else:
          logString = '  hdMPI_threadLogAttributes("' + logAttributes[fkt][0] + '", ' + logAttributes[fkt][1] + ');\n'

      eMpiFunc.write("  hdMPI_threadLogAttributes(\"cFile='%s' cLine='%u'\", file, line);\n")

      # write indirect call:
      if fkt in createFktHeaders:
          eMpiFunc.write("  " + fktName + "(" + callString + ");\n")
          mpiFunc.write("  " + fktName + "(" + callString + ");\n")
          # generate header for fkt in header file:
          outputHeader.write("void " + fktName + "(" + argString + ");\n")
          # write function finalize down:
          internalFunc.write(logString +"\n")
          internalFunc.write("}\n\n")
      else:
          eMpiFunc.write(logString + "\n")
          mpiFunc.write(logString + "\n")
          

      eMpiFunc.write( '  hdMPI_threadLogStateEnd();\n')
      mpiFunc.write( '  hdMPI_threadLogStateEnd();\n')
      #outputHeader.write("#pragma weak EMPI_" + fkt + " = MPI_" + fkt + "\n")


    if fkt in afterLog:
      eMpiFunc.write( '  ' + afterLog[fkt] + ';\n')
      mpiFunc.write( '  ' + afterLog[fkt] + ';\n')

    eMpiFunc.write( "  return ret;\n}\n\n")
    mpiFunc.write( "  return ret;\n}\n\n")

    outputC.write(internalFunc.getvalue())
    outputC.write(mpiFunc.getvalue())
    
    if createeMPIFunc:
      outputC.write(eMpiFunc.getvalue())


outputC.write("#endif\n\n")

outputC.close()

codeLocatorHeader.write("\n\n /* Extended MPI header, calls func & stores file & line */\n")
codeLocatorHeader.write(extendedMPIDecl.getvalue())
codeLocatorHeader.write("#if defined(__cplusplus)\n}\n#endif\n#endif\n")
codeLocatorHeader.close()

outputHeader.write("#endif\n\n")
outputHeader.close()
