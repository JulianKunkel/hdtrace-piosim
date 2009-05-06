ifeq ($(MPIIMP),openmpi)
	DIST_CFLAGS = -Iinclude/openmpi
else
	DIST_CFLAGS = -Iinclude/mpich
endif

CFLAGS  += -Wall -Wextra -pedantic -std=gnu99 -ggdb  $(DIST_CFLAGS) -I../HDTraceWritingCLibrary/include  -Iinclude/ $(shell pkg-config --cflags glib-2.0)  `getconf LFS_CFLAGS` -DhdTrace_INCLUDE_NESTED_TEST
LDFLAGS += $(shell pkg-config --libs glib-2.0)

.PHONY: all
all: lib/libhdTraceMPIWrapper.a

lib/libhdTraceMPIWrapper.a: src/hdTraceMPIWrapper.o $(LDFLAGS)
	ar rcs lib/libhdTraceMPIWrapper.a src/hdTraceMPIWrapper.o 

src/hdTraceMPIWrapper.o: src/hdTraceMPIWrapper.src.c

src/hdTraceMPIWrapper.c: src/hdTraceMPIWrapper.src.c scripts/create_sim-wrapper.py scripts/wrapper_conf.py include/interesting_funcs.h include/hdMPITracer.h.in src/hdMPITracer.c 
	cat src/hdTraceMPIWrapper.src.c > src/hdTraceMPIWrapper.c
	cat include/hdMPITracer.h.in > include/hdMPITracer.h 
	scripts/create_sim-wrapper.py include/interesting_funcs.h src/hdTraceMPIWrapper.c include/hdMPITracer.h 

.PHONY: test

test:
	cd test ; make

.PHONY: hdtest

hdtest: all
	cd test ; make hdtest

.PHONY: clean

clean:
	cd test ; make clean
	rm -f src/*.o lib/*.a
	rm -f src/hdTraceMPIWrapper.c
	rm -rf doc/doxygen/html
	rm -rf doc/doxygen/latex
	cd doc ; rm -rf mpiwrapper.aux mpiwrapper.bbl mpiwrapper.blg mpiwrapper.log mpiwrapper.pdf mpiwrapper.toc 
	rm scripts/*.pyc

.PHONY: doc

doc:
	doxygen Doxyfile
	cd doc; latex mpiwrapper

### libglib2.0-0
