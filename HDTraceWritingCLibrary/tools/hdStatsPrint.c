/**
 * @file hdStatsPrint.c
 *
 * @date 24.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */
#pragma alloca

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>
#include <malloc.h>

/* for byte order functions only */
#include "hdStats.h"

/**
 * Printf format specifier for printing int64_t.
 * On 64bit it is long int, on 32bit long long int.
 */
#if __WORDSIZE == 64
# define INT64_FORMAT "ld"
# define UINT64_FORMAT "lu"
#else
# define INT64_FORMAT "lld"
# define UINT64_FORMAT "llu"
#endif


static void printUsage(void);

static void readTimestamp(FILE *file, void *value);
static void read32bitValue(FILE *file, void *value);
static void read64bitValue(FILE *file, void *value);


int main(int argc, char **argv)
{

	char *filename = argv[1];
	char **value_formats = argv+2;
	size_t value_formats_length = (size_t) argc - 2;

	int ret;
	size_t uret;

	FILE *file;

	size_t header_length;
	char *header;

	char *names[value_formats_length];
	hdStatsValueType types[value_formats_length];

	if (filename == NULL || filename == '\0')
	{
		printUsage();
		exit(-1);
	}

	char *type;

	/* read value names and types from command line */
	for (int i = 0; i < (int) value_formats_length; ++i)
	{
		/* find position of '=' */
		char *eqidx = index(value_formats[i], '=');
		if (eqidx == NULL)
			fprintf(stderr, "Could not parse <formatValue%d> (\"%s\"):"
					" String does not contain '='\n", i, value_formats[i]);

		/* set current name pointer and terminate string */
		names[i] = value_formats[i];
		*eqidx = '\0';
		if (*(names[i]) == '\0')
			fprintf(stderr, "Could not parse <formatValue%d> (\"%s\"):"
					" Name empty\n", i, value_formats[i]);

		/* set type string pointer for current type */
		type = eqidx + 1;
		if (*type == '\0')
			fprintf(stderr, "Could not parse <formatValue%d> (\"%s\"):"
					" Type empty\n", i, value_formats[i]);

		/* set current type */
		if (strcmp(type, INT32_STRING) == 0)
			types[i] = INT32;
		else if (strcmp(type, INT64_STRING) == 0)
			types[i] = INT64;
		else if (strcmp(type, UINT64_STRING) == 0)
			types[i] = UINT64;
		else if (strcmp(type, FLOAT_STRING) == 0)
			types[i] = FLOAT;
		else if (strcmp(type, DOUBLE_STRING) == 0)
			types[i] = DOUBLE;
		else
			fprintf(stderr, "Unknown type \"%s\" in <formatValue%d>"
					" (\"%s\")\n", type, i,	value_formats[i]);
	}

	/* open file */
	file = fopen(filename, "rb");
	if (file == NULL)
		fprintf(stderr, "Unable to open file \"%s\":%s", filename, strerror(
				errno));

	/* read header length from file */
	ret = fscanf(file, "%zu\n", &header_length);
	if (ret != 1 || ret == EOF)
		fprintf(stderr, "Could not read header from file: %s\n", strerror(errno));

	/* read header from file */
	header = calloc(header_length, sizeof(*header));
	if (header == NULL)
		fprintf(
				stderr,
				"Could not allocate memory for header (header_length=%zu): %s\n",
				header_length, strerror(errno));
	clearerr(file);
	uret = fread(header, sizeof(*header), header_length, file);
	if (uret < header_length)
	{
		if (ferror(file))
		{
			fprintf(stderr,
					"Could not read header from file (header_length=%zu): %s\n",
					header_length, strerror(errno));
			exit(-1);
		}
		else if (feof(file))
		{
			fprintf(stderr, "End of file reached while reading header"
					"(header_length=%zu)", header_length);
			exit(-1);
		}
		else
			assert(!"Short read during fread call"
				" but no error nor EOF reported");
	}

	/* allocate memory for maximum value size of 64bit */
	void *value = malloc(8);

	/* read start timestamp */
	readTimestamp(file, value);

	/* convert start timestamp */
	order_bytes32ip((int32_t *) value);
	order_bytes32ip(((int32_t *) value) + 1);

	/* print start timestamp */
	printf("Start time: %010d,%09d\n", *((int32_t *) value),
			*(((int32_t *) value) + 1));

	while (1)
	{
		/* read timestamp */
		readTimestamp(file, value);

		/* convert timestamp */
		order_bytes32ip((int32_t *) value);
		order_bytes32ip(((int32_t *) value) + 1);

		/* print timestamp */
		printf("%010d.%09d : ", *((int32_t *) value),
				*(((int32_t *) value) + 1));

		/* read values */
		for (int i = 0; i < (int) value_formats_length; ++i)
		{
			/* read */
			switch (types[i])
			{
			case INT32:
			case FLOAT:
				/* read 32 bit */
				read32bitValue(file, value);
				break;
			case INT64:
			case UINT64:
			case DOUBLE:
				/* read 64 bit */
				read64bitValue(file, value);
				break;
			case STRING:
				assert(!"Type switch found STRING");
			default:
				assert(!"Type switch default");
			}

			/* convert */
			switch (types[i])
			{
			case INT32:
				order_bytes32ip((int32_t *) value);
				break;
			case INT64:
				order_bytes64ip((int64_t *) value);
				break;
			case UINT64:
				order_bytes64ip((uint64_t *) value);
				break;
			case FLOAT:
				order_bytes32fp((float *) value);
				break;
			case DOUBLE:
				order_bytes64fp((double *) value);
				break;
			case STRING:
				assert(!"Type switch found STRING");
			default:
				assert(!"Type switch default");
			}

			/* print name */
			printf("%s=", names[i]);
			switch (types[i])
			{
			case INT32:
				printf("%d", *((int32_t *) value));
				break;
			case INT64:
				printf("%" INT64_FORMAT, *((int64_t *) value));
				break;
			case UINT64:
				printf("%" UINT64_FORMAT, *((uint64_t *) value));
				break;
			case FLOAT:
				printf("%f", *((float *) value));
				break;
			case DOUBLE:
				printf("%f", *((double *) value));
				break;
			case STRING:
				assert(!"Type switch found STRING");
			default:
				assert(!"Type switch default");
			}

			/* print correct delimiter */
			if (i != (int) value_formats_length - 1)
				printf(", ");
			else
				printf("\n");
		}
	}
}

