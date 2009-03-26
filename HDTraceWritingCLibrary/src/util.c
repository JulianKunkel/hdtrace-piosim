/**
 * @file util.c
 *
 * Implementations of common utility functions
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include "util.h"

#include <string.h>

/**
 * @details
 * Valid strings means here a string containing characters. So the given string
 * is checked to be a NULL pointer or zero length string. If so, return false
 * since it is an invalid string.
 *
 * @param string String to check
 *
 * @return Truth value meaning if the given string is valid.
 *
 * @retval TRUE  \a string is valid
 * @retval FALSE \a string is invalid
 */
BOOL isValidString(const char *string) {
	if (string == NULL || strlen(string) == 0)
		return FALSE;
	else
		return TRUE;
}
