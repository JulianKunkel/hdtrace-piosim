/**
 * @file tracing.c
 *
 * @date 14.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#include "tracing.h"

#include <unistd.h>
#include <glib.h>
#include <glibtop.h>
#include <limits.h>
#include <errno.h>
#include <assert.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "config.h"
#include "common.h"
#include "hdStats.h"
#include "hdError.h"

#ifdef HAVE_PROCESSORSTATES
#include "processorstates.h"
#include <cpufreq.h>
#endif

/* ************************************************************************* */
/*               COMPILE TIME ERROR AND WARNING DEFINITIONS                  */
/* ************************************************************************* */

__warndecl(ptl_cte_too_few_hds_values_per_group,
		"WARNING: HDS_MAX_VALUES_PER_GROUP < RUT_MAX_STATS_VALUES");

/* ************************************************************************* */
/*                      STATIC FUNCTION DECLARATIONS                         */
/* ************************************************************************* */

static void doTracingStep(tracingDataStruct *tracingData);
static void doTracingStepCPU(tracingDataStruct *tracingData);
static void doTracingStepMEM(tracingDataStruct *tracingData);
static void doTracingStepNET(tracingDataStruct *tracingData);
static void doTracingStepHDD(tracingDataStruct *tracingData);

static GRegex * disk_regex;

static gchar ** readFileLines(const char * filename){
	char txt [4096*4];
	int fd = open(filename, O_RDONLY);
	if (fd < 0){
		WARNMSG("Could not read from /proc/diskstats\n");
		return NULL;
	}	
	int ret = read(fd, txt, 4096*4 - 1);
	close(fd);

	if (ret <= 0){
		WARNMSG("Could not read from /proc/diskstats\n");
		return NULL;
	}
	txt[ret] = 0;

	return g_strsplit(txt, "\n", RUT_MAX_HDD_STATS*3);
}

static gchar ** readDiskStats(){
	return readFileLines("/proc/diskstats");
}

static gint64 contextSwitches(){
	static int line = -1;
	gchar ** lines = readFileLines("/proc/stat");
	if (line == -1){
		// determine line starting with "ctxt"
		int lineNR = 0;
		while(lines[lineNR] != NULL){
			if ( strncmp(lines[lineNR], "ctxt ", 5) == 0){
				line = lineNR;
				break;
			}
			lineNR++; 
		}
		if (line == -1){
			WARNMSG("Could not read context-switches from /proc/stat");
		}
	}

	if (line == -1){
		g_strfreev(lines);
		return -1;
	}

	assert(strncmp(lines[line], "ctxt ", 5) == 0);
	
	gint64 value = atoll(lines[line] + 5);
	g_strfreev(lines);
	return value;
}

/* ************************************************************************* */
/*                    PUBLIC FUNCTION IMPLEMENTATIONS                        */
/* ************************************************************************* */

/**
 * Initialize the tracing stuff.
 *
 * - Creates the statistics group
 * - Units and value types are defined inside this function (TODO change that?)
 *
 * TODO Better error handling
 *
 * @param topoNode    Topology node to associate the trace with
 * @param topoLevel   Topology level of the topology node
 * @param tracingData Tracing Data Object to use
 *
 * @return error state
 *
 * @retval 0  Success
 */
