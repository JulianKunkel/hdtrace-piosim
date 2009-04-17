#include "common.h"

/**
 * Print a debug message to \a stdout with printf-like
 * formatting.
 *
 * \param format printf-like format string
 * \param ... arguments for format string
 */
static void printDebugMessage(const char * format, ...)
{
	va_list valist;
	va_start(valist, format);
	printf(DEBUG_MESSAGE_PREFIX);
	vprintf(format, valist);
	printf("\n");
	va_end(valist);
}


