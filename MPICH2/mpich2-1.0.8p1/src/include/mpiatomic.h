/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*
 *  (C) 2001 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 */

#ifndef MPIIMPLATOMIC_H_INCLUDED
#define MPIIMPLATOMIC_H_INCLUDED

#include <stddef.h>
#include <string.h>
#include "mpidu_atomic_primitives.h"
#include "mpidu_mem_barriers.h"
#include "mpidu_queue.h"

/*
    Shared Memory Operation Abstractions
    ------------------------------------
    This file defines a number of routines and data structures for managing
    interactions with shared memory.  Code that needs to manage shared memory
    should either use an existing abstraction or define a new one rather than
    directly using atomic primitive operations.  Using these abstractions has
    several advantages:
    - The code that uses these abstractions should have fewer bugs because it is
      easier to reason about the behavior of one of these abstract operations.
    - Clients of this code have the opportunity to be more portable and more
      performant on various plaforms because different platform-dependent
      implementations of the abstract behavior can be used with no impact to
      client correctness.
    - It provides us (the mpich developers) the flexibility to easily vary the
      completeness and implementation of our atomic primitives in the future.
*/

/* ======================================================
   reference counting routines
   ====================================================== */

/* Atomically increment a reference count */
#undef FUNCNAME
#define FUNCNAME MPIDU_Ref_add
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline void MPIDU_Ref_add(volatile int *ptr)
{
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_REF_ADD);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_REF_ADD);
    MPIDU_Atomic_incr(ptr);
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_REF_ADD);
}

/* Atomically decrement a reference count and test if the result is equal to
   zero.  Returns true if this was the final reference released. */
#undef FUNCNAME
#define FUNCNAME MPIDU_Ref_release_and_test
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline int MPIDU_Ref_release_and_test(volatile int *ptr)
{
    int retval;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_REF_RELEASE_AND_TEST);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_REF_RELEASE_AND_TEST);

#if defined(ATOMIC_DECR_AND_TEST_IS_EMULATED) && !defined(ATOMIC_FETCH_AND_DECR_IS_EMULATED)
    int prev;
    prev = MPIDU_Atomic_fetch_and_decr(ptr);
    retval = (1 == prev);
    goto fn_exit;
#elif defined(ATOMIC_DECR_AND_TEST_IS_EMULATED) && !defined(ATOMIC_CAS_INT_IS_EMULATED)
    int oldv, newv;
    do {
        oldv = *ptr;
        newv = oldv - 1;
    } while (oldv != MPIDU_Atomic_cas_int(ptr, oldv, newv));
    retval = (0 == newv);
    goto fn_exit;

    /* sketch of an LL/SC impl
#elif defined(ATOMIC_DECR_AND_TEST_IS_EMULATED) && defined(ATOMIC_LL_SC_SUPPORTED)
    int val;
    do {
        val = LL(ptr);
        --val;
    } while (!SC(ptr, val));
    retval = (0 == val);
    goto fn_exit;
    */
#else
    retval = MPIDU_Atomic_decr_and_test(ptr);
    goto fn_exit;
#endif

fn_exit:
    MPIU_Assert(*ptr >= 0); /* help find add/release mismatches */
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_REF_RELEASE_AND_TEST);
    return retval;
}

/* ======================================================
   Ownership Mechanisms
   ======================================================

   This section provides a set of routines for asserting ownership over an
   object.  It does so specifically by associating an owner ID with the object.
   
   Since this abstraction is not so different from a plain old mutex that has
   try_acquire() functionality, it might make sense to refactor it in the future
   into a general purpose mutex instead. */

#define MPIDU_OWNER_ID_NONE -1
#define MPIDU_OWNER_ID_ANY  -2
typedef int MPIDU_Owner_id;

/* This struct should be treated opaquely by any clients of the MPIDU_Owner_*
   functions.  In the future it could change to a different implementation or
   multiple implementations. */
