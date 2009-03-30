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
 * This function generates the filenames for the traces. If \a groupname is
 *  \a NULL, a name of the form "[Project]_[Level1]_[Level2]...[Affix]" is
 *  generated to be used in hdTrace. If \a groupname is not \a NULL a filename
 *  of the form "[Project]_[Level1]_[Level2]..._[Group][Affix]" is generated
 *  for hdStats.
 *
 * For hdTrace usage \a max_level should always be the depth of the topology
 *  minus 1 since hdTrace files are only allowed for topology leaf nodes.
 *
 * <b>The memory the returned \a char* pointing to is allocated by this
 *  function but the calling function has free it.</b>
 *
 * Example:
 * @code
 * char * filename = generateFilename(topology, hdT_getTopoDepth(topology), NULL, ".xml");
 * if (filename == NULL)
 *         // error
 * @endcode
 * @code
 * char * filename = generateFilename(topology, topologyLevel, groupName, ".dat");
 * if (filename == NULL)
 *         // error
 * @endcode
 *
 * @param project   Project the file is for
 * @param topology  Topology the file is for
 * @param max_level Maximum level to include in the filename
 * @param group     Name of statistics group or \a NULL for hdTrace filename
 * @param affix     Affix to append to the filename
 *
 * @return Generated filename of \a NULL on error setting \a errno
 *
 * @errno
 * - HD_ERR_INVALID_ARGUMENT
 */
char * generateFilename(const char *project, hdTopology topology,
		int max_level, const char *group, const char* affix)
{
	/* check input */
	if (!isValidString(project) || hdT_getTopoDepth(topology) >= max_level
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
	for (int i = 0; i <= max_level; ++i)
	{
		ret = snprintf(filename+pos, HD_MAX_FILENAME_LENGTH - pos, "_%s",
				hdT_getTopoLevel(topology, i));
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
