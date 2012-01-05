c  This is a example of the Fortran 77 standard
      program example
      include 'mpif.h'
      integer err
      PRINT '(A)', 'Starting'

      call mpi_init(err)
      call mpi_barrier(MPI_COMM_WORLD, err)
      PRINT '(A)', 'Hello, world'
      call mpi_finalize(err)
      end

