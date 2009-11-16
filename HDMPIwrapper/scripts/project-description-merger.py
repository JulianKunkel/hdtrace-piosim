#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

"""
This script takes an output file and a number of HDTraceMPIWrapper logfiles as 
an argument. 
It then writes the information found in the process-specific *.info files into 
the output file. 

### Example #####################################################################
#
# running mpi-io-test with 5 nodes results in the following files being written:

$ mpiexec -n 5 ./mpi-io-test
$ ls -1 mpi-io-test*
mpi-io-test_node01_0_0.info
mpi-io-test_node01_0_0.trc
mpi-io-test_node01_2_0.info
mpi-io-test_node01_2_0.trc
mpi-io-test_node01_4_0.info
mpi-io-test_node01_4_0.trc
mpi-io-test_node02_1_0.info
mpi-io-test_node02_1_0.trc
mpi-io-test_node02_3_0.info
mpi-io-test_node02_3_0.trc

#
# to process the output, call this script:
# 

$ project-description-merger.py -o mpi-io-test.proj mpi-io-test_*.info

#
# mpi-io-test.xml now contains the project data for the program.
#
#################################################################################

"""

import sys
import getopt
import re
import os

def split_filename(d):
   """
   Split a HDTraceWrapper filename into its main components. 
   A filename looks like this:
         
      <project>_<hostname>_<rank>_<thread>

   note: <project> may not contain underscores

   Example: 
   split_filename('trace_mpi-io-test_pvs-cluster.informatik.uni-heidelberg.de_0_0')
    == ["trace_mpi-io-test", "pvs-cluster.informatik.uni-heidelberg.de", "0", "0"]
   """

   split_name = d.split("_")

   if len(split_name) < 4:
      print '[ERROR]: "%s" is not a valid trace filename' % d
      sys.exit(-1)

   # assemble project name if it contains underscores
   while len(split_name) > 4:
      split_name = [split_name[0] + "_" + split_name[1]] + split_name[2:]

   return split_name

   
def usage():
   """
   print usage information for the program
   """
   print "Syntax: -o <outfile.xml> [-d <description>] [--distribution-class=<class>] \\"
   print "[--chunk-size=<size>] <log1>.info <log2>.info ... <logN>.info"
   print 
   print "  -o <outfile>                : write output to <outfile>. "
   print "                                this argument is mandatory"
   print "  -d <description>            : description for the project "
   print "  --distribution-class=<class>: piosim distribution class for MPI-IO files"
   print "  --chunk-size=<size>         : chunk size for MPI-IO files"
   print 
   print 



def get_options():
   """
   Read command line options and return a tuple containing the information.
   Print message and exit on error.
   """
   try:
      opts, files = getopt.getopt(sys.argv[1:], "ho:c:", ["help", "output_fname=", "comment=",
                                                          "distribution-class=", "chunk-size="])
   except getopt.GetoptError:
      print "could not parse options"
      usage()
      sys.exit(2)

   output_fname = None
   project_description = ""
   file_distribution_class = "de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe"         
   file_chunk_size = "64K"

   for opt, arg in opts:                
      if opt in ("-h", "--help"):      
         usage()                     
         sys.exit()                  
      elif opt in ("-o", "--output_fname"): 
         output_fname = arg               
      elif opt in ("-d", "--description"):
         project_description = arg
      elif opt in ("--distribution-class"):
         file_distribution_class = arg
      elif opt in ("--chunk-size"):
         file_chunk_size = arg

   if not output_fname:
      print "[ERROR]: no output filename given"
      sys.exit(-1)

   for i in xrange(0, len(files)):
      if files[i][-4:] == ".xml":
         files[i] = files[i][:-4]
      elif files[i][-5:] == ".info":
         files[i] = files[i][:-5]

   unique_files = {}
   for f in files:
      unique_files[f] = 1

   return (unique_files.keys(), output_fname, project_description, file_distribution_class, file_chunk_size)

class File:
   """
   Structure to simplify parsing file informations from *.info files
   """
   def __init__(self, name, size, id, distclass, chunk_size):
      self.name = name
      self.size = size
      self.id = id
      self.distribution_class = distclass
      self.chunk_size = chunk_size

   def xmlString(self):
      return ((' <File name="%s">\n' % self.name)  + 
              ('  <InitialSize>%s</InitialSize>\n' % self.size) +
              ('  <Distribution class="%s">\n' % self.distribution_class) +
              ('  <ChunkSize>%s</ChunkSize>\n' % self.chunk_size) +
	       '  </Distribution>\n' +
              (' </File>\n' )) 


