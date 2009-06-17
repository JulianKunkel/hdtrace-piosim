/**
 * @file util.c
 *
 * Implementations of general utility functions
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "util.h"

#include <string.h>
#include <ctype.h>
#include <assert.h>

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
 * Valid tag name must only contain graphical characters, not start with a
 *  number and must not contain one of the characters '$', '§', '%', '&', ';'.
 * In addition we do not accept ':' since it could be used as namespace
 * separator.
 * For simplicity currently we do only allow ASCII characters to avoid all
 * conflicts with different codepages, unicode and so on.
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
	/* must not be empty */
	if (!isValidString(string))
		return FALSE;

	/* must not start with a number */
	if (isdigit(string[0]))
		return FALSE;

	/* check each character */
	for (size_t i = 0; i < strlen(string); ++i)
	{
		/* must be graphical */
		if (!isgraph(string[i]))
			return FALSE;

		/* must be ASCII */
		if (!isascii(string[i]))
			return FALSE;

		/* must not be one of those ('§' missing since not ASCII) */
		switch (string[i])
		{
		case '$':
		case '%':
		case '&':
		case ';':
		case ':':
			return FALSE;
		default:
			break;
		}
	}

	return TRUE;
}

/**
 * Escape all characters not to be used in XML attribute values.
 *
 * Writes the input string to the output string escaping all characters
 * that should not appear in XML attribute values ('"', '<', '>', '&')
 *
 * @param dest Pointer to the memory for the resulting string
 * @param dlen Maximum length of the resulting string including '\\0'
 * @param src  String to use as input
 *
 * @return Success state
 *
 * @retval TRUE  Success
 * @retval FALSE Not enough space for resulting string
 */
BOOL escapeXMLString(char *dest, size_t dlen, const char *src)
{
	if (!isValidString(src) || strlen(src) > dlen - 1)
		return FALSE;

	/* clean output string */
	memset(dest, '\0', dlen);

	size_t o = 0;
	for (size_t i = 0; i < strlen(src); ++i)
	{
		switch(src[i])
		{
		case '"':
			if ( o + 6 > dlen )
				return FALSE;
			dest[o++] = '&';
			dest[o++] = 'q';
			dest[o++] = 'u';
			dest[o++] = 'o';
			dest[o++] = 't';
			dest[o++] = ';';
			break;
		case '<':
			if ( o + 4 > dlen )
				return FALSE;
			dest[o++] = '&';
			dest[o++] = 'l';
			dest[o++] = 't';
			dest[o++] = ';';
			break;
		case '>':
			if ( o + 4 > dlen )
				return FALSE;
			dest[o++] = '&';
			dest[o++] = 'g';
			dest[o++] = 't';
			dest[o++] = ';';
			break;
		case '&':
			if ( o + 4 > dlen )
				return FALSE;
			dest[o++] = '&';
			dest[o++] = 'a';
			dest[o++] = 'm';
			dest[o++] = 'p';
			dest[o++] = ';';
			break;
		default:
			dest[o++] = src[i];
		}
	}

	/* assure output string is '\0' terminated */
	assert (dest[dlen-1] == '\0');

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