int initTracing(hdTopoNode *topoNode, /* topoNode the trace belongs to */
int topoLevel, /* level of topology the trace take place */
tracingDataStruct *tracingData /* pointer to tracing Data */
) {
	/*
	 * Create statistics group for tracing
	 */

	/* generate statistics group */
	hdStatsGroup *group = hdS_createGroup("Utilization", topoNode, topoLevel);
	/* TODO Error handling */
	if (group == NULL) {
		assert(errno != HD_ERR_INVALID_ARGUMENT);
		assert(errno != HD_ERR_MALLOC);
		assert(errno != HD_ERR_BUFFER_OVERFLOW);
		assert(errno != HD_ERR_CREATE_FILE);
	}

	tracingData->group = group;

	/*
	 * Initialize static tracing data
	 */

	/* get number of CPUs */
	tracingData->staticData.cpu_num = (gint) sysconf(_SC_NPROCESSORS_CONF);

#ifdef HAVE_PROCESSORSTATES
	/* get number of c-states and init memory */
	tracingData->staticData.c_states_num = (gint) get_available_c_states();
	if (tracingData->staticData.c_states_num > 0) {
		rut_malloc(tracingData->oldValues.c_states,
				tracingData->staticData.c_states_num * tracingData->staticData.cpu_num,
				-1);
	} else {
		tracingData->oldValues.c_states = NULL;
	}

	/* get number of p-states and init memory */
	tracingData->staticData.p_states_num = (gint) get_available_p_states();
	if (tracingData->staticData.p_states_num > 0) {
		rut_malloc(tracingData->oldValues.p_states,
				tracingData->staticData.p_states_num * tracingData->staticData.cpu_num * 2,
				-1);

	} else {
		tracingData->oldValues.p_states = NULL;
	}

#endif

	/* get available network interfaces */
	tracingData->staticData.netifs = glibtop_get_netlist(
			&(tracingData->staticData.netlist));

	/*
	 * Specify tracing entry format
	 */

	rutSources sources = tracingData->sources;

#define RUT_STRING_BUFFER_LENGTH 50

	/* allocate string buffer */
	char strbuf[RUT_STRING_BUFFER_LENGTH];
	strbuf[0] = '\0';

#if HDS_MAX_VALUES_PER_GROUP < RUT_MAX_STATS_VALUES
	ptl_cte_too_few_hds_values_per_group();
#endif

	int ret = 0;

#define ADD_VALUE(group, string, type, unit, grouping) \
	do { \
		ret = hdS_addValue(group, string, type, unit, grouping); \
		if(ret < 0)	{ \
			g_assert(errno != HD_ERR_INVALID_ARGUMENT); \
			g_assert(errno != HD_ERR_BUFFER_OVERFLOW); \
			g_assert(errno != HDS_ERR_GROUP_COMMIT_STATE); \
		} \
	} while (0)

	/* specify entry format */
	if (sources.CPU_UTIL){
		ADD_VALUE(group, "CPU_TOTAL", FLOAT, "%", "CPU");

		gint64 val = contextSwitches();
		if (val != -1){
			ADD_VALUE(group, "CONTEXT_SWITCHES", INT64, NULL , "#");
		}
	}

	if (sources.CPU_UTIL_X)
		for (int i = 0; i < tracingData->staticData.cpu_num; ++i) {
			ret = snprintf(strbuf, RUT_STRING_BUFFER_LENGTH, "CPU_TOTAL_%d", i);
			g_assert(ret < RUT_STRING_BUFFER_LENGTH);
			g_assert(ret > 0);
			ADD_VALUE(group, strbuf, FLOAT, "%", "CPU");
		}

#ifdef HAVE_PROCESSORSTATES
	/*if cpuidle registers could not be found, disable it*/
	if(!cpufreq_available())
	{
		tracingData->sources.CPU_FREQ_X = 0;
		sources.CPU_FREQ_X = 0;
		INFOMSG("Could not find cpufreq. Tracing of P-States disabled.");
	}

	if (sources.CPU_FREQ_X) {
		for (int i = 0; i < tracingData->staticData.cpu_num; ++i)
		{
			ret = snprintf(strbuf, RUT_STRING_BUFFER_LENGTH, "CPU_FREQ_AVG_%d", i);
			g_assert(ret < RUT_STRING_BUFFER_LENGTH);
			g_assert(ret > 0);
			ADD_VALUE(group, strbuf, INT64, "kHz", "CPU_FREQ");
		}
		ret = snprintf(strbuf, RUT_STRING_BUFFER_LENGTH, "CPU_FREQ_AVG");
		g_assert(ret < RUT_STRING_BUFFER_LENGTH);
		g_assert(ret > 0);
		ADD_VALUE(group, strbuf, INT64, "kHz", "CPU_FREQ");
	}

	/*if cpuidle registers could not be found, disable it*/
	if(!cpuidle_available())
	{
		tracingData->sources.CPU_IDLE_X = 0;
		sources.CPU_IDLE_X = 0;
		INFOMSG("Could not find cpuidle. Tracing of C-States disabled.");
	}

	if (sources.CPU_IDLE_X) {
		for (int i = 0; i < tracingData->staticData.cpu_num; ++i)
		{
			for (int j = 0; j < tracingData->staticData.c_states_num; ++j)
			{
				ret = snprintf(strbuf, RUT_STRING_BUFFER_LENGTH, "CPU_IDLE_%d_C%d", i, j);
				g_assert(ret < RUT_STRING_BUFFER_LENGTH);
				g_assert(ret > 0);
				ADD_VALUE(group, strbuf, FLOAT, "%", "CPU_IDLE");
			}
		}
		ret = snprintf(strbuf, RUT_STRING_BUFFER_LENGTH, "CPU_IDLE_CPU_C0");
		g_assert(ret < RUT_STRING_BUFFER_LENGTH);
		g_assert(ret > 0);
		ADD_VALUE(group, strbuf, FLOAT, "%", "CPU_IDLE");
	}
#endif

#define MEM_UNIT "B"

	if (sources.MEM_USED)
		ADD_VALUE(group,"MEM_USED", INT64, MEM_UNIT, "MEM");

	if (sources.MEM_FREE)
		ADD_VALUE(group,"MEM_FREE", INT64, MEM_UNIT, "MEM");

	if (sources.MEM_SHARED)
		ADD_VALUE(group,"MEM_SHARED", INT64, MEM_UNIT, "MEM");

	if (sources.MEM_BUFFER)
		ADD_VALUE(group,"MEM_BUFFER", INT64, MEM_UNIT, "MEM");

	if (sources.MEM_CACHED)
		ADD_VALUE(group,"MEM_CACHED", INT64, MEM_UNIT, "MEM");

#define NET_UNIT "B"

	for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i) {
		char name[255];

		if (sources.NET_IN_X) {
			ret = snprintf(name, 255, "NET_IN_%s",
					tracingData->staticData.netifs[i]);
			assert(ret < 255);
			ADD_VALUE(group, name, INT64, NET_UNIT, "NET");
		}

		if (sources.NET_OUT_X) {
			ret = snprintf(name, 255, "NET_OUT_%s",
					tracingData->staticData.netifs[i]);
			assert(ret < 255);
			ADD_VALUE(group, name, INT64, NET_UNIT, "NET");
		}

	}

	if (sources.NET_IN_EXT)
		ADD_VALUE(group, "NET_IN_EXT", INT64, NET_UNIT, "NET");

	if (sources.NET_OUT_EXT)
		ADD_VALUE(group, "NET_OUT_EXT", INT64, NET_UNIT, "NET");

	if (sources.NET_IN)
		ADD_VALUE(group, "NET_IN", INT64, NET_UNIT, "NET");

	if (sources.NET_OUT)
		ADD_VALUE(group, "NET_OUT", INT64, NET_UNIT, "NET");