class Comm:
   """
   Structure to simplify parsing communicator informations from *.info files
   """
   def __init__(self, map, id, name):
      map = map.split(";")[:-1]
      self.id = id
      self.name = name
      self.map = {}
      for i in xrange(0, len(map)):
         t = map[i].split("->")
         assert len(t) == 2
         self.map[t[0]] = t[1]

         

class Type:
   """
   Structure to simplify parsing type informations from *.info files
   """
   def __init__(self, id, combiner, name, integers="", addresses="", types=""):
      self.id = id
      self.combiner = combiner
      self.name = name
      self.integers = integers.split(";")[:-1]
      self.addresses = addresses.split(";")[:-1]
      self.types = types.split(";")[:-1]


def parse_info_files(file_basenames, distclass, chunksize):
   """
   take a list of log file base names (without file extension), read the
   *.info files and extract following information:

   dict comms[log-filename] = [list of comms in that file] each element has the
                         class Comm
   list files = [list of used files], each element has the class File
   dict types[log-filename] = [list of types in that file], each element has the
                              class Type

   return the tuple (comms, files, types)
   """

   comms = {}
   files = {}
   types = {}

   for name in file_basenames:
      comms[name] = []
      types[name] = []
      inp = open(name + ".info")

      for line in inp:
         res = re.match("File name=\"([^\"]*)\" .*Size=(\d*) id=(\d*)", line)
         if res :
            print "File: " + res.group(1) + " " + res.group(2) + " " + res.group(3)    
            # only store unique files (unique name)
            files[res.group(1)] = File(res.group(1), res.group(2), res.group(3), distclass, chunksize)
            continue

         res = re.match("Comm map='([^']*)' id=(\d*) name='([^']*)'", line)
         if res : 
            print "Comm " + res.group(1) + " " + res.group(2) + " " + res.group(3)
            comms[name].append( Comm(res.group(1), res.group(2), res.group(3)) )
            continue

         res = re.match("Type id='([^']*)' combiner='([^']*)' name='([^']*)' integers='([^']*)' addresses='([^']*)' types='([^']*)'", line)
         if res:
            print "Type " + res.group(1) + " " + res.group(2) + " " + res.group(3)
            types[name].append( Type(res.group(1), res.group(2), res.group(3), res.group(4), res.group(5), res.group(6)) )
            continue

         res = re.match("Type id='([^']*)' combiner='([^']*)' name='([^']*)'", line)
         if res:
            print "Type " + res.group(1) + " " + res.group(2) + " " + res.group(3)
            types[name].append( Type(res.group(1), res.group(2), res.group(3)) )
            continue
      inp.close()

   return (comms, files.values(), types)


def get_project_name(files):
   """
   Check, if all files have the same project name and return it.
   Print message and exit on error.
   """
   assert(files)
   split_filenames = [split_filename(x) for x in files]
   for name in split_filenames[1:]:
      if name[0] != split_filenames[0][0]:
         print "[ERROR]: all filenames must have the same project name, but"
         print '         "%s" differs from "%s"' % (name[0], split_filenames[0][0])
         print
         sys.exit(0) 

   pname = split_filenames[0][0]
   last_slash = pname.rfind("/")
   if last_slash < 0:
      return pname
   else:
      return pname[last_slash+1:]


def topology_string(logfiles):
   """
   this function extracts the topology information from the names of the logfiles
   and returns an xml string describing it.
   """
   out = ""
   out += (" <Topology>\n")
   out += ('  <Level type="Hostname">\n')
   out += ('   <Level type="Rank">\n')
   out += ('    <Level type="Thread">\n')
   out += ('    </Level>\n')
   out += ('   </Level>\n')
   out += ('  </Level>\n\n')

   description2 = logfiles
   description2.sort()

   level_1 = {}
   for d in description2:
      names = split_filename(d)
      #print "file topology: " + str(names[1:4])
      if not (names[1] in level_1):
         level_1[names[1]] = { names[2] : [names[3]] }
      else:
         if not(names[2] in level_1[names[1]]):
            level_1[names[1]][names[2]] = [names[3]]
         else:
            level_1[names[1]][names[2]].append(names[3])

   for l1 in level_1:
      out += ('  <Node name="%s">\n' % l1 )
      for l2 in level_1[l1]:
         out += ('   <Node name="%s">\n' % l2 )
         for l3 in level_1[l1][l2]:
            out += ('    <Node name="%s" />\n' % l3)
         out += ('   </Node>\n')
      out += ('  </Node>\n')

   out += (" </Topology>\n\n")

   return out





