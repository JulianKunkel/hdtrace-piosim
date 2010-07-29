#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>



int main(int argc, char ** argv) {
  initDynamicLoader();

  int fd;
  fd = open("MYFILE.txt", O_RDWR | O_TRUNC | O_CREAT, 0x777);

  char buff[255];
  sprintf(buff, "%s\n ", "Hello World, Where there is will, there is a way.");
  write(fd,buff, 255);
  close(fd) ;

  return 0;
}
