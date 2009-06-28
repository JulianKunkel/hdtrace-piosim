#include <stdlib.h>
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/select.h>
#include <string.h>
#include <ctype.h>
#include <arpa/inet.h>
#include <assert.h>
#include <regex.h>

#include "ptError.h"
#include "serial.h"
#include "LMG.h"
#include "hdTopo.h"
#include "hdStats.h"
#include "hdError.h"

/*
 * Enumeration for operation mode of the measurement device
 */
enum opMode {
	MODE_ASCII,
	MODE_BIN
};

/*
 * Bitfield of supported values to measure
 */
typedef struct {
	int Utrms : 1;
	int Itrms : 1;
	int P : 1;
} ValueField;

/*
 * Structure for trace file description
 */
typedef struct trace_s {
	short num;
#if 0
	int bin : 1;
	int ascii : 1;
#endif
	int hdstats : 1;
	int channel;
	ValueField values;
	char *output;

    char *actn; /* action script part for this trace */
    int   size;    /* size of expected response in [byte] */

    hdTopoNode tnode;   /* topology node, reference needed for destruction */
    hdStatsGroup group; /* statistics group this belongs to */

    struct trace_s *prev;
} TraceStruct;

/**
 * List head (or actual foot) for list of traces
 */
typedef struct traceList_s {
	/** Last element of the linked list */
	TraceStruct *last;
} TraceListStruct;

/**
 * Structure for configuration file
 */
typedef struct {
	/** filename */
	const char *filename;
	/** file object */
	FILE *file;
	/** current line number */
	int linenr;
} ConfigFileStruct;

/**
 * Structure for configuration
 */
typedef struct {
	/** Type of measurement device used */
	char *device;
	/** Operation mode of the device */
    enum opMode mode;
    /** Host the measurement device is connected to */
	char *host;
	/** Port the measurement device is connected to */
	char *port;
	/** Time of one measurement cycle */
	float cycle;
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
} ConfigStruct;

static void addTraceToList(TraceStruct *trace, TraceListStruct *list) {
	trace->prev = list->last;
	trace->num = (list->last == NULL) ? 0 : list->last->num + 1;
	list->last = trace;
}

#define FOR_TRACES(config) \
	(TraceStruct *trace = (*(config)).traces.last; trace != NULL; trace = trace->prev)

static void freeAllTraces(TraceListStruct *list) {
	TraceStruct *this = list->last;
	while (this != NULL) {
		TraceStruct *next = this->prev;
		free(this);
		this = next;
	}
}

/*
 * Start time storage
 */
static struct timeval tv_start;

/*
 * Read data of one measuring iteration,
 *  split, prepare and write into the files
 */
static
int trace_iteration(int serial_fd, ConfigStruct *config)
{
    int ret;

    /* currently only binary operation mode is used */
    assert(config->mode == MODE_BIN);

    /* in binary mode each value has 4 bytes */
    assert(config->isize % 4 == 0);

    size_t isize = config->isize;

    char buffer[isize];  /* I/O buffer */
    size_t osize;        /* Output size */

    /*
     * Read message
     */
    ret = LMG_readBinaryMessage(serial_fd, buffer, isize);
    if (ret < 0)
    	LMG_READBINARYMESSAGE_ERROR_CHECK;
    if (ret != isize)
    {
        ERROR("Unexpected response size.");
        return(ERR_CUSTOM);
    }


    /*
     * Write response part for each trace to the correct file
     */
    char *bufptr = buffer;
    for FOR_TRACES(config)
    {
       	/* use int pointer since order_bytes32ip is assumed to be faster */
    	uint32_t *intptr = (uint32_t *) bufptr;
    	for (int j = 0; j < trace->size / 4; ++j)
    		order_bytes32ip(intptr+j);

    	osize = trace->size;

    	ret = hdS_writeEntry(trace->group, bufptr, osize);
    	if (ret < 0) {
    		// all these errors are programmer's faults, not user's faults
    		assert(errno != HD_ERR_INVALID_ARGUMENT);
			assert(errno != HD_ERR_TRACE_DISABLED);
			assert(errno != HDS_ERR_GROUP_COMMIT_STATE);
			assert(errno != HDS_ERR_UNEXPECTED_ARGVALUE);
			assert(errno != HDS_ERR_ENTRY_STATE);
			ERROR_UNKNOWN;
			return ERR_UNKNOWN;
    	}

        /* set bufptr to the start of the next trace's part */
        bufptr+=osize;
    }

    return(OK);
}

