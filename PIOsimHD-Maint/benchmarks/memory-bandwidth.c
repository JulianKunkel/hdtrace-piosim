/*
 @author Julian M. Kunkel
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
  long numberOfTimingsPerIteration = 10;
  if (argc > 1){
    size = atol(argv[1]);
  }
  if (argc > 2){
    iterations = atol(argv[2]);
  }
  if (argc > 3){
    numberOfTimingsPerIteration = atol(argv[3]);
  }

  printf("size: %ld iterations:%ld numberOfTimingsPerIteration:%ld \n", size, iterations, numberOfTimingsPerIteration);
  
  long * buffer = malloc(size);


  double t = getTime();   
  for(long i=0 ; i < iterations; i++){
	memset(buffer, i, size);
  }
  double e = getTime() - t;
  printf("Aggregated time: size: %ld iterations:%ld time:%fs MB/s:%f\n", size, iterations, e, iterations * size / 1024 / 1024 / e);


  double * time = malloc(iterations * sizeof(double) * numberOfTimingsPerIteration);


  long bytesPerTimeing = (long) size / numberOfTimingsPerIteration;
  long maxIterCountPerTiming = bytesPerTimeing / sizeof(long);

  // per iteration time: 
  for(long i=0 ; i < iterations; i++){
	volatile register long value = 0;

	for(int t = 0; t < numberOfTimingsPerIteration; t++){
		long * tmp = & buffer[maxIterCountPerTiming * t];

		double c = getTime();
		for(long c = 0; c < maxIterCountPerTiming ; c++){
			tmp[c] = value;
		}
		time[i*numberOfTimingsPerIteration + t] = getTime() - c;
	}
  }

  printResults(iterations*numberOfTimingsPerIteration, time, bytesPerTimeing, "Write 64 byte");

  for(long i=0 ; i < iterations; i++){
	volatile register long value = 0;

	for(int t = 0; t < numberOfTimingsPerIteration; t++){
		long * tmp = & buffer[maxIterCountPerTiming * t];

		double c = getTime();
		for(long c = 0; c < maxIterCountPerTiming ; c++){
			value = tmp[c];
		}
		time[i*numberOfTimingsPerIteration + t] = getTime() - c;
	}
  }
  printResults(iterations*numberOfTimingsPerIteration, time, bytesPerTimeing, "Read 64 byte");

  for(long i=0 ; i < iterations; i++){
	volatile register long value = 0;

	for(int t = 0; t < numberOfTimingsPerIteration; t++){
		long * tmp = & buffer[maxIterCountPerTiming * t];

		double c = getTime();
		for(long c = 0; c < maxIterCountPerTiming ; c++){
			value = tmp[c];
		}
		for(long c = 0; c < maxIterCountPerTiming ; c++){
			tmp[c] = value;
		}
		for(long c = 0; c < maxIterCountPerTiming ; c++){
			tmp[c] = value;
		}
		time[i*numberOfTimingsPerIteration + t] = getTime() - c;
	}
  }
  printResults(iterations*numberOfTimingsPerIteration, time, bytesPerTimeing, "Read Write Write 64 byte");

  
  return 0;
}
