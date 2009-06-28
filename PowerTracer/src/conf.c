/**
 * @file conf.c
 *
 * @date 28.06.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#include <stdlib.h>
//#include <stdio.h>   /* Standard input/output definitions */
//#include <unistd.h>  /* UNIX standard function definitions */
#include <string.h>
#include <strings.h>
#include <ctype.h>
#include <regex.h>
#include <errno.h>
#include <assert.h>

#include "conf.h"

#include "ptError.h"


/**
 * Structure for configuration file
 */
typedef struct configFile_s {
	/** filename */
	const char *filename;
	/** file object */
	FILE *file;
	/** current line number */
	int linenr;
} ConfigFileStruct;



static char** parsePath(char *output, int *plen) {

	// count number of tokens in output delimited by '_'
	*plen = 1;
	char *ptr = output;
	while ((ptr = index(ptr, '_')) != NULL)
		++*plen, ++ptr;

	// allocate space for pointers
	char ** path;
	path = malloc(*plen * sizeof(*path));

	// allocate space for strings
	int outlen = strlen(output);
	if (outlen <= 0)
		return NULL;
	path[0] = malloc(outlen+1 * sizeof(*(path[0])));

	// copy output string to allocated memory
	strcpy(path[0], output);

	// create path string array
	// by setting the pointers and replacing '_' with '\0'
	ptr = path[0];
	for (int i = 1; i < *plen; ++i) {
		ptr = index(ptr, '_');
		assert(*ptr == '_');
		*ptr = '\0';
		path[i] = ++ptr;
	}

	return path;
}

/**
 * Parse the strings used to configure traces on command line
 *
 * @param ntraces
 * @param strings
 * @param config
 * @return
 */
int parseTraceStrings(int ntraces, char * strings[], ConfigStruct * config) {

	int ret;

	// parse all traces
	for (int i = 0; i < ntraces; ++i) {

		TraceStruct *trace = malloc(sizeof(*trace));
		char *ptr = strings[i];

		// parse types of trace
#if 0
		trace->bin = 0;
		trace->ascii = 0;
#endif
		trace->hdstats = 0;
		while (*ptr != ':') {
			switch(*ptr) {
#if 0
			case 'b':
				trace->bin = 1;
				break;
			case 'a':
				trace->ascii = 1;
				break;
#endif
			case 's':
				trace->hdstats = 1;
				break;
			case '\0':
				puts("Usage()");
				exit(-1);
				break;
			default:
				puts("Unsupported trace type letter.");
				exit(-1);
				break;
			}
			ptr++;
		}

		assert(*ptr == ':');
		ptr++;

		// parse channel of trace
		char *tmp = index(ptr, ':');                  // find next colon
		if (tmp == NULL)
			puts("Usage()"), exit(-1);
		*tmp = '\0';                                  // write '\0' there
		ret = sscanf(ptr, "%d", &(trace->channel));   // scan channel number
		if (ret < 1)
			puts("Usage()"), exit(-1);
		ptr = tmp + 1;                                // set ptr to the beginning of the next token

		trace->output = ptr;
		if (ptr == '\0')
			puts("Usage()"), exit(-1);


		// activate default values
		trace->values.Utrms = 1;
		trace->values.Itrms = 1;
		trace->values.P = 1;

		addTraceToList(trace, &(config->traces));

	}

	return 0;
}

/**
 * Create all configured traces
 *
 * Sets:
 * - config->topology  for cleanup
 * - config->isize     for tracing
 * - for each trace:
 *   - trace->tnode  for cleanup
 *   - trace->group  for tracing
 *   - trace->actn   for tracing
 *   - trace->size   for tracing
 *
 * @param config  Configuration
 *
 * @return Error state
 */