static
int trace_data(
        int serial_fd,      /* file descriptor of serial port */
        ConfigStruct *config
        )
{
    int ret;

    u_int8_t done;

    fd_set input;
    struct timeval timeout;
    int n;

    int stdin_fd = fileno(stdin);

    /*
     * check value of cycle time
     */
    if(config->cycle < 0.05 || config->cycle > 60)
    {
        ERROR("trace_data(): Cycle time out of range (0.05..60).");
    }

    /*
     * Define commands to be executed
     */
    size_t actn_len = 5; /* bytes for "ACTN;" */

	for FOR_TRACES(config)
        actn_len += strlen(trace->actn) + 1; /* 1 byte for the separating ';' */

    char actn[actn_len];
    actn[0] = '\0';
    strcat(actn, "ACTN");
    for FOR_TRACES(config) {
        strcat(actn, ";");
        strcat(actn, trace->actn);
    }

    /*
     * Send action script to LMG
     */
    ret = serial_sendMessage(serial_fd, actn);
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Set cycle time
     */
    char buffer[32];
    sprintf(buffer, "CYCL %f", config->cycle);
    ret = serial_sendMessage(serial_fd, buffer);
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Set output format
     */
    switch(config->mode)
    {
        case MODE_BIN:
            ret = serial_sendMessage(serial_fd, "FRMT PACKED");
            SERIAL_SENDMESSAGE_RETURN_CHECK;
            break;
        case MODE_ASCII:
            ret = serial_sendMessage(serial_fd, "FRMT ASCII");
            SERIAL_SENDMESSAGE_RETURN_CHECK;
            break;
    }
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Start continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT ON");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Enable tracing groups
     */
    for FOR_TRACES(config) {
    	hdS_enableGroup(trace->group);
    }

    /*
     * Tracing iterations loop
     */
    done = 0;
    while(!done)
    {
        /* Initialize the input set */
        FD_ZERO(&input);
        FD_SET(serial_fd, &input);
        FD_SET(stdin_fd, &input);

        /* Initialize the timeout structure */
        timeout.tv_sec  = 2;
        timeout.tv_usec = 0;

        /* Do the select */
        n = select(serial_fd+1, &input, NULL, NULL, &timeout);
        if (n < 0)
        {
          ERROR_ERRNO("select()");
          return(ERR_ERRNO);
        }
        else if (n == 0)
        {
            /* timeout */
            done = 1;
            break;
        }

        if (FD_ISSET(stdin_fd, &input))
        {
            switch(getchar())
            {
                case 'q':
                case 'Q':
                    /* end continous mode */
                    ret = serial_sendMessage(serial_fd, "CONT OFF");
                    SERIAL_SENDMESSAGE_RETURN_CHECK;
                    /* now the input buffer will run empty and select will
                     * run into its timeout and so end the loop */
                    break;
            }
        }

        if (FD_ISSET(serial_fd, &input))
        {
            ret = trace_iteration(serial_fd, config);
            switch(ret)
            {
                case OK:
                    break;
                case ERR_ERRNO: /* read(), readBytes() */ \
                case ERR_BSIZE: \
                case ERR_NO_MSG: \
                case ERR_MSG_FORMAT: \
                case ERR_UNKNOWN: \
                case ERR_CUSTOM: \
                    return(ret); \
                default: \
                    ERROR_UNKNOWN; \
                    return(ERR_UNKNOWN); \
            }
        }
    }

    /*
     * Stop continuous mode
     */
    ret = serial_sendMessage(serial_fd, "CONT OFF");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Disable tracing groups
     */
    for FOR_TRACES(config) {
    	hdS_disableGroup(trace->group);
    }

    return (OK);
}

