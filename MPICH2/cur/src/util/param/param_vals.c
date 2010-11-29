/* -*- Mode: C; c-basic-offset:4 ; indent-tabs-mode:nil ; -*- */
/*
 *  (C) 2010 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 */
/* automatically generated
 *   by:   ./maint/genparams
 *   at:   Wed Nov 17 10:51:54 2010
 *   from: src/util/param/params.yml (md5sum 438294756082600db7df65888db19616)
 *
 * DO NOT EDIT!!!
 */

#include "mpiimpl.h"

/* array of category info for runtime usage */
struct MPIR_Param_category_t MPIR_Param_categories[MPIR_PARAM_NUM_CATEGORIES] = {
    { MPIR_PARAM_CATEGORY_ID_collective,
      "collective",
      "parameters that control collective communication behavior" },
    { MPIR_PARAM_CATEGORY_ID_pt2pt,
      "pt2pt",
      "parameters that control point-to-point communication behavior" },
    { MPIR_PARAM_CATEGORY_ID_intranode,
      "intranode",
      "intranode communication parameters" },
    { MPIR_PARAM_CATEGORY_ID_developer,
      "developer",
      "useful for developers working on MPICH2 itself" },
    { MPIR_PARAM_CATEGORY_ID_error_handling,
      "error_handling",
      "parameters that control error handling behavior (stack traces, aborts, etc)" },
    { MPIR_PARAM_CATEGORY_ID_debugger,
      "debugger",
      "parameters relevant to the \"MPIR\" debugger interface" },
    { MPIR_PARAM_CATEGORY_ID_checkpointing,
      "checkpointing",
      "parameters relevant to checkpointing" },
    { MPIR_PARAM_CATEGORY_ID_threads,
      "threads",
      "multi-threading parameters" },
    { MPIR_PARAM_CATEGORY_ID_nemesis,
      "nemesis",
      "parameters that control behavior of the ch3:nemesis channel" },
};

