/*
 * topo.h
 *
 *  Created on: Mar 1, 2010
 *      Author: Timo Minartz
 */

#ifndef TOPO_H_
#define TOPO_H_

#include "pt-pkg.h"

#ifdef HAVE_HDTWLIB
#include "hdTopo.h"
#include "hdStats.h"
#include "hdError.h"
#else

#include <stdint.h>
#include <string.h>

#include <byteswap.h>

#define HDS_MAX_VALUES_PER_GROUP 256

#define INT32_STRING "INT32"
#define INT64_STRING "INT64"
#define FLOAT_STRING "FLOAT"
#define DOUBLE_STRING "DOUBLE"
#define STRING_STRING "STRING"

typedef struct _hdTopology hdTopology;
typedef struct _hdTopoNode hdTopoNode;

hdTopology * hdT_createTopology(const char *project, const char **levels,
		int nlevels);

int hdT_getTopoDepth(const hdTopology *topology);

int hdT_destroyTopology(hdTopology *topology);

hdTopoNode * hdT_createTopoNode(hdTopology *topology, const char **path,
		int length);

int hdT_getTopoNodeLevel(const hdTopoNode *node);

const char * hdT_getTopoPathString(const hdTopoNode *node);

const char * hdT_getTopoPathLabel(const hdTopoNode *node, int level);

int hdT_destroyTopoNode(hdTopoNode *node);

/** end hdTopo **/
/** begin hdStats **/

typedef enum _hdStatsValueType hdStatsValueType;

enum _hdStatsValueType {
    INT32,
    INT64,
    FLOAT,
    DOUBLE,
    STRING
};


typedef struct _hdStatsGroup hdStatsGroup;

hdStatsGroup * hdS_createGroup (
        const char *groupName, /* Name of the new statistics group */
        hdTopoNode *topoNode,   /* Topology node to use */
        int topoLevel          /* Topology level the group shell belong to */
        );

int hdS_addValue (
        hdStatsGroup *group,     /* Statistics Group */
        const char* name,       /* Name of the new value */
        hdStatsValueType type,  /* Type of the new value */
        const char* unit,       /* Unit string of the new value */
        const char* grouping    /* Grouping string for the new value */
        );

int hdS_commitGroup (
        hdStatsGroup *group       /* Statistics Group */
        );

int hdS_enableGroup(hdStatsGroup *group);

int hdS_disableGroup(hdStatsGroup *group);

int hdS_isEnabled(hdStatsGroup *group);

int hdS_writeEntry (
        hdStatsGroup *group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        size_t entryLength          /* Length of the entry to write */
        );

int hdS_writeInt32Value (
        hdStatsGroup *group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        );

int hdS_writeInt64Value (
        hdStatsGroup *group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        );

int hdS_writeFloatValue (
        hdStatsGroup *group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        );

int hdS_writeDoubleValue (
        hdStatsGroup *group,
        double value
        );

int hdS_writeString (
        hdStatsGroup *group,
        const char * str
        );

int hdS_finalize(hdStatsGroup *group);

#if __BYTE_ORDER == __LITTLE_ENDIAN
# define order_bytes32ip(x) *(x) = bswap_32(*(x))
# define order_bytes64ip(x) *(x) = bswap_64(*(x))
#elif __BYTE_ORDER == __BIG_ENDIAN
# define order_bytes32ip(x) (x)
# define order_bytes64ip(x) (x)
#endif

#if __FLOAT_WORD_ORDER == __LITTLE_ENDIAN
/* the memcpys areneeded for ISO C's strict aliasing rules */
# define order_bytes32fp(x) \
	do { \
		uint32_t *tmp = alloca(sizeof(*tmp)); \
		memcpy(tmp, x, 4); \
		*tmp = bswap_32(*tmp); \
		memcpy(x, tmp, 4); \
	} while (0);
# define order_bytes64fp(x) \
	do { \
		uint64_t *tmp = alloca(sizeof(*tmp)); \
		memcpy(tmp, x, 8); \
		*tmp = bswap_64(*tmp); \
		memcpy(x, tmp, 8); \
	} while (0);
#elif __FLOAT_WORD_ORDER == __BIG_ENDIAN
# define order_bytes32f(x) (x)
# define order_bytes64f(x) (x)
#endif

/** end hdStats **/
/** begin hdError **/

#define HD_MIN_ERRNO_VALUE  800000

enum hdCommonError {
	/** Invalid argument */
	HD_ERR_INVALID_ARGUMENT = HD_MIN_ERRNO_VALUE,
    /** Error while memory allocation */
    HD_ERR_MALLOC,
    /** Error due to buffer overflow */
    HD_ERR_BUFFER_OVERFLOW,
    /** Error while getting system time */
    HD_ERR_GET_TIME,
    /** Error while creating a file */
    HD_ERR_CREATE_FILE,
    /** Error while writing a file */
	HD_ERR_WRITE_FILE,
    /** Error while closing a file */
	HD_ERR_CLOSE_FILE,
	/** Timeout occurred */
	HD_ERR_TIMEOUT,
	/** Trace is disabled */
	HD_ERR_TRACE_DISABLED,
	/** function may not be called in this context */
	HD_ERR_INVALID_CONTEXT,
	/** Error with unknown cause */
	HD_ERR_UNKNOWN
};

/**
 * Enumeration for errors in trace functions.
 * hdT_* functions  can set errno to one of these values in case of an error
 */
enum hdTraceError {
	/** Example error */
	HDT_EXAMPLE  = HD_MIN_ERRNO_VALUE + 100
};

/**
 * Enumeration for errors in statistics functions.
 * hdS_* functions can set errno to one of these values in case of an error
 */
enum hdStatsError {
	/** Statistics group's commit state is not the needed */
	HDS_ERR_GROUP_COMMIT_STATE = HD_MIN_ERRNO_VALUE + 200,
	/** One of the arguments has an unexpected value */
	HDS_ERR_UNEXPECTED_ARGVALUE,
	/** State of the current entry is wrong for requested action */
	HDS_ERR_ENTRY_STATE
};

/******** @cond api_only ********/
/**
 * @enum hdCommonError
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_INVALID_ARGUMENT
 * @ingroup hdError
 */
/**
 * @var HD_ERR_MALLOC
 * @ingroup hdError
 */
/**
 * @var HD_ERR_BUFFER_OVERFLOW
 * @ingroup hdError
 */
/**
 * @var HD_ERR_GET_TIME
 * @ingroup hdError
 */
/**
 * @var HD_ERR_CREATE_FILE
 * @ingroup hdError
 */
/**
 * @var HD_ERR_WRITE_FILE
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_CLOSE_FILE
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_TIMEOUT
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_TRACE_DISABLED
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_UNKNOWN
 * @ingroup hdError
 */
/**
 * @enum hdStatsError
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_GROUP_COMMIT_STATE
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_UNEXPECTED_ARGVALUE
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_ENTRY_STATE
 * @ingroup hdError
 */
/** @endcond **/


char* hdT_strerror(int errno);

#endif

#endif /* TOPO_H_ */
