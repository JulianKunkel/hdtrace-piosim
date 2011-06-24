./scripts/create_fortran_bridge.py interesting_funcs2.h out.c yes
mpicc out.c -c
mpif90 hello-fortran.f90  out.o 


exit 1

mkdir wrapper 2> /dev/null

cat src/hdTraceMPIWrapper.src.c > wrapper/hdTraceMPIWrapper.c || exit 1
#cat src/hdMPITracer.h > wrapper/hdMPITracer.h || exit 1
touch wrapper/hdMPITracer.h || exit 1
cp src/hdMPI* wrapper
scripts/create_sim-wrapper.py interesting_funcs.h wrapper/hdTraceMPIWrapper.c  wrapper/hdMPITracer.h wrapper/hdMPITracerCodeLocator.h


echo "Completed!"
