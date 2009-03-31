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

#@memoize
def split_filename(d):
   p2 = d.rfind("_")
   p1 = d.rfind("_", 0, p2)
   p0 = d.find("_", 0, p1)
   return [d[0:p0], d[p0+1:p1], d[p1+1:p2], d[p2+1:]]

def usage():
   print ("Syntax: -o <output_fname.xml> -i <TraceDescription> [-c comment] \\" + 
          "[--distribution-class=class] [--chunk-size=size]")

desc = ""
output_fname = "trace.xml"
project_description = ""
file_distribution_class = "de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe"         
file_chunk_size = "64K"

try:
   opts, args = getopt.getopt(sys.argv[1:], "ho:i:c:", ["help", "output_fname=", "input=", "comment=",
                                                        "distribution-class=", "chunk-size="])
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
   elif opt in ("-c", "--comment"):
      project_description = arg
   elif opt in ("--distribution-class"):
      file_distribution_class = arg
   elif opt in ("--chunk-size"):
      file_chunk_size = arg
          

description = open(desc).read().splitlines()


processCount = int(description[0]);

if output_fname.find ("/") <= -1 :
   regex = re.match("([^.]*)\.", output_fname)
   filename = regex.group(1)
else:
   regex = re.match(".*[/]([^./]*)\.", output_fname)
   filename = regex.group(1)

print "Writing application " + filename + " into " + output_fname;

#################
path = os.path.dirname(desc)
if path != "":
   path += "/"
#################


comms = {}
files = []
types = {}
for i in range(0, processCount):
   comms[i] = []
   types[i] = []
   inp = open(path + description[i+1] + ".info")

   for line in inp:
      res = re.match("File name=\"([^\"]*)\" .*Size=(\d*) id=(\d*)", line)
      if res :
         print "File: " + res.group(1) + " " + res.group(2) + " " + res.group(3)    
         files[i].append( [res.group(1), res.group(2), res.group(3)] )
         continue
      res = re.match("Comm map='([^']*)' id=(\d*) name='([^']*)'", line)
      if res : 
         print "Comm " + res.group(1) + " " + res.group(2) + " " + res.group(3)
         comms[i].append( [res.group(1), res.group(2), res.group(3)] )
         continue
      res = re.match("Type id='([^']*)' combiner='([^']*)' name='([^']*)' integers='([^']*)' addresses='([^']*)' types='([^']*)'", line)
      if res:
         types[i].append( [res.group(1), res.group(2), res.group(3), res.group(4), res.group(5), res.group(6)] )
         continue

      res = re.match("Type id='([^']*)' combiner='([^']*)' name='([^']*)'", line)
      if res:
         print "Type " + res.group(1) + " " + res.group(2) + " " + res.group(3)
         types[i].append( [res.group(1), res.group(2), res.group(3)] )
         continue


   inp.close()

print comms
print files
print types

out = open(output_fname, "w")

#Generate header
out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
out.write("<Application name=\"" + filename  +  "\" processCount=\"" + str(processCount) +  "\">\n")
out.write(" <Description>")
out.write(project_description)
out.write("</Description>\n")


out.write(" <FileList>\n")

for (name, size, id) in files:
   out.write(' <File name="%s">\n' % name)
   out.write('  <InitialSize>%s</InitialSize>\n' % size)
   out.write('  <Distribution class="%s">\n' % file_distribution_class)
   out.write('  <ChunkSize>%s</ChunkSize>\n' % file_chunk_size)
   out.write(' </File>\n')

out.write(" </FileList>\n\n")

out.write(" <Topology>\n")

out.write('  <Level name="Hostname">\n')
out.write('   <Level name="Rank">\n')
out.write('    <Level name="Thread">\n')
out.write('    </Level>\n')
out.write('   </Level>\n')
out.write('  </Level>\n\n')

