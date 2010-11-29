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
extern FORT_DLL_SPEC void FORT_CALL MPI_QUERY_THREAD( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread__( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread_( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL pmpi_query_thread_( MPI_Fint *, MPI_Fint * );

#pragma weak MPI_QUERY_THREAD = pmpi_query_thread__
#pragma weak mpi_query_thread__ = pmpi_query_thread__
#pragma weak mpi_query_thread_ = pmpi_query_thread__
#pragma weak mpi_query_thread = pmpi_query_thread__
#pragma weak pmpi_query_thread_ = pmpi_query_thread__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_QUERY_THREAD( MPI_Fint *, MPI_Fint * );

#pragma weak MPI_QUERY_THREAD = PMPI_QUERY_THREAD
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread__( MPI_Fint *, MPI_Fint * );

#pragma weak mpi_query_thread__ = pmpi_query_thread__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread( MPI_Fint *, MPI_Fint * );

#pragma weak mpi_query_thread = pmpi_query_thread
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread_( MPI_Fint *, MPI_Fint * );

#pragma weak mpi_query_thread_ = pmpi_query_thread_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_QUERY_THREAD  MPI_QUERY_THREAD
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_query_thread__  mpi_query_thread__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_query_thread  mpi_query_thread
#else
#pragma _HP_SECONDARY_DEF pmpi_query_thread_  mpi_query_thread_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_QUERY_THREAD as PMPI_QUERY_THREAD
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_query_thread__ as pmpi_query_thread__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_query_thread as pmpi_query_thread
#else
#pragma _CRI duplicate mpi_query_thread_ as pmpi_query_thread_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_QUERY_THREAD( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread__( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread( MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_query_thread_( MPI_Fint *, MPI_Fint * );

#pragma weak MPI_QUERY_THREAD = mpi_query_thread__
#pragma weak mpi_query_thread_ = mpi_query_thread__
#pragma weak mpi_query_thread = mpi_query_thread__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_query_thread_ PMPI_QUERY_THREAD
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_query_thread_ pmpi_query_thread__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_query_thread_ pmpi_query_thread
#else
#define mpi_query_thread_ pmpi_query_thread_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Query_thread
#define MPI_Query_thread PMPI_Query_thread 

#else

#ifdef F77_NAME_UPPER
#define mpi_query_thread_ MPI_QUERY_THREAD
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_query_thread_ mpi_query_thread__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_query_thread_ mpi_query_thread
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_query_thread_ ( MPI_Fint *v1, MPI_Fint *ierr ){
    *ierr = MPI_Query_thread( v1 );
}