int createTraces(ConfigStruct *config) {

	int ret;

	/*
	 * Test if there are any traces configured
	 */
	if (config->traces.last == NULL) {
		config->topology = NULL;
		return -1;
	}

	/*
	 * Create topology
	 */
	int tlen;
	char ** levels = parsePath(config->topo, &tlen);
	config->topology = hdT_createTopology((const char *) config->project,
			(const char **) levels, tlen);

	printf("Topology: %s", levels[0]);
	for (int i = 1; i < tlen; ++i) {
		printf(" - %s", levels[i]);
	}
	printf("\n");

	// free memory allocated by parsePath()
	free(levels[0]);
	free(levels);

	config->isize = 0;

	for FOR_TRACES(config) {

		char **path = NULL;
		int plen = 0;

		if (trace->hdstats) {
			// create path array from path string in trace->output
			path = parsePath(trace->output, &plen);

			// create topology node
			trace->tnode =
				hdT_createTopoNode(config->topology,(const char **) path, plen);
			if (trace->tnode == NULL) {
				switch(errno) {
				case HD_ERR_MALLOC:
					ERROR_ERRNO("Memory problem during topology node creation");
					break;
					strerror(6);
					return ERR_CUSTOM;
				default:
					assert(errno != HD_ERR_INVALID_ARGUMENT);
					ERROR_UNKNOWN;
					return ERR_UNKNOWN;
				}
				return -1;
			}

			// create statistics group
			trace->group =
				hdS_createGroup("Energy", trace->tnode, plen);
			if (trace->group == NULL) {
				switch(errno) {
				case HD_ERR_MALLOC:
					ERROR_ERRNO("Memory problem during statistics group creation");
					break;
				case HD_ERR_BUFFER_OVERFLOW:
					ERROR_ERRNO("Buffer overflow during statistics group creation");
					break;
				case HD_ERR_CREATE_FILE:
					ERROR_ERRNO("Problems with file creation for statistics group");
					break;
				default:
					assert(errno != HD_ERR_INVALID_ARGUMENT);
					ERROR_UNKNOWN;
				}
				return -1;
			}

			if (trace->values.Utrms) {
				ret = hdS_addValue(trace->group, "Utrms", FLOAT, "V", "Voltage");
				/* TODO error checking */
			}
			if (trace->values.Itrms) {
				ret = hdS_addValue(trace->group, "Itrms", FLOAT, "A", "Current");
				/* TODO error checking */
			}
			if (trace->values.P) {
				ret = hdS_addValue(trace->group, "P", FLOAT, "W", "Power");
				/* TODO error checking */
			}

			ret = hdS_commitGroup(trace->group);
			/* TODO error checking */
		}
		else {
			trace->tnode = NULL;
			trace->group = NULL;
		}

		// free memory allocated by parsePath()
		free(path[0]);
		free(path);

		char buffer[10];
		trace->actn = malloc(21 * sizeof(*(trace->actn)));
		trace->actn[0] = '\0';
		trace->size = 0;
		if (trace->values.Utrms) {
			snprintf(buffer, 10, "UTRMS%d?;", trace->channel);
			strcat(trace->actn, buffer);
			trace->size += (config->mode == MODE_BIN) ? 4 : 9;
		}
		if (trace->values.Itrms) {
			snprintf(buffer, 10, "ITRMS%d?;", trace->channel);
			strcat(trace->actn, buffer);
			trace->size += (config->mode == MODE_BIN) ? 4 : 9;
		}
		if (trace->values.P) {
			snprintf(buffer, 10, "P%d?;", trace->channel);
			strcat(trace->actn, buffer);
			trace->size += (config->mode == MODE_BIN) ? 4 : 9;
		}
		if (config->mode != MODE_BIN)
			trace->size += 2;

		// remove last ';'
		int slen = strlen(trace->actn);
		assert(slen > 0);
		trace->actn[slen-1] = '\0';

	    /* add response size of this trace to the one of the whole config */
		config->isize += trace->size;

		// print parsed trace config
		printf("%d: ", trace->num);
#if 0
		if (trace->bin)
			printf("BINARY, ");
		if (trace->ascii)
			printf("ASCII, ");
#endif
		if (trace->hdstats)
			printf("HDSTATS, ");

		printf("Channel: %d", trace->channel);

		if (trace->hdstats)
			printf(", Path: %s", hdT_getTopoPathString(trace->tnode));

#if 0
		if (trace->bin || trace->ascii) {
			printf(", Filename: %s", trace->output);
		}
#endif
		printf("\n");
	}

	assert(config->isize > 0);

	return 0;
}

