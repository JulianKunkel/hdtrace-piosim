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

if len(sys.argv) < 3:
  print "usage: %s <header.h> <output:c-bridge file> [DEBUG]"
  sys.exit(0)


DEBUG = False
if len(sys.argv) == 4:
  DEBUG = True

# defines the maximum length an translated array might have.
MAXARRAY_LENGTH = 1024;

lines = []
lastFunction = ""

# input file
nestingDepth = 0;
functionStarted = False;

reFunction = re.compile("[^;]*([a-z0-9A-Z]+)[\w]*")

funcs = open(sys.argv[1]).readlines()
# scan for non nested lines (potential function definitions)

# output file
bridge = open(sys.argv[2], "w")

bridge.write("#include <string.h>\n")
bridge.write("#include <assert.h>\n")
bridge.write("#include <stdio.h>\n")

for i in xrange(0, len(funcs)):
  f = funcs[i].strip()

  #ignore comments
  if len(f) < 5 :
    continue;

  if f[0] == "/":
    continue
  if f.find("#include") > -1:
    bridge.write(f + "\n")
    continue;

  regex = re.match("(.*)[\t ]([^(]+)\(([^)]*)\)[\t ]*;", f)
  if not regex:
    sys.stderr.write("[WARNING] in file '%s', line %d:\n" % (sys.argv[1], i))
    sys.stderr.write("\tContent: %s\n" % f)
    continue

  tReturn = regex.group(1).strip(" \t")
  tName = regex.group(2).strip(" \t")

  # strip whitespace:
  tParam = map(lambda x: x.strip(), regex.group(3).split(","))
  #print tReturn + " -- "  + tName + " -- " + str(tParam)

  returnData = True

  if tReturn == "void":
    returnData = False


  # parse parameters and perform transitions
  paramTransition = []

  I = 0
  for param in tParam:
    posL = param.find("*")
    changePointer = True
    multipointer = False
    datatype = param

    if posL != -1: # already a pointer!
      datatype = param[0 : posL].strip()
      posR = param.rfind("*")
      if posR != posL:
	sys.stderr.write("[Warning] double pointer not supported, yet! In line %d Content: %s\n Will generate a dummy!\n" % (i, f))
	multipointer = True
      changePointer = False

    if datatype == "...":
	sys.stderr.write("[Warning] var_list arguments not supported, yet! In line %d Content: %s\n Will ignore that function!\n" % (i, f))
	continue;

    if param == "void":
      continue;

    paramTransition.append( [ param, "key" + str(I), changePointer, multipointer, datatype, datatype == "char" and not changePointer and not multipointer
 ] )
    I+=1

  TYPE=0
  VARNAME=1
  CHANGEPOINTER=2
  MULTIPOINTER=3
  DATATYPE=4
  STRARRAYLENGTHADDED=5

  # generate bridge:
  bridge.write(tReturn + " " + tName + "_ (" )

  # generate new parameters:
  firstParam = True
  for param in paramTransition:
    if not firstParam:
      bridge.write (", ")
    firstParam = False
    if param[CHANGEPOINTER] == True:
      bridge.write(param[TYPE] + " * " + param[VARNAME]);
    else:
      bridge.write(param[TYPE] + param[VARNAME] );

  # get length of array parameters (previously marked as pointers!)
  for param in paramTransition:
    #print str(param[DATATYPE] == "char") + " " + param[DATATYPE] + " " + str(len(param[DATATYPE]))
    if param[STRARRAYLENGTHADDED] :
      bridge.write(", int %slen " % (param[VARNAME]) );

  bridge.write("){\n")

  if returnData:
    bridge.write(tReturn + " ret;\n")

  # define variables for temporary data:
  for param in paramTransition:
    if param[STRARRAYLENGTHADDED]:
      bridge.write("%s %smod[%d];\n" % (param[DATATYPE], param[VARNAME], MAXARRAY_LENGTH));

  # copy elements to the new location
  for param in paramTransition:
    if param[STRARRAYLENGTHADDED]:
      bridge.write("assert(%slen < %d);\n" % (param[VARNAME], MAXARRAY_LENGTH));
      bridge.write("memcpy(%smod, %s, %slen * sizeof(%s));\n" % (param[VARNAME], param[VARNAME], param[VARNAME], param[TYPE]));
      bridge.write("%smod[%slen] = 0;\n" % (param[VARNAME], param[VARNAME]));

  if DEBUG:
    bridge.write("printf(\"Entering wrapper function %s\\n\");" % ( tName ));

  # generate representation of new parameters:
  if returnData:
    bridge.write(" ret = ")


  # issue function call to the original function
  bridge.write(tName + "(")

  firstParam = True
  for param in paramTransition:
    if not firstParam:
      bridge.write (", ")
    firstParam = False

    if param[STRARRAYLENGTHADDED] :
      bridge.write(param[VARNAME] + "mod")
    else:
      if param[CHANGEPOINTER]:
	bridge.write("* " + param[VARNAME])
      else:
	if param[MULTIPOINTER]:
	  bridge.write("NULL");
	else:
	  bridge.write(param[VARNAME]);


  # close function
  bridge.write(");\n" )

  if returnData:
     bridge.write("return ret;\n");

  bridge.write("}\n")


  # generate aliases:
  suffix = "_ () __attribute__ ((weak, alias (\"" + tName + "_\")));\n"
  bridge.write(tReturn + " " + tName.lower() + suffix);
  bridge.write(tReturn + " " + tName.lower() + "_" + suffix);
  bridge.write(tReturn + " " + tName.upper() + "_" + suffix );
  bridge.write(tReturn + " " + tName.upper() + suffix);
  bridge.write(tReturn + " " + tName + "_" + suffix );

bridge.close();
