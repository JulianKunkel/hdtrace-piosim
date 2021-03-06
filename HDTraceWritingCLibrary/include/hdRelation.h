/**
 * hdRelation.h
 *
 * A relation is similar to a normal tracefile.
 * However, in a normal tracefile nested operations must be covered by the parent operation.
 * Also, concurrent operations are not permitted in a normal tracefile.
 *
 * In contrast a relation allows each operation to start a set of parallel operations.
 * These operations can overlap and can be run even after the initial operation finished (for instance to track write-behind operations).
 *
 * A relation is accessed via a token.
 *
 * Multiple relation files can be related as well, thus allowing to track source and triggered activities across servers.
 *
 * Most functions are thread-safe, however only on a topology token level i.e. if you use multiple threads on a single topology
 * then you must ensure that only one thread works on a token of a specific toplogy at a time. => use mutex
 *
 *
 * @date 05.06.2009
 * @author julian
 * @version \$Id$
 */

#ifndef HDRELATION_H_
#define HDRELATION_H_

#include <stdint.h>

#include "hdTopo.h"

typedef struct _hdR_token *      hdR_token;
typedef struct _hdRelationTopo * hdR_topoToken;

/**
* Initialize relation handling on topology, can be used multiple times on different topologies.
* Note: this function is NOT thread-safe
*/
int hdR_initTopology(hdTopoNode *topNode, hdR_topoToken * outTopoToken);

/**
 * finalize & close the relation trace for a given topology
 */
int hdR_finalize(hdR_topoToken * token_p);

/**
 * Shutdown the API and free global ressources.
 */
int hdR_closeAPI(void);

hdR_token hdR_createTopLevelRelation(hdR_topoToken topoToken);

/**
 * Relate a token with a new token from the same topology.
 */
hdR_token hdR_relateTopologyInternalToken(hdR_token parentToken);

/**
 * Relate a token from a different topology but yet in the same process (if the topToken is known)
 */
hdR_token hdR_relateProcessLocalToken(hdR_topoToken newTopologyToken, hdR_token parentToken);

/**
 * Relate a token from a different topology (if the topToken is only known as a string)
 * This is likely to be exchanged between different processes
 */
hdR_token hdR_relateLocalToken(hdR_topoToken newTopologyToken, const char * strLocalTopoToken);

/**
* Relate a token which was send by a remote machine
*/
hdR_token hdR_relateRemoteToken(hdR_topoToken topoToken, const char * remoteToken);

/**
 * Remove a registered relation
 */
int hdR_destroyRelation(hdR_token * token);

/**
* Return a token as a string which can be passed to a different process on the same machine.
* The caller is responsible to free the string.
*/
char * hdR_getLocalToken(hdR_token token);

/**
* Return a token as a string which can be passed to a different process (on a potential different machine) and used
* by a relateRemoteToken
*/
char * hdR_getRemoteToken(hdR_token token);

/**
 * Start a state without additional data
 */
int hdR_startS(hdR_token token, const char * name);

/**
 * Start a state with additional attributes
 * Note: the attributes are merged with the ones specified in the end state call.
 * Therefore use disjoint names to the end state, otherwise the attributes are overwritten
 * by the ones given in the end call.
 * If possible avoid to add information to the start call.
 */
int hdR_start(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values);

/**
 * Start a state, extended version to add arbitrary content.
 * Note: the attributes is merged with the one specified in the end state call.
 * The XML data is just added to the one given in the end call.
 * Therefore use disjoint names to the end state, otherwise the attributes are overwritten
 * by the ones given in the end call.
 * If possible avoid to add information to the start call.
 */
int hdR_startE(hdR_token token, const char * name, int attr_count, const char** attr_keys, const char **attr_values, const char * data_format, ... ) __attribute__ ((format (printf, 6, 7)));

/**
 * End the previously started state
 */
int hdR_endS(hdR_token token);

/**
 * End the previously started state
 */
int hdR_end(hdR_token token, int attr_count, const char** attr_keys, const char **attr_values);

int hdR_endE(hdR_token token, int attr_count, const char** attr_keys, const char **attr_values, const char * data_format, ...) __attribute__ ((format (printf, 5, 6)));

#endif /* HDRELATION_H_ */
