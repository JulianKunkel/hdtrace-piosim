///
/// @file interesting_funcs.h
///
///All functions declared here will be traced by the MPI Wrapper.
/// This file is processed by a script to automatically generate a
/// C source file, which means only a small subset of valid C function
/// declarations may be used.
///
/// In particular, only argument types may be used in the declaration.
/// Using an argument name will cause the script to produce
/// an erroneous source file.
/// Lines beginning with a forward slash ("/") are ignored.
///
///
/// Correct declaration:
/// @code
/// int MPI_Init(int *, char ***);
/// @endcode
///
/// Incorrect declaration:
/// @code
/// int MPI_Init(int *argc, char ***argv);
/// @endcode
///
int MPI_Send(void*, int, MPI_Datatype, int, int, MPI_Comm);
int MPI_Recv(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Status *);
int MPI_Bsend(void*, int, MPI_Datatype, int, int, MPI_Comm);
int MPI_Ssend(void*, int, MPI_Datatype, int, int, MPI_Comm);
int MPI_Rsend(void*, int, MPI_Datatype, int, int, MPI_Comm);
int MPI_Isend(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Ibsend(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Issend(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Irsend(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Irecv(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Wait(MPI_Request *, MPI_Status *);
int MPI_Test(MPI_Request *, int *, MPI_Status *);
int MPI_Waitany(int, MPI_Request *, int *, MPI_Status *);
int MPI_Testany(int, MPI_Request *, int *, int *, MPI_Status *);
int MPI_Waitall(int, MPI_Request *, MPI_Status *);
int MPI_Testall(int, MPI_Request *, int *, MPI_Status *);
int MPI_Waitsome(int, MPI_Request *, int *, int *, MPI_Status *);
int MPI_Testsome(int, MPI_Request *, int *, int *, MPI_Status *);
int MPI_Iprobe(int, int, MPI_Comm, int *, MPI_Status *);
int MPI_Probe(int, int, MPI_Comm, MPI_Status *);
int MPI_Cancel(MPI_Request *);
int MPI_Test_cancelled(MPI_Status *, int *);
int MPI_Send_init(void*, int, MPI_Datatype, int, int, MPI_Comm, MPI_Request *);
int MPI_Bsend_init(void*, int, MPI_Datatype, int,int, MPI_Comm, MPI_Request *);
int MPI_Ssend_init(void*, int, MPI_Datatype, int,int, MPI_Comm, MPI_Request *);
int MPI_Rsend_init(void*, int, MPI_Datatype, int,int, MPI_Comm, MPI_Request *);
int MPI_Recv_init(void*, int, MPI_Datatype, int,int, MPI_Comm, MPI_Request *);
int MPI_Start(MPI_Request *);
int MPI_Startall(int, MPI_Request *);
int MPI_Sendrecv(void *, int, MPI_Datatype,int, int, void *, int, MPI_Datatype, int, int, MPI_Comm, MPI_Status *);
int MPI_Sendrecv_replace(void*, int, MPI_Datatype, int, int, int, int, MPI_Comm, MPI_Status *);
int MPI_Barrier(MPI_Comm );
int MPI_Bcast(void*, int, MPI_Datatype, int, MPI_Comm );
int MPI_Gather(void* , int, MPI_Datatype, void*, int, MPI_Datatype, int, MPI_Comm);
int MPI_Gatherv(void* , int, MPI_Datatype, void*, int *, int *, MPI_Datatype, int, MPI_Comm);
int MPI_Scatter(void* , int, MPI_Datatype, void*, int, MPI_Datatype, int, MPI_Comm);
int MPI_Scatterv(void* , int *, int *,  MPI_Datatype, void*, int, MPI_Datatype, int, MPI_Comm);
int MPI_Allgather(void* , int, MPI_Datatype, void*, int, MPI_Datatype, MPI_Comm);
int MPI_Allgatherv(void* , int, MPI_Datatype, void*, int *, int *, MPI_Datatype, MPI_Comm);
int MPI_Alltoall(void* , int, MPI_Datatype, void*, int, MPI_Datatype, MPI_Comm);
int MPI_Alltoallv(void* , int *, int *, MPI_Datatype, void*, int *, int *, MPI_Datatype, MPI_Comm);
int MPI_Reduce(void* , void*, int, MPI_Datatype, MPI_Op, int, MPI_Comm);
int MPI_Allreduce(void* , void*, int, MPI_Datatype, MPI_Op, MPI_Comm);
int MPI_Reduce_scatter(void* , void*, int *, MPI_Datatype, MPI_Op, MPI_Comm);
int MPI_Scan(void* , void*, int, MPI_Datatype, MPI_Op, MPI_Comm );
int MPI_Init(int *, char ***);
int MPI_Init_thread(int *, char ***, int, int *);
int MPI_Finalize(void);
int MPI_Abort(MPI_Comm, int);
int MPI_Comm_set_name(MPI_Comm, char *);
int MPI_Win_complete(MPI_Win);
int MPI_Win_fence(int, MPI_Win);
int MPI_Win_free(MPI_Win *);
int MPI_Win_get_group(MPI_Win, MPI_Group *);
int MPI_Win_lock(int, int, int, MPI_Win);
int MPI_Win_post(MPI_Group, int, MPI_Win);
int MPI_Win_start(MPI_Group, int, MPI_Win);
int MPI_Win_test(MPI_Win, int *);
int MPI_Win_unlock(int, MPI_Win);
int MPI_Win_wait(MPI_Win);
int MPI_Exscan(void *, void *, int, MPI_Datatype, MPI_Op, MPI_Comm) ;
int MPI_Grequest_complete(MPI_Request);

int MPI_File_open(MPI_Comm, char *, int, MPI_Info, MPI_File *);
int MPI_File_close(MPI_File *);
int MPI_File_delete(char *, MPI_Info);
int MPI_File_set_size(MPI_File, MPI_Offset);
int MPI_File_get_size(MPI_File, MPI_Offset *);
int MPI_File_preallocate(MPI_File, MPI_Offset);
int MPI_File_read(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_read_all(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_write(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_write_at(MPI_File, MPI_Offset, void* , int, MPI_Datatype, MPI_Status *);
int MPI_File_write_at_all(MPI_File, MPI_Offset, void* , int, MPI_Datatype, MPI_Status *);
int MPI_File_read_at(MPI_File, MPI_Offset, void* , int, MPI_Datatype, MPI_Status *);
int MPI_File_read_at_all(MPI_File, MPI_Offset, void* , int, MPI_Datatype, MPI_Status *);
int MPI_File_write_all(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_iread(MPI_File, void *, int, MPI_Datatype, MPIO_Request *);
int MPI_File_iwrite(MPI_File, void *, int, MPI_Datatype, MPIO_Request *);
int MPI_File_iwrite_at(MPI_File ,MPI_Offset ,void *,int ,MPI_Datatype , MPI_Request *);
int MPI_File_seek(MPI_File, MPI_Offset, int);
int MPI_File_get_position(MPI_File, MPI_Offset *);
int MPI_File_read_shared(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_write_shared(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_read_ordered(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_write_ordered(MPI_File, void *, int, MPI_Datatype, MPI_Status *);
int MPI_File_seek_shared(MPI_File, MPI_Offset, int);
int MPI_File_get_position_shared(MPI_File, MPI_Offset *);
int MPI_File_read_at_all_begin(MPI_File,	MPI_Offset , void *, int , MPI_Datatype);
int MPI_File_read_at_all_end(MPI_File, void *, MPI_Status *);
int MPI_File_write_at_all_begin(MPI_File , MPI_Offset, void *, int, MPI_Datatype );
int MPI_File_write_at_all_end(MPI_File, void *, MPI_Status *);
int MPI_File_read_all_begin(MPI_File, void *, int, MPI_Datatype);
int MPI_File_read_all_end(MPI_File, void *, MPI_Status *);
int MPI_File_write_all_begin(MPI_File, void *, int, MPI_Datatype);
int MPI_File_write_all_end(MPI_File, void *, MPI_Status *);
int MPI_File_read_ordered_begin(MPI_File, void *, int, MPI_Datatype);
int MPI_File_read_ordered_end(MPI_File, void *, MPI_Status *);
int MPI_File_write_ordered_begin(MPI_File, void *, int, MPI_Datatype);
int MPI_File_write_ordered_end(MPI_File, void *, MPI_Status *);
int MPI_File_get_type_extent(MPI_File, MPI_Datatype, MPI_Aint *);
int MPI_File_set_atomicity(MPI_File, int);
int MPI_File_sync(MPI_File);

int MPI_File_set_info(MPI_File, MPI_Info);

int MPI_Pcontrol(const int , ...);

int MPI_Type_vector(int, int, int, MPI_Datatype, MPI_Datatype *);

int MPI_Type_commit(MPI_Datatype *);

int MPI_File_set_view(MPI_File, MPI_Offset, MPI_Datatype, MPI_Datatype, char *, MPI_Info);

int MPI_Type_free(MPI_Datatype *);
int MPI_Comm_free(MPI_Comm *);
int MPI_Comm_create(MPI_Comm, MPI_Group, MPI_Comm *);
int MPI_Comm_dup(MPI_Comm, MPI_Comm *);
int MPI_Comm_split(MPI_Comm, int, int, MPI_Comm *);
