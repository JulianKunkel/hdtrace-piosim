./create_fortran_bridge.py ../interesting_funcs.h fortran-bridge.c

# add MPI Init declaration to the bridge

echo "void MPI_Init_ (int *err){
	MPI_Init(NULL, NULL);
}" >> fortran-bridge.c

# compile the bridge
mpicc -c fortran-bridge.c 
