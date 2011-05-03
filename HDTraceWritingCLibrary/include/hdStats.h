/**
 * @file hdStats.h
 *
 * Declarations of all functions and types for writing statistics
 *
 * @ifnot api_only
 *  @ingroup hdStats
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

/**
 * @addtogroup hdStats HDTrace Statistics Writing Library
 *
 * @details
 * In HDTrace format statistics are a special kind of trace for periodically
 * occurring data. One canonical usage is to trace utilization data
 * like CPU load, memory usage, used amounts of network or I/O bandwidth.
 *
 * The statistics are categorized in so called groups. Each group is associated
 * with the tracing topology at any level and each group's data is written
 * binary to its own data file.
 *
 * A statistics group consists of entries, each marked with a timestamp. One
 * entry consists of a tuple of values, each with one of several well defined
 * types. In one group, all entries have exactly the same number of values
 * with exactly the same types and so exactly the same length, except when
 * using values of string type (not yet implemented).
 *
 * The HDTrace statistics group data file is self-descriptive. At the very
 * beginning there is a header in XML describing the values and types
 * each entry in the following binary part of the file contains.
 */

#ifndef HDSTATS_H_
#define HDSTATS_H_

#include <endian.h>
#include <stdio.h>   /* standard input/output stuff */
#include <stdint.h>  /* standard integer type */

#ifdef _AIX
#warning "Why - its for science..."
/* On AIX not existing ... */

static inline uint32_t  bswap_32 (unsigned int buf)
{
	uint32_t ret;
	char * o = (char *) & ret;
	char * b = (char *) & buf;
	o[0] = b[3];
	o[1] = b[2];
	o[2] = b[1];
	o[3] = b[0];

	return ret;
}

static inline uint64_t  bswap_64 (unsigned int buf)
{
	uint64_t ret;
	char * o = (char *) & ret;
	char * b = (char *) & buf;
	o[0] = b[7];
	o[1] = b[6];
	o[2] = b[5];
	o[3] = b[4];
	o[4] = b[3];
	o[5] = b[2];
	o[6] = b[1];
	o[7] = b[0];

	return ret;
}

#else
#include <byteswap.h>
#endif

#include "hdError.h" /* error definitions */
#include "hdTopo.h" /* topology stuff */

/* ************************************************************************* *
 *                        FIXED VALUES DEFINITIONS                           *
 * ************************************************************************* */

/**
 * Maximum of statistics values per group
 */
#define HDS_MAX_VALUES_PER_GROUP 256


/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/**
 * Enumeration of value types for statistics groups.
 */
enum _hdStatsValueType {
    /** 32 bit integer */
    INT32,
    /** 64 bit integer */
    INT64,
    /** 64 bit unsigned integer */
    UINT64,
    /** single precision floating point */
    FLOAT,
    /** double precision floating point */
    DOUBLE,
    /** String */
    STRING
};

/** Definition of string representation of type INT32 */
#define INT32_STRING "INT32"
/** Definition of string representation of type INT64 */
#define INT64_STRING "INT64"
/** Definition of string representation of type UINT64 */
#define UINT64_STRING "UINT64"
/** Definition of string representation of type FLOAT */
#define FLOAT_STRING "FLOAT"
/** Definition of string representation of type DOUBLE */
#define DOUBLE_STRING "DOUBLE"
/** Definition of string representation of type STRING */
#define STRING_STRING "STRING"

/**
 * Type to use for value types for statistics groups.
 */
typedef enum _hdStatsValueType hdStatsValueType;

/**
 * Type to use for statistics groups.
 */
typedef struct _hdStatsGroup hdStatsGroup;


/* ************************************************************************* *
 *                      PUBLIC FUNCTION DECLARATIONS                         *
 * ************************************************************************* */

/**
 * @addtogroup hdStats
 *
 * @section seclu Library Usage
 * <b>Outline of creating HDTrace Statistics Groups using this library:</b>
 *  -# Create the group first (@ref hdS_createGroup, called once)
 *  -# Add all value info (@ref hdS_addValue, called several times)
 *  -# Commit the group (@ref hdS_commitGroup, called once)
 *  -# Use the group to trace data (@b hdS_write..., called many times)
 *  -# Finalize the group (@ref hdS_finalize, called once)
 *
 */

/**
 * Create a new statistics group.
 */
hdStatsGroup * hdS_createGroup (
        const char *groupName, /* Name of the new statistics group */
        hdTopoNode *topoNode,   /* Topology node to use */
        int topoLevel          /* Topology level the group shell belong to */
        );
/**
 * Create a new statistics group.
 * @param path_prefix Additional prefix added to the filename of the statistics file, e.g. /tmp/
 * @param abort Abort if statistics file already exists. 0=Don't abort, all other values abort.
 */
hdStatsGroup * hdS_createGroupWithFilenamePrefixAndAbortIfExists (
        const char *groupName, /* Name of the new statistics group */
        hdTopoNode *topoNode,   /* Topology node to use */
        int topoLevel,          /* Topology level the group shell belong to */
        const char *path_prefix, /* Prefix for the statistics file path */
        int abort /* Abort if file already exists */
        );

/**
 * Add a new value to the entry structure of a statistics group.
 */
int hdS_addValue (
        hdStatsGroup *group,     /* Statistics Group */
        const char* name,       /* Name of the new value */
        hdStatsValueType type,  /* Type of the new value */
        const char* unit,       /* Unit string of the new value */
        const char* grouping    /* Grouping string for the new value */
        );

/**
 * Commit statistics group, closes initialization step.
 */
int hdS_commitGroup (
        hdStatsGroup *group       /* Statistics Group */
        );

