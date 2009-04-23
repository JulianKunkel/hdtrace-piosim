all:
	$(CC) -o comm comm.c
	$(CC) -o file file.c
	$(CC) -o nested nested.c
	$(CC) -o types types.c
	$(CC) -o mpi-io-test mpi-io-test.c
	$(CC) -o nonblocking nonblocking.c

clean:
	rm -f *.o *~ trace* comm file nested types mpi-io-test *.tmp *.out
