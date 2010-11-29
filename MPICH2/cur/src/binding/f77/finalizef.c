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
#if defined(HAVE_MULTIPLE_PRAGMA_WEAK)
extern FORT_DLL_SPEC void FORT_CALL MPI_FINALIZE( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize__( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize_( MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak MPI_FINALIZE = PMPI_FINALIZE
#pragma weak mpi_finalize__ = PMPI_FINALIZE
#pragma weak mpi_finalize_ = PMPI_FINALIZE
#pragma weak mpi_finalize = PMPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_FINALIZE = pmpi_finalize__
#pragma weak mpi_finalize__ = pmpi_finalize__
#pragma weak mpi_finalize_ = pmpi_finalize__
#pragma weak mpi_finalize = pmpi_finalize__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_FINALIZE = pmpi_finalize_
#pragma weak mpi_finalize__ = pmpi_finalize_
#pragma weak mpi_finalize_ = pmpi_finalize_
#pragma weak mpi_finalize = pmpi_finalize_
#else
#pragma weak MPI_FINALIZE = pmpi_finalize
#pragma weak mpi_finalize__ = pmpi_finalize
#pragma weak mpi_finalize_ = pmpi_finalize
#pragma weak mpi_finalize = pmpi_finalize
#endif



#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_FINALIZE( MPI_Fint * );

#pragma weak MPI_FINALIZE = PMPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize__( MPI_Fint * );

#pragma weak mpi_finalize__ = pmpi_finalize__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize( MPI_Fint * );

#pragma weak mpi_finalize = pmpi_finalize
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize_( MPI_Fint * );

#pragma weak mpi_finalize_ = pmpi_finalize_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_FINALIZE  MPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_finalize__  mpi_finalize__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_finalize  mpi_finalize
#else
#pragma _HP_SECONDARY_DEF pmpi_finalize_  mpi_finalize_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_FINALIZE as PMPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_finalize__ as pmpi_finalize__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_finalize as pmpi_finalize
#else
#pragma _CRI duplicate mpi_finalize_ as pmpi_finalize_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_FINALIZE( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize__( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize( MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_finalize_( MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak mpi_finalize__ = MPI_FINALIZE
#pragma weak mpi_finalize_ = MPI_FINALIZE
#pragma weak mpi_finalize = MPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_FINALIZE = mpi_finalize__
#pragma weak mpi_finalize_ = mpi_finalize__
#pragma weak mpi_finalize = mpi_finalize__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_FINALIZE = mpi_finalize_
#pragma weak mpi_finalize__ = mpi_finalize_
#pragma weak mpi_finalize = mpi_finalize_
#else
#pragma weak MPI_FINALIZE = mpi_finalize
#pragma weak mpi_finalize__ = mpi_finalize
#pragma weak mpi_finalize_ = mpi_finalize
#endif

#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK)
/* Define the weak versions of the PMPI routine*/
#ifndef F77_NAME_UPPER
extern FORT_DLL_SPEC void FORT_CALL PMPI_FINALIZE( MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_2USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_finalize__( MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_finalize_( MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER
extern FORT_DLL_SPEC void FORT_CALL pmpi_finalize( MPI_Fint * );

#endif

#if defined(F77_NAME_UPPER)
#pragma weak pmpi_finalize__ = PMPI_FINALIZE
#pragma weak pmpi_finalize_ = PMPI_FINALIZE
#pragma weak pmpi_finalize = PMPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak PMPI_FINALIZE = pmpi_finalize__
#pragma weak pmpi_finalize_ = pmpi_finalize__
#pragma weak pmpi_finalize = pmpi_finalize__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak PMPI_FINALIZE = pmpi_finalize_
#pragma weak pmpi_finalize__ = pmpi_finalize_
#pragma weak pmpi_finalize = pmpi_finalize_
#else
#pragma weak PMPI_FINALIZE = pmpi_finalize
#pragma weak pmpi_finalize__ = pmpi_finalize
#pragma weak pmpi_finalize_ = pmpi_finalize
#endif /* Test on name mapping */
#endif /* Use multiple pragma weak */

#ifdef F77_NAME_UPPER
#define mpi_finalize_ PMPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_finalize_ pmpi_finalize__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_finalize_ pmpi_finalize
#else
#define mpi_finalize_ pmpi_finalize_
#endif /* Test on name mapping */

/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Finalize
#define MPI_Finalize PMPI_Finalize 

#else

#ifdef F77_NAME_UPPER
#define mpi_finalize_ MPI_FINALIZE
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_finalize_ mpi_finalize__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_finalize_ mpi_finalize
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_finalize_ ( MPI_Fint *ierr ){
    *ierr = MPI_Finalize(  );
}
