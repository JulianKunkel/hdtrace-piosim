/**
 * @file hdStats.h
 * @ingroup hdStats
 *
 * Declarations of all functions and types for writing statistics
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

/**
 * @addtogroup hdStats HDTrace Statistics Writing Library
 *
 * @details
 * In HDTrace Format statistics are a special kind of trace for periodically
 * occurring data. One canonical usage for that is to trace performance data
 * like CPU load, Memory usage, used amounts of Network or I/O bandwidth.
 *
 * The statistics are categorized in so called groups. Each group is associated
 * with the tracing topology at any level (not only the highest as the normal
 * traces) and each group's data is written binary to its own data file.
 *
 * A statistics group consists of entries, each marked with a timestamp. One
 * entry consists of a row of values, each with one of several well defined
 * types. In one group, all entries have exactly the same number of values
 * with exactly the same types and so exactly the same length, except when
 * using values of string type.
 *
 * The HDTrace statistics group data file is self-descriptive. At the very
 * beginning there is a header in pseudo-XML describing the values and types
 * each entry in the following binary part of the file contains.
 */

#ifndef HDSTATS_H_
#define HDSTATS_H_

#include <stdio.h>   /* standard input/output stuff */
#include <stdint.h>  /* standard integer type */

#include "hdError.h" /* error definitions */
#include "hdTopo.h" /* topology stuff */


/**
 * Enumeration of value types for statistics groups.
 */
enum _hdStatsValueType {
    /** 32 bit integer */
    INT32,
    /** 64 bit integer */
    INT64,
    /** single precision floating point */
    FLOAT,
    /** double precision floating point */
    DOUBLE,
    /** String */
    STRING
};

/**
 * Type to use for value types for statistics groups.
 */
typedef enum _hdStatsValueType hdStatsValueType;

/**
 * Structure representing one statistics group.
 */
struct _hdStatsGroup {
    /**
     * File descriptor of the statistics group file
     */
	int fd;

    /**
     * Filename of the statistics group file (for error output only)
     */
    char *tracefile;

    /**
     * Buffer for creating header and collecting entries
     */
    char *buffer;

    /**
     * Offset for buffer to write next byte
     */
    int offset;

    /**
     * True if string values are defined
     * => reduced error checking
     */
    int hasString;

    /**
     * Length that an entry should have
     */
    int entryLength;

    /**
     * Types of the defined values (for error checking)
     * '-1' terminated in @ref hdS_commitGroup
     */
    hdStatsValueType *valueTypes;

    /**
     * Index of the next value to write (for error checking)
     */
    int nextValueIdx;

    /**
     * True if the group is committed (for error checking)
     */
    int isCommitted;

    /**
     * True if the group is enabled to trace
     */
    int isEnabled;
};

/**
 * Type to use for statistics groups.
 */
typedef struct _hdStatsGroup * hdStatsGroup;

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
hdStatsGroup hdS_createGroup (
        const char *groupName, /* Name of the new statistics group */
        hdTopology topology,   /* Topology to use, only needed for project name */
        hdTopoNode topoNode,   /* Topology node to use */
        int topoLevel          /* Topology level the group shell belong to */
        );

/**
 * Add a new value to statistics group.
 */
int hdS_addValue (
        hdStatsGroup group,      /* Statistics Group */
        const char* name,        /* Name of the new value */
        hdStatsValueType type,   /* Type of the new value */
        const char* unit,        /* Unit string of the new value */
        long readOutMultiplier   /* Multiplier to match unit */
        );

/**
 * Commit Group, closes initialization step.
 */
int hdS_commitGroup (
        hdStatsGroup group       /* Statistics Group */
        );

/**
 * Enable statistics group.
 */
int hdS_enableGroup(hdStatsGroup group);

/**
 * Disable statistics group.
 */
int hdS_disableGroup(hdStatsGroup group);

/**
 * Get if statistics group is enabled.
 */
int hdS_isEnabled(hdStatsGroup group);

/**
 * @addtogroup hdStats
 *
 * @subsection ssecwv Writing Values
 * Write order of the values must be the exactly the same as they are
 * registered!
 *
 * At the beginning of each entry, the current time is taken and a timestamp is
 * written for the new entry. This is always done, when the first @b hdS_write*
 * function for an entry is called. Of cause a call to @ref hdS_writeEntry is
 * always the first such call for an entry.@n
 * The timestamp is 4 byte integer seconds and 4 byte integer nanoseconds since
 * epoch (Jan 01 1970). Function @b gettimeofday is used to get the time and the
 * returned microseconds are transferred to nanoseconds.
 *
 * @subsection ssecfc File Content
 * The file written for each statistics group is constructed in three parts.
 * -# The first part is only 5 bytes an contains the length of the header, the
 *  second part, as string in decimal notation with leading zeros.
 * -# The second part is the header. It describes the binary data, the third
 *  part, and is written in incomplete XML notation.
 * -# The third part is where the actual trace data are written to in entries
 *  like described in "Writing Values".
 *
 * Example:
 * @code
 * 00152
 * <Energy timestampDatatype="EPOCH">
 *   <Voltage type="float" unit="mV"/>
 *   <Current type="float" unit="mA"/>
 *   <Power type="float" unit="mW"/>
 * </Energy>
 * BINARYBINARYBINARYBINARYBINAY.......
 * @endcode
 * 152 since '\\n' and spaces also count of cause.
 */

/**
 * Writes a complete entry to a statistics group.
 */
int hdS_writeEntry (
        hdStatsGroup group,      /* Statistics Group */
        void * entry,            /* Pointer to the entry to write */
        int entryLength          /* Length of the entry to write */
        );

/**
 * Writes 4 byte integer as next value to a statistics group.
 */
int hdS_writeInt32Value (
        hdStatsGroup group,      /* Statistics Group */
        int32_t value            /* INT32 value to write */
        );

/**
 * Writes 8 byte integer as next value to a statistics group.
 */
int hdS_writeInt64Value (
        hdStatsGroup group,      /* Statistics Group */
        int64_t value            /* INT64 value to write */
        );

/**
 * Writes 4 byte float as next value to a statistics group.
 */
int hdS_writeFloatValue (
        hdStatsGroup group,      /* Statistics Group */
        float value              /* FLOAT value to write */
        );

/**
 * Writes 8 byte double as next value to a statistics group.
 */
int hdS_writeDoubleValue (
        hdStatsGroup group,      /* Statistics Group */
        double value             /* DOUBLE value to write */
        );

/**
 * Writes string as the next value to a statistics group.
 */
int hdS_writeString (
        hdStatsGroup group,      /* Statistics Group */
        const char * str         /* STRING value to write */
        );

/**
 * Finalizes a statistics group.
 */
int hdS_finalize(
        hdStatsGroup group      /* Statistics Group */
        );

#endif /* HDSTATS_H_ */

/* vim: set sw=4 sts=4 et tw=80: */
