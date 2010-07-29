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

from wrapper_conf import before, after, attributes, Options

TRACE = Options["Trace"]

def add_dll_opens(groupFiles, output):
  for group in groupFiles:
      f = open(group)
      funcs = f.readlines()
      f.close();

      groupName = funcs[0].strip()

      output.write("#ifdef " + groupName + "\n")
      output.write("OPEN_DLL(%s);\n" % (groupName))

      for i in xrange(2, len(funcs)):
	f = funcs[i].strip()

	#ignore comments
	if len(f) < 5 :
	  continue;

	regex = re.match("(.*)[\t ]([^(]+)\(", f)
	if not regex:
	  sys.stderr.write("[WARNING] regex does not match in file '%s', line %d:\n" % (group, i))
	  sys.stderr.write("\tContent: %s\n" % f)
	  continue

	tName = regex.group(2).strip()
	output.write("ADD_SYMBOL(%s);\n" %( tName ))


      output.write("#endif\n")

  return

def add_functions(groupFiles, output):
  for group in groupFiles:
    f = open(group)
    funcs = f.readlines()
    f.close();

    groupName = funcs[0].strip()
    groupFunction = funcs[1].strip()

    output.write("#ifdef " + groupName + "\n")

    for i in xrange(2, len(funcs)):
      f = funcs[i].strip()

      #ignore comments
      if len(f) < 5 :
	continue;
      if f[0] == "/":
	continue

      regex = re.match("(.*)[\t ]([^(]+)\(([^)]*)\)[\t ]*;", f)
      if not regex:
	sys.stderr.write("[WARNING] add functions regex does not match in file '%s', line %d:\n" % (group, i))
	sys.stderr.write("\tContent: %s\n" % f)
	continue

      tReturn = regex.group(1).strip()
      tName = regex.group(2).strip()

      # strip whitespace:
      tParam = map(lambda x: x.strip(), regex.group(3).split(","))

      paramNames = []
      paramTypes = []

      for param in tParam:
	regex = re.match("(.*[ \t*])([a-zA-Z0-9_]+)$", param)
	if not regex:
	  sys.stderr.write("[WARNING] parameter %s in file '%s', line %d:\n" % (param, group, i))
	  sys.stderr.write("\tContent: %s\n" % f)
	  continue

	paramNames.append(regex.group(2))
	paramTypes.append(regex.group(1))

      # does the function return sth useful?
      returnDatatype = True

      if tReturn == "void":
	returnDatatype = False;

      # generate wrapper function:

      # example:
      # int (*func)(int, void*, size_t) = g_hash_table_lookup(loadedSymbols, read);
      # int ret = (*func)(fd, buf, count);
      # return ret;
      output.write(f.rstrip(";") + "{\n");

      if DEBUG:
	output.write("printf(\"Entering " + groupFunction + " " + tName + "\\n\");\n")

      if TRACE:
	output.write("hdMPI_threadLogStateStart(\"" + groupFunction + "_" + tName + "\");\n");

      # lookup dynamic symbol:
      output.write(tReturn + " (*func) ( %s ) = g_hash_table_lookup(loadedSymbols, %s);\n" % (",".join(paramTypes), tName) );

      if tName in before:
	output.write(before[tName] + "\n")

      if returnDatatype:
	output.write(tReturn + " ret = ")

      output.write("(*func) (%s);\n" % ",".join(paramNames));

      if TRACE and tName in attributes:
	output.write("hdMPI_threadLogAttributes(\"%s\", %s);\n" % ( attributes[tName][0], attributes[tName][1] ) )

      if TRACE:
	output.write("hdMPI_threadLogStateEnd();\n");

      if tName in after:
	output.write(after[tName] + "\n");


      if returnDatatype:
	output.write("return ret;\n");

      output.write("}\n\n");

      # end group

    output.write("#endif\n")

  return


if len(sys.argv) < 3:
  print "usage: %s <inputSkeleton> <output:c-wrapper file> <group-file-to-trace:group-file...>  [Debug yes?]"
  sys.exit(0)


DEBUG = False
if len(sys.argv) == 4:
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

for line in inputSkeleton.readlines():
  if line.find("PYTHON_ADD_DLL_OPEN") != -1 :
    add_dll_opens(groupFilesToTrace, output)
  elif line.find("PYTHON_ADD_FUNCTIONS") != -1 :
    add_functions(groupFilesToTrace, output)
  else:
    output.write(line)

inputSkeleton.close()
output.close()

exit(0)