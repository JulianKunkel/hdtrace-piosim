/**
 * @file util.h
 *
 * Declarations of general utility functions and types
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef UTIL_H_
#define UTIL_H_

#include <stdint.h>
#include <sys/types.h>

/**
 * Define boolean type
 */
typedef uint8_t BOOL;

#ifndef TRUE
/**
 * Define truth value
 */
#define TRUE  1
#endif
#ifndef FALSE
/**
 * Define truth value
 */
#define FALSE 0
#endif

/**
 * Check if the given string is valid.
 */
BOOL isValidString(const char *string);

/**
 * Check if the given string is a valid XML tag name
 */
BOOL isValidXMLTagString(const char *string);

/**
 * Escape all characters not to be used in XML attribute values.
 */
BOOL escapeXMLString(char *dest, size_t dlen, const char *src);

/**
 * Returns minimum of the two size arguments.
 */
size_t minSize(size_t a, size_t b);

/**
 * Returns maximum of the two size arguments.
 */
size_t maxSize(size_t a, size_t b);

#endif /* UTIL_H_ */
