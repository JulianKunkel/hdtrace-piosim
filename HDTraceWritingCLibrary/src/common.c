/**
 * @file common.c
 *
 * Implementation of functions used by hdTrace and hdStats in common
 *
 * @date 28.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */


#include "common.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#include "hdError.h"
#include "hdTopo.h"
#include "util.h"

#include "config.h"

/**
 * Generate well formed filename.
 *
 * This function generates the filenames for the traces. If \a group is
 *  \a NULL, a name of the form "[Project]_[Level1]_[Level2]...[Affix]" is
 *  generated to be used in hdTrace. If \a group is not \a NULL a filename of
 *  the form "[Project]_[Level1]_[Level2]..._[Group][Affix]" is generated for
 *  hdStats.
 *
 * For hdTrace usage \a level should always be the number of the highest level
 *  of the topology since hdTrace files are only allowed for topology leaf
 *  nodes.
 *
 * <b>The memory for the returned \a char* pointing to is allocated by this
 *  function but the caller has to free it by himself.</b>
 *
 * Example:
 * @code
 * char * filename = generateFilename(toponode, hdT_getTopoNodeLevel(toponode), NULL, ".xml");
 * if (filename == NULL)
 *         // error
 * @endcode
 * @code
 * char * filename = generateFilename(toponode, topoLevel, groupName, ".dat");
 * if (filename == NULL)
 *         // error
 * @endcode
 *
 * @param project   Project the file is for
 * @param toponode  Topology node to use
 * @param level     Topology level to create the filename for
 * @param group     Name of statistics group or \a NULL for hdTrace filename
 * @param affix     Affix to append to the filename
 *
 * @return Generated filename of \a NULL on error setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
char * generateFilename(const char *project, hdTopoNode toponode,
		int level, const char *group, const char* affix)
{
	/* check input */
	if (!isValidString(project) || hdT_getTopoNodeLevel(toponode) <= level
			|| !isValidString(affix))
	{
		errno = HD_ERR_INVALID_ARGUMENT;
		return NULL;
	}

	/* generate filename */
	char *filename = malloc(HD_MAX_FILENAME_LENGTH * sizeof(*filename));
	size_t pos = 0;
	size_t ret;

#define ERROR_CHECK \
	if (ret >= HD_MAX_FILENAME_LENGTH - pos) \
	{ \
		errno = HD_ERR_CREATE_FILE; \
		free(filename); \
		return NULL; \
	} \

	ret = snprintf(filename, HD_MAX_FILENAME_LENGTH, project);
	ERROR_CHECK
	pos = strlen(filename);

	/* append "_level" for each topology level */
	for (int i = 1; i <= level; ++i)
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos, "_%s",
				hdT_getTopoPathLabel(toponode, i));
		ERROR_CHECK
		pos = strlen(filename);
	}

	if (group == NULL)
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos, "%s", affix);
		ERROR_CHECK
	}
	else
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos,
				"_%s%s", group, affix);
		ERROR_CHECK
	}

#undef ERROR_CHECK

	return filename;
}
