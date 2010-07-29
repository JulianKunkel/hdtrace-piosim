# -*- coding: utf-8 -*-


Options = {
  "Trace" : False
}

before = {}
after = {}

attributes = {
  "read" : ["fd='%d' size='%lld' return='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "write" : ["fd='%d' size='%lld' return='%lld'", "fd, (long long int) count, (long long int) ret" ],
  "open" : ["fd='%d' name='%s' return='%lld'", "ret, pathname" ],
  "close" : ["fd='%d' return='%d'", "fd, return"]
}

