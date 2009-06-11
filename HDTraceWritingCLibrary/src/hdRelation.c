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
#include <errno.h>
#include <sys/time.h>

#include "common.h"
#include "hdRelation.h"
#include "hdError.h"

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
 * Macro to print info message (fix string)
 */
#define hdr_info(topoToken, string) \
	printf("I: [RELATION][%s]: %s\n", \
			hdT_getTopoPathString(topoToken->topoNode), string);


/**
 * Contains all topology mappings from topology to topology token
 */
static GHashTable * topoMap = NULL;

/**
 * Token prefix for remote access == <HOSTNAME><FQDN><PID>
 */
static char * remoteTokenPrefix;

/**
 * Identifies the host
 */
static char * uniqueHostID;

/**
 * Length of the remote token prefix
 */
static size_t remoteTokenLen;

/**
 * Token prefix for local access == <PID>
 */
static char * localTokenPrefix;

/**
 * Length of the remote local prefix
 */
static size_t localTokenLen;


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
	 * global time adjustment
	 */
	struct timeval timeAdjustment;

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
/*
inline static int getTopologyNumber(gconstpointer key){
	return ((hdR_topoToken) key)->topologyNumber;
}

static guint structRelationHash(gconstpointer key){
	return (guint) getTopologyNumber(key);
}

static gboolean structRelationEqual(gconstpointer a, gconstpointer b){
	return ( getTopologyNumber(a) == getTopologyNumber(b) ) ? TRUE : FALSE;
}
*/

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

	// prepare unique token prefix
	gethostname(hostname, HOST_NAME_MAX);
	int pid = getpid();
	snprintf(pidstr, 10, "%d", pid);

	localTokenLen = strlen(pidstr);
	localTokenPrefix = strdup(pidstr);

        uniqueHostID = strdup(hostname);

	remoteTokenLen = strlen(hostname) + localTokenLen ;
	remoteTokenPrefix = malloc(remoteTokenLen + 1);
	sprintf(remoteTokenPrefix, "%s:%s", hostname, localTokenPrefix);

	return 0;
}

static int flushBuffer(hdR_topoToken topoToken){
	if(topoToken->buffer_pos == 0){
		return 0;
	}

	if( topoToken->log_fd == -1 ){
		// file already closed
		return -1;
	}

	ssize_t written = writeToFile(topoToken->log_fd, topoToken->buffer, topoToken->buffer_pos, topoToken->logfile);
	if (written < 0)
	{
		switch (errno)
		{
		case HD_ERR_TIMEOUT:
			hdr_info(topoToken,	"Timeout during writing of trace info,"
					" stop logging");
		case HD_ERR_MALLOC:
			hdr_info(topoToken,
					"Out of memory during writing of trace info,"
					" stop logging");
		case HD_ERR_WRITE_FILE:
			hdr_info(topoToken, "Write error during writing of trace info,"
					" stop logging");
		case HD_ERR_UNKNOWN:
			hdr_info(topoToken, "Unknown error during writing of trace info,"
					" stop logging");
		default:
			assert(written >= 0);
		}

		/* disable further tracing (does not touch errno) */
		// TODO

		/* do not change errno, just return error */
		return -1;
	}
	return 0;
}

static int writeToBuffer(hdR_topoToken topoToken, const char* format, ...) 
__attribute__ ((format (printf, 2, 3)));

