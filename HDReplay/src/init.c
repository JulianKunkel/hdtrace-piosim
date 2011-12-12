#include <glib.h>
#include "init.h"


char numbuf[10];
int i;
int commsLen;

void init
	(GSList** traceFile, GSList** comms, GSList ** fileList, GSList** dataTypes,
	 int rank, int size, int argc, char** argv)
	{
		/*
		 *full path to the project file
		 */
		gchar* projectFile;
		/*
		 *path to the folder containing the
		 */
		gchar* traceFileFolderPath;
		/*
		 *path to the traces file for the mpi rank
		 */
		gchar* traceFilePath;
		/*
		 *hostname for the trace file name
		 */
		gchar* hostname;
		/*
		 *name of the traced application
		 */
		gchar* programName;

		/*
		 *commucator list
		 */
		struct Communicator* communicators;


		read_cli_args( &projectFile, &traceFileFolderPath, argc, argv);


		readproject(&programName ,&hostname, comms, fileList, dataTypes, rank,
				projectFile);

		commsLen = (int) g_slist_length(comms[0]);

		communicators = (struct Communicator*)
			malloc(sizeof(struct Communicator)*commsLen);

		g_slist_foreach(comms[0], (GFunc)gen_communicator_hash, (gpointer) communicators);

		traceFilePath = gen_trace_file_name(traceFileFolderPath, programName,hostname,
			rank);

		read_trace(traceFile, traceFilePath);

		free(projectFile);
		free(traceFileFolderPath);
		free(traceFilePath);
		free(hostname);
		free(programName);
	}

void read_cli_args
(gchar** projectFile, gchar** traceFileFolderPath, int argc, char** argv)
{

	GError *gError = NULL;
	 GOptionContext *context;

  context = g_option_context_new ("- HDReplay");

	GOptionEntry entries[] =
	{

		{ "traces", 't', 0, G_OPTION_ARG_STRING, traceFileFolderPath, 
				"Path to the folder containing the trace flies.", "path" }
	};

	g_option_context_add_main_entries (context, entries, NULL);

  if (!g_option_context_parse(context, &argc, &argv, &gError))
  {
		/*
		 * acctually the parsing function doesn't return this error code but
		 * it is used for error handling
		 */
		error(ERR, HD_CLI_PARSE, "%s\n", &gError->message);
	}
	
	/*
	 *If the remaining arguments are more then the application name and a project 
	 *file raise an error.
	 */
	if(argc > 2)
	{
		GString* args = g_string_new((gchar*) "");
		for (int i = 1; i < argc; i++) 
		{
			args = g_string_append(args, (gchar*) argv[i]);
			args = g_string_append(args, (gchar*) "\n");
		}
		error(ERR, HD_CLI_PARSE, "More than one project file provided:\n %s",args->str);
	}
	/*
	 *If no project file is provided rais an error.
	 */
	else if(argc < 2)
	{
		error(ERR, HD_CLI_PARSE, "%s","No project file provided.");
	}
	else
	{
		projectFile[0] = g_strdup(argv[1]);
	}
	/*
	 * if no trace file path is given the current pwd is used to search for the
	 * trace flies
	 */
  if(traceFileFolderPath[0] == NULL)
  {
		traceFileFolderPath[0] = g_strdup("./"); 
  }
}

gchar *gen_trace_file_name
(gchar *traceFileFolderPath, gchar *programName, gchar *hostname, int rank)
{
	/*
	 *GString containing the path to the trace file that will be read by 
	 *this rank
	 */
	GString* traceFilePath = g_string_new((const gchar*) traceFileFolderPath);
	/*
	 *the rank of the MPI process convertet into a gchar
	 */
	gchar* rankString = g_strdup_printf("%i",rank);


	/*
	 *TODO: regex search?
	 *append a slash to the end of the string if none is found 
	 */
  if(traceFilePath->str[(traceFilePath->len-1)] != '/')
  {
    g_string_append(traceFilePath, (gchar*) "/");
  }


  traceFilePath = g_string_append(traceFilePath, programName);
  traceFilePath = g_string_append(traceFilePath, (gchar*) "_");
  traceFilePath = g_string_append(traceFilePath, hostname);
  traceFilePath = g_string_append(traceFilePath, (gchar*) "_");
  traceFilePath = g_string_append(traceFilePath, rankString);
  traceFilePath = g_string_append(traceFilePath, (gchar*)"_0.trc");
	
	return traceFilePath->str;
}

void gen_communicator_hash(struct Communicator* communicators)
{

}