#define HDD_UNIT "Byte"

	// right now read harddisk partition to use from environment
	char * mountpoint = getenv("RUT_HDD_MOUNTPOINT");
	if (mountpoint == NULL) {
		mountpoint = "/";
		WARNMSG("Use environment variable RUT_HDD_MOUNTPOINT to set mount point to trace.\n"
				"Right now '%s' is used by default", mountpoint);
	}
	rut_malloc(tracingData->staticData.hdd_mountpoint, strlen(mountpoint) + 1, -1)
	strcpy(tracingData->staticData.hdd_mountpoint, mountpoint);

	if (sources.HDD_READ)
		ADD_VALUE(group, "HDD_READ", INT64, HDD_UNIT, "HDD");

	if (sources.HDD_WRITE)
		ADD_VALUE(group, "HDD_WRITE", INT64, HDD_UNIT, "HDD");

	disk_regex = g_regex_new( " +", 0, 0, NULL );

	// check the available block devices and pick the ones that are interesting

	gchar ** diskstats = readDiskStats();
	if(diskstats != NULL){
		// parse input
		gchar ** curPos = diskstats;

		const gchar * curstr = curPos[0];
		int count_block_devices = 0;
		char * block_devices[RUT_MAX_HDD_STATS+1];
		gint block_devices_index[RUT_MAX_HDD_STATS + 1];

		int line = 0;
		gchar * lastname = g_strdup("DUMMY_BLOCK_NAME");
		while(curstr != NULL){
			//printf("%s \n", curstr);
			gchar ** entries = g_regex_split(disk_regex, curstr, 0);
			if(entries[0] != NULL){
				gchar * name = entries[3];
				//WARNMSG("%s %s", name, lastname);

				if( strstr(name, lastname) != name ){
					lastname = g_strdup(name);
					if ( atoll(entries[4 + DISKSTAT_HDD_WRITES_COMPLETED]) != 0 || atoll(entries[4 + DISKSTAT_HDD_READ_COMPLETED]) != 0){
						block_devices_index[count_block_devices] = line;
						block_devices[count_block_devices] = g_strdup(entries[3]);					
						count_block_devices++;
					}		
				}
			}
			g_strfreev(entries);

			curstr = *(++curPos);
			line++;
		}

		g_strfreev(diskstats);

		tracingData->staticData.block_devices_num = count_block_devices;
		if(count_block_devices > 0){
			tracingData->staticData.block_devices = malloc(sizeof(char*) * count_block_devices); 
			memcpy(tracingData->staticData.block_devices, block_devices, count_block_devices * sizeof(char*));		

			tracingData->staticData.block_devices_index = malloc(sizeof(gint) * count_block_devices);
			memcpy(tracingData->staticData.block_devices_index, block_devices_index, count_block_devices * sizeof(gint));		
		}

		//int bd;	
		//for (bd = 0; bd < tracingData->staticData.block_devices_num ; bd++){
			//WARNMSG("%d %d %d",  tracingData->staticData.block_devices_index[bd], block_devices_index[bd], count_block_devices);
		//}
	}else{
		tracingData->staticData.block_devices_num = 0;
	}

	int bd;
	if (tracingData->staticData.block_devices_num > 0){
		for(bd=0; bd < DISKSTAT_COUNT ; bd++){
			if ( tracingData->sources.PROC_HDD_STATS[bd] ){
				rut_malloc( tracingData->oldValues.io_completed[bd], sizeof(guint) * tracingData->staticData.block_devices_num, -1 );
			}
		}
	}

