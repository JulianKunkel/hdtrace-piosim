/*
 @author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h> 
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

double getTime(){
	struct timeval tv;
	gettimeofday(& tv, NULL);
	return tv.tv_sec + tv.tv_usec * 0.000001;
}



void printResults(long operations, double * time, long recordSizeInByte, char const *  name){
  double  sizeInMiB = recordSizeInByte / 1024.0 / 1024;
  printf("%s MiB per second: ", name);
  printf("%.10f", sizeInMiB / time[0]);
  for(long i=1; i < operations; i++){
	printf(",%.8f", sizeInMiB / time[i]);
  }
  printf("\n");
}


int main (int argc, char *argv[])
{
  long fileSize = 100*1024*1024;
  long recordSize = 16*1024; // 16 KiB
  int doRandom = 1;
  int doWrite = 1;
  char * filename;

  if(argc < 2){
	printf("Syntax: %s <filename> [fileSizeInByte] [recordSizeInByte] [doRandom] [doWrite]\n", argv[0]);
	return 1;
  }

  filename = argv[1];

  if (argc > 2){
    fileSize = atol(argv[2]);
  }
  if (argc > 3){
    recordSize = atol(argv[3]);
  }
  if (argc > 4){
    doRandom = atoi(argv[4]);
  }
  if (argc > 5){
    doWrite = atoi(argv[5]);
  }

  long operations = fileSize / recordSize;

  printf("Size of size_t:%lu off_t:%lu\n", sizeof(size_t), sizeof(off_t));
  printf("fileSize: %ld iterations:%ld operations:%ld doRandom:%d doWrite:%d file:%s\n", fileSize, recordSize, operations, doRandom, doWrite, filename);

  double * time = malloc(operations*sizeof(double));

  int fd = open(filename, O_RDWR|O_CREAT|O_TRUNC);
  if(fd <= 2){
	printf("Error in opening file: %s\n", filename );
	exit(1);
  }
  

  char * buff = malloc(recordSize);

  // perform I/O operation
  for(long i=0; i < operations; i++ ){
	size_t ret = 0;
	if(doRandom){
		size_t byte;	
		byte = ((rand() % fileSize) / recordSize) * recordSize;
		if (lseek(fd, 0, SEEK_CUR) != byte){	
			printf("Error in seeking to position: %lld\n", (long long int) lseek(fd, 0,SEEK_CUR) );
			exit(1);
		}
	}
        double t = getTime();
	if(doWrite){
		ret = write(fd, buff, recordSize);
	}else{
		ret = read(fd, buff, recordSize);		
	}
        time[i] = getTime() - t;
 	if(ret != recordSize){
		printf("Error in accessing position:%lld return was:%lld\n", (long long int) lseek(fd, 0,SEEK_CUR), (long long int) ret );
		exit(1);
	}
  }

  close(fd);

  printResults(operations, time, recordSize, doWrite ? "Write" : "Read" );

  free(time);
  free(buff);

  return 0;
}