typedef struct MPIDU_Owner_info {
    volatile MPIDU_Owner_id id;
    /* allow clients to pad to cache-line, don't pad here */
} MPIDU_Owner_info;

typedef enum MPIDU_Owner_result {
    MPIDU_OWNERSHIP_ERROR = -1,
    MPIDU_OWNERSHIP_ACQUIRED,
    MPIDU_OWNERSHIP_ALREADY_ACQUIRED,
    MPIDU_OWNERSHIP_ALREADY_RELEASED,
    MPIDU_OWNERSHIP_BUSY,
    MPIDU_OWNERSHIP_RELEASED
} MPIDU_Owner_result;


/*
    Will initialize the pre-allocated shared 'info' structure with appropriate
    starting values.  'my_id' should be a unique identifier that is NOT equal to
    MPIDU_OWNER_ID_NONE (-1).  All processes that will be participating in the
    ownership algorithm must call this function prior to any calls by any
    process to MPIDU_Owner_acquire.
*/
#undef FUNCNAME
#define FUNCNAME MPIDU_Owner_init
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline void MPIDU_Owner_init(volatile MPIDU_Owner_info *info)
{
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_OWNER_INIT);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_OWNER_INIT);

    /* It's OK if multiple processes call this on the same bit of shared_info
       because of the restriction given in the comment above. */
    info->id = MPIDU_OWNER_ID_NONE;

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_OWNER_INIT);
}

/* 'after_owner' is an OUT value */
#undef FUNCNAME
#define FUNCNAME MPIDU_Owner_try_acquire
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
MPIDU_Owner_result MPIDU_Owner_try_acquire(volatile MPIDU_Owner_info *info,
                                           MPIDU_Owner_id my_id,
                                           MPIDU_Owner_id *after_owner)
{
    /* A pthread_mutex_trylock version of this would be an excellent alternative
       implementation for fairly broad portability. */
    MPIDU_Owner_result retval;
    MPIDU_Owner_id prev_id;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_OWNER_TRY_ACQUIRE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_OWNER_TRY_ACQUIRE);

    MPIU_DBG_MSG_D(ALL, VERBOSE, "... my_id=%d", my_id);
    MPIU_DBG_MSG_P(ALL, VERBOSE, "... &info->id=%p", &info->id);

    prev_id = MPIDU_Atomic_cas_int(&info->id, MPIDU_OWNER_ID_NONE, my_id);
    if (after_owner) *after_owner = my_id;

    if (my_id == prev_id) {
        retval = MPIDU_OWNERSHIP_ALREADY_ACQUIRED;
        MPIU_DBG_MSG(ALL, VERBOSE, "... already acquired");
    }
    else if (MPIDU_OWNER_ID_NONE == prev_id) {
        retval = MPIDU_OWNERSHIP_ACQUIRED;
        MPIU_DBG_MSG(ALL, VERBOSE, "... acquired");
    }
    else {
        if (after_owner) *after_owner = prev_id;
        retval = MPIDU_OWNERSHIP_BUSY;
        MPIU_DBG_MSG_D(ALL, VERBOSE, "... busy, prev_id=%d", prev_id);
    }

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_OWNER_TRY_ACQUIRE);
    return retval;
}

