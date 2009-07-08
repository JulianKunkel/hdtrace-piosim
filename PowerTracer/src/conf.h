/**
 * @file conf.h
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef CONF_H_
#define CONF_H_

#include "hdTopo.h"
#include "trace.h"

/**
 * Enumeration for operation mode of the measurement device
 */
enum opMode {
	MODE_ASCII,
	MODE_BIN
};


/**
 * Structure for configuration
 */
typedef struct config_s {
	/** Type of measurement device used */
	char *device;
	/** Operation mode of the device */
    enum opMode mode;
    /** Host the measurement device is connected to */
	char *host;
	/** Port the measurement device is connected to */
	char *port;
	/** Time of one measurement cycle */
	int cycle;
	/** Name of the project */
	char *project;
	/** String representation of the topology */
	char *topo;
	/** Topology used for hdStats traces */
	hdTopology topology;
	/** List of traces */
	TraceListStruct traces;
	/** Response size in bytes expected from the device in each iteration */
	size_t isize;
	/** allocated components */
	struct {
		int device : 1;
		int host : 1;
		int port : 1;
		int project : 1;
		int topo : 1;
	} allocated;
} ConfigStruct;



/**
 * Fills the configuration with data read from file.
 */
int readConfigFromFile(const char * filename, ConfigStruct *config);

/**
 * Parse the strings used to configure traces on command line
 */
int parseTraceStrings(int ntraces, char * strings[], ConfigStruct * config);

/**
 * Create all configured traces
 */
int createTraces(ConfigStruct *config);

/**
 * Check configuration for consistency
 */
int checkConfig(ConfigStruct *config);

/**
 * Close files and free memory
 */
void cleanupConfig(ConfigStruct *config);


#endif /* CONF_H_ */
