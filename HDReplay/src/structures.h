
#ifndef _STRUCTURES_H_
#define _STRUCTURES_H_

#include "readxml.h"
#include "glib.h"
#include "constant.h"

struct MpiBarrier
{
  int cid;
  int cLine;
};


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
  MpiFinalize,
  MpiInit,
  MpiRecv,
  MpiSend,
  MpiSendrecv
};

struct Element
{
  struct Element* next;
  enum Type  type;
  double start;
  double end;
  
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

struct Rank
{
  int global;
  int local;
  int cid;
};

struct Communicator
{
  char name[COMM_NAME_LEN];
  GSList* ranks;
};

struct FileList
{
  char name[PATH_LEN];
  int initialSize;
  char implementation[PATH_LEN];
};

#endif 
