!  (C) 2008 by Argonne National Laboratory.
!      See COPYRIGHT in top-level directory.       
       MODULE MPI_BASE
       IMPLICIT NONE
!      This module was created by the script buildiface
       INTERFACE
       SUBROUTINE MPI_ABORT(v0,v1,ierror)
       INTEGER v0, v1
       INTEGER ierror
       END SUBROUTINE MPI_ABORT

       SUBROUTINE MPI_START(v0,ierror)
       INTEGER v0
       INTEGER ierror
       END SUBROUTINE MPI_START

       SUBROUTINE MPI_FINALIZED(v0,ierror)
       LOGICAL v0
       INTEGER ierror
       END SUBROUTINE MPI_FINALIZED

       SUBROUTINE MPI_CANCEL(v0,ierror)
       INTEGER v0
       INTEGER ierror
       END SUBROUTINE MPI_CANCEL

       SUBROUTINE MPI_PROBE(v0,v1,v2,v3,ierror)
       USE MPI_CONSTANTS,ONLY:MPI_STATUS_SIZE
       INTEGER v0, v1, v2, v3(MPI_STATUS_SIZE)
       INTEGER ierror
       END SUBROUTINE MPI_PROBE

       SUBROUTINE MPI_INITIALIZED(v0,ierror)
       LOGICAL v0
       INTEGER ierror
       END SUBROUTINE MPI_INITIALIZED

       SUBROUTINE MPI_WAIT(v0,v1,ierror)
       USE MPI_CONSTANTS,ONLY:MPI_STATUS_SIZE
       INTEGER v0, v1(MPI_STATUS_SIZE)
       INTEGER ierror
       END SUBROUTINE MPI_WAIT

       SUBROUTINE MPI_TEST(v0,v1,v2,ierror)
       USE MPI_CONSTANTS,ONLY:MPI_STATUS_SIZE
       INTEGER v0
       LOGICAL v1
       INTEGER v2(MPI_STATUS_SIZE)
       INTEGER ierror
       END SUBROUTINE MPI_TEST

       SUBROUTINE MPI_BARRIER(v0,ierror)
       INTEGER v0
       INTEGER ierror
       END SUBROUTINE MPI_BARRIER

       SUBROUTINE MPI_IPROBE(v0,v1,v2,v3,v4,ierror)
       USE MPI_CONSTANTS,ONLY:MPI_STATUS_SIZE
       INTEGER v0, v1, v2
       LOGICAL v3
       INTEGER v4(MPI_STATUS_SIZE)
       INTEGER ierror
       END SUBROUTINE MPI_IPROBE


        SUBROUTINE MPI_INIT(ierror)
        INTEGER ierror
        END SUBROUTINE MPI_INIT

        SUBROUTINE MPI_INIT_THREAD(v0,v1,ierror)
        INTEGER v0, v1, ierror
        END SUBROUTINE MPI_INIT_THREAD

        FUNCTION MPI_WTIME()
            REAL*8 MPI_WTIME
        END FUNCTION MPI_WTIME
!
        FUNCTION MPI_WTICK()
            REAL*8 MPI_WTICK
        END FUNCTION MPI_WTICK

        FUNCTION PMPI_WTIME()
            REAL*8 PMPI_WTIME
        END FUNCTION PMPI_WTIME
!
        FUNCTION PMPI_WTICK()
            REAL*8 PMPI_WTICK
        END FUNCTION PMPI_WTICK

        SUBROUTINE MPI_NULL_DELETE_FN(a,b,c,d,e)
          INTEGER a,b,c,d,e
        END SUBROUTINE MPI_NULL_DELETE_FN

        SUBROUTINE MPI_DUP_FN(a,b,c,d,e,f,g)
          INTEGER a,b,c,d,e,g
          LOGICAL f
        END SUBROUTINE MPI_DUP_FN

        SUBROUTINE MPI_NULL_COPY_FN(a,b,c,d,e,f,g)
          INTEGER a,b,c,d,e,g
          LOGICAL f
        END SUBROUTINE MPI_NULL_COPY_FN

        SUBROUTINE MPI_COMM_NULL_DELETE_FN(a,b,c,d,e)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,e
          INTEGER (KIND=MPI_ADDRESS_KIND) c, d
        END SUBROUTINE MPI_COMM_NULL_DELETE_FN

        SUBROUTINE MPI_COMM_DUP_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_COMM_DUP_FN

        SUBROUTINE MPI_COMM_NULL_COPY_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_COMM_NULL_COPY_FN

        SUBROUTINE MPI_TYPE_NULL_DELETE_FN(a,b,c,d,e)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,e
          INTEGER (KIND=MPI_ADDRESS_KIND) c, d
        END SUBROUTINE MPI_TYPE_NULL_DELETE_FN

        SUBROUTINE MPI_TYPE_DUP_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_TYPE_DUP_FN

        SUBROUTINE MPI_TYPE_NULL_COPY_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_TYPE_NULL_COPY_FN

        SUBROUTINE MPI_WIN_NULL_DELETE_FN(a,b,c,d,e)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,e
          INTEGER (KIND=MPI_ADDRESS_KIND) c, d
        END SUBROUTINE MPI_WIN_NULL_DELETE_FN

        SUBROUTINE MPI_WIN_DUP_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_WIN_DUP_FN

        SUBROUTINE MPI_WIN_NULL_COPY_FN(a,b,c,d,e,f,g)
          USE MPI_CONSTANTS,ONLY: MPI_ADDRESS_KIND
          INTEGER a,b,g
          INTEGER (KIND=MPI_ADDRESS_KIND) c,d,e
          LOGICAL f
        END SUBROUTINE MPI_WIN_NULL_COPY_FN

       END INTERFACE
       END MODULE MPI_BASE
