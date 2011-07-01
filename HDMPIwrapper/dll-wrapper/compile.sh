touch MYFILE.txt
./create-func-wrapper.py sotracer.c wrapper.c functionLists/posixIO:functionLists/netcdf  2>&1

# -DDEBUG
gcc -c -ggdb -Wall ./wrapper.c  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0`
gcc test.c -ggdb -Wall wrapper.o -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt


mpicc -c -ggdb -I /opt/hdtrace/1.0/wrapper/include/  -I /opt/hdtrace/1.0/HDTraceWritingCLibrary/include/ -Wall ./wrapper.c -o wrapper-hdtracempi.o -D HDTRACE  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0`


#gcc -ggdb -Wall ./wrapper.c  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt
#mpicc -ggdb -o wrapper-hdtracempi.o -Wall ./wrapper.c -DHDTRACE  -I/home/julian/Dokumente/Projekte/PIOsimHD/installed/include  -c -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE  -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt

rm -f MYFILE.txt

PREFIX=/opt/hdtrace/1.0/sotrace/
echo installing under $PREFIX

mkdir -p $PREFIX/include
mkdir -p $PREFIX/lib

cp sotracer.h $PREFIX/include
cp wrapper-hdtracempi.o $PREFIX/lib