#undef FUNCNAME
#define FUNCNAME MPIDU_Owner_release
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
/* 'after_owner' is an OUT value */
static inline
MPIDU_Owner_result MPIDU_Owner_release(volatile MPIDU_Owner_info *info,
                                       MPIDU_Owner_id my_id,
                                       MPIDU_Owner_id *after_owner)
{
    MPIDU_Owner_result retval;
    MPIDU_Owner_id prev_id;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_OWNER_RELEASE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_OWNER_RELEASE);

    MPIU_DBG_MSG_D(ALL, VERBOSE, "... my_id=%d", my_id);

    if (after_owner) *after_owner = MPIDU_OWNER_ID_NONE;

    if (MPIDU_OWNER_ID_ANY == my_id) {
        prev_id = MPIDU_Atomic_swap_int(&info->id, MPIDU_OWNER_ID_NONE);
        if (MPIDU_OWNER_ID_NONE == prev_id) {
            retval = MPIDU_OWNERSHIP_ALREADY_RELEASED;
        }
        else {
            retval = MPIDU_OWNERSHIP_RELEASED;
        }
    }
    else {
        prev_id = MPIDU_Atomic_cas_int(&info->id, my_id, MPIDU_OWNER_ID_NONE);

        if (my_id == prev_id) {
            retval = MPIDU_OWNERSHIP_RELEASED;
            MPIU_DBG_MSG(ALL, VERBOSE, "... released");
        }
        else if (MPIDU_OWNER_ID_NONE == prev_id) {
            retval = MPIDU_OWNERSHIP_ALREADY_RELEASED;
            MPIU_DBG_MSG(ALL, VERBOSE, "... already released");
        }
        else {
            if (after_owner) *after_owner = prev_id;
            retval = MPIDU_OWNERSHIP_BUSY;
            MPIU_DBG_MSG_D(ALL, VERBOSE, "... busy, prev_id=%d", prev_id);
        }
    }

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_OWNER_RELEASE);
    return retval;
}

#undef FUNCNAME
#define FUNCNAME MPIDU_Owner_peek
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
/* Has the potential for misuse, but this routine is useful for laying down
   assertions about ownership in the middle of algorithms. */
static inline
MPIDU_Owner_id MPIDU_Owner_peek(volatile MPIDU_Owner_info *info)
{
    return info->id;
}


/* ======================================================
   Allocation Routines
   ======================================================

   Routines to manage allocation by id of fixed-size records from a
   non-resizable buffer.  The implementation may or may not support
   deallocating those records once they are allocated.


   // pseudo-code for how this interface would be used to handle allocation for
   // up to N barrier_t elements
   size_t buf_size = MPIDU_Alloc_calc_size(N, sizeof(barrier_t));
   void *buf = my_shm_alloc(buf_size);
   MPIDU_Alloc_pool *pool;
   mpi_errno = MPIDU_Alloc_create_pool(buf, buf_size, sizeof(barrier_t), &barrier_init_func, &pool);
   if (mpi_errno) MPIU_ERR_POP(mpi_errno);
   // pass 'pool' to future alloc_by_id/free operations 
*/


typedef struct MPIDU_Alloc_pool_entry {
    int id;
} MPIDU_Alloc_pool_entry;

/* `buf' points not to the beginning of the user-specified buffer, but rather to
   the first element of "free" storage.  That is, immediately after the array of
   MPIDU_Alloc_pool_entry.  See the ascii art comment near _create_pool for a
   visual description of the layout. */
typedef struct MPIDU_Alloc_pool {
    volatile MPIDU_Alloc_pool_entry *entries;
    volatile void *buf;
    size_t elt_size;
    int count;
} MPIDU_Alloc_pool;

enum {
    MPIDU_ALLOC_NULL_ID = -1
};

/* XXX DJG I don't love this algorithm.  It doesn't allow us to safely free
   entries so there are a number of situations where it can't really be used
   safely.  I would prefer to replace it with an adaptation of the lock-free
   list-based set algorithm described here:
     http://www.research.ibm.com/people/m/michael/spaa-2002.pdf */
#undef FUNCNAME
#define FUNCNAME MPIDU_Alloc_by_id
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
volatile void * MPIDU_Alloc_by_id(MPIDU_Alloc_pool *pool, int id)
{
    volatile void *retval;
    int i;
    int prev;
    volatile char *ptr;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_ALLOC_BY_ID);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_ALLOC_BY_ID);
    
    MPIU_Assert(MPIDU_ALLOC_NULL_ID != id);

    ptr = pool->buf;

    for (i = 0; i < pool->count; ++i) {
        prev = MPIDU_Atomic_cas_int(&pool->entries[i].id, MPIDU_ALLOC_NULL_ID, id);
        if (MPIDU_ALLOC_NULL_ID == prev || id == prev) {
            retval = &ptr[i*pool->elt_size];
            goto fn_exit;
        }
    }


    /* no memory slots are available */
    retval = NULL;

