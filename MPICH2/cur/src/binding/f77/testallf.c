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
extern FORT_DLL_SPEC void FORT_CALL MPI_TESTALL( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall__( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall_( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak MPI_TESTALL = PMPI_TESTALL
#pragma weak mpi_testall__ = PMPI_TESTALL
#pragma weak mpi_testall_ = PMPI_TESTALL
#pragma weak mpi_testall = PMPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_TESTALL = pmpi_testall__
#pragma weak mpi_testall__ = pmpi_testall__
#pragma weak mpi_testall_ = pmpi_testall__
#pragma weak mpi_testall = pmpi_testall__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_TESTALL = pmpi_testall_
#pragma weak mpi_testall__ = pmpi_testall_
#pragma weak mpi_testall_ = pmpi_testall_
#pragma weak mpi_testall = pmpi_testall_
#else
#pragma weak MPI_TESTALL = pmpi_testall
#pragma weak mpi_testall__ = pmpi_testall
#pragma weak mpi_testall_ = pmpi_testall
#pragma weak mpi_testall = pmpi_testall
#endif



#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_TESTALL( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak MPI_TESTALL = PMPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_testall__( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_testall__ = pmpi_testall__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_testall( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_testall = pmpi_testall
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_testall_( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#pragma weak mpi_testall_ = pmpi_testall_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_TESTALL  MPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_testall__  mpi_testall__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_testall  mpi_testall
#else
#pragma _HP_SECONDARY_DEF pmpi_testall_  mpi_testall_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_TESTALL as PMPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_testall__ as pmpi_testall__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_testall as pmpi_testall
#else
#pragma _CRI duplicate mpi_testall_ as pmpi_testall_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_TESTALL( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall__( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
extern FORT_DLL_SPEC void FORT_CALL mpi_testall_( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#if defined(F77_NAME_UPPER)
#pragma weak mpi_testall__ = MPI_TESTALL
#pragma weak mpi_testall_ = MPI_TESTALL
#pragma weak mpi_testall = MPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak MPI_TESTALL = mpi_testall__
#pragma weak mpi_testall_ = mpi_testall__
#pragma weak mpi_testall = mpi_testall__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak MPI_TESTALL = mpi_testall_
#pragma weak mpi_testall__ = mpi_testall_
#pragma weak mpi_testall = mpi_testall_
#else
#pragma weak MPI_TESTALL = mpi_testall
#pragma weak mpi_testall__ = mpi_testall
#pragma weak mpi_testall_ = mpi_testall
#endif

#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#if defined(USE_WEAK_SYMBOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK)
/* Define the weak versions of the PMPI routine*/
#ifndef F77_NAME_UPPER
extern FORT_DLL_SPEC void FORT_CALL PMPI_TESTALL( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_2USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_testall__( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER_USCORE
extern FORT_DLL_SPEC void FORT_CALL pmpi_testall_( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );
#endif
#ifndef F77_NAME_LOWER
extern FORT_DLL_SPEC void FORT_CALL pmpi_testall( MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint *, MPI_Fint * );

#endif

#if defined(F77_NAME_UPPER)
#pragma weak pmpi_testall__ = PMPI_TESTALL
#pragma weak pmpi_testall_ = PMPI_TESTALL
#pragma weak pmpi_testall = PMPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma weak PMPI_TESTALL = pmpi_testall__
#pragma weak pmpi_testall_ = pmpi_testall__
#pragma weak pmpi_testall = pmpi_testall__
#elif defined(F77_NAME_LOWER_USCORE)
#pragma weak PMPI_TESTALL = pmpi_testall_
#pragma weak pmpi_testall__ = pmpi_testall_
#pragma weak pmpi_testall = pmpi_testall_
#else
#pragma weak PMPI_TESTALL = pmpi_testall
#pragma weak pmpi_testall__ = pmpi_testall
#pragma weak pmpi_testall_ = pmpi_testall
#endif /* Test on name mapping */
#endif /* Use multiple pragma weak */

#ifdef F77_NAME_UPPER
#define mpi_testall_ PMPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_testall_ pmpi_testall__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_testall_ pmpi_testall
#else
#define mpi_testall_ pmpi_testall_
#endif /* Test on name mapping */

/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Testall
#define MPI_Testall PMPI_Testall 

#else

#ifdef F77_NAME_UPPER
#define mpi_testall_ MPI_TESTALL
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_testall_ mpi_testall__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_testall_ mpi_testall
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_testall_ ( MPI_Fint *v1, MPI_Fint *v2, MPI_Fint *v3, MPI_Fint *v4, MPI_Fint *ierr ){
    int l3;

#ifndef HAVE_MPI_F_INIT_WORKS_WITH_C
    if (MPIR_F_NeedInit){ mpirinitf_(); MPIR_F_NeedInit = 0; }
#endif

    if (v4 == MPI_F_STATUSES_IGNORE) { v4 = (MPI_Fint *)MPI_STATUSES_IGNORE; }
    *ierr = MPI_Testall( *v1, (MPI_Request *)(v2), &l3, (MPI_Status *)v4 );
    *v3 = MPIR_TO_FLOG(l3);
}