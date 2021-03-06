/**
 * @file config.h
 *
 * Definition of some common constants
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef CONFIG_H_
#define CONFIG_H_

/* ************************************************************************* *
 *                        USER CONFIGURABLE VALUES                           *
 * ************************************************************************* */

/**
 * Default verbosity level
 *  (overridden by environment variable HDT_VERBOSITY)
 */
#define VLEVEL 0

/**
 * Maximal length of trace file names.
 */
#define HD_MAX_FILENAME_LENGTH   1024

/**
 * Timeout for writing files in seconds.
 */
#define HD_WRITE_TIMEOUT  10

/**
 * Maximal length of group names
 */
#define HDS_MAX_GROUP_NAME_LENGTH  20

/**
 * Maximal length of value names
 */
#define HDS_MAX_VALUE_NAME_LENGTH  50

/**
 * Maximal length of units
 */
#define HDS_MAX_UNIT_NAME_LENGTH  10

/**
 * Maximal length of grouping names
 */
#define HDS_MAX_GROUPING_NAME_LENGTH  20

/**
 * Size of buffer to use for collecting entry values
 */
#define HDS_ENTRY_BUF_SIZE 1024


/* ************************************************************************* *
 *                   HDTRACE PROTOCOL SPECIFIED VALUES                       *
 *                                                                           *
 *       !!! DO NOT CHANGE UNLESS YOU KNOW WHAT YOU ARE DOING !!!            *
 * ************************************************************************* */

/**
 * String to use for indentation
 */
#define HD_INDENT_STRING  "  "

/**
 * Length of one timestamp in bytes
 */
#define HDS_TIMESTAMP_LENGTH 8

/**
 * Length of the header size in bytes
 */
#define HDS_HEADER_SIZE_LENGTH 5

/**
 * Size of buffer to use for header creation
 *
 * Needs to be 10 ^ HDS_HEADER_SIZE_LENGTH - 1 + HDS_HEADER_SIZE_LENGTH + 1
 * since this is highest number that can take place as decimal in
 * HDS_HEADER_SIZE_LENGTH bytes plus the bytes for the number itself and the
 * separating newline.
 */
#define HDS_HEADER_BUF_SIZE (100000 -1 + 5 + 1)


#endif /* CONFIG_H_ */
