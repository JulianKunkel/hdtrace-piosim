WRAPPER_MPI = $(shell pwd)
WRAPPER_CC = $(shell pwd)/bin/mpicc
WRAPPER_LIB = " $(shell pkg-config --libs glib-2.0) $(shell pwd)/../lib/libhdTraceMPIWrapper.a  $(shell pwd)/../../HDTraceWritingCLibrary/lib/libhdTrace.a"

.PHONY: all
all:  test test2 hdtest

.PHONY: hdtest
hdtest: 
	cd HDTests ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) CC=$(WRAPPER_CC) make

.PHONY: hdtest-no-wrapper
hdtest-no-wrapper: 
	cd HDTests ; SIM_WRAPPER_LIB="" CC=mpicc make


.PHONY: test2

test2: mpi2test ../lib/libhdTraceMPIWrapper.a ../../HDTraceWritingCLibrary/lib/libhdTrace.a
	cd mpi2test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) ./configure --with-mpi=$(WRAPPER_MPI) 
	cd mpi2test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) make 

.PHONY: mpi2test
mpi2test: 
	tar -zxf mpi2test.tar.gz

.PHONY: test
test: mpich-test ../lib/libhdTraceMPIWrapper.a ../../HDTraceWritingCLibrary/lib/libhdTrace.a
	cd mpich-test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) ./configure -cc=$(WRAPPER_CC) -fc=mpif77
	cd mpich-test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) make -k

.PHONY: mpich-test
mpich-test: mpich-test-2006.tar.gz
	tar -zxf mpich-test-2006.tar.gz

.PHONY: mpich-test-no-wrapper
mpich-test-no-wrapper: 
	tar -zxf mpich-test-2006.tar.gz
	cd mpich-test ; SIM_WRAPPER_LIB="" ./configure -cc=mpicc -fc=mpif77
	cd mpich-test ; SIM_WRAPPER_LIB="" make 


.PHONY: runtests
runtests:
	cd mpich-test ; make runtest

.PHONY : clean
clean:
	cd HDTests ; make clean
	rm -rf mpi2test mpich-test

