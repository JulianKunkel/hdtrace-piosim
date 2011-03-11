/**
 * @file RUT.c
 *
 * @date 11.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

//#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <dirent.h>

/**
 * just a stub right now
 */
unsigned int cpuidle_available() {
	DIR *cpudir = opendir("/sys/devices/system/cpu/cpuidle");
	if (!cpudir) {
		return 0;
	} else {
		closedir(cpudir);
		return 1;
	}
}


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


unsigned int get_c_state_time(unsigned int core, unsigned int state_number) {
	return 42;
}

