/**
 * @file config.h
 *
 * Definition of some common constants
 *
 * @date 30.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef CONFIG_H_
#define CONFIG_H_

/**
 * Maximal length of trace file names.
 */
#define HD_MAX_FILENAME_LENGTH   1024

/**
 * Timeout for writing files in seconds.
 */
#define HD_WRITE_TIMEOUT  10

/**
 * Verbosity level
 * TODO: Make this controlled by environment
 */
#define VLEVEL 5

/**
 * Maximum of statistics values per group
 */
#define HDS_MAX_VALUES_PER_GROUP 256

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

/**
 * String to use for indentation
 */
#define HD_INDENT_STRING  "  "

/**
 * Size of buffer to use for collecting entry values
 */
#define HDS_ENTRY_BUF_SIZE 1024



#endif /* CONFIG_H_ */
