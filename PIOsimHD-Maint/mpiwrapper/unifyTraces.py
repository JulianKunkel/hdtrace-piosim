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

import sys
import getopt
import re
import os

def usage():
   print "Syntax: -o <output_fname.xml> -i <TraceDescription>"

desc="trace-desc-partdiff-par.xml"
output_fname="result.xml"

try:
  opts, args = getopt.getopt(sys.argv[1:], "ho:i:d", ["help", "output_fname=", "input="]) 
except getopt.GetoptError:
    usage()
    sys.exit(2)
    
for opt, arg in opts:                
      if opt in ("-h", "--help"):      
          usage()                     
          sys.exit()                  
      elif opt == '-d':                
          global _debug               
          _debug = 1                  
      elif opt in ("-o", "--output_fname"): 
          output_fname = arg               
      elif opt in ("-i", "--input"): 
          desc = arg               

description = open(desc).read().splitlines()

out = open(output_fname, "w")

processCount = int(description[0]);

if output_fname.find ("/") <= -1 :
  regex = re.match("([^.]*)\.", output_fname)
  filename = regex.group(1)
else:
  regex = re.match(".*[/]([^./]*)\.", output_fname)
  filename = regex.group(1)

print "Writing application " + filename + " into " + output_fname;

#################
path = os.path.dirname(desc) + "/"
#################

#Generate header
out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
out.write("<Application name=\"" + filename  +  "\" processCount=\"" + str(processCount) +  "\">\n");
out.write(" <Description>     </Description> <FileList> \n");
for i in range(0, processCount):
  inp = open(path + description[i+1] + ".info")
  # File_open name="./visualization.dat" comm="WORLD" flags=5 InitialSize=0 id=10000
  for line in inp:
	  res = re.match("File_open name=\"([^\"]*)\" comm=\"([^\"]*)\" .*Size=(\d*) id=(\d*)", line)
	  if res :
  		print res.group(1) + " " + res.group(2) + " " + res.group(3)  + " " + res.group(4)  
		out.write("<File name=\"" + res.group(1) + "\" id=\"" + res.group(4) + "\">\n")
		out.write("<InitialSize>" + res.group(3) + "</InitialSize>\n")
		out.write("               <Distribution name=\"SimpleStripe\"> <Chunk-Size>100K</Chunk-Size>   </Distribution>  </File>\n")

  inp.close()


out.write("\n </FileList> <CommunicatorList> <Communicator name=\"WORLD\"/>  </CommunicatorList> ");
out.write("<ProcessList>\n");

for i in range(0, processCount):
  inp = open(path + description[i+1] + ".xml")
  out.write(inp.read() + "\n")
  inp.close()

#generate trailer
out.write("</ProcessList></Application>\n");

out.close()

print "Done"
