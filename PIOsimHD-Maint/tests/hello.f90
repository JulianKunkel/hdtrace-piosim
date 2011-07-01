program hello_world
include 'mpif.h'
integer ierr
call MPI_INIT(ierr)
print *, "hello world"
call MPI_FINALIZE(ierr)
WRITE (6,7)
7 FORMAT(13H HELLO, WORLD)
stop
end