fn_exit:
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_ALLOC_BY_ID);
    return retval;
}

#undef FUNCNAME
#define FUNCNAME MPIDU_Alloc_free
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Alloc_free(MPIDU_Alloc_pool *pool, volatile void *element) {
    int mpi_errno = MPI_SUCCESS;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_ALLOC_FREE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_ALLOC_FREE);
    /* Do nothing for now... this datastructure isn't safe for deallocation yet.
       Consider replacing with something like
         http://www.research.ibm.com/people/m/michael/spaa-2002.pdf */
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_ALLOC_FREE);
    return mpi_errno;
}

typedef int (*MPIDU_Alloc_element_init_fp)(volatile void *element);

/* Calculates the amount of shared memory needed to hold the specified number of
   elements of a given size. */
#undef FUNCNAME
#define FUNCNAME MPIDU_Alloc_calc_size
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
size_t MPIDU_Alloc_calc_size(size_t nmemb, size_t elt_size)
{
    size_t bytes_needed;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_ALLOC_CALC_SIZE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_ALLOC_CALC_SIZE);

    bytes_needed = 0;
    bytes_needed += sizeof(MPIDU_Alloc_pool);
    /* each elt has the overhead of one MPIDU_Alloc_pool_entry */
    bytes_needed += nmemb * (sizeof(MPIDU_Alloc_pool_entry) + elt_size);

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_ALLOC_CALC_SIZE);
    return bytes_needed;
}

/* The current implementation stores N elements of data in buf in the
   following manner:
  
                LAYOUT                            CORRESPONDING POINTERS
         ---------------------------------------
         |  element N                          |
         ---------------------------------------
         |        ...                          |
         ---------------------------------------
         |  element 1                          |
         ---------------------------------------
         |  element 0                          |  pool->buf
         ---------------------------------------
         |  MPIDU_Alloc_pool_entry N           |
         ---------------------------------------
         |        ...                          |
         ---------------------------------------
         |  MPIDU_Alloc_pool_entry 1           |
         ---------------------------------------
         |  MPIDU_Alloc_pool_entry 0           |  pool->entries
    ^    ---------------------------------------
    |    |                                     |
    |    |                                     |
    |    |                                     |
    |    |  MPIDU_Alloc_pool                   |  buf (passed to _create_pool)
   addr  ---------------------------------------

   (addresses increase numerically from bottom to top)

*/
#undef FUNCNAME
#define FUNCNAME MPIDU_Alloc_create_pool
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Alloc_create_pool(volatile void *buf,
                            size_t buf_size,
                            size_t elt_size,
                            MPIDU_Alloc_element_init_fp initializer,
                            MPIDU_Alloc_pool **pool)
{
    int mpi_errno = MPI_SUCCESS;
    int i;
    volatile char *ptr;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_ALLOC_CREATE_POOL);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_ALLOC_CREATE_POOL);

    MPIU_Assert(pool != NULL);
    MPIU_Assert(buf != NULL);
    MPIU_Assert(buf_size > 0);
    MPIU_Assert(elt_size > 0);

    ptr = buf;
    *pool = (MPIDU_Alloc_pool *)ptr;
    ptr += sizeof(**pool);
    buf_size -= sizeof(**pool);

    (*pool)->elt_size = elt_size;

    /* For each element there is a corresponding _pool_entry, hence the number
       of entries that we can fit is: */
    (*pool)->count = buf_size / (sizeof(MPIDU_Alloc_pool_entry) + elt_size);

    (*pool)->entries = (MPIDU_Alloc_pool_entry *)ptr;
    ptr += (*pool)->count * sizeof(*((*pool)->entries));

    (*pool)->buf = ptr;

    for (i = 0; i < (*pool)->count; ++i) {
        (*pool)->entries[i].id = MPIDU_ALLOC_NULL_ID;
        if (initializer != NULL) {
            mpi_errno = (*initializer)(&ptr[i*(*pool)->elt_size]);
            if (mpi_errno) MPIU_ERR_POP (mpi_errno);
        }

    }

