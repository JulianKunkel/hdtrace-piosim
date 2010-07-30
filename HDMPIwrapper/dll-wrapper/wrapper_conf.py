# -*- coding: utf-8 -*-


Options = {
  "Trace" : True
}

before = {}
after = {}

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
#  "write" : "fd > 2",
#  "read" : "fd != 1",
}