#!/usr/bin/env python
# -*- coding: utf-8 -*-
# encoding: utf-8

"""
/*
* Copyright (c) 2010 Julian M. Kunkel
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

import re
import sys

from wrapper_conf import before, beforeTracing, after, attributes, conditions, Options

TRACE = Options["Trace"]

def add_functions(groupFiles, open_dll_statements, wrapper_functions, varDef):
  for group in groupFiles:
    f = open(group)
    funcs = f.readlines()
    f.close();

    groupName = funcs[0].strip()
    groupFunction = funcs[1].strip()

    open_dll_statements.append("#ifdef " + groupName + "\n")
    open_dll_statements.append("OPEN_DLL(%s,\"%s\");\n" % (groupName, groupName))
    wrapper_functions.append("#ifdef " + groupName + "\n")
    varDef.append("#ifdef " + groupName + "\n")

    for i in xrange(2, len(funcs)):
      f = funcs[i].strip()
      error = False

      #ignore comments
      if len(f) < 5 :
	continue;
      if f[0] == "/":
	continue

      regex = re.match("(.*[\t *])([a-zA-Z0-9_]+)[\t ]*\(([^)]*)\)[\t ]*;", f)
      if not regex:
	sys.stderr.write("[WARNING] file '%s', line %d add functions regex does not match in file:\n" % (group, i+1))
	sys.stderr.write("\tContent: %s\n" % f)
	continue

      tReturn = regex.group(1).strip()
      tName = regex.group(2).strip()

      # strip whitespace:
      tParam = map(lambda x: x.strip(), regex.group(3).split(","))

      paramNames = []
      paramTypes = []

      for param in tParam:
	if param == "void":
	  continue;

	regex = re.match("(.*[ \t*])([a-zA-Z0-9_]+)[ \t]*(\[[ \t]*\])?$", param)
	if not regex:
	  sys.stderr.write("[ERROR] file '%s', line %d,  parameter %s in\n" % (group, i+1, param))
	  sys.stderr.write("\tContent: %s\n" % f)
	  error = True
	  continue

	paramNames.append(regex.group(2))

	if regex.group(3) != None:
	  paramTypes.append(regex.group(1) + "*")
	else:
	  paramTypes.append(regex.group(1))

      # does the function return sth useful?
      returnDatatype = True

      if tReturn == "void":
	returnDatatype = False;
      
      # Skip this function if we can't guarantee to pass on the call correctly.
      if error:
	sys.stderr.write("\tSkipping this function.\n")
	continue

      # generate the dll open function:

      open_dll_statements.append("ADD_SYMBOL(%s);\n" %( tName ))
      open_dll_statements.append("static_%s = symbol;\n" %( tName ))

      # generate wrapper function:

      # example:
      # int (*func)(int, void*, size_t) = g_hash_table_lookup(loadedSymbols, read);
      # int ret = (*func)(fd, buf, count);
      # return ret;


      varDef.append("static %s (* static_%s) ( %s ) = NULL;\n" % (tReturn, tName, ",".join(paramTypes)))

      # wrapper_functions.write("#undef %s\n" % (tName) );
      wrapper_functions.append(f.rstrip(";") + "{\n");

      wrapper_functions.append("if(! initalized_tracing){ printf(\"[SOTRACE] Warning early initalization\\n\"); sotracer_initalize(); }\n");

      if tName in before:
	wrapper_functions.append(before[tName] + "\n")

      if tName in conditions:
	wrapper_functions.append("if (started_tracing && %s ){\n" % (conditions[tName]));
      else:
	wrapper_functions.append("if (started_tracing){\n");

      if DEBUG:
	wrapper_functions.append("printf(\"Entering " + groupFunction + " " + tName + "\\n\");\n")

      if TRACE:
        if tName in beforeTracing:
	    wrapper_functions.append(beforeTracing[tName] + "\n")
	wrapper_functions.append("hdMPI_threadLogStateStart(\"" + groupFunction + "_" + tName + "\");\n");
      wrapper_functions.append("}\n");

      if returnDatatype:
	wrapper_functions.append(tReturn + " ret = ")

      wrapper_functions.append("(* static_%s) (%s);\n" % (tName, ",".join(paramNames)));

      if tName in conditions:
	wrapper_functions.append("if (started_tracing && %s ){\n" % (conditions[tName]));
      else:
	wrapper_functions.append("if (started_tracing){\n");
      if TRACE and tName in attributes:
	wrapper_functions.append("hdMPI_threadLogAttributes(\"%s\", %s);\n" % ( attributes[tName][0], attributes[tName][1] ) )

      if TRACE:
	wrapper_functions.append("hdMPI_threadLogStateEnd();\n");

      if tName in after:
	wrapper_functions.append(after[tName] + "\n");

      wrapper_functions.append("}\n");

      if returnDatatype:
	wrapper_functions.append("return ret;\n");

      wrapper_functions.append("}\n\n");

      # end group

    varDef.append("#endif\n")
    wrapper_functions.append("#endif\n")
    open_dll_statements.append("#endif\n")

  return


if len(sys.argv) < 4:
  print "usage: %s <inputSkeleton> <output:c-wrapper file> <group-file-to-trace:group-file...>  [Debug yes?]"
  sys.exit(0)


DEBUG = False
if len(sys.argv) == 5:
  DEBUG = True

lines = []
lastFunction = ""

nestingDepth = 0;
functionStarted = False;

reFunction = re.compile("[^;]*([a-z0-9A-Z]+)[\w]*")

# input file
inputSkeleton = open(sys.argv[1])

output = open(sys.argv[2], "w")

groupFilesToTrace = sys.argv[3].split(":")

print "I will trace " + str(groupFilesToTrace) + " input " + sys.argv[1]

dllOpenStatements = []
wrapperFunctions = []
varDef = []
add_functions(groupFilesToTrace, dllOpenStatements, wrapperFunctions, varDef)

for line in inputSkeleton.readlines():
  if line.find("PYTHON_ADD_DLL_OPEN") != -1 :
    output.write("".join(dllOpenStatements))
  elif line.find("PYTHON_ADD_FUNCTIONS") != -1 :
    output.write("".join(varDef))
    output.write("".join(wrapperFunctions))
  else:
    output.write(line)

inputSkeleton.close()
output.close()

exit(0)
