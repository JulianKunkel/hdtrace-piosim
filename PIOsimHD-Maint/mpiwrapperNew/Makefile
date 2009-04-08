CFLAGS  += -Wall -pedantic -std=gnu99 -ggdb  -I../../HDTraceWritingCLibrary/include  -Iinclude/ $(shell pkg-config --cflags glib-2.0)  `getconf LFS_CFLAGS` -DHDTRACE_INCLUDE_NESTED_TEST
LDFLAGS += $(shell pkg-config --libs glib-2.0)

.PHONY: all
all: libs/libHDTraceMPIWrapper.a

libs/libHDTraceMPIWrapper.a: src/HDTraceMPIWrapper.o $(LDFLAGS)
	ar rcs libs/libHDTraceMPIWrapper.a src/HDTraceMPIWrapper.o 

src/HDTraceMPIWrapper.o: src/HDTraceMPIWrapper.src.c 


src/HDTraceMPIWrapper.c: src/HDTraceMPIWrapper.src.c scripts/create_sim-wrapper.py scripts/wrapper_conf.py include/interesting_funcs.h
	cat src/HDTraceMPIWrapper.src.c > src/HDTraceMPIWrapper.c
	scripts/create_sim-wrapper.py include/interesting_funcs.h >> src/HDTraceMPIWrapper.c

.PHONY: test

test:
	cd test ; make

.PHONY: hdtest

hdtest: all
	cd test ; make hdtest

.PHONY: clean

clean:
	cd test ; make clean
	rm -f src/*.o libs/*.a
	rm -f src/HDTraceMPIWrapper.c
	rm -rf doc/doxygen/html
	rm -rf doc/doxygen/latex

.PHONY: doc

doc:
	doxygen Doxyfile

### libglib2.0-0
