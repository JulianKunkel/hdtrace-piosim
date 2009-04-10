/**
 * @file config.h
 *
 * Definition of some common constants
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#ifndef CONFIG_H_
#define CONFIG_H_

/* ************************************************************************* *
 *                        USER CONFIGURABLE VALUES                           *
 * ************************************************************************* */

/**
 * Verbosity level
 * TODO: Make this controlled by environment
 */
#define VLEVEL 5

/**
 * Maximal length of trace file names.
 */
#define HD_MAX_FILENAME_LENGTH   1024

/**
 * Timeout for writing files in seconds.
 */
#define HD_WRITE_TIMEOUT  10

/**
 * Maximum of statistics values per group
 */
#define HDS_MAX_VALUES_PER_GROUP 256

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
#define HDS_HEADER_BUF_SIZE (10000 -1 + 5 + 1)


#endif /* CONFIG_H_ */
