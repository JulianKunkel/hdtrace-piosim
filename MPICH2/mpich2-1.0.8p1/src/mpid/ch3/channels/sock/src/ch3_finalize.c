/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*
 *  (C) 2001 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 */

#include "mpidi_ch3_impl.h"

/* FIXME: Who uses this routine?  Should it be a no-op (no routine even)? */
/* FIXME: Should this use the common func enter/exit macros? */
#undef FUNCNAME
#define FUNCNAME MPIDI_CH3_Finalize
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
int MPIDI_CH3_Finalize( void )
{
    int mpi_errno = MPI_SUCCESS;
    MPIDI_STATE_DECL(MPID_STATE_MPID_CH3_FINALIZE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPID_CH3_FINALIZE);

    mpi_errno = MPIDI_CH3I_Progress_finalize();
    if (mpi_errno) { MPIU_ERR_POP(mpi_errno); }

 fn_fail:
    MPIDI_FUNC_EXIT(MPID_STATE_MPID_CH3_FINALIZE);
    return mpi_errno;
}
