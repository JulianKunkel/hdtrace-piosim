/**
 * @file util.c
 *
 * Implementations of general utility functions
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 1
 */

#include "util.h"

#include <string.h>
#include <ctype.h>

/**
 * Check if the given string is valid.
 *
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

/**
 * Check if the given string is a valid XML tag name
 *
 * Valid tag string must not contain other characters than alphanumeric ascii.
 * We do not allow special characters and Unicode in out Tags.
 *
 * @param string String to check
 *
 * @return Truth value meaning if the given string is valid tag.
 *
 * @retval TRUE  \a string is valid tag
 * @retval FALSE \a string not valid tag
 */
BOOL isValidXMLTagString(const char *string)
{
	if (!isValidString(string))
		return FALSE;

	for (size_t i = 0; i < strlen(string); ++i)
	{
		/* must be ascii */
		if (!isascii(string[i]))
			return FALSE;
		/* must be alphanumeric */
		if (!isalnum(string[i]))
			return FALSE;
	}

	return TRUE;
}

/**
 * Returns minimum of the two size arguments.
 *
 * @param a First size
 * @param b Second size
 *
 * @return Minimum out of @a a and @a b
 */
size_t minSize(size_t a, size_t b)
{
	return a < b ? a : b;
}

/**
 * Returns maximum of the two size arguments.
 *
 * @param a First size
 * @param b Second size
 *
 * @return Maximum out of @a a and @a b
 */
size_t maxSize(size_t a, size_t b)
{
	return a > b ? a : b;
}
