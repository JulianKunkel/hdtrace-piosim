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
extern FORT_DLL_SPEC void FORT_CALL MPI_INFO_GET_NKEYS( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys__( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys_( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL pmpi_info_get_nkeys_( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak MPI_INFO_GET_NKEYS = pmpi_info_get_nkeys__
#pragma weak mpi_info_get_nkeys__ = pmpi_info_get_nkeys__
#pragma weak mpi_info_get_nkeys_ = pmpi_info_get_nkeys__
#pragma weak mpi_info_get_nkeys = pmpi_info_get_nkeys__
#pragma weak pmpi_info_get_nkeys_ = pmpi_info_get_nkeys__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_INFO_GET_NKEYS( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak MPI_INFO_GET_NKEYS = PMPI_INFO_GET_NKEYS
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys__( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_info_get_nkeys__ = pmpi_info_get_nkeys__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_info_get_nkeys = pmpi_info_get_nkeys
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys_( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_info_get_nkeys_ = pmpi_info_get_nkeys_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_INFO_GET_NKEYS  MPI_INFO_GET_NKEYS
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_info_get_nkeys__  mpi_info_get_nkeys__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_info_get_nkeys  mpi_info_get_nkeys
#else
#pragma _HP_SECONDARY_DEF pmpi_info_get_nkeys_  mpi_info_get_nkeys_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_INFO_GET_NKEYS as PMPI_INFO_GET_NKEYS
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_info_get_nkeys__ as pmpi_info_get_nkeys__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_info_get_nkeys as pmpi_info_get_nkeys
#else
#pragma _CRI duplicate mpi_info_get_nkeys_ as pmpi_info_get_nkeys_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_INFO_GET_NKEYS( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys__( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys( MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys_( MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak MPI_INFO_GET_NKEYS = mpi_info_get_nkeys__
#pragma weak mpi_info_get_nkeys_ = mpi_info_get_nkeys__
#pragma weak mpi_info_get_nkeys = mpi_info_get_nkeys__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_info_get_nkeys_ PMPI_INFO_GET_NKEYS
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_info_get_nkeys_ pmpi_info_get_nkeys__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_info_get_nkeys_ pmpi_info_get_nkeys
#else
#define mpi_info_get_nkeys_ pmpi_info_get_nkeys_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Info_get_nkeys
#define MPI_Info_get_nkeys PMPI_Info_get_nkeys 

#else

#ifdef F77_NAME_UPPER
#define mpi_info_get_nkeys_ MPI_INFO_GET_NKEYS
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_info_get_nkeys_ mpi_info_get_nkeys__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_info_get_nkeys_ mpi_info_get_nkeys
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_info_get_nkeys_ ( MPI_Fint *v1, MPI_Fint *v2, MPI_Fint *ierr ){
    *ierr = MPI_Info_get_nkeys( (MPI_Info)(*v1), v2 );
}
