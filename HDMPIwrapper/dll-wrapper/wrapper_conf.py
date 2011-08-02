# -*- coding: utf-8 -*-


Options = {
  "Trace" : True
}


  # write can actually invoke a nested write from the tracing library, this must be prevented
before = {
  "write" :  "static int write_entered = 0;\n if (write_entered){ return (* static_write) (fd,buf,count);\n}"
	 }
beforeTracing = {               
      "write" :  "write_entered = 1;"
	 }

after = { "write" : "write_entered = 0;"}

attributes = {
  "read" : ["fd='%d' size='%lld' ret='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "write" : ["fd='%d' size='%lld' ret='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "open" : ["fd='%d' name='%s'", "ret, pathname" ],
  "close" : ["fd='%d' ret='%d'", "fd, ret"],
  "fopen" : ["ret='%d' name='%s'", "ret != NULL, filename" ],
  "freopen" : ["ret='%d' name='%s'", "ret != NULL, filename" ],
  "fdopen" : ["ret='%d' fd='%d'", "ret != NULL, fd" ],
}

conditions = {
  "write" : "(! hdTrace_isWritingToFile())",
#  "read" : "fd != 1",
}