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
extern FORT_DLL_SPEC void FORT_CALL MPI_CLOSE_PORT( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port__( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port_( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL pmpi_close_port_( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_CLOSE_PORT = pmpi_close_port__
#pragma weak mpi_close_port__ = pmpi_close_port__
#pragma weak mpi_close_port_ = pmpi_close_port__
#pragma weak mpi_close_port = pmpi_close_port__
#pragma weak pmpi_close_port_ = pmpi_close_port__


#elif defined(HAVE_PRAGMA_WEAK)

#if defined(F77_NAME_UPPER)
extern FORT_DLL_SPEC void FORT_CALL MPI_CLOSE_PORT( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_CLOSE_PORT = PMPI_CLOSE_PORT
#elif defined(F77_NAME_LOWER_2USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port__( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_close_port__ = pmpi_close_port__
#elif !defined(F77_NAME_LOWER_USCORE)
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_close_port = pmpi_close_port
#else
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port_( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak mpi_close_port_ = pmpi_close_port_
#endif

#elif defined(HAVE_PRAGMA_HP_SEC_DEF)
#if defined(F77_NAME_UPPER)
#pragma _HP_SECONDARY_DEF PMPI_CLOSE_PORT  MPI_CLOSE_PORT
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _HP_SECONDARY_DEF pmpi_close_port__  mpi_close_port__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _HP_SECONDARY_DEF pmpi_close_port  mpi_close_port
#else
#pragma _HP_SECONDARY_DEF pmpi_close_port_  mpi_close_port_
#endif

#elif defined(HAVE_PRAGMA_CRI_DUP)
#if defined(F77_NAME_UPPER)
#pragma _CRI duplicate MPI_CLOSE_PORT as PMPI_CLOSE_PORT
#elif defined(F77_NAME_LOWER_2USCORE)
#pragma _CRI duplicate mpi_close_port__ as pmpi_close_port__
#elif !defined(F77_NAME_LOWER_USCORE)
#pragma _CRI duplicate mpi_close_port as pmpi_close_port
#else
#pragma _CRI duplicate mpi_close_port_ as pmpi_close_port_
#endif
#endif /* HAVE_PRAGMA_WEAK */
#endif /* USE_WEAK_SYMBOLS */
/* End MPI profiling block */


/* These definitions are used only for generating the Fortran wrappers */
#if defined(USE_WEAK_SYBMOLS) && defined(HAVE_MULTIPLE_PRAGMA_WEAK) && \
    defined(USE_ONLY_MPI_NAMES)
extern FORT_DLL_SPEC void FORT_CALL MPI_CLOSE_PORT( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port__( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );
extern FORT_DLL_SPEC void FORT_CALL mpi_close_port_( char * FORT_MIXED_LEN_DECL, MPI_Fint * FORT_END_LEN_DECL );

#pragma weak MPI_CLOSE_PORT = mpi_close_port__
#pragma weak mpi_close_port_ = mpi_close_port__
#pragma weak mpi_close_port = mpi_close_port__
#endif

/* Map the name to the correct form */
#ifndef MPICH_MPI_FROM_PMPI
#ifdef F77_NAME_UPPER
#define mpi_close_port_ PMPI_CLOSE_PORT
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_close_port_ pmpi_close_port__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_close_port_ pmpi_close_port
#else
#define mpi_close_port_ pmpi_close_port_
#endif
/* This defines the routine that we call, which must be the PMPI version
   since we're renaming the Fortran entry as the pmpi version.  The MPI name
   must be undefined first to prevent any conflicts with previous renamings,
   such as those put in place by the globus device when it is building on
   top of a vendor MPI. */
#undef MPI_Close_port
#define MPI_Close_port PMPI_Close_port 

#else

#ifdef F77_NAME_UPPER
#define mpi_close_port_ MPI_CLOSE_PORT
#elif defined(F77_NAME_LOWER_2USCORE)
#define mpi_close_port_ mpi_close_port__
#elif !defined(F77_NAME_LOWER_USCORE)
#define mpi_close_port_ mpi_close_port
/* Else leave name alone */
#endif


#endif /* MPICH_MPI_FROM_PMPI */

/* Prototypes for the Fortran interfaces */
#include "fproto.h"
FORT_DLL_SPEC void FORT_CALL mpi_close_port_ ( char *v1 FORT_MIXED_LEN(d1), MPI_Fint *ierr FORT_END_LEN(d1) ){
    char *p1;

    {char *p = v1 + d1 - 1;
     int  li;
        while (*p == ' ' && p > v1) p--;
        p++;
        p1 = (char *)MPIU_Malloc( p-v1 + 1 );
        for (li=0; li<(p-v1); li++) { p1[li] = v1[li]; }
        p1[li] = 0; 
    }
    *ierr = MPI_Close_port( p1 );
    MPIU_Free( p1 );
}
