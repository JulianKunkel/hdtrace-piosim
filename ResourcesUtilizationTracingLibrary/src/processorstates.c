/**
 * @file processorstates.c
 *
 * @date 10.03.2011
 * @author Christian Seyda <seydanator@web.de>
 * @version \$Id$
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <dirent.h>
#include <string.h>
#include <glib.h>

#include "common.h"

/**
 * checks if the sysfs interface for cpuidle is active
 *
 * @return availability of cpuidle
 *
 * @retval 1 Success
 * @retval 0 Failure
 */
unsigned int cpuidle_available() {
	DIR *cpudir = opendir("/sys/devices/system/cpu/cpu0/cpuidle");
	if (!cpudir) {
		return 0;
	} else {
		closedir(cpudir);
		return 1;
	}
}

/**
 * checks if the sysfs interface for cpufreq is active
 * 
 * @return availability of cpuifreq
 *
 * @retval 1 Success
 * @retval 0 Failure
 */
unsigned int cpufreq_available() {
	DIR *cpudir = opendir("/sys/devices/system/cpu/cpufreq");
	if (!cpudir) {
		cpudir = opendir("/sys/devices/system/cpu/cpu0/cpufreq");
		if (!cpudir)
			return 0;
		else {
			closedir(cpudir);
			return 1;
		}
	} else {
		closedir(cpudir);
		return 1;
	}
}

 /**
 * checks if the sysfs interface for cpufreq stats is active
 * 
 * @return availability of cpuifreq stats
 *
 * @retval 1 Success
 * @retval 0 Failure
 */
unsigned int cpufreq_stats_available() {
	DIR *cpudir = opendir("/sys/devices/system/cpu/cpu0/cpufreq/stats");
	if (!cpudir) {
		return 0;
	} else {
		closedir(cpudir);
		return 1;
	}
}

/**
 * returns the number of readable c-states
 *
 * @return number of c-states provided by sysfs
 */
int get_available_c_states(){
	int c_states = 0;
	DIR *cpuidle = opendir("/sys/devices/system/cpu/cpu0/cpuidle");

	if(cpuidle == NULL)
		return c_states;

	struct dirent *entry;
	
	while ((entry = readdir(cpuidle))) {
		if (strlen(entry->d_name) < 6)
			continue;
		c_states++;
	}
	
	closedir(cpuidle);
	return c_states;
}

/**
 * returns the number of readable p-states
 *
 * @return number of p-states provided by sysfs
 */
int get_available_p_states(){
	gchar *buffer;
	gsize length;
	GError *error = NULL;
	gint p_states = 0;

	if (g_file_get_contents("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state", &buffer, &length, &error)) {
		for (int i = 0; i < length; i++) {
			if (buffer[i] == '\n') {
				p_states++;
			}
		}
		DEBUGMSG("Found %d pstates!", p_states);
	} else {
		DEBUGMSG("Error reading frequencies from file: %s", error->message);
		g_error_free(error);
		return p_states;
	}

	g_free(buffer);

	return p_states;
}

/**
 * saves the actual values of the time-files in the cpuidle sysfs interface
 *
 * @param c_states pointer to array in order to save new values
 * @param cpu_num number of CPUs
 * @param c_states_num number of c-states
 *
 * @retval 0 success
 */
int get_c_state_times(unsigned long int *c_states, int cpu_num, int c_states_num){
	char filename[128], *f;
	char line[4096];
	FILE *file = NULL;
	struct dirent *entry;
	DIR *dir;
	int clevel, len;
	
	for (int i=0; i<cpu_num; ++i){
		len = sprintf(filename, "/sys/devices/system/cpu/cpu%d/cpuidle", i);
		clevel = 0;
		
		dir = opendir(filename);
		if (!dir)
			continue;
		
		while ((entry = readdir(dir))) {
			if (strlen(entry->d_name) < 6)
				continue;
			sprintf(filename + len, "/%s/time", entry->d_name);
			file = fopen(filename, "r");
			if (!file) {
				continue;
			}
			
			memset(line, 0, 4096);
			f = fgets(line, 4096, file);
			fclose(file);
			
			if (f == NULL) {
				closedir(dir);
				return -1;
			}

			c_states[i * c_states_num + clevel] = 1+strtoull(line, NULL, 10);
			
			clevel++;
		}

		closedir(dir);
	}
	
	return 0;
}

/**
 * saves the actual values of the time-file in the cpufreq sysfs interface
 *
 * @param p_states pointer to array in order to save new values
 * @param cpu_num number of CPUs
 * @param p_states_num number of p-states
 *
 * @retval 0 success
 */
int get_p_state_times(unsigned long int *p_states, int cpu_num, int p_states_num) 
{
	char filename[128], *f;
	char line[4096];
	char *token = NULL;
	char *delim = " ";
	FILE *file = NULL;
	int len = 0;
	int plevel = 0;

	unsigned long int freq = 0;
	unsigned long int count = 0;

	for (int i = 0; i < cpu_num; i += 1) {
		len = sprintf(filename, "/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", i);
		file = fopen(filename, "r");
		plevel = 0;

		if (!file) {
			continue;
		}

		while (fgets(line, sizeof(line), file) != NULL) {
			token = strtok(line, delim);
			freq = strtoul(token, NULL, 10);
			token = strtok(NULL, delim);
			count = strtoul(token, NULL, 10);

			p_states[(i * p_states_num) + plevel] = freq;
			p_states[(cpu_num * p_states_num) + (i * p_states_num) + plevel] = count;
			memset(line, 0, 4096);
			plevel++;

		}
		fclose(file);
	}
	return 0;
}
