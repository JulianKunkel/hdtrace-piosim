/*
 * topo.c
 *
 *  Created on: Mar 2, 2010
 *      Author: Timo Minartz
 */

#include "topo.h"

#include <stdio.h>

#ifndef HAVE_HDTWLIB

/** None of these method should be called **/

hdTopology * hdT_createTopology(const char *project, const char **levels,
		int nlevels) {
	printf("hdT_createTopology\n");
	return NULL;
}

int hdT_getTopoDepth(const hdTopology *topology) {
	printf("hdT_getTopoDepth\n");
	return 0;
}

int hdT_destroyTopology(hdTopology *topology) {
	printf("hdT_destroyTopology\n");
	return 0;
}

hdTopoNode * hdT_createTopoNode(hdTopology *topology, const char **path,
		int length) {
	printf("hdT_createTopoNode\n");
	return NULL;
}

int hdT_getTopoNodeLevel(const hdTopoNode *node) {
	printf("hdT_getTopoNodeLevel\n");
	return 0;
}

const char * hdT_getTopoPathString(const hdTopoNode *node) {
	printf("hdT_getTopoPathString\n");
	return "";
}

const char * hdT_getTopoPathLabel(const hdTopoNode *node, int level) {
	printf("hdT_getTopoPathLabel\n");
	return "";
}

int hdT_destroyTopoNode(hdTopoNode *node) {
	printf("hdT_destroyTopoNode\n");
	return 0;
}

hdStatsGroup * hdS_createGroup(const char *groupName, /* Name of the new statistics group */
hdTopoNode *topoNode, /* Topology node to use */
int topoLevel /* Topology level the group shell belong to */
) {
	printf("hdS_createGroup\n");
	return NULL;
}

int hdS_addValue(hdStatsGroup *group, /* Statistics Group */
const char* name, /* Name of the new value */
hdStatsValueType type, /* Type of the new value */
const char* unit, /* Unit string of the new value */
const char* grouping /* Grouping string for the new value */
) {
	printf("hdS_addValue\n");
	return 0;
}

int hdS_commitGroup(hdStatsGroup *group /* Statistics Group */
) {
	printf("hdS_commitGroup\n");
	return 0;
}

int hdS_enableGroup(hdStatsGroup *group) {
	printf("hdS_enableGroup\n");
	return 0;
}

int hdS_disableGroup(hdStatsGroup *group) {
	printf("hdS_disableGroup\n");
	return 0;
}

int hdS_isEnabled(hdStatsGroup *group) {
	printf("hdS_isEnabled\n");
	return 0;
}

int hdS_writeEntry(hdStatsGroup *group, /* Statistics Group */
void * entry, /* Pointer to the entry to write */
size_t entryLength /* Length of the entry to write */
) {
	printf("hdS_writeEntry\n");
	return 0;
}

int hdS_writeInt32Value(hdStatsGroup *group, /* Statistics Group */
int32_t value /* INT32 value to write */
) {
	printf("hdS_writeInt32Value\n");
	return 0;
}

int hdS_writeInt64Value(hdStatsGroup *group, /* Statistics Group */
int64_t value /* INT64 value to write */
) {
	printf("hdS_writeInt64Value\n");
	return 0;
}

int hdS_writeFloatValue(hdStatsGroup *group, /* Statistics Group */
float value /* FLOAT value to write */
) {
	printf("hdS_writeFloatValue\n");
	return 0;
}

int hdS_writeDoubleValue(hdStatsGroup *group, double value) {
	printf("hdS_writeDoubleValue\n");
	return 0;
}

int hdS_writeString(hdStatsGroup *group, const char * str) {
	printf("hdS_writeString\n");
	return 0;
}

int hdS_finalize(hdStatsGroup *group) {
	printf("hdS_finalize\n");
	return 0;
}

char* hdT_strerror(int errno) {
	printf("hdS_strerror\n");
	return "";
}

#endif
