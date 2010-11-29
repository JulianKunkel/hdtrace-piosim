/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*  
 *  (C) 2001 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 *
 * This file is automatically generated by buildiface 
 * DO NOT EDIT
 */
#include "mpi_fortimpl.h"

#ifdef MPI_TYPE_NULL_COPY_FN
#undef MPI_TYPE_NULL_COPY_FN
#endif

/* Begin MPI profiling block */
#if defined(USE_WEAK_SYMBOLS) && !defined(USE_ONLY_MPI_NAMES) 
#if defined(HAVE_MULTIPLE_PRAGMA_WEAK)
extern FORT_DLL_SPEC void FORT_CALL MPI_TYPE_NULL_COPY_FN( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn__( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn_( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak MPI_TYPE_NULL_COPY_FN = PMPI_TYPE_NULL_COPY_FN
#pragma weak mpi_type_null_copy_fn__ = PMPI_TYPE_NULL_COPY_FN
#pragma weak mpi_type_null_copy_fn_ = PMPI_TYPE_NULL_COPY_FN
#pragma weak mpi_type_null_copy_fn = PMPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn__
#pragma weak mpi_type_null_copy_fn__ = pmpi_type_null_copy_fn__
#pragma weak mpi_type_null_copy_fn_ = pmpi_type_null_copy_fn__
#pragma weak mpi_type_null_copy_fn = pmpi_type_null_copy_fn__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn_
#pragma weak mpi_type_null_copy_fn__ = pmpi_type_null_copy_fn_
#pragma weak mpi_type_null_copy_fn_ = pmpi_type_null_copy_fn_
#pragma weak mpi_type_null_copy_fn = pmpi_type_null_copy_fn_
#else
#pragma weak MPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn
#pragma weak mpi_type_null_copy_fn__ = pmpi_type_null_copy_fn
#pragma weak mpi_type_null_copy_fn_ = pmpi_type_null_copy_fn
#pragma weak mpi_type_null_copy_fn = pmpi_type_null_copy_fn
#endif



#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_TYPE_NULL_COPY_FN( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#pragma weak MPI_TYPE_NULL_COPY_FN = PMPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn__( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_type_null_copy_fn__ = pmpi_type_null_copy_fn__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_type_null_copy_fn = pmpi_type_null_copy_fn
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn_( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_type_null_copy_fn_ = pmpi_type_null_copy_fn_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_TYPE_NULL_COPY_FN  MPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_type_null_copy_fn__  mpi_type_null_copy_fn__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_type_null_copy_fn  mpi_type_null_copy_fn
#else
#pragma _HP_SECONDARY_DEF pmpi_type_null_copy_fn_  mpi_type_null_copy_fn_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_TYPE_NULL_COPY_FN as PMPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_type_null_copy_fn__ as pmpi_type_null_copy_fn__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_type_null_copy_fn as pmpi_type_null_copy_fn
#else
#pragma _CRI duplicate mpi_type_null_copy_fn_ as pmpi_type_null_copy_fn_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_TYPE_NULL_COPY_FN( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn__( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn_( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak mpi_type_null_copy_fn__ = MPI_TYPE_NULL_COPY_FN
#pragma weak mpi_type_null_copy_fn_ = MPI_TYPE_NULL_COPY_FN
#pragma weak mpi_type_null_copy_fn = MPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_TYPE_NULL_COPY_FN = mpi_type_null_copy_fn__
#pragma weak mpi_type_null_copy_fn_ = mpi_type_null_copy_fn__
#pragma weak mpi_type_null_copy_fn = mpi_type_null_copy_fn__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_TYPE_NULL_COPY_FN = mpi_type_null_copy_fn_
#pragma weak mpi_type_null_copy_fn__ = mpi_type_null_copy_fn_
#pragma weak mpi_type_null_copy_fn = mpi_type_null_copy_fn_
#else
#pragma weak MPI_TYPE_NULL_COPY_FN = mpi_type_null_copy_fn
#pragma weak mpi_type_null_copy_fn__ = mpi_type_null_copy_fn
#pragma weak mpi_type_null_copy_fn_ = mpi_type_null_copy_fn
#endif

#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK)
/* Define the weak versions of the PMPI routine*/
#ifndef F77_NAME_UPPER
extern FORT_DLL_SPEC void FORT_CALL PMPI_TYPE_NULL_COPY_FN( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_2USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_type_null_copy_fn__( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_type_null_copy_fn_( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER
extern FORT_DLL_SPEC void FORT_CALL pmpi_type_null_copy_fn( MPI_Fint*, MPI_Fint*, MPI_Aint *, MPI_Aint *, MPI_Aint *, MPI_Fint *, MPI_Fint * );

#endif

#if defined(F77_NAME_UPPER)
#pragma weak pmpi_type_null_copy_fn__ = PMPI_TYPE_NULL_COPY_FN
#pragma weak pmpi_type_null_copy_fn_ = PMPI_TYPE_NULL_COPY_FN
#pragma weak pmpi_type_null_copy_fn = PMPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak PMPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn__
#pragma weak pmpi_type_null_copy_fn_ = pmpi_type_null_copy_fn__
#pragma weak pmpi_type_null_copy_fn = pmpi_type_null_copy_fn__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak PMPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn_
#pragma weak pmpi_type_null_copy_fn__ = pmpi_type_null_copy_fn_
#pragma weak pmpi_type_null_copy_fn = pmpi_type_null_copy_fn_
#else
#pragma weak PMPI_TYPE_NULL_COPY_FN = pmpi_type_null_copy_fn
#pragma weak pmpi_type_null_copy_fn__ = pmpi_type_null_copy_fn
#pragma weak pmpi_type_null_copy_fn_ = pmpi_type_null_copy_fn
#endif /* Test on name mapping */
#endif /* Use multiple pragma weak */

#ifdef F77_NAME_UPPER
#define mpi_type_null_copy_fn_ PMPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_type_null_copy_fn_ pmpi_type_null_copy_fn__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_type_null_copy_fn_ pmpi_type_null_copy_fn
#else
#define mpi_type_null_copy_fn_ pmpi_type_null_copy_fn_
#endif /* Test on name mapping */

/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_mpi_type_null_copy_fn
#define MPI_mpi_type_null_copy_fn PMPI_mpi_type_null_copy_fn 

#else

#ifdef F77_NAME_UPPER
#define mpi_type_null_copy_fn_ MPI_TYPE_NULL_COPY_FN
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_type_null_copy_fn_ mpi_type_null_copy_fn__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_type_null_copy_fn_ mpi_type_null_copy_fn
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_type_null_copy_fn_ ( MPI_Fint*v1, MPI_Fint*v2, MPI_Aint *v3, MPI_Aint *v4, MPI_Aint *v5, MPI_Fint *v6, MPI_Fint *ierr ){
        *ierr = MPI_SUCCESS;
        *v6 = MPIR_TO_FLOG(0);
}