/**
 * Read the next line in the file (until next '\n' or EOF)
 *
 * If a line (not NULL) is returned, freeing of the memory is up to the caller.
 *
 * @param file File to read
 *
 * @return Next line of file
 *  or NULL if EOF is still reached before the call or an error occurred.
 */
static char * readLineFromFile(ConfigFileStruct *cfile) {

	// create initial buffer
	size_t bsize = 20;
	char *buffer = malloc(bsize * sizeof(*buffer));

	// initialize counter
	size_t i = 0;

	// read characters until '\n' or EOF
	while (1) {
		// read next character
		char c = (char) fgetc(cfile->file);

		// if the first character is EOF, there is no more line or an error
		if (i == 0 && c == EOF) {
			free(buffer);
			return NULL;
		}

		// on '\n' of EOF the line is complete
		if (c == '\n' || c == EOF) {
			buffer[i] = '\0';
			break;
		}

		// copy character to buffer and increase counter
		buffer[i++] = c;

		// check if we have still space in buffer
		if (i >= bsize) {
			// reallocate larger buffer (double the old one)
			bsize *= 2;
			buffer = realloc(buffer, bsize);
		}
	}

	// resize buffer to needed size
	buffer = realloc(buffer, strlen(buffer)+1);

	// increase line number
	cfile->linenr++;

	return buffer;
}

/**
 * Get the next non-empty line from file.
 *
 * If a line (not NULL) is returned, freeing of the memory is up to the caller.
 *
 * @param file File
 *
 * @return Next non-empty line or NULL if no more valid line or error.
 */
static char * getNextNonemptyLine(ConfigFileStruct *cfile) {

	char *ptr;

	while(1) {
		// get next line
		char *line = readLineFromFile(cfile);

		// return NULL if there is no more line
		if (line == NULL) {
			return NULL;
		}

		// remove comment (first # until end of line)
		if ((ptr = index(line, '#')) != NULL) {
			*ptr = '\0';
		}

		// try next line if this one is empty
		if (*line == '\0') {
			free(line);
			continue;
		}

		// try next line if this one has only whitespaces
		for (ptr = line; *ptr != '\0'; ++ptr) {
			if (isspace(*ptr))
				continue;
			else
				break;
		}
		if (ptr == '\0' /* only space found */ ) {
			free(line);
			continue;
		}

		// we found a non-empty line
		return line;
	}
}

/**
 * Removes spaces at the beginning and end of the given string.
 *
 * Pay attention that this function changes the pointer *string as well as the
 *  string **string.
 *
 * @param string Pointer to the String to work on
 */
static void removeTrailingSpaces(char **string) {

	char *ptr;
	size_t slen = strlen(*string);

	// search from the end
	for (ptr = *string + slen - 1; isspace(*ptr); ptr--) {}
	// ptr points now to the last nonspace char
	*(ptr+1) = '\0';

	// search from the beginning
	for (ptr = *string; isspace(*ptr); ptr++) {}
	// ptr points now to the first nonspace char
	*string = ptr;
}


/**
 * Check validity of device in configuration.
 *
 * @param config Configuration
 *
 * @retval   0  Device is known and supported
 * @retval  -1  Device is unknown/unsupported
 * @retval  -2  No device is set
 */
static int checkDevice(ConfigStruct *config) {
	if (config->device == NULL || *(config->device) == '\0' ) {
		return -2;
	}

	if (strcmp(config->device, "LMG450") == 0) {
		return 0;
	}
	else {
		ERROR("Device \"%s\" is unknown and not supported.", config->device);
		return -1;
	}
}

/**
 * Split the port in the configuration into hostname and port component
 *  if needed.
 *
 * @param config Configuration
 *
 * @retval   1  Nothing to do
 * @retval   0  Port splitted
 * @retval  -2  No port set
 */
static int splitPort(ConfigStruct *config) {
	if (config->port == NULL || *(config->port) == '\0' ) {
		return -2;
	}

	char *ptr = index(config->port, ':');
	if (ptr == NULL) {
		return 1;
	}

	*ptr = '\0';
	config->host = config->port;
	config->port = ptr + 1;
	return 0;
}

/**
 * Check validity of cycle time in configuration.
 *
 * @param config Configuration
 *
 * @retval  0  Cycle time is valid
 * @retval  -1  Cycle time is invalid
 */