###############################################################################
#
# write the output file
#
###############################################################################

(logfiles, output, description, distribution_class, chunk_size) = get_options()
project_name = get_project_name(logfiles)

print "Writing application into " + output
print "Project name is " + project_name

(comms, files, types) = parse_info_files(logfiles, distribution_class, chunk_size)

out = open(output, "w")

###############################################################################
# write header
###############################################################################
out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
out.write("<Application name=\"" + project_name  +  "\">\n")
out.write("<Description>")
out.write(description)
out.write("</Description>\n")


out.write(" <FileList>\n")


#########################################################################################
# write file list
#########################################################################################
for f in files:
   out.write(f.xmlString())
out.write(" </FileList>\n\n")

out.write(topology_string(logfiles))

def comm_string(comms):
   """
   This function finds out, which communicator definitions in the
   different logfiles belong together i.e. represent the same
   communicator. Then it returns an xml string representing 
   each communicator, and the internal mapping between the world rank
   (called Rank name) and the communicator rank (called cid)
   """
   result = ""
   result +=  " <CommunicatorList>\n"

   for i in comms:
      for c in comms[i]:
         if not c:
            continue
         current_map = c.map
         result +=  '  <Communicator name="%s">\n' % c.name

         # find the first comm with the same map, write tag and
         # remove the comm from the list
         for filename in comms:
            for cc in xrange(0, len(comms[filename])):
               # only search in files of participating ranks
               if not split_filename(filename)[2] in current_map:
                  continue

               if not comms[filename][cc]:
                  continue
               
               # if maps match, search no further, delete this map
               if comms[filename][cc].map == current_map:
                  rank_nr = split_filename(filename)[2]
                  result +=  '   <Rank global="%s" local="%s" cid="%s" />\n' % (rank_nr, current_map[rank_nr], comms[filename][cc].id)
                  comms[filename][cc] = None
                  break

         result +=  '  </Communicator>\n'
   result +=  " </CommunicatorList>\n\n"
   return result

out.write(comm_string(comms))



###############################################################################
# 
# Now, we have to document the used filetypes. There are the simple ones with
# a fixed number of arguments: those are listed in type_format, using 
# the tuple (<list of integer names>, <list of address names>, 
# <list of type names>) and then written out by the function type_string(...). 
# 
# The somewhat more complicated types have their own formatting functions. Which 
# function is being called for which type is listed in the dictionary 
# file_format_functions below.
#
###############################################################################

type_format = {
   "DUP" : ([], [], ["oldType"]),
   "CONTIGUOUS" : (["count"], 
                   [], 
                   ["oldType"]), 
   "VECTOR" : (["count", "blocklength", "stride"], 
               [], 
               ["oldType"]),
   "HVECTOR" : (["count", "blocklength"], ["stride"], ["oldType"]),
   "HVECTOR_INTEGER" : (["count", "blocklength"], ["stride"], ["oldType"]),
   "RESIZED" : ([], ["lowerBound", "extent"], ["oldType"]),
}

def indexed_string(id, combiner, name, integers, addresses, types):
   assert int(integers[0])*2 + 1 == len(integers)

   result = ""
   result += '  <%s id="%s" name="%s" count="%s" oldType="%s">\n' % (combiner, id, name, integers[0], types[0])

   for i in range(0, int(integers[0])):
      result += '   <Block len="%s" index="%s" />\n' % (integers[1 + i], integers[1 + int(integers[0]) + i] )
   result += '  </%s>\n' % combiner

   return result
                                                      

def hindexed_string(id, combiner, name, integers, addresses, types):
   assert int(integers[0]) + 1 == len(integers)
   assert int(integers[0]) == len(addresses)

   result = ""
   result += '  <%s id="%s" name="%s" count="%s" oldType="%s">\n' % (combiner, id, name, integers[0], types[0])

   for i in range(0, int(integers[0])):
      result += '   <Block len="%s" index="%s" />\n' % (integers[1 + i], addresses[i] )
   result += '  </%s>\n' % combiner

   return result

def indexed_block_string(id, combiner, name, integers, addresses, types):
   assert int(integers[0]) + 2 == len(integers)

   result = ""
   result += '  <%s id="%s" name="%s" count="%s" oldType="%s" blockLength="%s">\n' % (combiner, id, name, integers[0], types[0], integers[1])

   for i in range(0, int(integers[0])):
      result += '   <Block displacement="%s" />\n' % (integers[2 + i])
   result += '  </%s>\n' % combiner

   return result

