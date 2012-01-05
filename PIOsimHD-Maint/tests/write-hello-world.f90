program hello_world
include 'mpif.h'
integer ierr
call MPI_INIT(ierr)

open(20, file='test.txt')
write(20,*) 'hello world'
endfile(20)
close(20)


call MPI_FINALIZE(ierr)
stop
end

