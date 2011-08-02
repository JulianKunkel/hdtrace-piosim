#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>




// Author J.K. 2011
// Determine the CPU speed.

// This function is not thread-safe and returns a value from the stack!
static char * getValueFromProc(char const * const what){
	// TODO generalize this function
        char buff[102400];
	
        int fd = open("/proc/cpuinfo", O_RDONLY);
	if (fd <= 0){
		printf("Error /proc/cpuinfo not found\n");
		return NULL;
	}
	
        int ret = read(fd, buff, 102400 - 1 );

        buff[ret >= 102400 ? 102400-1: ret] = 0;
	
        char * line = strstr(buff, what);
	

	if (line == 0){
		printf("Error %s not found in %s \n", what, buff);
		return NULL;
	}

	line += strlen(what) + 1;

        while(line[0] == ' ' || line[0] == ':'|| line[0] == '\t'){
                line++;
        }
        
        int pos = 0;
        while(line[pos] != '\n'){
                pos++;
        }
        line[pos] = 0;
	
        close(fd);

        return strdup(line);
}




#define DEFAULT_MHZ 100000
unsigned processorCPUspeedinMHZ(){
	char * val = getValueFromProc("cpu MHz");
	if(val == NULL || strlen(val) < 2){
	  return DEFAULT_MHZ;
	}
	int mhz = atoi(val);
	
	if(mhz > 0){	 	
	  return (unsigned) mhz;
	}else{
	  return DEFAULT_MHZ;
	}
}


char * processorModelName(){
    char * val = getValueFromProc("model name");
    
    if(val == NULL || strlen(val) < 2){
      return strdup("Unknown Model");
    }
    return val;
}
