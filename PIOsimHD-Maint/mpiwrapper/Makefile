CFLAGS  += $(shell pkg-config --cflags glib-2.0)  `getconf LFS_CFLAGS`
LDFLAGS += $(shell pkg-config --libs glib-2.0)


all: sim-wrapper.c sim-wrapper.a

sim-wrapper.a: sim-wrapper.o sim-wrapper-core.o $(LDFLAGS)
	ar rcs sim-wrapper.a sim-wrapper.o sim-wrapper-core.o 
	
sim-wrapper-core.o: sim-wrapper-core.c
	gcc -Wall -ggdb -c sim-wrapper-core.c -Iinclude/ -o sim-wrapper-core.o   $(CFLAGS)

sim-wrapper.o: sim-wrapper.c
	gcc -Wall -ggdb -c sim-wrapper.c -Iinclude/ -o sim-wrapper.o  $(CFLAGS)

sim-wrapper.c: sim-wrapper.src.c ./create_sim-wrapper.py
	cat sim-wrapper.src.c > sim-wrapper.c
	./create_sim-wrapper.py >> sim-wrapper.c

clean:
	rm *~ *.o sim-wrapper.c sim-wrapper.a
### libglib2.0-0
