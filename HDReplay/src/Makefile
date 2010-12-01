CC = mpicc

CFLAGS = -ggdb -std=c99 -rdynamic 

LIBS = -L /home/weging/bin/lib -l expat -l glib-2.0

INCLUDE = -I /usr/include/glib-2.0 -I /usr/lib/glib-2.0/include -I /home/weging/bin/include 

OBJS = main.o init.o error.o readproject.o readtrace.o

.c.o: Makefile
	$(CC) -c $(INCLUDE) $(CFLAGS)

all: hdreplay Makefile

hdreplay: $(OBJS) 
	$(CC) $(LIBS) -o $@ $(OBJS) $(CFLAGS)

main.o: Makefile
	$(CC) $(INCLUDE) -c $(CFLAGS)  $*.c

readproject.o: Makefile
	$(CC) $(INCLUDE) -c $(CFLAGS) $*.c

readtrace.o: Makefile
	$(CC) $(INCLUDE) -c $(CFLAGS) $*.c

error.o: Makefile
	$(CC) $(INCLUDE) -c $(CFLAGS) $*.c

init.o: Makefile
	$(CC) $(INCLUDE) -c $(CFLAGS) $*.c


clean:
	rm -rf *.o
	rm -f hdreplay