static void printUsage(void)
{
	printf("Usage: hdStatsPrint <hdStatsFile> <formatValue1>"
		" [<formatValue2> [...]]\n");
	printf("\tformatValue: ValueName=ValueType\n");
}

static void readTimestamp(FILE *file, void *value)
{
	clearerr(file);
	size_t ret = fread(value, sizeof(int32_t), 2, file);
	if (ret < 2)
	{
		if (ferror(file))
		{
			fprintf(stderr, "An error occurred while reading timestamp:"
				" %s\n", strerror(errno));
			exit(-1);
		}
		else if (feof(file))
			if (ret != 0)
			{
				fprintf(stderr, "End of file reached while reading timestamp");
				exit(-1);
			}
			else
				/* reached normal end of file */
				exit(0);
		else
			assert(!"Short read during fread call"
				" but no error nor EOF reported");
	}
}

static void read32bitValue(FILE *file, void *value)
{
	/* read 32 bit */
	clearerr(file);
	size_t ret = fread(value, sizeof(int32_t), 1, file);
	if (ret < 1)
	{
		if (ferror(file))
		{
			fprintf(stderr, "An error occurred while reading next value:"
					" %s\n", strerror(errno));
			exit(-1);
		}
		else if (feof(file))
		{
			fprintf(stderr, "End of file reached while reading next value");
			exit(-1);
		}
		else
			assert(!"Short read during fread call"
				" but no error nor EOF reported");
	}
}

static void read64bitValue(FILE *file, void *value)
{
	clearerr(file);
	size_t ret = fread(value, sizeof(int64_t), 1, file);
	if (ret < 1)
	{
		if (ferror(file))
		{
			fprintf(stderr, "An error occurred while reading next value:"
					" %s\n", strerror(errno));
			exit(-1);
		}
		else if (feof(file))
		{
			fprintf(stderr, "End of file reached while reading next value");
			exit(-1);
		}
		else
			assert(!"Short read during fread call"
				" but no error nor EOF reported");
	}
}
