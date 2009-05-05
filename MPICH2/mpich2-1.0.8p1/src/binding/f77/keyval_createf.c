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
extern FORT_DLL_SPEC void FORT_CALL MPI_KEYVAL_CREATE( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create__( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create_( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL pmpi_keyval_create_( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak MPI_KEYVAL_CREATE = pmpi_keyval_create__
#pragma weak mpi_keyval_create__ = pmpi_keyval_create__
#pragma weak mpi_keyval_create_ = pmpi_keyval_create__
#pragma weak mpi_keyval_create = pmpi_keyval_create__
#pragma weak pmpi_keyval_create_ = pmpi_keyval_create__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_KEYVAL_CREATE( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak MPI_KEYVAL_CREATE = PMPI_KEYVAL_CREATE
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create__( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak mpi_keyval_create__ = pmpi_keyval_create__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak mpi_keyval_create = pmpi_keyval_create
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create_( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak mpi_keyval_create_ = pmpi_keyval_create_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_KEYVAL_CREATE  MPI_KEYVAL_CREATE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_keyval_create__  mpi_keyval_create__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_keyval_create  mpi_keyval_create
#else
#pragma _HP_SECONDARY_DEF pmpi_keyval_create_  mpi_keyval_create_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_KEYVAL_CREATE as PMPI_KEYVAL_CREATE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_keyval_create__ as pmpi_keyval_create__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_keyval_create as pmpi_keyval_create
#else
#pragma _CRI duplicate mpi_keyval_create_ as pmpi_keyval_create_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_KEYVAL_CREATE( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create__( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_keyval_create_( MPI_Copy_function, MPI_Delete_function, MPI_Fint *, void*, MPI_Fint * );

#pragma weak MPI_KEYVAL_CREATE = mpi_keyval_create__
#pragma weak mpi_keyval_create_ = mpi_keyval_create__
#pragma weak mpi_keyval_create = mpi_keyval_create__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_keyval_create_ PMPI_KEYVAL_CREATE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_keyval_create_ pmpi_keyval_create__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_keyval_create_ pmpi_keyval_create
#else
#define mpi_keyval_create_ pmpi_keyval_create_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Keyval_create
#define MPI_Keyval_create PMPI_Keyval_create 

#else

#ifdef F77_NAME_UPPER
#define mpi_keyval_create_ MPI_KEYVAL_CREATE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_keyval_create_ mpi_keyval_create__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_keyval_create_ mpi_keyval_create
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
 
#ifndef MPICH_MPI_FROM_PMPI
#undef MPI_Comm_create_keyval
#define MPI_Comm_create_keyval PMPI_Comm_create_keyval
#endif
FORT_DLL_SPEC void FORT_CALL mpi_keyval_create_ ( MPI_Copy_function v1, MPI_Delete_function v2, MPI_Fint *v3, void*v4, MPI_Fint *ierr ){
        *ierr = MPI_Comm_create_keyval( v1, v2, v3, v4 );
        if (!*ierr) {
            MPIR_Keyval_set_fortran( *v3 );
        }
}