static int writeToBuffer(hdR_topoToken topoToken, const char* format, ...)
{

	if( topoToken->log_fd == -1 ){
		// file already closed
		return -1;
	}

	char buffer[HD_TMP_BUF_SIZE];
	va_list argptr;
	size_t count;

	va_start(argptr, format);
	count = (size_t) vsnprintf(buffer, HD_TMP_BUF_SIZE, format, argptr);
	va_end( argptr );

	if (count >= HD_TMP_BUF_SIZE)
	{
		hdr_info(topoToken, "Temporary buffer too small for message.");
		return HD_ERR_BUFFER_OVERFLOW;
	}


	if(topoToken->buffer_pos + count > HD_LOG_BUF_SIZE){
		// write to file.
		if(flushBuffer(topoToken) != 0){
			return -1;
		}
		topoToken->buffer_pos = 0;
	}

	// append to buffer
	memcpy(topoToken->buffer + topoToken->buffer_pos, buffer, count);

	topoToken->buffer_pos += count;

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

	hdR_topoToken topoToken;
	hd_malloc(topoToken,1, -1);

	topoToken->logfile = generateFilename( topNode, topNode->length, NULL, ".rel" );

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

	// register topology in the topology map:
	g_hash_table_insert(topoMap, (gpointer) topNode, topoToken);

	// init time adjustment:
	gettimeofday(& topoToken->timeAdjustment, NULL);
	
	char hostname[HOST_NAME_MAX];
	gethostname(hostname, HOST_NAME_MAX);

	// print header to new file:
	writeToBuffer(topoToken, "<relation version=\"1\" topologyNumber=\"%d\" localToken=\"%s\" hostID=\"%s\" timeAdjustment=\"%" UINT64_FORMAT "\">\n",
			topoToken->topologyNumber, localTokenPrefix, uniqueHostID, (long long unsigned) topoToken->timeAdjustment.tv_sec);

	/*
	 * Alternative way to write local part.
	 * int i;
	for(i=1 ; i<= localDepth; i++){
		writeToBuffer(topoToken, "%s", hdT_getTopoPathLabel(topoToken->topoNode, i));
	}
	writeToBuffer(topoToken, "\">\n");
	*/

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


	// finalize contained data:
	writeToBuffer(token, "</relation>\n");
	flushBuffer(token);
	close(token->log_fd);

	token->log_fd = -1;

	// cleanup phase:
	free(token->logfile);
	free(token);

	const guint size = g_hash_table_size(topoMap);
	if (size == 0){
		g_hash_table_destroy(topoMap);

		topoMap = NULL;

		free(remoteTokenPrefix);
		free(localTokenPrefix);

		remoteTokenPrefix = NULL;
	}

	return 0;
}

char * hdR_getLocalToken(hdR_token token){
	if(token == NULL){
		return NULL;
	}

	char * buffer = malloc(25 * sizeof(char));
	snprintf(buffer, 25, "%s:%d:%"INT64_FORMAT, localTokenPrefix, token->topoToken->topologyNumber, token->id);
	return buffer;
}

char * hdR_getRemoteToken(hdR_token token){
	if(token == NULL){
		return NULL;
	}

	char * buffer = malloc(25 * sizeof(char) + remoteTokenLen);
	snprintf(buffer, 25 + remoteTokenLen, "%s:%d:%"UINT64_FORMAT, remoteTokenPrefix, token->topoToken->topologyNumber, token->id);
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
	hdR_token newToken = createToken(topoToken);

	writeToBuffer(topoToken, "<rel t=\"%" INT64_FORMAT "\"/>", newToken->id);

	return newToken;
}

hdR_token hdR_relateProcessLocalToken(hdR_topoToken newTopologyToken, hdR_token parentToken){
	if(parentToken == NULL){
		return NULL;
	}

	hdR_token newToken = createToken(newTopologyToken);

	writeToBuffer(newTopologyToken, "<rel t=\"%" INT64_FORMAT "\" p=\"%d:%"INT64_FORMAT"\"/>", newToken->id, parentToken->topoToken->topologyNumber, parentToken->id);

	return newToken;
}

hdR_token hdR_relateLocalToken(hdR_topoToken newTopologyToken, const char * strTopoToken){
	if(strTopoToken == NULL){
		return NULL;
	}

	hdR_token newToken = createToken(newTopologyToken);

	writeToBuffer(newTopologyToken, "<rel t=\"%"INT64_FORMAT"\" l=\"%s\"/>", newToken->id, strTopoToken);

	return newToken;
}

hdR_token hdR_relateRemoteToken(hdR_topoToken newTopologyToken, const char * remoteToken){
	if(remoteToken == NULL){
		return NULL;
	}

	hdR_token newToken = createToken(newTopologyToken);
	writeToBuffer(newTopologyToken, "<rel t=\"%"INT64_FORMAT"\" r=\"%s\"/>", newToken->id, remoteToken);
	return newToken;
}

