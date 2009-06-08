/**
 * @file hdRelation.c
 *
 * @date 08.06.2009
 * @author Julian M. Kunkel
 * @version 0.6
 */

#include <glib.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>

#include "hdRelation.h"

/**
remote_id = <hostname><pid>
topo = <unique topo design>
unique id for a token =  remote_id + <ID>

XML:
<topo val="fqn" id=<topo_id>/> <!-- alternativ in extra datei, einmal pro
topologie -->
<rel v=<id> top=<id>/>
<rem rid=<new_id> val=<remote ID>/> <!-- alternativ in extra datei, einmal pro
remote endpunkt -->
<rel v=<id> rem=<rid>:<id>/>
<rel v=<id> rel=<id>/>
<s time="" v=<id> "attributes> data </start>
<e time="" v=<id> "attributes> data </end>
</un v=<id>>
*/


/**
 * Contains all topology mappings from topology to topoToken
 */
static GHashTable * topoMap = NULL;

/**
 * Contains all topology mappings from topology token to topology data
 */
static GHashTable * tokenTopoMap = NULL;

/**
 * Token prefix for remote access == <HOSTNAME><FQDN><PID>
 */
static char * remoteTokenPrefix;

/**
 * Unique number for the topology token
 */
static int     lastTopologyNumber = 0;

/**
 * Unique number for the token number
 */
static uint64_t lastTokenNumber = 0;

/**
 * Structure representing one relation trace
 */
struct _hdRelationTrace {
	/**
	 * File descriptor of the XML file
	 */
	int log_fd;

	/**
	 * Name of the log file (for error output)
	 */
	char *logfile;

	/**
	 * This is the write buffer of the trace file. Data is not written
	 * directly to the output file but to this buffer, unless
	 * flushing is forced (\a hdT_setForceFlush(...))
	 */
	char buffer[HD_LOG_BUF_SIZE];

	/**
	 * This variable keeps track of the position at which writing
	 * can occur on \a buffer.
	 */
	size_t buffer_pos;

	/**
	 * Topology leaf this trace belongs to.
	 */
	hdTopoNode topoNode;

	/**
	 * \a isEnabled = 1 if the trace is enabled.
	 * \a isEnabled = 0 if the trace is disabled.
	 *
	 * Enabling and disabling can be done via
	 * \a hdR_enableTrace(...) and \a hdR_disableTrace(...)
	 */
	int isEnabled;
};

/**
 * Represents a unique relation
 */
struct _hdR_token{
	/**
	 * The real id which is unique under the given topology.
	 */
	uint64_t id;

	/**
	 * Topology ID, unique for this process
	 */
	hdR_topoToken topoToken;
};

static guint topologyHash(gconstpointer key){
	return g_str_hash(((hdTopoNode) key)->string);
}

static gboolean topologyEqual(gconstpointer a, gconstpointer b){
	return (strcmp( ((hdTopoNode) a)->string, ((hdTopoNode) b)->string) == 0) ? TRUE : FALSE;
}

/**
 * Initalize all required data structures.
 */
static int hdR_init(void){
    char hostname[HOST_NAME_MAX];
    char pidstr[10];

	topoMap = g_hash_table_new(& topologyHash, & topologyEqual);
	if(topoMap == NULL){
		return -1;
	}

	tokenTopoMap = g_hash_table_new(& g_int_hash, & g_int_equal);
	if(tokenTopoMap == NULL){
		free(topoMap);
		return -1;
	}

	// prepare unique token prefix
    gethostname(hostname, HOST_NAME_MAX);
    int pid = getpid();
    snprintf(pidstr, 10, "%d", pid);

	remoteTokenPrefix = malloc(strlen(hostname) + strlen(pidstr) + 1);
	sprintf(remoteTokenPrefix, "%s%s", hostname, pidstr);

	return 0;
}

int hdR_initTopology(hdTopoNode topNode, hdR_topoToken * outTopoToken){
	assert(outTopoToken != NULL);

	if(topoMap == NULL){
		int ret = hdR_init();
		if(ret != 0){
			return ret;
		}
	}

	// check if topology is already registered:
	const int* token = (int*) g_hash_table_lookup(topoMap, (gpointer) topNode);
	if(token != NULL){
		return -1;
	}

	// if not fine:
	int * lastValue = malloc(sizeof(int));
	if(lastValue == NULL){
		return -1;
	}
	*lastValue = lastTopologyNumber++;

	*outTopoToken = *lastValue;

	g_hash_table_insert(topoMap, (gpointer) topNode, lastValue);
	g_hash_table_insert(tokenTopoMap, (gpointer) lastValue, topNode);

	return 0;
}

int hdR_finalize(hdTopoNode topNode){
	if(topoMap == NULL){
		return -1;
	}

	int* token = (int*) g_hash_table_lookup(topoMap, (gpointer) topNode);
	if(token == NULL){
		return -1;
	}

	assert( g_hash_table_remove(topoMap, topNode) == TRUE);

	assert( g_hash_table_remove(tokenTopoMap, token) == TRUE);

	free(token);

	const guint size = g_hash_table_size(topoMap);
	if (size == 0){
		g_hash_table_destroy(topoMap);
		g_hash_table_destroy(tokenTopoMap);

		topoMap = NULL;

		free(remoteTokenPrefix);

		remoteTokenPrefix = NULL;
	}

	return 0;
}

hdR_token hdR_createTopLevelRelation(hdR_topoToken topoToken){
	struct _hdR_token * token = malloc(sizeof(struct _hdR_token));

	token->id = lastTokenNumber++;
	token->topoToken = topoToken;

	return token;
}

int hdR_destroyRelation(hdR_token token){
	if(token == NULL){
		return -1;
	}
	free(token);

	return 0;
}

char * hdR_getLocalToken(hdR_token token){
	if(token == NULL){
			return NULL;
	}

	char * buffer = malloc(25 * sizeof(char));
	snprintf(buffer, 25, "%llu:%llu", (long long unsigned) token->id, (long long unsigned) token->topoToken);
	return buffer;
}