#define REGISTER_PROC(I, NAME, UNIT, SUBGROUP) 	if ( tracingData->sources.PROC_HDD_STATS[I] ){\
		char * localname = g_strdup_printf("%s:%s", NAME, name);\
		ADD_VALUE(group, localname, INT64, UNIT, SUBGROUP);\
		g_free(localname);\
	} 
	for (bd = 0; bd < tracingData->staticData.block_devices_num ; bd++){
		char * name = tracingData->staticData.block_devices[bd];
		
		REGISTER_PROC(0, "READS" , "#" , "BLOCK_ACCESSED")
		REGISTER_PROC(1, "READS_MERGED", "#" , "BLOCK_MERGED")
		REGISTER_PROC(2, "READS_SECTORS", "#" , "BLOCK_SECTORS")
		REGISTER_PROC(3, "READS_TIME_SPEND","ms" , "BLOCK_TIME")
		REGISTER_PROC(4, "WRITES_COMPLETED", "#" , "BLOCK_ACCESSED")
		REGISTER_PROC(5, "WRITES_MERGED", "#", "BLOCK_MERGED")
		REGISTER_PROC(6, "WRITTEN_SECTORS", "#" , "BLOCK_SECTORS")
		REGISTER_PROC(7, "WRITES_TIME_SPEND", "ms" , "BLOCK_TIME")
		REGISTER_PROC(8, "IOS_INPROGRESS", "#" , "BLOCK_INPROGRESS")
		REGISTER_PROC(9, "IOS_TIME_SPEND", "ms", "BLOCK_TIME")
		REGISTER_PROC(10,"WEIGHTED_WAITTIME" , "ms" , "BLOCK_TIME")
	
	}

#undef REGISTER_PROC
	/*
	 * Commit statistics group
	 */
	hdS_commitGroup(tracingData->group);

	/*
	 * Initialize internal tracing data
	 */

	/* allocate memory for saving network interfaces statistics values */
	rut_malloc(tracingData->oldValues.netload,
			tracingData->staticData.netlist.number, -1);

	/* mark old values invalid */
	tracingData->oldValues.valid = FALSE;

	return 0;
}

/**
 * Run function of the tracing thread.
 *
 * This function and so the thread is running the tracing loop all
 * the time when \a tracingData->control->started is \a TRUE. For
 * terminating the loop, \a tracingData->control->terminate must
 * become \a TRUE.
 *
 * @param tracingDataPointer  Pointer to the tracing data structure
 *
 * @return Always returns NULL when the tread is terminated
 */
gpointer tracingThreadFunc(gpointer tracingDataPointer) {
	int ret;

	tracingDataStruct *tracingData = (tracingDataStruct *) tracingDataPointer;

	/* create timer */
	gulong currentTime, waitTime;
	GTimer *timer = g_timer_new();

	/*
	 * Tracing iterations loop
	 */

	/* should the traces become enabled/disabled ? */
	int enable_trace = 0;
	int disable_trace = 0;

	/* terminate on error with retval */
	int terminate = 0;

	/* are the traces and the device enabled? */
	int enabled = 0;

	/*  do initial tracing step to determine values*/
	doTracingStep(tracingData);

	while (1) {
		INFOMSG("Entering tracing loop");

		/* error handling */
		if (terminate) {
			/* try to disable traces and device */
			if (enabled) {
				hdS_disableGroup(tracingData->group);
			}
			break;
		}

		g_mutex_lock(tracingData->control->mutex);

		/* if the thread is stopped but the traces are enabled, disable them */
		if (!tracingData->control->started && enabled) {
			disable_trace = 1;
		}
		/* else wait until the thread is started or started again */
		else {
			while (!(tracingData->control->started
					|| tracingData->control->terminate)) {
				assert(!enabled);
				assert(!enable_trace);
				assert(!disable_trace);
				INFOMSG("Waiting for tracing becomes started");
				/* mark old statistics data invalid */
				tracingData->oldValues.valid = FALSE;
				/* wait for tracing becomes enabled */g_cond_wait (tracingData->control->stateChanged,
						tracingData->control->mutex);
			}

			/* if the thread is started but the traces are disabled, enable them */
			if (tracingData->control->started && !enabled) {
				enable_trace = 1;
			}
			/* if termination requested, disable traces and terminate */
			if (tracingData->control->terminate) {
				if (enabled)
					disable_trace = 1;
				else {
					g_mutex_unlock(tracingData->control->mutex);
					break;
				}
			}
		}
		g_mutex_unlock(tracingData->control->mutex);

		/* enable and disable makes no sense together */
		assert(!(enable_trace && disable_trace));

		/* enable traces if necessary */
		if (enable_trace) {

			/* enable statistic group */
			ret = hdS_enableGroup(tracingData->group);
			assert(ret >= 0);

			enable_trace = 0;
			enabled = 1;
		}

		/* disable traces if necessary */
		if (disable_trace) {

			/* disable statistic group */
			ret = hdS_disableGroup(tracingData->group);
			assert(ret >= 0);

			disable_trace = 0;
			enabled = 0;

			/* restart loop to wait for next start */
			continue;
		}
		/* wait for next multiple of interval time */
		currentTime = (gulong) (1000.0 * g_timer_elapsed(timer, NULL));

		waitTime = (gulong) tracingData->interval - (currentTime
				% (gulong) tracingData->interval);

		DEBUGMSG("Wait %ld ms", waitTime);
		g_usleep(waitTime * 1000);

		/*  do tracing step */
		doTracingStep(tracingData);
	}
	INFOMSG("Loop exited");

	/* disable statistics group */
	hdS_disableGroup(tracingData->group);

	/* finalize trace */
	hdS_finalize(tracingData->group);

	/* free timer */
	g_timer_destroy(timer);

	/* free memory used for thread data */rut_free(tracingData->oldValues.netload);
	for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i)
		rut_free(tracingData->staticData.netifs[i]);
	rut_free(tracingData->staticData.netifs);
	rut_free(tracingData->staticData.hdd_mountpoint);

