# -*- coding: utf-8 -*-


Options = {
  "Trace" : True
}

before = {}
after = {}

attributes = {
  "read" : ["fd='%d' size='%lld' return='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "write" : ["fd='%d' size='%lld' return='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "open" : ["fd='%d' name='%s'", "ret, pathname" ],
  "close" : ["fd='%d' return='%d'", "fd, ret"]
}

conditions = {
#  "write" : "fd > 2",
#  "read" : "fd != 1",
}