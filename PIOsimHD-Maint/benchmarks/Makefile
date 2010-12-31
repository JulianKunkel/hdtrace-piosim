all: mpi-bench mpi-bench1G memory-bandwidth

memory-bandwidth: memory-bandwidth.c
	mpicc -Wall memory-bandwidth.c -o memory-bandwidth -std=c99

mpi-bench: mpi-bench.c
	mpicc -Wall -std=c99 mpi-bench.c -o mpi-bench


mpi-bench1G: mpi-bench.c
	mpicc -Wall -std=c99 mpi-bench.c -o mpi-bench1G -DGByte
	
clean:
	rm mpi-bench1G mpi-bench