#ifdef HAVE_PROCESSORSTATES
	rut_free(tracingData->oldValues.c_states);
	rut_free(tracingData->oldValues.p_states);
#endif

	rut_free(tracingData);

	g_free(NULL);

	return NULL;
}

/* ************************************************************************* */
/*                    STATIC FUNCTION IMPLEMENTATIONS                        */
/* ************************************************************************* */

/**
 * Get all tracing values for one iteration step and write them
 *  to the statistics group.
 *
 * @param tracingData  Tracing Data Object
 */
static void doTracingStep(tracingDataStruct *tracingData) {

	INFOMSG("Step!");

	doTracingStepCPU(tracingData);

	doTracingStepMEM(tracingData);

	doTracingStepNET(tracingData);

	doTracingStepHDD(tracingData);

	/* mark old statistics values saved as valid */
	tracingData->oldValues.valid = TRUE;
}

/**
 * Check for errors and use assert if one occurred.
 *  ret is the return value of one of the hdS_write*Value functions
 */
#define CHECK_WRITE_VALUE_ERROR(ret) \
	do { \
		if (ret < 0) \
			switch (errno)	{ \
			case HD_ERR_INVALID_ARGUMENT: assert(!"HD_ERR_INVALID_ARGUMENT"); \
			case HD_ERR_TRACE_DISABLED: assert(!"HD_ERR_TRACE_DISABLED"); \
			case HDS_ERR_GROUP_COMMIT_STATE: assert(!"HDS_ERR_GROUP_COMMIT_STATE"); \
			case HDS_ERR_ENTRY_STATE: assert(!"HDS_ERR_ENTRY_STATE"); \
			default: assert(ret == 0); \
			} \
	} while (0)

/**
 * Write a INT32 value to the tracing group tracingData->group.
 * Use \ref CHECK_WRITE_VALUE_ERROR for error handling
 */
#define WRITE_I32_VALUE(tracingData, value) \
	do { \
		int ret = hdS_writeInt32Value((tracingData)->group, value); \
		CHECK_WRITE_VALUE_ERROR(ret); \
	} while (0)

/**
 * Write a INT64 value to the tracing group tracingData->group.
 * Use \ref CHECK_WRITE_VALUE_ERROR for error handling
 */
#define WRITE_I64_VALUE(tracingData, value) \
	do { \
		int ret = hdS_writeInt64Value((tracingData)->group, value); \
		CHECK_WRITE_VALUE_ERROR(ret); \
	} while (0)

/**
 * Write a FLOAT value to the tracing group tracingData->group.
 * Use \ref CHECK_WRITE_VALUE_ERROR for error handling
 */
#define WRITE_FLOAT_VALUE(tracingData, value) \
	do { \
		int ret = hdS_writeFloatValue((tracingData)->group, value); \
		CHECK_WRITE_VALUE_ERROR(ret); \
	} while (0)

/* ************************************************************************
 * CPU
 */

/**
 * Get the CPU tracing values for one iteration step and write them
 *  to the statistics group.
 *
 * @param tracingData  Tracing Data Object
 */
