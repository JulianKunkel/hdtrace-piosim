
WRAPPER_MPI = $(shell pwd)
WRAPPER_CC = $(shell pwd)/bin/mpicc
WRAPPER_LIB = " `pkg-config --libs glib-2.0` $(shell pwd)/../libHDTraceMPIWrapper.a  $(shell pwd)/../../../HDTraceWritingCLibrary/lib/libhdTrace.a"

.PHONY : clean
.PHONY : mpich-test
.PHONY : mpich-test-no-wrapper
.PHONY : hdtest

all:  test test2 hdtest

#../sim-wrapper.a:
#	cd .. ; make

hdtest: 
	cd HDTests ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) CC=$(WRAPPER_CC) make

hdtest-no-wrapper: 
	cd HDTests ; SIM_WRAPPER_LIB="" CC=mpicc make

test2: mpi2test ../libHDTraceMPIWrapper.a ../../../HDTraceWritingCLibrary/lib/
	cd mpi2test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) ./configure --with-mpi=$(WRAPPER_MPI) 
	cd mpi2test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) make 

mpi2test: 
	tar -zxf mpi2test.tar.gz

test: mpich-test ../libHDTraceMPIWrapper.a ../../../HDTraceWritingCLibrary/lib/
	cd mpich-test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) ./configure -cc=$(WRAPPER_CC) -fc=mpif77
	cd mpich-test ; SIM_WRAPPER_LIB=$(WRAPPER_LIB) make -k

mpich-test: mpich-test-2006.tar.gz
	tar -zxf mpich-test-2006.tar.gz

mpich-test-no-wrapper: 
	tar -zxf mpich-test-2006.tar.gz
	cd mpich-test ; SIM_WRAPPER_LIB="" ./configure -cc=mpicc -fc=mpif77
	cd mpich-test ; SIM_WRAPPER_LIB="" make 



runtests:
	cd mpich-test ; make runtest
#	cd mpi2test ; make runtest

clean:
	rm -rf mpi2test mpich-test