static int checkCycle(ConfigStruct *config) {
	if (config->cycle >= 0.05 && config->cycle <= 60) {
		return 0;
	}
	else {
		WARN("Invalid cycle time %f, has to be in [0.05,60.0] for %s.",
				config->cycle, config->device);
		return 1;
	}
}

/**
 * Check validity of channel for device in configuration.
 *
 * This is only working correctly if the device is not changed thereafter
 *
 * @param config Configuration
 *
 * @retval   0  Channel is valid
 * @retval  -1  Channel is invalid
 */
static int checkChannel(TraceStruct *trace, ConfigStruct *config) {
	if (trace->channel >= 1 && trace->channel <= 4) {
		return 0;
	}
	else {
		WARN("Invalid channel %f, has to be in [1,4] for %s.",
						trace->channel, config->device);
		return 1;
	}
}

/**
 * Check configuration for consistency
 *
 * @param filename
 * @param config
 * @return
 */
int checkConfig(ConfigStruct *config) {
	// TODO Add more consistency checks

	int result = 0;

	result &= checkCycle(config);

	for FOR_TRACES(config) {
		result &= checkChannel(trace, config);
	}

	if (result)
		ERROR("Consistency check of configuration FAILED. Check warnings above.");
	else
		puts("Consistency check of configuration PASSED.");

	return result;
}

/**
 * Fills the configuration with data read from file.
 *
 * @param filename Configuration file
 * @param config   Configuration
 *
 * @return Error state
 */
