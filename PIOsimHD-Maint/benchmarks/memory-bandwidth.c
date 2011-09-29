/*
 Author Julian M. Kunkel
 2010
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h> 

double getTime(){
	struct timeval tv;
	gettimeofday(& tv, NULL);
	return tv.tv_sec + tv.tv_usec * 0.000001;
}

void printResults(long iterations, double * time, double size, char const *  name){
  size = size / 1000 / 1000;
  printf("%s 1,000,000 (M)byte per second: ", name);
  printf("%.10f", size / time[0]);
  for(long i=1; i < iterations; i++){
	printf(",%.8f", size / time[i]);
  }
  printf("\n");
}

int main (int argc, char *argv[])
{
  long size = 10*1024*1024;
  long iterations = 10;
  if (argc > 1){
    size = atol(argv[1]);
  }
  if (argc > 2){
    iterations = atol(argv[2]);
  }

  printf("size: %ld iterations:%ld\n", size, iterations);
  
  long * buffer = malloc(size);


  double t = getTime();   
  for(long i=0 ; i < iterations; i++){
	memset(buffer, i, size);
  }
  double e = getTime() - t;
  printf("Aggregated time: size: %ld iterations:%ld time:%fs MB/s:%f\n", size, iterations, e, iterations * size / 1024 / 1024 / e);


  long max = size / sizeof(long);

  double * time = malloc(iterations * sizeof(double));


  // per iteration time: 
  for(long i=0 ; i < iterations; i++){
	double c = getTime();
	register long value = 0;
	for(long c = 0; c < max ; c++){
		buffer[c] = value;
	}
	time[i] = getTime() - c;
  }

  printResults(iterations, time, size, "Write 64 byte");

  for(long i=0 ; i < iterations; i++){
	double c = getTime();
	volatile register long value = 0;
	for(long c = 0; c < max ; c++){
		value = buffer[c];
	}
	time[i] = getTime() - c;
  }
  printResults(iterations, time, size, "Read 64 byte");

  for(long i=0 ; i < iterations; i++){
	double c = getTime();
	volatile register long value = 0;
	for(long c = 0; c < max ; c++){
		buffer[c] = value;
	}
	for(long c = 0; c < max ; c++){
		value = buffer[c];
	}
	for(long c = 0; c < max ; c++){
		value = buffer[c];
	}
	time[i] = getTime() - c;
  }
  printResults(iterations, time, size, "Read Write Write 64 byte");

  
  return 0;
}