def struct_string(id, combiner, name, integers, addresses, types):
   assert int(integers[0]) + 1 == len(integers)
   assert int(integers[0]) == len(addresses)
   assert int(integers[0]) == len(types)

   result = ""
   result += '  <%s id="%s" name="%s" count="%s" >\n' % (combiner, id, name, integers[0])

   for i in range(0, int(integers[0])):
      result += '   <Type id="%s" displacement="%s" blocklen="%s"/>\n' % (types[i], addresses[i] ,integers[1 + i])
   result += '  </%s>\n' % combiner

   return result


def subarray_string(id, combiner, name, integers, addresses, types):
   assert int(integers[0])*3 + 2 == len(integers)
   
   size = int(integers[0])
   
   result = ""
   result += '  <%s id="%s" name="%s" ndims="%s" order="%s" oldType="%s" >\n' % (combiner, id, name, size, integers[-1], types[0])

   for i in range(0, size):
      result += '   <Dimension size="%s" subsize="%s" start="%s" />\n' % (integers[1 + i], integers[1 + size + i], integers[1 + size*2 + i])
   result += '  </%s>\n' % combiner

   return result      


def darray_string(id, combiner, name, integers, addresses, types):
   assert int(integers[2]) * 4 + 4 == len(integers)
   result = ""
   result += '  <%s id="%s" name="%s" size="%s" order="%s" rank="%s" dims="%s">\n' % (combiner, id, name, integers[0], integers[-1], integers[1], integers[2])

   dims = int(integers[2])

   for i in range(0, dims):
      result += '   <Dimension gsize="%s" distrib="%s" darg="%s" psize="%s"  />\n' % (integers[3 + i], integers[3 + dims + i], integers[3 + dims*2 + i], integers[3 + dims*3 + i])

   result += '  </%s>\n' % combiner

   return result      
   
   
type_format_functions = {
   "INDEXED" : indexed_string,
   "HINDEXED" : hindexed_string, 
   "INDEXED_BLOCK" : indexed_block_string,
   "STRUCT" : struct_string, 
   "STRUCT_INTEGER" : struct_string, 
   "SUBARRAY" : subarray_string, 
   "DARRAY" : darray_string, 
}

def type_string(id, combiner, name, integers = [], addresses = [], types = []):
   """
   Assemble the type information into an xml representation and return it. 
   Types with a fixed argument count are listed in the dictionary type_format.
   Types with a variable argument count are processed by custom functions, as
   listed in the dict type_format_functions
   """
   result = ""
   if combiner[0:13] == "MPI_COMBINER_":
      combiner = combiner[13:]

      name = name
      result = '  <%s id="%s" name="%s" ' % (combiner, id, name)

      if combiner in type_format:
         int_names = type_format[combiner][0]
         add_names = type_format[combiner][1]
         type_names = type_format[combiner][2]

         assert len(int_names) <= len(integers)
         assert len(add_names) <= len(addresses)
         assert len(type_names) <= len(types)

         for i in xrange(0, len(int_names)):
            result += '%s="%s" ' % (int_names[i], integers[i])

         for i in xrange(0, len(add_names)):
            result += '%s="%s" ' % (add_names[i], addresses[i])

         for i in xrange(0, len(type_names)):
            result += '%s="%s" ' % (type_names[i], types[i])
         
         if len(int_names) == len(integers) and len(add_names) == len(addresses) and  len(type_names) == len(types):
            result += " />\n" 
         else:
            result += " >\n"
            
            result += "  </%s>\n" % combiner
         
         pass
      elif combiner in type_format_functions:
         result = type_format_functions[combiner](id, combiner, name, integers, addresses, types)
      else:
         if (not integers) and (not addresses) and (not types):
            result += " />\n"
         else:
            result += " >\n"
            for i in integers:
               result += '    <Integer>%s</Integer>\n' % i 
            for a in addresses:
               result += '    <Address>%s</Address>\n' % a
            for t in types:
               result += '    <Type>%s</Type>\n' % t 
            result += "  </%s>\n" % combiner
            
   else:
      print "unknown combiner " + combiner
      return ""

   return result


###############################################################################
# write the datatype section
###############################################################################
out.write(" <Datatypes>\n")
for i in types:
   out.write('  <Rank name="%s" thread="%s">\n' % (split_filename(i)[2], split_filename(i)[3]) )
   for type in types[i]:
      out.write(type_string(type.id,
                            type.combiner,
                            type.name,
                            type.integers,
                            type.addresses,
                            type.types))

   out.write('  </Rank>\n')

out.write(" </Datatypes>\n")
out.write("</Application>\n");
out.close()

print "Done"