fn_fail:
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_ALLOC_CREATE_POOL);
    return mpi_errno;
}


/* ================================================================
   A thread-safe, IPC-safe, shared memory barrier implementation.
   ================================================================
  
   The current implementation isn't super efficient, but it should work just
   fine for small numbers of processors on several architectures.  Please note,
   this provides a barrier that works ONLY FOR SHARED MEMORY.  That is, you will
   need to setup your own message-based barrier (that may optionally use this
   interface for local synchronization) for remote nodes.
   
   There are actually two barrier interfaces right now with corresponding
   implementations.  The first is a simple barrier (MPIDU_Shm_barrier_simple),
   with the semantics that you would expect from a barrier routine.  All
   participants call the routine and no call will return until all participants
   have entered the routine.

   The second barrier interface is slightly more complicated and is designed to
   be used efficiently as a step in a larger barrier operation.  It consists of
   two calls: MPIDU_Shm_barrier_enter() and MPIDU_Shm_barrier_release().  The
   most notable difference between the simple barrier and this pair of routines
   is that the latter has the notion of a "boss process".  This boss process is
   the process that will be doing work during the middle of the barrier
   operation.  It is responsible for releasing the other participants from
   _barrier_enter() by calling _barrier_release() after it has done its special
   task(s).  All non-boss processes will remain in _barrier_enter() until
   _barrier_release() is called by the boss.

   For example (modulo error handling code):
   --------------------------------------------------------------------------
   int boss_rank = 0;
   MPIDU_Shm_barrier_t *b_var = <initialization code>;
   MPIDU_Shm_barrier_enter(b_var, local_num, my_local_rank, boss_rank);
   MPIDU_Shm_barrier_enter(b_var, local_num, my_local_rank, boss_rank);
   if (my_local_rank == boss_rank) {
       do_non_local_message_barrier(...);
       MPIDU_Shm_barrier_release(b_var, local_num, my_local_rank, boss_rank);
   }
   --------------------------------------------------------------------------
*/

/* Pick an arbitrary cacheline size for now, we can setup a mechanism to detect
   it at build time later on.  This should work well on most intel systems at
   the very least. */
#define MPIDU_SHM_BARRIER_CACHELINE_PADDING 128
typedef struct {
    volatile int num_waiting;
    char _padding[MPIDU_SHM_BARRIER_CACHELINE_PADDING - sizeof(int)];
    volatile int sig;
    volatile int sig_boss;
} MPIDU_Shm_barrier_t;

/* Pass in a sizeof(MPIDU_Shm_barrier_t) sized chunk of memory that is shared
   between all processes/threads that you wish to synchronize. */
#undef FUNCNAME
#define FUNCNAME MPIDU_Shm_barrier_init
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Shm_barrier_init(MPIDU_Shm_barrier_t *barrier)
{
    int mpi_errno = MPI_SUCCESS;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_SHM_BARRIER_INIT);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_SHM_BARRIER_INIT);

    barrier->num_waiting = 0;
    barrier->sig = 0;
    barrier->sig_boss = 0;
    MPIDU_Shm_write_barrier();

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_SHM_BARRIER_INIT);
    return mpi_errno;
}

/* Actually perform a barrier synchronization.  Specify;
   barrier - A barrier object that has been initialized with
             MPIDU_Shm_barrier_init.
   num_processes - The number of processes that will be participating in this
                   barrier.
   rank - The caller's rank among the participants.

   NOTE: `rank' is not used in the current implementation, but it might be
         necessary in a fancier tree-based or tournament algorithm.  */
