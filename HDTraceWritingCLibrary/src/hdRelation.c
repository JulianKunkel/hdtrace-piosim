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
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "common.h"
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
 * Contains all topology mappings from topology to topology token
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
 * Length of the remote token prefix
 */
static size_t remoteTokenLen;

/**
 * Unique number for the topology token
 */
static int     lastTopologyNumber = 0;

/**
 * Unique number for the token number
 */
static uint64_t lastTokenNumber = 0;

/**
 * Structure representing one relation trace.
 * For each topology one trace is created.
 */
struct _hdRelationTopo {
	/**
	 * Each topology has its own unique number
	 */
	int topologyNumber;

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

/**
 * Reduce the number of cast warnings by using this function:
 */
inline static int getTopologyNumber(gconstpointer key){
	return ((hdR_topoToken) key)->topologyNumber;
}

static guint structRelationHash(gconstpointer key){
	return (guint) getTopologyNumber(key);
}

static gboolean structRelationEqual(gconstpointer a, gconstpointer b){
	return ( getTopologyNumber(a) == getTopologyNumber(b) ) ? TRUE : FALSE;
}

/**
 * Reduce the number of cast warnings by using this function:
 */
inline static char * getTopologyNodeString(gconstpointer key){
	return ((hdTopoNode) key)->string;
}

static guint topologyHash(gconstpointer key){
	return g_str_hash(getTopologyNodeString(key));
}

static gboolean topologyEqual(gconstpointer a, gconstpointer b){
	return (strcmp(getTopologyNodeString(a), getTopologyNodeString(b)) == 0) ? TRUE : FALSE;
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

	tokenTopoMap = g_hash_table_new(& structRelationHash, & structRelationEqual);
	if(tokenTopoMap == NULL){
		free(topoMap);
		return -1;
	}

	// prepare unique token prefix
    gethostname(hostname, HOST_NAME_MAX);
    int pid = getpid();
    snprintf(pidstr, 10, "%d", pid);

    remoteTokenLen = strlen(hostname) + strlen(pidstr) ;
	remoteTokenPrefix = malloc(remoteTokenLen + 1);
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
	const hdR_topoToken token = (hdR_topoToken) g_hash_table_lookup(topoMap, (gpointer) topNode);
	if(token != NULL){
		return -1;
	}

	hdR_topoToken topoToken = malloc(sizeof(struct _hdRelationTopo));
	if(topoToken == NULL){
		return -1;
	}

	topoToken->logfile = generateFilename( topNode, topNode->length, NULL, "rel" );

	topoToken->log_fd = open(topoToken->logfile, O_CREAT | O_WRONLY | O_TRUNC | O_NONBLOCK, 0662);

	if( topoToken->log_fd == -1){
		free(topoToken->logfile);
		free(topoToken);
		return -1;
	}


	topoToken->buffer_pos = 0;
	topoToken->isEnabled = TRUE;
	topoToken->topoNode = topNode;
	topoToken->topologyNumber = lastTopologyNumber++;

	*outTopoToken = topoToken;

	g_hash_table_insert(topoMap, (gpointer) topNode, topoToken);
	g_hash_table_insert(tokenTopoMap, (gpointer) topoToken, topNode);

	return 0;
}

int hdR_finalize(hdTopoNode topNode){
	if(topoMap == NULL){
		return -1;
	}

	hdR_topoToken token = (hdR_topoToken) g_hash_table_lookup(topoMap, (gpointer) topNode);
	if(token == NULL){
		return -1;
	}

	assert( g_hash_table_remove(topoMap, topNode) == TRUE);

	assert( g_hash_table_remove(tokenTopoMap, token) == TRUE);

	// free and finalize contained data:
	close(token->log_fd);
	free(token->logfile);
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

char * hdR_getLocalToken(hdR_token token){
	if(token == NULL){
			return NULL;
	}

	char * buffer = malloc(25 * sizeof(char));
	snprintf(buffer, 25, "%llu:%llu", (long long unsigned) token->id, (long long unsigned) token->topoToken->topologyNumber);
	return buffer;
}

char * hdR_getUniqueToken(hdR_token token){
	if(token == NULL){
			return NULL;
	}

	char * buffer = malloc(25 * sizeof(char) + remoteTokenLen);
	snprintf(buffer, 25 + remoteTokenLen, "%s:%llu:%llu", remoteTokenPrefix, (long long unsigned) token->id, (long long unsigned) token->topoToken->topologyNumber);
	return buffer;
}

/**
 * internal function:
 */
static inline hdR_token createToken(hdR_topoToken topoToken){
	struct _hdR_token * token = malloc(sizeof(struct _hdR_token));

	token->id = lastTokenNumber++;
	token->topoToken = topoToken;

	return token;
}




hdR_token hdR_createTopLevelRelation(hdR_topoToken topoToken){
	return createToken(topoToken);
}

hdR_token hdR_relateTopoToken(hdR_topoToken newTopologyToken, hdR_token parentToken){
	if(parentToken == NULL){
		return NULL;
	}

	return createToken(newTopologyToken);
}

hdR_token hdR_relateToken(hdR_topoToken newTopologyToken, const char * strTopoToken){
	if(strTopoToken == NULL){
			return NULL;
	}

	return createToken(newTopologyToken);
}

hdR_token hdR_relateRemoteToken(hdR_topoToken newTopologyToken, const char * remoteToken){
	if(remoteToken == NULL){
			return NULL;
	}

	return createToken(newTopologyToken);
}

int hdR_destroyRelation(hdR_token * token){
	if(token == NULL || *token == NULL){
		return -1;
	}
	free(*token);
	*token = NULL;

	return 0;
}

int hdR_start(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values, const char data_format, ...){
	return 0;
}

int hdR_end(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values, const char data_format, ... ){
	return 0;
}



