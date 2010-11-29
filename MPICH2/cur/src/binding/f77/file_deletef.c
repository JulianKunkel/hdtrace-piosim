/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*  
 *  (C) 2001 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 *
 * This file is automatically generated by buildiface 
 * DO NOT EDIT
 */
#include "mpi_fortimpl.h"


/* Begin MPI profiling block */
#if defined(USE_WEAK_SYMBOLS) && !defined(USE_ONLY_MPI_NAMES) 
#if defined(HAVE_MULTIPLE_PRAGMA_WEAK) && defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL MPI_FILE_DELETE( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL pmpi_file_delete_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_FILE_DELETE = pmpi_file_delete__
#pragma weak mpi_file_delete__ = pmpi_file_delete__
#pragma weak mpi_file_delete_ = pmpi_file_delete__
#pragma weak mpi_file_delete = pmpi_file_delete__
#pragma weak pmpi_file_delete_ = pmpi_file_delete__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_FILE_DELETE( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_FILE_DELETE = PMPI_FILE_DELETE
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_file_delete__ = pmpi_file_delete__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_file_delete = pmpi_file_delete
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_file_delete_ = pmpi_file_delete_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_FILE_DELETE  MPI_FILE_DELETE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_file_delete__  mpi_file_delete__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_file_delete  mpi_file_delete
#else
#pragma _HP_SECONDARY_DEF pmpi_file_delete_  mpi_file_delete_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_FILE_DELETE as PMPI_FILE_DELETE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_file_delete__ as pmpi_file_delete__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_file_delete as pmpi_file_delete
#else
#pragma _CRI duplicate mpi_file_delete_ as pmpi_file_delete_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_FILE_DELETE( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_file_delete_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_FILE_DELETE = mpi_file_delete__
#pragma weak mpi_file_delete_ = mpi_file_delete__
#pragma weak mpi_file_delete = mpi_file_delete__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_file_delete_ PMPI_FILE_DELETE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_file_delete_ pmpi_file_delete__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_file_delete_ pmpi_file_delete
#else
#define mpi_file_delete_ pmpi_file_delete_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_File_delete
#define MPI_File_delete PMPI_File_delete 

#else

#ifdef F77_NAME_UPPER
#define mpi_file_delete_ MPI_FILE_DELETE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_file_delete_ mpi_file_delete__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_file_delete_ mpi_file_delete
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_file_delete_ ( char *v1 FORT_MIXED_LEN(d1), MPI_Fint *v2, MPI_Fint *ierr FORT_END_LEN(d1) ){
#ifdef MPI_MODE_RDONLY
    char *p1;

    {char *p = v1 + d1 - 1;
     int  li;
        while (*p == ' ' && p > v1) p--;
        p++;
        p1 = (char *)MPIU_Malloc( p-v1 + 1 );
        for (li=0; li<(p-v1); li++) { p1[li] = v1[li]; }
        p1[li] = 0; 
    }
    *ierr = MPI_File_delete( p1, (MPI_Info)(*v2) );
    MPIU_Free( p1 );
#else
*ierr = MPI_ERR_INTERN;
#endif
}