static void doTracingStepCPU(tracingDataStruct *tracingData) {

	if (!(tracingData->sources.CPU_UTIL || tracingData->sources.CPU_UTIL_X
#ifdef HAVE_PROCESSORSTATES
			|| tracingData->sources.CPU_FREQ_X
#endif
			))
		return;

#define CPUDIFF(val) \
	((gdouble) (cpu.val - tracingData->oldValues.cpu.val))
	/* overflows are too rare (max num-of-CPUs times per 497 days)
	 * so overflow handling would be disproportional costly.  */

	gfloat valuef;
#ifdef HAVE_PROCESSORSTATES
	gint64 valuei64;
#endif
	glibtop_cpu cpu;

	glibtop_get_cpu(&cpu);

	gint64 contextSwitcheNum = contextSwitches();

	if (tracingData->oldValues.valid) {
		if (tracingData->sources.CPU_UTIL) {

			valuef = (gfloat) (1.0 - ((CPUDIFF(idle) + CPUDIFF(iowait))
					/ CPUDIFF(total)));
			WRITE_FLOAT_VALUE(tracingData, valuef * 100);
			DEBUGMSG("CPU_TOTAL = %f%%", valuef * 100);
			
			if (contextSwitcheNum != -1){
				gint64 delta;
				if (contextSwitcheNum >=  tracingData->oldValues.contextSwitches) {
					delta = contextSwitcheNum - tracingData->oldValues.contextSwitches;
				}else{ // Overflow
					delta = contextSwitcheNum + 1<<63 - tracingData->oldValues.contextSwitches;
				}
				WRITE_I64_VALUE(tracingData, delta );
			}
		}

		if (tracingData->sources.CPU_UTIL_X) {
			for (int i = 0; i < tracingData->staticData.cpu_num; ++i) {
				/* assert(CPUDIFF(xcpu_total[i]) != 0); */

				/* TODO: Check CPU enable state (flags) */
				valuef = (gfloat) (1.0 - ((CPUDIFF(xcpu_idle[i])
						+ CPUDIFF(xcpu_iowait[i])) / CPUDIFF(xcpu_total[i])));
				WRITE_FLOAT_VALUE(tracingData, valuef * 100);
				DEBUGMSG("CPU_TOTAL_%d = %f%%", i, valuef * 100);
			}
		}
#ifdef HAVE_PROCESSORSTATES
		gint cpu_num = tracingData->staticData.cpu_num;
		gint p_states_num = tracingData->staticData.p_states_num;

		if (tracingData->sources.CPU_FREQ_X)
		{
			/* temp for old values */
			guint64 p_states[p_states_num * cpu_num * 2];

			/* save old values */
			memcpy(p_states, tracingData->oldValues.p_states, sizeof(guint64) * p_states_num * cpu_num * 2);

			/* get new values */
			get_p_state_times( tracingData->oldValues.p_states, cpu_num, p_states_num);

			/* difference */
			for (int d = (p_states_num * cpu_num); d < (p_states_num * cpu_num * 2); d++) {
				p_states[d] = tracingData->oldValues.p_states[d] - p_states[d];
			}

			/* calculate avg. cpu freq */
			/* interval ms / time_in_state file 10ms */

			DEBUGMSG("INTERVAL: %d",tracingData->interval);

			gfloat freq_sum = 0;
			for (int c = 0; c < cpu_num; c++) {
				gfloat freq = 0;
				for (int p = 0; p < p_states_num; p++) {
					if (p_states[(cpu_num * p_states_num) + (c * p_states_num) + p] > (tracingData->interval / 10)) {
						p_states[(cpu_num * p_states_num) + (c * p_states_num) + p] = tracingData->interval / 10;
					}
					DEBUGMSG("CPU_FREQ_AVG_%d TIME_IN_STATE_%ld %ld", c, p_states[(c * p_states_num) + p], p_states[(cpu_num * p_states_num) + (c * p_states_num) + p]);
					freq += (gfloat)
					/* frequency MHz */
					((gfloat) (p_states[(c * p_states_num) + p]) *
							/* time in state 10ms */
							(gfloat) (p_states[(cpu_num * p_states_num) + (c * p_states_num) + p] / (gfloat) (tracingData->interval / 10)));
				}
				freq_sum += freq;
				WRITE_I64_VALUE(tracingData, (gint64) freq);
				DEBUGMSG("CPU_FREQ_AVG_%d = %d kHz", c, (gint64) freq);
			}
			WRITE_I64_VALUE(tracingData, (gint64) (freq_sum / cpu_num));
			DEBUGMSG("CPU_FREQ_AVG = %d kHz", (gint64) (freq_sum / cpu_num));
		}

		if (tracingData->sources.CPU_IDLE_X)
		{
			/* temp for old values */
			guint64 c_states[tracingData->staticData.c_states_num * tracingData->staticData.cpu_num];

			/* save old values */
			memcpy(c_states, tracingData->oldValues.c_states, sizeof(guint64) * tracingData->staticData.c_states_num * tracingData->staticData.cpu_num);

			/* get new values */
			get_c_state_times(
					tracingData->oldValues.c_states,
					tracingData->staticData.cpu_num,
					tracingData->staticData.c_states_num);

			/* difference */
			for (int d=0;
					d < (tracingData->staticData.c_states_num * tracingData->staticData.cpu_num);
					++d)
			{
				c_states[d] = tracingData->oldValues.c_states[d] - c_states[d];
			}

			gfloat valuef_sum = 0;
			for (int i = 0; i < tracingData->staticData.cpu_num; ++i)
			{
				guint64 total = 0;

				/* total time in idle */
				for (int j = 0; j < tracingData->staticData.c_states_num; ++j) {
					total += c_states[i * tracingData->staticData.c_states_num + j];
				}

				/* time not in idle: easy, interval - time in idle,
				 * careful as time in idle is given in microsecs
				 **/
				//TODO: use real elapsed time
				guint64 interval = (tracingData->interval) * 1000;

				if (total > interval) {
					/* rounding errors in measurement might make c0 go slightly negative*/
					interval = total;
				}
				/* percentage: time in c0(microsecs) / total time(millisecs) */

				valuef = (interval - total) * 100.0 / interval;
				valuef_sum += valuef;

				WRITE_FLOAT_VALUE(tracingData, valuef);
				DEBUGMSG("CPU_IDLE_C%d_%d = %f%%", 0, i, valuef);

				for (int j = 1; j < tracingData->staticData.c_states_num; ++j)
				{

					if (c_states[i * tracingData->staticData.c_states_num + j] == 0) {
						valuef = 0.0;
					} else {
						valuef = c_states[i * tracingData->staticData.c_states_num + j] * 100 / interval;
					}

					WRITE_FLOAT_VALUE(tracingData, valuef);
					DEBUGMSG("CPU_IDLE_C%d_%d = %f%%", j, i, valuef);
				}
			}
			WRITE_FLOAT_VALUE(tracingData, (gfloat) (valuef_sum / cpu_num));
			DEBUGMSG("CPU_IDLE_C0 = %f%%", (gfloat) (valuef_sum / cpu_num));
		}
	} else {
		get_c_state_times(
				tracingData->oldValues.c_states,
				tracingData->staticData.cpu_num,
				tracingData->staticData.c_states_num);
		get_p_state_times(
				tracingData->oldValues.p_states,
				tracingData->staticData.cpu_num,
				tracingData->staticData.p_states_num);
#endif

	}
	/* save current CPU statistics for next step */
	tracingData->oldValues.cpu = cpu;
	tracingData->oldValues.contextSwitches = contextSwitcheNum;

#undef CPUDIFF

}