int readConfigFromFile(const char * filename, ConfigStruct *config) {

	// identifies the section we are currently in
	enum { NONE, GENERAL, TRACE } section = NONE;

	// true after reading one general section
	int general_done = 0;

	// counter for trace sections
	int ntraces = 0;

	// pointer to value (temp)
	char *value;

	// pointer to current trace
	TraceStruct *trace;

	// true if needed option is set in current trace
	struct {
		int type : 1;
		int output : 1;
		int channel : 1;
		int values : 1;
	} set;

#define CLEAN_SET set.type = set.output = set.channel = set.values = 0

	/*
	 * Initialize configfile structure
	 */
	ConfigFileStruct cfile;
	cfile.filename = filename;
	cfile.linenr = 0;
	cfile.file = fopen(filename, "r");


	/*
	 * Compile needed regex
	 */
	int ret;
#define REGCOM(name, regex) \
	regex_t name; \
	if (ret = regcomp(&name, regex, REG_EXTENDED | REG_ICASE | REG_NOSUB)) { \
		char errbuf[100]; \
		regerror(ret,&re_section,errbuf,100); \
		ERROR(errbuf); \
		return ERR_UNKNOWN; \
	}

#define RE_SPACE "[[:space:]]*"

	REGCOM(re_section, "^" RE_SPACE "\\[")
	REGCOM(re_sec_general, "^" RE_SPACE "\\[" RE_SPACE "general" RE_SPACE "]" RE_SPACE)
	REGCOM(re_sec_trace, "^" RE_SPACE "\\[" RE_SPACE "trace" RE_SPACE "]" RE_SPACE)
	REGCOM(re_key_device, "^" RE_SPACE "device" RE_SPACE "=")
	REGCOM(re_key_port, "^" RE_SPACE "port" RE_SPACE "=")
	REGCOM(re_key_cycle, "^" RE_SPACE "cycle" RE_SPACE "=")
	REGCOM(re_key_project, "^" RE_SPACE "project" RE_SPACE "=")
	REGCOM(re_key_topology, "^" RE_SPACE "topology" RE_SPACE "=")
	REGCOM(re_key_type, "^" RE_SPACE "type" RE_SPACE "=")
	REGCOM(re_key_node, "^" RE_SPACE "node" RE_SPACE "=")
	REGCOM(re_key_channel, "^" RE_SPACE "channel" RE_SPACE "=")
	REGCOM(re_key_values, "^" RE_SPACE "values" RE_SPACE "=")

#undef RE_SPACE
#undef REGCOM

#define REGEX_CLEANUP \
	do { \
	regfree(&re_section); \
	regfree(&re_sec_general); \
	regfree(&re_sec_trace); \
	regfree(&re_key_device); \
	regfree(&re_key_port); \
	regfree(&re_key_cycle); \
	regfree(&re_key_project); \
	regfree(&re_key_topology); \
	regfree(&re_key_type); \
	regfree(&re_key_node); \
	regfree(&re_key_channel); \
	regfree(&re_key_values); \
	} while (0);



#define CFILE_ERROR(msg, ...) \
	ERROR(msg " in %s:%d : \"%s\"", ## __VA_ARGS__, cfile.filename, cfile.linenr, line)

#define CFILE_WARN(msg, ...) \
	WARN(msg " in %s:%d", ## __VA_ARGS__, cfile.filename, cfile.linenr)

#define RETURN_SYNTAX_ERROR \
	do { \
	REGEX_CLEANUP; \
	fclose(cfile.file); \
	free(line); \
	return(ERR_SYNTAX); \
	} while (0)


	/*
	 * File reading and processing loop
	 */
	while(1) {
		// get next non-empty line
		int linenr;
		char *line = getNextNonemptyLine(&cfile);
		if (line == NULL) {
			// nothing more to read, finish processing and exit loop
			// if last section was [Trace] add the trace
			// TODO this is duplicated code, avoid this
			if (section == TRACE) {
				// check if all mandatory options are set
				char missing[30];
				*missing = '\0';
				if (!set.type)
					strcat(missing, "type, ");

				if (!set.output)
					strcat(missing, "node, ");

				if (!set.channel)
					strcat(missing, "channel, ");

				if (!set.values)
					strcat(missing, "values, ");

				int mlen = strlen(missing);
				if (mlen != 0) {
					missing[mlen-2] = '\0'; // removing ", "
					CFILE_ERROR("End of file reached after"
							" incomplete [Trace] section (%s missing)", missing);
					free(trace);
					RETURN_SYNTAX_ERROR;
				}
				else {
					addTraceToList(trace, &(config->traces));
				}
			}

			REGEX_CLEANUP;
			fclose(cfile.file);
			free(line);
			break;
		}


		/*
		 * try reading section start
		 */
		//   ^\s*\[
		if(regexec(&re_section, line, 0, NULL, 0) != REG_NOMATCH) {
			// handle old section
			if (section == TRACE) {
				// check if all mandatory options are set
				char missing[30];
				*missing = '\0';
				if (!set.type)
					strcat(missing, "type, ");

				if (!set.output)
					strcat(missing, "node, ");

				if (!set.channel)
					strcat(missing, "channel, ");

				if (!set.values)
					strcat(missing, "values, ");

				int mlen = strlen(missing);
				if (mlen != 0) {
					missing[mlen-2] = '\0'; // removing ", "
					CFILE_ERROR("Start of new section after"
							" incomplete [Trace] section (%s missing)", missing);
					free(trace);
					RETURN_SYNTAX_ERROR;
				}
				else {
					addTraceToList(trace, &(config->traces));
					trace = NULL; // for easier debugging
				}
			}

			//   \s*\[\s*[Gg][Ee][Nn][Ee][Rr][Aa][Ll]\s*\]\s*
			if (regexec(&re_sec_general, line, 0, NULL, 0) != REG_NOMATCH) {
				if (general_done) {
					CFILE_WARN("Additional [General] section");
				}
				section = GENERAL;
				general_done = 1;
			}

			//   \s*\[\s*[Tt][Rr][Aa][Cc][Ee]\s*]\s*
			else if (regexec(&re_sec_trace, line, 0, NULL, 0) != REG_NOMATCH) {
				section = TRACE;
				ntraces++;
				trace = malloc(sizeof(*trace));
				CLEAN_SET;
			}

			/* unknown section or syntax error */
			else {
				CFILE_ERROR("Unknown section");
				RETURN_SYNTAX_ERROR;
			}
		}

#define COPY_STRING_VALUE_TO(target) \
	do { \
	value = (index(line,'=') + 1); \
	removeTrailingSpaces(&value); \
	target = malloc((strlen(value) + 1) * sizeof(*(target))); \
	strcpy(target, value); \
	} while (0)

#define IS_KEY(key) regexec(&re_key_##key, line, 0, NULL, 0) != REG_NOMATCH

		/*
		 * Try reading key=value pairs for current section
		 */
		else if (section == GENERAL) {
			/* "[:space:]*device[:space:]*=" */
			if (IS_KEY(device)) {
				COPY_STRING_VALUE_TO(config->device);
				if (checkDevice(config) != 0)
					return -1;
			}

			/* "[:space:]*port[:space:]*=" */
			else if (IS_KEY(port)) {
				COPY_STRING_VALUE_TO(config->port);
				if (splitPort(config) < 0)
					CFILE_WARN("Problem parsing port value");
			}

			/* "[:space:]*cycle[:space:]*=" */
			else if (IS_KEY(cycle)) {
				if (sscanf(line, " cycle = %f", &(config->cycle)) != 1)
					CFILE_WARN("Problem parsing cycle value");
				// validity check is done later by checkConfig()
			}

			/* "[:space:]*project[:space:]*=" */
			else if (IS_KEY(project)) {
				COPY_STRING_VALUE_TO(config->project);
			}

			/* "[:space:]*topology[:space:]*=" */
			else if (IS_KEY(topology)) {
				COPY_STRING_VALUE_TO(config->topo);
			}

			else {
				CFILE_WARN("Ignoring unknown entry in [General] section");
			}

		}
		else if (section == TRACE) {
			/* "[:space:]*type[:space:]*=" (type=HDSTATS) */
			if (IS_KEY(type)) {
				char *type;
				COPY_STRING_VALUE_TO(type);
				removeTrailingSpaces(&type);
				if (strcmp(type, "HDSTATS") == 0) {
					trace->hdstats = 1;
					set.type = 1;
				}
				else
					CFILE_ERROR("Unknown type in [Trace] section" );
				free(type);
			}

			/* "[:space:]*topology[:space:]*=" (node=pvs_node06) */
			else if (IS_KEY(node)) {
				COPY_STRING_VALUE_TO(trace->output);
				set.output = 1;
			}

			/* "[:space:]*channel[:space:]*=" (channel=1) */
			else if (IS_KEY(channel)) {
				if (sscanf(line, " channel = %d", &(trace->channel)) != 1)
					CFILE_WARN("Problem parsing channel value");
				// validity check is done later by checkConfig()
				set.channel = 1;
			}

			/* "[:space:]*values[:space:]*=" (values=Utrms,Itrms,P) */
			if (IS_KEY(values)) {
				char *values;
				COPY_STRING_VALUE_TO(values);

				char *saveptr;
				for (char *tok = strtok_r(values, ",", &saveptr); tok != NULL;
						tok = strtok_r(NULL, ",", &saveptr)) {
					if (strcmp(tok, "Utrms") == 0) {
						trace->values.Utrms = 1;
						set.values = 1;
					}
					else if (strcmp(tok, "Itrms") == 0) {
						trace->values.Itrms = 1;
						set.values = 1;
					}
					else if (strcmp(tok, "P") == 0) {
						trace->values.P = 1;
						set.values = 1;
					}
					else
						WARN("Unknown value for trace ignored in %s:%d",
								cfile.filename, cfile.linenr);
				}
			}
		}

		else if (section == NONE) {
			CFILE_WARN("Uncommented entry outside of any section");
		}

		else {
			assert(!"Bug!");
		}
	}

	return 0;

#undef CLEAN_SET
#undef CFILE_ERROR
#undef CFILE_WARN
#undef RETURN_SYNTAX_ERROR
#undef COPY_STRING_VALUE_TO
}

int doConfig(ConfigStruct config, ConfigStruct override, char *configfile) {


}

/**
 * Close files and free memory
 */
void cleanupConfig(ConfigStruct *config) {
	for FOR_TRACES(config)
	{
		if (trace->hdstats) {
			hdS_finalize(trace->group);
			hdT_destroyTopoNode(trace->tnode);
		}
		if (trace->actn)
			free(trace->actn); // allocated in createTraces()
	}

	freeAllTraces(&(config->traces));
	if (config->topology)
		hdT_destroyTopology(config->topology);
}


/* vim: set sw=4 sts=4 et fdm=syntax: */