/**
 * Commit statistics group and initialize it at the given timestamp,
 * closes initialization step.
 */
int hdS_commitGroupAtTimestamp (
        hdStatsGroup *group,      /* Statistics Group */
        uint64_t timestamp        /* Passed timestamp */
        );

/**
 * Enable statistics group.
 */
int hdS_enableGroup(hdStatsGroup *group);

/**
 * Disable statistics group.
 */
int hdS_disableGroup(hdStatsGroup *group);

/**
 * Get if statistics group is enabled.
 */
int hdS_isEnabled(hdStatsGroup *group);

/**
 * @addtogroup hdStats
 *
 * @subsection ssecwv Writing Values
 * Write order of the values must be exactly the same as they were registered!
 *
 * At the beginning of each entry, the current time is taken and a timestamp is
 * written for the new entry. This is always done, when the first @b hdS_write*
 * function for an entry is called. A call to @ref hdS_writeEntry is always
 * the first such call for an entry.@n
 * The timestamp is 4 byte integer seconds and 4 byte integer nanoseconds since
 * epoch (Jan 01 1970). Function @b gettimeofday is used to get the time and the
 * returned microseconds are transferred to nanoseconds.
 *
 * @subsection ssecfc File Content
 * The file written for each statistics group is constructed in three parts.
 * -# The first part is only 6 bytes an contains the length of the header, the
 *  second part, as string in decimal notation with 5 numbers and leading zeros
 *  followed by a newline character '\\n'.
 * -# The second part is the header. It describes the binary data, the third
 *  part, and is written in correct XML notation.
 * -# The third part is where the actual trace data are written to in entries
 *  like described in "Writing Values".
 *
 * Example:
 * @code
 * 00362
 * <Group name="Utilization" timestampDatatype="EPOCH"
 *     timeAdjustment="-0000000000.000000000">
 *   <Value name="CPU_TOTAL" type="FLOAT" unit="%" grouping="CPU" />
 *   <Value name="MEM_USED" type="INT64" unit="B" grouping="MEM" />
 *   <Value name="NET_IN" type="INT64" unit="B" grouping="NET" />
 *   <Value name="NET_OUT" type="INT64" unit="B" grouping="NET" />
 * </Group>
 * BINARYBINARYBINARYBINARYBINAY.......
 * @endcode
 * 362 since '\\n' and spaces also count of cause.
 */

/**
 * Writes a complete entry to a statistics group.
 */
int hdS_writeEntry (
        hdStatsGroup *group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        size_t entryLength          /* Length of the entry to write */
        );

/**
 * Writes 4 byte integer as next value to a statistics group.
 */
int hdS_writeInt32Value (
        hdStatsGroup *group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        );

/**
 * Writes 8 byte integer as next value to a statistics group.
 */
int hdS_writeInt64Value (
        hdStatsGroup *group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        );

/**
 * Writes 8 byte unsigned integer as next value to a statistics group.
 */
int hdS_writeUInt64Value (
        hdStatsGroup *group,      /* Statistics Group */
        uint64_t value            /* UINT64 value to write */
        );

/**
 * Writes 4 byte float as next value to a statistics group.
 */
int hdS_writeFloatValue (
        hdStatsGroup *group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        );

/**
 * Writes 8 byte double as next value to a statistics group.
 */
int hdS_writeDoubleValue (
        hdStatsGroup *group,      /* Statistics Group */
        double value             /* DOUBLE value to write */
        );

/**
 * Writes 8 byte double as next value at given timestamp to a statistics group.
 */
int hdS_writeDoubleValueAtTimestamp (
        hdStatsGroup *group,      /* Statistics Group */
        double value,             /* DOUBLE value to write */
        uint64_t timestamp        /* Timestamp corresponding to the value */
        );

/**
 * Writes string as the next value to a statistics group.
 */
int hdS_writeString (
        hdStatsGroup *group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        );

/**
 * Finalizes a statistics group.
 */
int hdS_finalize(
        hdStatsGroup *group      /* Statistics Group */
        );


/* ************************************************************************* *
 *                        PUBLIC MACRO DEFINITIONS                           *
 * ************************************************************************* */

/* Defines for byte order conversion */
//#include <endian.h>
//#include <byteswap.h>

//#define order_bytes32_p(x) *((uint32_t *) x) = bswap_32(*((uint32_t *) x))
//#define order_bytes64_p(x) *((uint64_t *) x) = bswap_64(*((uint64_t *) x))

/**
 * @def order_bytes32ip
 * Conversation of a 32 bit integer to network byte order.
 * The argument has to be a pointer to the integer and becomes converted in
 *  place.
 */
/**
 * @def order_bytes64ip
 * Conversation of a 64 bit integer to network byte order.
 * The argument has to be a pointer to the integer and becomes converted in
 *  place.
 */
#if __BYTE_ORDER == __LITTLE_ENDIAN
# define order_bytes32ip(x) *(x) = bswap_32(*(x))
# define order_bytes64ip(x) *(x) = bswap_64(*(x))
#elif __BYTE_ORDER == __BIG_ENDIAN
# define order_bytes32ip(x) (x)
# define order_bytes64ip(x) (x)
#endif
/**
 * @def order_bytes32fp
 * Conversation of a 32 bit floating point number (float) to network byte
 *  order.
 * The argument has to be a pointer to the float and becomes converted in
 *  place.
 */
/**
 * @def order_bytes64fp
 * Conversation of a 64 bit floating point number (double) to network byte
 *  order.
 * The argument has to be a pointer to the double and becomes converted in
 *  place.
 */
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


#endif /* HDSTATS_H_ */

/* vim: set sw=4 sts=4 et tw=80: */
