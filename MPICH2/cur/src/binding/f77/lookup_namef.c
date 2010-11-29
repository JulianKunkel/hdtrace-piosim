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
extern FORT_DLL_SPEC void FORT_CALL MPI_LOOKUP_NAME( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL pmpi_lookup_name_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak MPI_LOOKUP_NAME = pmpi_lookup_name__
#pragma weak mpi_lookup_name__ = pmpi_lookup_name__
#pragma weak mpi_lookup_name_ = pmpi_lookup_name__
#pragma weak mpi_lookup_name = pmpi_lookup_name__
#pragma weak pmpi_lookup_name_ = pmpi_lookup_name__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_LOOKUP_NAME( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak MPI_LOOKUP_NAME = PMPI_LOOKUP_NAME
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak mpi_lookup_name__ = pmpi_lookup_name__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak mpi_lookup_name = pmpi_lookup_name
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak mpi_lookup_name_ = pmpi_lookup_name_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_LOOKUP_NAME  MPI_LOOKUP_NAME
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_lookup_name__  mpi_lookup_name__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_lookup_name  mpi_lookup_name
#else
#pragma _HP_SECONDARY_DEF pmpi_lookup_name_  mpi_lookup_name_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_LOOKUP_NAME as PMPI_LOOKUP_NAME
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_lookup_name__ as pmpi_lookup_name__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_lookup_name as pmpi_lookup_name
#else
#pragma _CRI duplicate mpi_lookup_name_ as pmpi_lookup_name_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_LOOKUP_NAME( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name__( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_lookup_name_( char * FORT_MIXED_LEN_DECL, MPI_Fint *, char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL FORT_END_LEN_DECL );

#pragma weak MPI_LOOKUP_NAME = mpi_lookup_name__
#pragma weak mpi_lookup_name_ = mpi_lookup_name__
#pragma weak mpi_lookup_name = mpi_lookup_name__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_lookup_name_ PMPI_LOOKUP_NAME
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_lookup_name_ pmpi_lookup_name__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_lookup_name_ pmpi_lookup_name
#else
#define mpi_lookup_name_ pmpi_lookup_name_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Lookup_name
#define MPI_Lookup_name PMPI_Lookup_name 

#else

#ifdef F77_NAME_UPPER
#define mpi_lookup_name_ MPI_LOOKUP_NAME
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_lookup_name_ mpi_lookup_name__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_lookup_name_ mpi_lookup_name
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_lookup_name_ ( char *v1 FORT_MIXED_LEN(d1), MPI_Fint *v2, char *v3 FORT_MIXED_LEN(d3), MPI_Fint *ierr FORT_END_LEN(d1) FORT_END_LEN(d3) ){
    char *p1;
    char *p3;

    {char *p = v1 + d1 - 1;
     int  li;
        while (*p == ' ' && p > v1) p--;
        p++;
        p1 = (char *)MPIU_Malloc( p-v1 + 1 );
        for (li=0; li<(p-v1); li++) { p1[li] = v1[li]; }
        p1[li] = 0; 
    }
    p3 = (char *)MPIU_Malloc( d3 + 1 );
    *ierr = MPI_Lookup_name( p1, (MPI_Info)(*v2), p3 );
    MPIU_Free( p1 );

    {char *p = v3, *pc=p3;
        while (*pc) {*p++ = *pc++;}
        while ((p-v3) < d3) { *p++ = ' '; }
    }
    MPIU_Free( p3 );
}