/* ************************************************************************
 * Memory
 */

/**
 * Get the MEM tracing values for one iteration step and write them
 *  to the statistics group.
 *
 * @param tracingData  Tracing Data Object
 */
static void doTracingStepMEM(tracingDataStruct *tracingData) {

	if (!(tracingData->sources.MEM_USED || tracingData->sources.MEM_FREE
			|| tracingData->sources.MEM_SHARED
			|| tracingData->sources.MEM_BUFFER
			|| tracingData->sources.MEM_CACHED))
		return;

	gint64 valuei64;
	glibtop_mem mem;

	glibtop_get_mem(&mem);

	if (tracingData->oldValues.valid) {

#define MEM_WRITE_VALUE(PART,part) \
	if (tracingData->sources.MEM_##PART) { \
		valuei64 = (gint64) (mem.part); \
		WRITE_I64_VALUE(tracingData, valuei64); \
		DEBUGMSG("MEM_" #PART " = %" G_GINT64_FORMAT " " MEM_UNIT, valuei64); \
	}

		MEM_WRITE_VALUE(USED, used)
		MEM_WRITE_VALUE(FREE, free)
		MEM_WRITE_VALUE(SHARED, shared)
		MEM_WRITE_VALUE(BUFFER, buffer)
		MEM_WRITE_VALUE(CACHED, cached)

#undef MEM_WRITE_VALUE

	}
	/* save current memory statistics for next step */
	tracingData->oldValues.mem = mem;

}

/* ************************************************************************
 * Network
 */

/**
 * Get the NET tracing values for one iteration step and write them
 *  to the statistics group.
 *
 * @param tracingData  Tracing Data Object
 */
static void doTracingStepNET(tracingDataStruct *tracingData) {

	if (!(tracingData->sources.NET_IN_X || tracingData->sources.NET_OUT_X
			|| tracingData->sources.NET_IN_EXT
			|| tracingData->sources.NET_OUT_EXT || tracingData->sources.NET_IN
			|| tracingData->sources.NET_OUT))

		return;

	gint64 valuei64;

	/* variables to aggregate in and out bytes */
	guint64 all_in = 0;
	guint64 all_out = 0;
	guint64 ext_in = 0;
	guint64 ext_out = 0;

	/* handle statistics for each network interface */
	for (size_t i = 0; i < tracingData->staticData.netlist.number; ++i) {
		glibtop_netload netload;
		glibtop_get_netload(&netload, tracingData->staticData.netifs[i]);

		guint64 in, out;

		guint64 new_in, old_in, new_out, old_out;
		new_in = netload.bytes_in;
		old_in = tracingData->oldValues.netload[i].bytes_in;
		new_out = netload.bytes_out;
		old_out = tracingData->oldValues.netload[i].bytes_out;

		DEBUGMSG("Got network traffic for %s:"
				" IN = %" G_GUINT64_FORMAT " " NET_UNIT ", "
				" OUT = %" G_GUINT64_FORMAT " " NET_UNIT,
				tracingData->staticData.netifs[i], new_in, new_out);

		/* handle system counter overflows */
		if (tracingData->oldValues.valid) {
			/* ATTENTION: Some assumptions take place here:
			 * - OS counter for network traffic is unsigned long.
			 * - (old < new) indicates a single overflow since last cycle.
			 */
			new_in += (new_in < old_in) ? (guint64) ULONG_MAX : 0;
			new_out += (new_out < old_out) ? (guint64) ULONG_MAX : 0;
		}

		in = new_in - old_in;
		out = new_out - old_out;

		/* count aggregated traffic of all interfaces */
		all_in += in;
		all_out += out;

		/* count aggregated traffic of external interfaces */
		if (!(netload.if_flags & (1 << GLIBTOP_IF_FLAGS_LOOPBACK))) {
			ext_in += in;
			ext_out += out;
		}

		if (tracingData->oldValues.valid) {
			/* trace single interfaces */

			if (tracingData->sources.NET_IN_X) {
				valuei64 = (gint64) in;
				WRITE_I64_VALUE(tracingData, valuei64);
				DEBUGMSG("NET_IN_%s = %" G_GINT64_FORMAT " " NET_UNIT,
						tracingData->staticData.netifs[i], valuei64);
			}

			if (tracingData->sources.NET_OUT_X) {
				valuei64 = (gint64) out;
				WRITE_I64_VALUE(tracingData, valuei64);
				DEBUGMSG("NET_OUT_%s = %" G_GINT64_FORMAT " " NET_UNIT,
						tracingData->staticData.netifs[i], valuei64);
			}
		}

		/* save current network interface statistics for next step */
		tracingData->oldValues.netload[i] = netload;
	}

	/* handle aggregated statistics of all interfaces */
	if (tracingData->oldValues.valid) {
		if (tracingData->sources.NET_IN_EXT) {
			valuei64 = (gint64) ext_in;
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("NET_IN_EXT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

		if (tracingData->sources.NET_OUT_EXT) {
			valuei64 = (gint64) ext_out;
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("NET_OUT_EXT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

		if (tracingData->sources.NET_IN) {
			valuei64 = (gint64) all_in;
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("NET_IN = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}

		if (tracingData->sources.NET_OUT) {
			valuei64 = (gint64) all_out;
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("NET_OUT = %" G_GINT64_FORMAT " " NET_UNIT, valuei64);
		}
	}
}

/* *************************************************************************
 * HDD
 */

/**
 * Get the HDD tracing values for one iteration step and write them
 *  to the statistics group.
 *
 * @param tracingData  Tracing Data Object
 */
static void doTracingStepHDD(tracingDataStruct *tracingData) {

	if (!(tracingData->sources.HDD_READ || tracingData->sources.HDD_WRITE))
		return;

	gint64 valuei64;
	glibtop_fsusage fs;

	if (tracingData->sources.HDD_READ || tracingData->sources.HDD_WRITE) {
		glibtop_get_fsusage(&fs, tracingData->staticData.hdd_mountpoint);
	}

	if (tracingData->oldValues.valid) {
		/* TODO check if block size is always 512,
		 * the reported is the correct one for the file system but not
		 * the correct factor for the counted blocks		 */
		if (tracingData->sources.HDD_READ) {
			valuei64 = (gint64) (/*fs.block_size*/512 * (fs.read - tracingData->oldValues.fs.read));
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("DISK_READ = %" G_GINT64_FORMAT, valuei64);
		}

		if (tracingData->sources.HDD_WRITE) {
			valuei64 = (gint64) (/*fs.block_size*/512 * (fs.write - tracingData->oldValues.fs.write));
			WRITE_I64_VALUE(tracingData, valuei64);
			DEBUGMSG("DISK_WRITE = %" G_GINT64_FORMAT, valuei64);
		}
	}


	// PARSE DATA FROM DISKS
	
	// TODO make code more robust, e.g. by counting lines etc.
	gchar ** diskstats = readDiskStats();

	int bd;	

	// parse input of relevant lines
	for (bd = 0; bd < tracingData->staticData.block_devices_num ; bd++){
		int line_in_file = tracingData->staticData.block_devices_index[bd];

		// WARNMSG("%d", line_in_file);

		gchar ** entries = g_regex_split(disk_regex, diskstats[line_in_file], 0);				
		if(entries[0] == NULL){
			WARNMSG("Parsing of /proc/diskstats failed");
			g_strfreev(entries);
			continue;
		}
		gchar * name = entries[3];				
		if( strcmp(name, tracingData->staticData.block_devices[bd]) != 0 ){
			WARNMSG("Block device changed line in file: %s %s", name, tracingData->staticData.block_devices[bd]);
			g_strfreev(entries);
			continue;
		}

		int i;
		//WARNMSG("%s", diskstats[line_in_file]);
		for(i=0; i < DISKSTAT_COUNT ; i++){
			if ( tracingData->sources.PROC_HDD_STATS[i] ){
				guint64 value =  atoll(entries[4 + i]);

				if (tracingData->oldValues.valid) {
					guint64 reportValue;
					if( i != DISKSTAT_HDD_IOS_INPROGRESS){
						if ( value >= tracingData->oldValues.io_completed[i][bd] ){
							reportValue = value - tracingData->oldValues.io_completed[i][bd];
						}else{
							// overflow, TODO check for correct handling...
							reportValue = value + 1<<63 - tracingData->oldValues.io_completed[i][bd];
						}
					}else{
						// DISKSTAT_HDD_IOS_INPROGRESS
						reportValue = value;
					}

					WRITE_I64_VALUE(tracingData, reportValue);
					//WARNMSG("DISK_PROC_VALUE for %s = %" G_GINT64_FORMAT, name , reportValue);
				}

				tracingData->oldValues.io_completed[i][bd] = value;
			}
		}

		g_strfreev(entries);

	}
	
	g_strfreev(diskstats);
	tracingData->oldValues.fs = fs;
}

#undef WRITE_I64_VALUE
#undef CHECK_WRITE_VALUE_ERROR