/* array of parameter info for runtime usage */
struct MPIR_Param_t MPIR_Param_params[MPIR_PARAM_NUM_PARAMS] = {
    { MPIR_PARAM_ID_ALLTOALL_SHORT_MSG_SIZE,
      "ALLTOALL_SHORT_MSG_SIZE",
      "the short message algorithm will be used if the per-destination message size (sendcount*size(sendtype)) is <= this value",
      { MPIR_PARAM_TYPE_INT, 256, 0.0, "" } },
    { MPIR_PARAM_ID_ALLTOALL_MEDIUM_MSG_SIZE,
      "ALLTOALL_MEDIUM_MSG_SIZE",
      "the medium message algorithm will be used if the per-destination message size (sendcount*size(sendtype)) is <= this value and larger than ALLTOALL_SHORT_MSG_SIZE",
      { MPIR_PARAM_TYPE_INT, 32768, 0.0, "" } },
    { MPIR_PARAM_ID_ALLTOALL_THROTTLE,
      "ALLTOALL_THROTTLE",
      "max no. of irecvs/isends posted at a time in some alltoall algorithms. Setting it to 0 causes all irecvs/isends to be posted at once.",
      { MPIR_PARAM_TYPE_INT, 4, 0.0, "" } },
    { MPIR_PARAM_ID_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE,
      "REDSCAT_COMMUTATIVE_LONG_MSG_SIZE",
      "the long message algorithm will be used if the operation is commutative and the send buffer size is >= this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 524288, 0.0, "" } },
    { MPIR_PARAM_ID_BCAST_MIN_PROCS,
      "BCAST_MIN_PROCS",
      "the minimum number of processes in a communicator to use a non-binomial broadcast algorithm",
      { MPIR_PARAM_TYPE_INT, 8, 0.0, "" } },
    { MPIR_PARAM_ID_BCAST_SHORT_MSG_SIZE,
      "BCAST_SHORT_MSG_SIZE",
      "the short message algorithm will be used if the send buffer size is < this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 12288, 0.0, "" } },
    { MPIR_PARAM_ID_BCAST_LONG_MSG_SIZE,
      "BCAST_LONG_MSG_SIZE",
      "the long message algorithm will be used if the send buffer size is >= this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 524288, 0.0, "" } },
    { MPIR_PARAM_ID_ALLGATHER_SHORT_MSG_SIZE,
      "ALLGATHER_SHORT_MSG_SIZE",
      "For MPI_Allgather and MPI_Allgatherv, the short message algorithm will be used if the send buffer size is < this value (in bytes).",
      { MPIR_PARAM_TYPE_INT, 81920, 0.0, "" } },
    { MPIR_PARAM_ID_ALLGATHER_LONG_MSG_SIZE,
      "ALLGATHER_LONG_MSG_SIZE",
      "For MPI_Allgather and MPI_Allgatherv, the long message algorithm will be used if the send buffer size is >= this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 524288, 0.0, "" } },
    { MPIR_PARAM_ID_REDUCE_SHORT_MSG_SIZE,
      "REDUCE_SHORT_MSG_SIZE",
      "the short message algorithm will be used if the send buffer size is <= this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 2048, 0.0, "" } },
    { MPIR_PARAM_ID_ALLREDUCE_SHORT_MSG_SIZE,
      "ALLREDUCE_SHORT_MSG_SIZE",
      "the short message algorithm will be used if the send buffer size is <= this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 2048, 0.0, "" } },
    { MPIR_PARAM_ID_GATHER_VSMALL_MSG_SIZE,
      "GATHER_VSMALL_MSG_SIZE",
      "use a temporary buffer for intracommunicator MPI_Gather if the send buffer size is < this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 1024, 0.0, "" } },
    { MPIR_PARAM_ID_GATHER_INTER_SHORT_MSG_SIZE,
      "GATHER_INTER_SHORT_MSG_SIZE",
      "use the short message algorithm for intercommunicator MPI_Gather if the send buffer size is < this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 2048, 0.0, "" } },
    { MPIR_PARAM_ID_GATHERV_INTER_SSEND_MIN_PROCS,
      "GATHERV_INTER_SSEND_MIN_PROCS",
      "Use Ssend (synchronous send) for intercommunicator MPI_Gatherv if the \"group B\" size is >= this value.  Specifying \"-1\" always avoids using Ssend.  For backwards compatibility, specifying \"0\" uses the default value.",
      { MPIR_PARAM_TYPE_INT, 32, 0.0, "" } },
    { MPIR_PARAM_ID_SCATTER_INTER_SHORT_MSG_SIZE,
      "SCATTER_INTER_SHORT_MSG_SIZE",
      "use the short message algorithm for intercommunicator MPI_Scatter if the send buffer size is < this value (in bytes)",
      { MPIR_PARAM_TYPE_INT, 2048, 0.0, "" } },
    { MPIR_PARAM_ID_ALLGATHERV_PIPELINE_MSG_SIZE,
      "ALLGATHERV_PIPELINE_MSG_SIZE",
      "The smallest message size that will be used for the pipelined, large-message, ring algorithm in the MPI_Allgatherv implementation.",
      { MPIR_PARAM_TYPE_INT, 32768, 0.0, "" } },
    { MPIR_PARAM_ID_NOLOCAL,
      "NOLOCAL",
      "If true, force all processes to operate as though all processes are located on another node.  For example, this disables shared memory communication hierarchical collectives.",
      { MPIR_PARAM_TYPE_BOOLEAN, 0, 0.0, "" } },
    { MPIR_PARAM_ID_MEMDUMP,
      "MEMDUMP",
      "If true, list any memory that was allocated by MPICH2 and that remains allocated when MPI_Finalize completes.",
      { MPIR_PARAM_TYPE_BOOLEAN, 1, 0.0, "" } },
    { MPIR_PARAM_ID_PROCTABLE_SIZE,
      "PROCTABLE_SIZE",
      "Size of the \"MPIR\" debugger interface proctable (process table).",
      { MPIR_PARAM_TYPE_INT, 64, 0.0, "" } },
    { MPIR_PARAM_ID_PROCTABLE_PRINT,
      "PROCTABLE_PRINT",
      "If true, dump the proctable entries at MPIR_WaitForDebugger-time. (currently compile-time disabled by \"#if 0\")",
      { MPIR_PARAM_TYPE_BOOLEAN, 1, 0.0, "" } },
    { MPIR_PARAM_ID_PRINT_ERROR_STACK,
      "PRINT_ERROR_STACK",
      "If true, print an error stack trace at error handling time.",
      { MPIR_PARAM_TYPE_BOOLEAN, 1, 0.0, "" } },
    { MPIR_PARAM_ID_CHOP_ERROR_STACK,
      "CHOP_ERROR_STACK",
      "If >0, truncate error stack output lines this many characters wide.  If 0, do not truncate, and if <0 use a sensible default.",
      { MPIR_PARAM_TYPE_INT, 0, 0.0, "" } },
    { MPIR_PARAM_ID_NEM_LMT_DMA_THRESHOLD,
      "NEM_LMT_DMA_THRESHOLD",
      "Messages larger than this size will use the \"dma\" (knem) intranode LMT implementation, if it is enabled and available.",
      { MPIR_PARAM_TYPE_INT, 2097152, 0.0, "" } },
    { MPIR_PARAM_ID_NEMESIS_NETMOD,
      "NEMESIS_NETMOD",
      "If non-empty, this parameter specifies which network module should be used for communication.",
      { MPIR_PARAM_TYPE_STRING, -1, 0.0, "" } },
    { MPIR_PARAM_ID_DEBUG_HOLD,
      "DEBUG_HOLD",
      "If true, causes processes to wait in MPI_Init and MPI_Initthread for a debugger to be attached.  Once the debugger has attached, the variable 'hold' should be set to 0 in order to allow the process to continue (e.g., in gdb, \"set hold=0\").",
      { MPIR_PARAM_TYPE_BOOLEAN, 0, 0.0, "" } },
    { MPIR_PARAM_ID_ENABLE_CKPOINT,
      "ENABLE_CKPOINT",
      "If true, enables checkpointing support and returns an error if checkpointing library cannot be initialized.",
      { MPIR_PARAM_TYPE_BOOLEAN, 0, 0.0, "" } },
};

/* actual storage for parameters */
int MPIR_PARAM_ALLTOALL_SHORT_MSG_SIZE = 256;
int MPIR_PARAM_ALLTOALL_MEDIUM_MSG_SIZE = 32768;
int MPIR_PARAM_ALLTOALL_THROTTLE = 4;
int MPIR_PARAM_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE = 524288;
int MPIR_PARAM_BCAST_MIN_PROCS = 8;
int MPIR_PARAM_BCAST_SHORT_MSG_SIZE = 12288;
int MPIR_PARAM_BCAST_LONG_MSG_SIZE = 524288;
int MPIR_PARAM_ALLGATHER_SHORT_MSG_SIZE = 81920;
int MPIR_PARAM_ALLGATHER_LONG_MSG_SIZE = 524288;
int MPIR_PARAM_REDUCE_SHORT_MSG_SIZE = 2048;
int MPIR_PARAM_ALLREDUCE_SHORT_MSG_SIZE = 2048;
int MPIR_PARAM_GATHER_VSMALL_MSG_SIZE = 1024;
int MPIR_PARAM_GATHER_INTER_SHORT_MSG_SIZE = 2048;
int MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS = 32;
int MPIR_PARAM_SCATTER_INTER_SHORT_MSG_SIZE = 2048;
int MPIR_PARAM_ALLGATHERV_PIPELINE_MSG_SIZE = 32768;
int MPIR_PARAM_NOLOCAL = 0;
int MPIR_PARAM_MEMDUMP = 1;
int MPIR_PARAM_PROCTABLE_SIZE = 64;
int MPIR_PARAM_PROCTABLE_PRINT = 1;
int MPIR_PARAM_PRINT_ERROR_STACK = 1;
int MPIR_PARAM_CHOP_ERROR_STACK = 0;
int MPIR_PARAM_NEM_LMT_DMA_THRESHOLD = 2097152;
const char * MPIR_PARAM_NEMESIS_NETMOD = "";
int MPIR_PARAM_DEBUG_HOLD = 0;
int MPIR_PARAM_ENABLE_CKPOINT = 0;

#undef FUNCNAME
#define FUNCNAME MPIR_Param_init_params
#undef FCNAME
#define FCNAME MPIU_QUOTE(FUNCNAME)
int MPIR_Param_init_params(void)
{
    int mpi_errno = MPI_SUCCESS;
    int rc;

    rc = MPL_env2int("MPICH_ALLTOALL_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLTOALL_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLTOALL_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLTOALL_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLTOALL_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLTOALL_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLTOALL_MEDIUM_MSG_SIZE", &(MPIR_PARAM_ALLTOALL_MEDIUM_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLTOALL_MEDIUM_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLTOALL_MEDIUM_MSG_SIZE", &(MPIR_PARAM_ALLTOALL_MEDIUM_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLTOALL_MEDIUM_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLTOALL_THROTTLE", &(MPIR_PARAM_ALLTOALL_THROTTLE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLTOALL_THROTTLE");
    rc = MPL_env2int("MPIR_PARAM_ALLTOALL_THROTTLE", &(MPIR_PARAM_ALLTOALL_THROTTLE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLTOALL_THROTTLE");

    rc = MPL_env2int("MPICH_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE", &(MPIR_PARAM_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE", &(MPIR_PARAM_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_REDSCAT_COMMUTATIVE_LONG_MSG_SIZE");

    rc = MPL_env2int("MPICH_BCAST_MIN_PROCS", &(MPIR_PARAM_BCAST_MIN_PROCS));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_BCAST_MIN_PROCS");
    rc = MPL_env2int("MPIR_PARAM_BCAST_MIN_PROCS", &(MPIR_PARAM_BCAST_MIN_PROCS));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_BCAST_MIN_PROCS");

    rc = MPL_env2int("MPICH_BCAST_SHORT_MSG_SIZE", &(MPIR_PARAM_BCAST_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_BCAST_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_BCAST_SHORT_MSG_SIZE", &(MPIR_PARAM_BCAST_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_BCAST_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_BCAST_LONG_MSG_SIZE", &(MPIR_PARAM_BCAST_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_BCAST_LONG_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_BCAST_LONG_MSG_SIZE", &(MPIR_PARAM_BCAST_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_BCAST_LONG_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLGATHER_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLGATHER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLGATHER_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLGATHER_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLGATHER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLGATHER_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLGATHER_LONG_MSG_SIZE", &(MPIR_PARAM_ALLGATHER_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLGATHER_LONG_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLGATHER_LONG_MSG_SIZE", &(MPIR_PARAM_ALLGATHER_LONG_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLGATHER_LONG_MSG_SIZE");

    rc = MPL_env2int("MPICH_REDUCE_SHORT_MSG_SIZE", &(MPIR_PARAM_REDUCE_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_REDUCE_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_REDUCE_SHORT_MSG_SIZE", &(MPIR_PARAM_REDUCE_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_REDUCE_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLREDUCE_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLREDUCE_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLREDUCE_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLREDUCE_SHORT_MSG_SIZE", &(MPIR_PARAM_ALLREDUCE_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLREDUCE_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_GATHER_VSMALL_MSG_SIZE", &(MPIR_PARAM_GATHER_VSMALL_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_GATHER_VSMALL_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_GATHER_VSMALL_MSG_SIZE", &(MPIR_PARAM_GATHER_VSMALL_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_GATHER_VSMALL_MSG_SIZE");

    rc = MPL_env2int("MPICH_GATHER_INTER_SHORT_MSG_SIZE", &(MPIR_PARAM_GATHER_INTER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_GATHER_INTER_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_GATHER_INTER_SHORT_MSG_SIZE", &(MPIR_PARAM_GATHER_INTER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_GATHER_INTER_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH2_GATHERV_MIN_PROCS", &(MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH2_GATHERV_MIN_PROCS");
    rc = MPL_env2int("MPICH_GATHERV_INTER_SSEND_MIN_PROCS", &(MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_GATHERV_INTER_SSEND_MIN_PROCS");
    rc = MPL_env2int("MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS", &(MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_GATHERV_INTER_SSEND_MIN_PROCS");

    rc = MPL_env2int("MPICH_SCATTER_INTER_SHORT_MSG_SIZE", &(MPIR_PARAM_SCATTER_INTER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_SCATTER_INTER_SHORT_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_SCATTER_INTER_SHORT_MSG_SIZE", &(MPIR_PARAM_SCATTER_INTER_SHORT_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_SCATTER_INTER_SHORT_MSG_SIZE");

    rc = MPL_env2int("MPICH_ALLGATHERV_PIPELINE_MSG_SIZE", &(MPIR_PARAM_ALLGATHERV_PIPELINE_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ALLGATHERV_PIPELINE_MSG_SIZE");
    rc = MPL_env2int("MPIR_PARAM_ALLGATHERV_PIPELINE_MSG_SIZE", &(MPIR_PARAM_ALLGATHERV_PIPELINE_MSG_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ALLGATHERV_PIPELINE_MSG_SIZE");

    rc = MPL_env2bool("MPICH_NO_LOCAL", &(MPIR_PARAM_NOLOCAL));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_NO_LOCAL");
    rc = MPL_env2bool("MPIR_PARAM_NO_LOCAL", &(MPIR_PARAM_NOLOCAL));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_NO_LOCAL");
    rc = MPL_env2bool("MPICH_NOLOCAL", &(MPIR_PARAM_NOLOCAL));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_NOLOCAL");
    rc = MPL_env2bool("MPIR_PARAM_NOLOCAL", &(MPIR_PARAM_NOLOCAL));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_NOLOCAL");

    rc = MPL_env2bool("MPICH_MEMDUMP", &(MPIR_PARAM_MEMDUMP));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_MEMDUMP");
    rc = MPL_env2bool("MPIR_PARAM_MEMDUMP", &(MPIR_PARAM_MEMDUMP));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_MEMDUMP");

    rc = MPL_env2int("MPICH_PROCTABLE_SIZE", &(MPIR_PARAM_PROCTABLE_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_PROCTABLE_SIZE");
    rc = MPL_env2int("MPIR_PARAM_PROCTABLE_SIZE", &(MPIR_PARAM_PROCTABLE_SIZE));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_PROCTABLE_SIZE");

    rc = MPL_env2bool("MPICH_PROCTABLE_PRINT", &(MPIR_PARAM_PROCTABLE_PRINT));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_PROCTABLE_PRINT");
    rc = MPL_env2bool("MPIR_PARAM_PROCTABLE_PRINT", &(MPIR_PARAM_PROCTABLE_PRINT));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_PROCTABLE_PRINT");

    rc = MPL_env2bool("MPICH_PRINT_ERROR_STACK", &(MPIR_PARAM_PRINT_ERROR_STACK));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_PRINT_ERROR_STACK");
    rc = MPL_env2bool("MPIR_PARAM_PRINT_ERROR_STACK", &(MPIR_PARAM_PRINT_ERROR_STACK));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_PRINT_ERROR_STACK");

    rc = MPL_env2int("MPICH_CHOP_ERROR_STACK", &(MPIR_PARAM_CHOP_ERROR_STACK));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_CHOP_ERROR_STACK");
    rc = MPL_env2int("MPIR_PARAM_CHOP_ERROR_STACK", &(MPIR_PARAM_CHOP_ERROR_STACK));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_CHOP_ERROR_STACK");

    rc = MPL_env2int("MPICH_NEM_LMT_DMA_THRESHOLD", &(MPIR_PARAM_NEM_LMT_DMA_THRESHOLD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_NEM_LMT_DMA_THRESHOLD");
    rc = MPL_env2int("MPIR_PARAM_NEM_LMT_DMA_THRESHOLD", &(MPIR_PARAM_NEM_LMT_DMA_THRESHOLD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_NEM_LMT_DMA_THRESHOLD");

    rc = MPL_env2str("MPICH_NEMESIS_NETMOD", &(MPIR_PARAM_NEMESIS_NETMOD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_NEMESIS_NETMOD");
    rc = MPL_env2str("MPIR_PARAM_NEMESIS_NETMOD", &(MPIR_PARAM_NEMESIS_NETMOD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_NEMESIS_NETMOD");

    rc = MPL_env2bool("MPICH_DEBUG_HOLD", &(MPIR_PARAM_DEBUG_HOLD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_DEBUG_HOLD");
    rc = MPL_env2bool("MPIR_PARAM_DEBUG_HOLD", &(MPIR_PARAM_DEBUG_HOLD));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_DEBUG_HOLD");

    rc = MPL_env2bool("MPICH_ENABLE_CKPOINT", &(MPIR_PARAM_ENABLE_CKPOINT));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPICH_ENABLE_CKPOINT");
    rc = MPL_env2bool("MPIR_PARAM_ENABLE_CKPOINT", &(MPIR_PARAM_ENABLE_CKPOINT));
    MPIU_ERR_CHKANDJUMP1((-1 == rc),mpi_errno,MPI_ERR_OTHER,"**envvarparse","**envvarparse %s","MPIR_PARAM_ENABLE_CKPOINT");

fn_fail:
    return mpi_errno;
}

