touch MYFILE.txt
./create-func-wrapper.py sotracer.c wrapper.c functionLists/posixIO:functionLists/netcdf:functionLists/hdf5:functionLists/cdi  2>&1

# -DDEBUG
gcc -c -ggdb -Wall ./wrapper.c  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0`
gcc test.c -ggdb -Wall wrapper.o -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt


# COMPILE WITH NETCDF
echo "Compiling with mpicc"
mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/  -DCDILIB='"/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libcdi.so"' -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -I/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -DHDF5LIB='"/opt/hdf5/1.8.7-mpich2-1.4.1p1/lib/libhdf5.so"' -DNETCDFLIB='"/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libnetcdf.so"' -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1
mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/  -DCDILIB='"/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libcdi.so"' -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -I/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-cdi.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1
mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-plain.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1
mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/  -DCDILIB='"/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libcdi.so"' -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -I/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -DHDF5LIBS='"/opt/hdf5/1.8.7-mpich2-1.4.1p1/lib/libhdf5.so"' -DNETCDFLIB='"/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libnetcdf.so"' -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-cdi-netcdf.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1
mpicc -c -ggdb  -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -DHDF5LIB='"/opt/hdf5/1.8.7-mpich2-1.4.1p1/lib/libhdf5.so"' -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-hdf5-only.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1

mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/  -DCDILIB='"/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libcdi.so"' -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -I/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -DHDF5LIB='"/opt/hdf5/1.8.7-mpich2-1.4.1p1/lib/libhdf5.so"' -DNETCDFLIB____='"/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libnetcdf.so"' -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-hdf5-cdi.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1
mpicc -c -ggdb -I/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/  -DCDILIB___='"/opt/cdi/1.5.2-netcdf-4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libcdi.so"' -I/opt/hdf5/1.8.7-mpich2-1.4.1p1/include -I/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/include/ -DHDF5LIB='"/opt/hdf5/1.8.7-mpich2-1.4.1p1/lib/libhdf5.so"' -DNETCDFLIB='"/opt/netcdf/4.1.3-hdf5-1.8.7-mpich2-1.4.1p1/lib/libnetcdf.so"' -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi-hdf5-netcdf.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` || exit 1


#mpicc -c -ggdb -DNETCDFLIB=/opt/netcdf/4.1.1/lib/libnetcdf.so.4.0.0 -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0`
#gcc -ggdb -Wall ./wrapper.c  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt
#mpicc -ggdb -o wrapper-hdtracempi.o -Wall ./wrapper.c -DHDTRACE  -I/home/julian/Dokumente/Projekte/PIOsimHD/installed/include  -c -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt

rm -f MYFILE.txt

PREFIX=/opt/hdtrace/1.0/sotrace/
echo installing under $PREFIX

mkdir -p $PREFIX/include
mkdir -p $PREFIX/lib

cp sotracer.h $PREFIX/include
cp wrapper-hdtracempi* $PREFIX/lib