#undef FUNCNAME
#define FUNCNAME MPIDU_Shm_barrier_simple
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Shm_barrier_simple(MPIDU_Shm_barrier_t *barrier, int num_processes, int rank)
{
    int mpi_errno = MPI_SUCCESS;
    int cur_sig;
    int prev_waiting;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_SHM_BARRIER_SIMPLE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_SHM_BARRIER_SIMPLE);

    MPIU_Assert(barrier != NULL);
    /* not strictly needed, but not checking it is a bug waiting to happen */
    MPIU_Assert(rank < num_processes);

    cur_sig = barrier->sig;

    prev_waiting = MPIDU_Atomic_fetch_and_incr(&barrier->num_waiting);

    if ((num_processes - 1) == prev_waiting) {
        /* we are the last one to enter the barrier, so we are responsible for
           releasing everyone from it */
        barrier->num_waiting = 0;
        MPIDU_Shm_write_barrier();
        ++(barrier->sig); /* must come last to avoid race */
    }
    else {
        /* wait for the last arriving process to release us from the barrier */
        while (barrier->sig == cur_sig) {
            MPIDU_Atomic_busy_wait();
        }
    }

    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_SHM_BARRIER_SIMPLE);
    return mpi_errno;
}


#undef FUNCNAME
#define FUNCNAME MPIDU_Shm_barrier_enter
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Shm_barrier_enter(MPIDU_Shm_barrier_t *barrier,
                            int num_processes,
                            int rank,
                            int boss_rank)
{
    int mpi_errno = MPI_SUCCESS;
    int prev;
    int cur_sig;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_SHM_BARRIER_ENTER);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_SHM_BARRIER_ENTER);

    MPIU_Assert(num_processes > 0);
    MPIU_Assert(rank >= 0);

    if (1 == num_processes) goto fn_exit; /* trivial barrier */

    if (rank == boss_rank) {
        while (0 == barrier->sig_boss)
            MPIDU_Atomic_busy_wait();
    }
    else {
        cur_sig = barrier->sig;
        MPIDU_Shm_read_barrier();

        prev = MPIDU_Atomic_fetch_and_incr(&barrier->num_waiting);
        /* -2 because it's the value before we added 1 and we're not waiting for the boss */
        if ((num_processes - 2) == prev) {
            MPIDU_Shm_write_barrier();
            barrier->sig_boss = 1;
        }

        /* wait to be released by the boss */
        while (barrier->sig == cur_sig)
            MPIDU_Atomic_busy_wait();
    }

fn_exit:
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_SHM_BARRIER_ENTER);
    return mpi_errno;
}

/* only to be called by the boss process */
#undef FUNCNAME
#define FUNCNAME MPIDU_Shm_barrier_release
#undef FCNAME
#define FCNAME MPIDI_QUOTE(FUNCNAME)
static inline
int MPIDU_Shm_barrier_release(MPIDU_Shm_barrier_t *barrier,
                              int num_processes,
                              int rank,
                              int boss_rank)
{
    int mpi_errno = MPI_SUCCESS;
    MPIDI_STATE_DECL(MPID_STATE_MPIDU_SHM_BARRIER_RELEASE);

    MPIDI_FUNC_ENTER(MPID_STATE_MPIDU_SHM_BARRIER_RELEASE);

    if (1 == num_processes) goto fn_exit; /* trivial barrier */

    /* makes this safe to use outside of a conditional */
    if (rank == boss_rank) {
        barrier->sig_boss = 0;
        barrier->num_waiting = 0;
        MPIDU_Shm_write_barrier();
        ++(barrier->sig);
    }

fn_exit:
    MPIDI_FUNC_EXIT(MPID_STATE_MPIDU_SHM_BARRIER_RELEASE);
    return mpi_errno;
}


/* these macros are undef'ed in order to prevent errors in some compilation
   units where FCNAME is defined as a char array instead of a macro */
#undef FUNCNAME
#undef FCNAME

#endif /* defined(MPIIMPLATOMIC_H_INCLUDED) */