static
int test_binmode(int serial_fd)
{
    int ret;

    /*
     * Send command for ASCII mode
     */
    ret = serial_sendMessage(serial_fd, "INIM;UTRMS?;ITRMS?;P?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /* read data */

    char buffer[255];

    ret = LMG_readTextMessage(serial_fd, buffer, sizeof(buffer));
    if (ret < 0)
        LMG_READTEXTMESSAGE_ERROR_CHECK;

    /* print message */
    if(puts(buffer) == EOF)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }


    /*
     * Set output format to binary mode
     */
    ret = serial_sendMessage(serial_fd, "FRMT PACKED");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /*
     * Send command for binary mode
     */
    ret = serial_sendMessage(serial_fd, "UTRMS?;ITRMS?;P?");
    SERIAL_SENDMESSAGE_RETURN_CHECK;

    /* read data */
    struct {
        float utrms;
        float itrms;
        float p;
    } op;

    ret = LMG_readBinaryMessage(serial_fd, &op, sizeof(op));
    if (ret < 0)
    {
        LMG_READBINARYMESSAGE_ERROR_CHECK;
    }

    if (printf("%.4E;%.4E;%.4E\n", op.utrms, op.itrms, op.p ) < 0)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }

    return (OK);
}

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

static int parseTraceStrings(int ntraces, char * strings[], ConfigStruct * config) {

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
static int createTraces(ConfigStruct *config) {

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
				hdS_createGroup("Energy", trace->tnode, 1);
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
	config->host = ptr + 1;
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
static int checkConfig(ConfigStruct *config) {
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
 *
 * @param filename
 * @param config
 * @return
 */
static int readConfigFromFile(const char * filename, ConfigStruct *config) {

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

/**
 * Close files and free memory
 */
static void cleanup(ConfigStruct *config) {
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


/**
 * Main function
 *
 * Takes following commandline arguments:
 *
 * [-P PROJECT] [-t TOPOLOGY] [-p PORT] TRACE [TRACE [TRACE [...]]]]
 *
 * PROJECT = Name of the project (default: MyProject)
 * TOPOLOGY = level1_level2_level3 (default: Host_Process_Thread)
 * TRACE = TYPES:CHANNEL:OUTPUT
 *
 * TYPES = String containing 'a' (ASCII), 'b' (BINARY), 's' (HDSTATS)
 * CHANNEL = Number of the input channel
 * OUTPUT = If TYPES contains 'b' or 'a', this is simply the output file
 *  without the extension. For 'a' .txt is added and '.bin' for 'b'.
 *  If TYPES contains 's', this describes a topology node and must be in the
 *  form "path1_path2" (until now only 's' is implemented)
 *
 * PORT = "/dev/ttyUSB0"
 *
 * @return error code
 */
int main(int argc, char **argv)
{
	int ret;

	/*
	 * Initialize configuration
	 */
	ConfigStruct config;
	config.topology = NULL;

	/*
	 * Set defaults
	 */
	config.device = "LMG450";
    config.mode = MODE_BIN;
	config.host = NULL;
	config.port = "/dev/ttyUSB0";
	config.cycle = 0.1;
	config.project = "MyProject";
	config.topo = "Host_Process_Thread";
	config.traces.last = NULL;


	/*
	 * Read commandline parameters
	 */
	char *configfile = NULL;
	char *project = NULL;
	char *topo = NULL;
	char *port = NULL;
	char o;
	while ((o = getopt(argc, argv, "c:P:t:p:")) >= 0) {
		switch(o) {
		case 'c':
			configfile = optarg;
			break;
		case 'P':
			project = optarg;
			break;
		case 't':
			topo = optarg;
			break;
		case 'p':
			port = optarg;
			break;
		default:
			break;
		}
	}

	/*
	 * Read configuration file if given
	 */
	if (configfile != NULL)
		readConfigFromFile(configfile, &config);

	/*
	 * Override configuration file options with commandline options
	 */
	if (port != NULL)
		config.port = port;
	if (project != NULL)
		config.project = project;
	if (topo != NULL)
		config.topo = topo;

	/*
	 * Add traces from commandline
	 */

	int ntraces = argc - optind;
	ret = parseTraceStrings(ntraces, argv+optind, &config);

	/*
	 * Do consistency check of the final configuration
	 */
	ret = checkConfig(&config);
	if (ret != 0) {
		cleanup(&config);
		exit(-1);
	}


	/*
	 * Print configuration
	 */
	printf("Device: %s\n", config.device);
	printf("Host: %s\n", config.host == NULL ? "NULL" : config.host);
	printf("Port: %s\n", config.port);
	printf("Project: %s\n", config.project);


	/*
	 * Create the traces found in configuration
	 */
	ret = createTraces(&config);
	if (ret == -1) {
		fputs("No traces configured\n", stderr);
		cleanup(&config);
		exit(-1);
	}

	int serial_fd;
    char buffer[255];  /* Input buffer */

    /*
     * Take start time
     */
    ret = gettimeofday(&tv_start, NULL);

    /*
     * Open serial port
     */
    serial_fd = serial_openPort(config.port);
    if(serial_fd < 0)
        return(serial_fd);

    /*
     * Setup serial port
     *
     * Configuration on device:
     * ComA: Custom
     * ComA 8N1
     * Baudrate: 57600
     * EOS: <lf>
     * Echo: Off
     * Protocol: RTS/CTS
     */
    ret = serial_setupPort(serial_fd, 57600);
    SERIAL_SETUPPORT_RETURN_CHECK;

    /*
     * Setup LMG
     */
    ret = LMG_setup(serial_fd);
    LMG_SETUP_RETURN_CHECK;

    /*
     * Get and print identity string from LMG
     */
    ret = LMG_getIdentity(serial_fd, buffer, sizeof(buffer));
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Errno error while getting identity.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while getting identity.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while getting identity.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect message format while getting identity.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while getting identity.\n", stderr);
            break;
        default:
            fputs("Unknown error while getting identity.\n", stderr);
    }

    ret = puts(buffer);
    if(ret == EOF)
    {
        ERROR_UNKNOWN;
        return(ERR_UNKNOWN);
    }

    ret = test_binmode(serial_fd);
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Other error while testing binary mode.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while testing binary mode.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while testing binary mode.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect Message format while testing binary mode.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while testing binary mode.\n", stderr);
            break;
        default:
            fputs("Unknown error while testing binary mode.\n", stderr);
    }


    puts("Start tracing!");

    ret = trace_data(serial_fd, &config);
    printf("Returns %d\n", ret);
    /* TODO: error handling */

    puts("End tracing!");


    cleanup(&config);

    /*
     * Get and print all errors from LMG
     */
    ret = LMG_getAllErrors(serial_fd, buffer, sizeof(buffer));
    switch(ret)
    {
        case OK:
            break;
        case ERR_ERRNO:
            fputs("Errno error while getting all errors.\n", stderr);
            break;
        case ERR_WRITE:
            fputs("Write error while getting all errors.\n", stderr);
            break;
        case ERR_NO_MSG:
            fputs("No message ready while getting all errors.\n", stderr);
            break;
        case ERR_MSG_FORMAT:
            fputs("Incorrect message format while getting all errors.\n", stderr);
            break;
        case ERR_BSIZE:
            fputs("Buffersize to low while getting all errors.\n", stderr);
            break;
        default:
            fputs("Unknown error while getting all errors.\n", stderr);
    }

    ret = puts(buffer);
    if (ret == EOF)
    {
        ERROR_UNKNOWN;
    }

    /*
     * Close LMG connection
     */
    ret = LMG_close(serial_fd);
    LMG_CLOSE_RETURN_CHECK;

    /*
     * Close serial port
     */
    ret = serial_closePort(serial_fd);
    SERIAL_CLOSEPORT_RETURN_CHECK;


}

/* vim: set sw=4 sts=4 et fdm=syntax: */
