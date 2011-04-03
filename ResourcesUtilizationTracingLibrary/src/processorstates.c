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
	}
	
	closedir(dir);
	
	return 0;
}
