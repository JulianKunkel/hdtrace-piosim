touch MYFILE.txt
./create-func-wrapper.py trace.c wrapper.c functionLists/posixIO:functionLists/netcdf
gcc test.c -ggdb -Wall ./wrapper.c -ldl `pkg-config --libs glib-2.0 --cflags glib-2.0` && rm -f ./MYFILE.txt && ./a.out && cat MYFILE.txt
