#ifndef PROCESSORSTATES_H_
#define PROCESSORSTATES_H_

unsigned int cpuidle_available();
unsigned int cpufreq_available();
unsigned int get_c_state_time(unsigned int core, unsigned int state_number);
#endif /*PROCESSORSTATES_H_*/