int hdR_destroyRelation(hdR_token * token){
	if(token == NULL || *token == NULL){
		return -1;
	}

	// trace information
	writeToBuffer((*token)->topoToken, "<un t=\"%" INT64_FORMAT "\"/>", (*token)->id);

	// free token

	free(*token);
	*token = NULL;

	return 0;
}

inline static void writeAttributesAndTime(hdR_token token,  int attr_count, const char** attr_keys, const char **attr_values, int finalizeTag){
	struct timeval cur_time;
	gettimeofday(& cur_time, NULL);

	writeToBuffer(token->topoToken, " t=\"%"UINT64_FORMAT"\" time=\"%"UINT64_FORMAT".%.6u\"",
	token->id,
	(long long unsigned) (cur_time.tv_sec - token->topoToken->timeAdjustment.tv_sec), (unsigned) cur_time.tv_usec);

	int i;
	for(i=0; i < attr_count ; i++){
		writeToBuffer(token->topoToken, " %s=\"%s\"", attr_keys[i], attr_values[i]);
	}

	if (finalizeTag){
	 writeToBuffer(token->topoToken, "/>");
	}else{
	 writeToBuffer(token->topoToken, ">");
	}
}


static int hdR_starti(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values, const char * buffer){
	writeToBuffer(token->topoToken, "<s name=\"%s\"", name);
	writeAttributesAndTime(token, attr_count, attr_keys, attr_values, buffer == NULL);
	if(buffer != NULL){
		writeToBuffer(token->topoToken, "%s</s>", buffer);
	}
	return 0;
}

static int hdR_endi(hdR_token token, int attr_count, const char** attr_keys, const char **attr_values, const char * buffer){
	writeToBuffer(token->topoToken, "<e");
	writeAttributesAndTime(token, attr_count, attr_keys, attr_values, buffer == NULL);
	if(buffer != NULL){
		writeToBuffer(token->topoToken, "%s</e>", buffer);
	}
	return 0;
}

int hdR_startS(hdR_token token, const char * name){
	if(token == NULL || name == NULL){
		return -1;
	}

	return hdR_starti(token,name, 0, NULL, NULL, NULL);
}

int hdR_endS(hdR_token token){
	if(token == NULL){
		return -1;
	}

	return hdR_endi(token,  0, NULL, NULL, NULL);
}

int hdR_start(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values){
	if(token == NULL || name == NULL){
		return -1;
	}

	return hdR_starti(token,name, attr_count, attr_keys, attr_values, NULL);
}

int hdR_end(hdR_token token, int attr_count, const char** attr_keys, const char **attr_values){
	if(token == NULL){
		return -1;
	}

	return hdR_endi(token, attr_count, attr_keys, attr_values, NULL);
}

int hdR_startE(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values, const char * data_format, ...){
	if(token == NULL || name == NULL){
		return -1;
	}

	char buffer[HD_TMP_BUF_SIZE];

	va_list argptr;
	size_t count;

	va_start(argptr, data_format);
	count = (size_t) vsnprintf(buffer, HD_TMP_BUF_SIZE, data_format, argptr);
	va_end( argptr );

	if (count >= HD_TMP_BUF_SIZE)
	{
		hdr_info(token->topoToken, "Temporary buffer too small for message.");
		return HD_ERR_BUFFER_OVERFLOW;
	}

	return hdR_starti(token,name, attr_count, attr_keys, attr_values, buffer);
}

int hdR_endE(hdR_token token, int attr_count, const char** attr_keys, const char **attr_values, const char * data_format, ... ){
	if(token == NULL){
		return -1;
	}

	char buffer[HD_TMP_BUF_SIZE];

	va_list argptr;
	size_t count;

	va_start(argptr, data_format);
	count = (size_t) vsnprintf(buffer, HD_TMP_BUF_SIZE, data_format, argptr);
	va_end( argptr );

	if (count >= HD_TMP_BUF_SIZE)
	{
		hdr_info(token->topoToken, "Temporary buffer too small for message.");
		return HD_ERR_BUFFER_OVERFLOW;
	}

	return hdR_endi(token, attr_count, attr_keys, attr_values, buffer);
}



