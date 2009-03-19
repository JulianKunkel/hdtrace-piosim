CFLAGS  += -Wall -ggdb  -Iinclude/ $(shell pkg-config --cflags glib-2.0)  `getconf LFS_CFLAGS`
LDFLAGS += $(shell pkg-config --libs glib-2.0)

all: libHDTrace.a  libHDTraceMPIWrapper.a

HDTraceWriter.o: HDTraceWriter.c HDTraceWriter.h

libHDTrace.a: HDTraceWriter.o
	ar rcs libHDTrace.a HDTraceWriter.o

libHDTraceMPIWrapper.a: HDTraceMPIWrapper.o $(LDFLAGS)
	ar rcs libHDTraceMPIWrapper.a HDTraceMPIWrapper.o 

HDTraceMPIWrapper.src.o: HDTraceMPIWrapper.src.c

HDTraceMPIWrapper.c: HDTraceMPIWrapper.src.c ./create_sim-wrapper.py
	cat HDTraceMPIWrapper.src.c > HDTraceMPIWrapper.c
	./create_sim-wrapper.py >> HDTraceMPIWrapper.c

clean:
	rm *~ *.o *.a HDTraceMPIWrapper.c

### libglib2.0-0
