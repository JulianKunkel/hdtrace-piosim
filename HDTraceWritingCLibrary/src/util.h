/**
 * @file util.h
 *
 * Declarations of common utility functions and types
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef UTIL_H_
#define UTIL_H_

#include <stdint.h>
#include <sys/types.h>

/**
 * Define boolean type
 */
typedef int8_t BOOL;

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
 * Returns minimum of the two size arguments.
 */
size_t minSize(size_t a, size_t b);

#endif /* UTIL_H_ */
