#ifndef _STRUCTURES_H_
#define _STRUCTURES_H_

#include "readxml.h"
#include "glib.h"
#include "constant.h"

/*
 * FIXME: May split this header file in multiple files, one file for each
 * GSList containing data of a trace and project file.
 */

/*
 *The following section contains all the MPI data structs for the XML parsing.
 */

/**
 * @brief Data type for a MPI_Barrier. 
 *
 * This struct contains the specific data for a MPI_Barrier read form a trace
 * file.
 */
struct MpiBarrier
{
	/**
	 * The communicator id.
	 */
  int cid;
	/**
	 * The source code line the Barrier appers in the orignal traced program.
	 */
  int cLine;
};


/**
 * @brief 
 * 
 * This struct contains the sp
 */
struct MpiComm_create
{
  int cid;
  int newcid;
  int cLine;
};

struct MpiComm_free
{
  int cid;
  int cLine;
};

struct MpiBcast
{
  int cid;
  int cLine;
  int size;
  int rootRank;
  int count;
  int tid;
};


struct MpiFinalize
{
  int cLine;
};


struct MpiInit
{
  int cLine;
};


struct MpiRecv
{
  int fromRank;
  int fromTag;
  int cid;
  int cLine;
};

struct MpiSend
{

  int size;
  int count;  
  int tid;
  int toRank;
  int toTag;
  int cid;
  int cLine;
};

struct MpiSendrecv
{
  int size;
  int toRank;
  int toTag;
  int fromRank;
  int fromTag;
  int cLine;
  int cid;
  int count;
  int sendTid;
  int recvTid;
};


enum Type
{
  MpiBarrier,
  MpiComm_create,
	MpiComm_free,
  MpiFinalize,
  MpiInit,
  MpiRecv,
  MpiSend,
  MpiSendrecv
};

enum DataName 
{
 DataVECTOR,
 DataNAMED,
 DataSTRUCT,
 DataTYPE,
};


struct DataVECTOR
{
  int count;
  int blocklength;
  int stride;
  int oldType;
};

struct DataNAMED
{
	int id;
	gchar* name;
};

struct DataSTRUCT
{
  gchar* name;
  int count;
  struct DataType* data;
};

struct DataTYPE
{
  int displacement;
  int blocklen;
};

struct DataType
{
  enum DataName type;
  int id;
  gchar* name;
  union
  {
    struct DataVECTOR dataVECTOR;
    struct DataNAMED dataNAMED;
    struct DataSTRUCT dataSTRUCT;
  }u;
};

struct Element
{
  struct Element* next;
  enum Type  type;
  double start;
  double end;
	int cLine;
  
  union 
  {
    struct MpiBarrier     mpiBarrier;
    struct MpiComm_create mpiComm_create;
    struct MpiComm_free   mpiComm_free;
    struct MpiBcast       mpiBcast;
    struct MpiFinalize    mpiFinalize;
    struct MpiRecv        mpiRecv;
    struct MpiInit        mpiInit;
    struct MpiSend        mpiSend;
    struct MpiSendrecv    mpiSendrecv;
  }u;
};
/**
 * This struct represents a rank inside a communicator.  
 */
struct Rank
{
	/**
	 * The rank of the rank inside the communicator COMM_WORLD.
	 */
  int global;
	/**
	 * The rank of the rank inside the new communicator. 
	 */
  int local;
	/**
	 * The communicator id.
	 */
  int cid;
};

/**
 * @brief Communicator struct
 *
 * This struct represents a MPI communicator.
 * Ever communicator contains a list of ranks.
 */
struct Communicator
{
	/**
	 * Name of the communicator or a empty string.
	 */
  gchar* name;
	/**
	 * List of ranks inside the 
	 */
  GSList* ranks;
};

struct FileList
{
  gchar* path;
  int initialSize;
  gchar* implementation;
};

#endif