# parse log file names into level_1 
description2 = description[1:]
description2.sort()
level_1 = {}
for d in description2:
   names = split_filename(d)
   print "file topology: " + str(names[1:4])
   if not (names[1] in level_1):
      level_1[names[1]] = { names[2] : [names[3]] }
   else:
      if not(names[2] in level_1[names[1]]):
         level_1[names[1]][names[2]] = [names[3]]
      else:
         level_1[names[1]][names[2]].append(names[3])

# print level_1
for l1 in level_1:
   out.write('  <Label value="%s">\n' % l1 )
   for l2 in level_1[l1]:
      out.write('   <Label value="%s">\n' % l2 )
      for l3 in level_1[l1][l2]:
         out.write('    <Label value="%s" />\n' % l3)
      out.write('   </Label>\n')
   out.write('  </Label>\n')
out.write(" </Topology>\n\n")

def parse_client_map(mapstring):
   s = mapstring.split(';')[0:-1]
   result = {}
   for map in s:
      a = map.split('->')
      a[0] = int(a[0])
      a[1] = int(a[1])

      if a[0] in result:
         print "Warning: rank %d appears more than once in comm description:\n%s" %  (a[0], mapstring)
      result[a[0]] = a[1]
   return result

for log_number in comms:
   for comm in comms[log_number]:
      comm[0] = parse_client_map(comm[0])


def remove_comm(commlist, comm):
   """remove first occurence of comm from commlist. only 
      participant lists are compared
   """
   for c in xrange(0, len(commlist)):
      if commlist[c][0] == comm:
         id = int(commlist[c][1])
         del(commlist[c])
         return id


unique_comms = []

print comms

for log_number in comms:
   while comms[log_number]:
      current_comm = [comms[log_number][0][0], {}, comms[log_number][0][2]]
      for i in current_comm[0]: # all the participating clients
         print i, comms[log_number]
         current_comm[1][i] = remove_comm(comms[i], current_comm[0])
         
      print len(comms[log_number])
      unique_comms.append(current_comm)

print unique_comms


out.write(" <CommunicatorList>\n")
for comm in unique_comms:
   out.write('  <Communicator name="%s">\n' % comm[2])
   for rank in comm[1]:
      out.write('   <Rank name="%s" cid="%d" commrank="%d">\n' % (split_filename(description[rank+1])[2], comm[1][rank], comm[0][rank]))
   out.write('  </Communicator>\n')
out.write(" </CommunicatorList>\n\n")



# format: (["integer-names"], ["address-names"], ["type-names"])
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
#  STRUCT_INTEGER, STRUCT
#  SUBARRAY
#  DARRAY
   "RESIZED" : ([], ["lowerBound", "extent"], ["oldType"]),
#   "" : ([], [], []),
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
   
   result = ""
   result += '  <%s id="%s" name="%s" count="%s" order="%s">\n' % (combiner, id, name, integers[0], integers[-1])

   size = int(integers[0])
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
      result += '   <Dim gsize="%s" distrib="%s" darg="%s" psize="%s"  />\n' % (integers[3 + i], integers[3 + dims + i], integers[3 + dims*2 + i], integers[3 + dims*3 + i])

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

#   result 
#    result = ""
#    result += '   <Type id="%s" combiner="%s" name="%s">\n' % (id, combiner, name)


#    result += '   </Type>\n'
#    return result



out.write(" <Datatypes>\n")
for i in types:
   out.write('  <Rank name="%s">\n' % (split_filename(description[i+1])[2]) )
   for type in types[i]:
      #res = re.match("Type id='([^']*)' combiner='([^']*)' name='([^']*)' integers='([^']*)' addresses='([^']*)' types='([^']*)'", line)
      if len(type) == 6:
         out.write(type_string(type[0], type[1], type[2], 
                               type[3].split(";")[0:-1],
                               type[4].split(";")[0:-1],
                               type[5].split(";")[0:-1]))
      else:
         out.write(type_string(type[0], type[1], type[2]))



   out.write('  </Rank>\n')

   pass

out.write(" </Datatypes>\n")

out.write("</Application>\n");

out.close()

print "Done"
