#ifndef PROCESSORSTATES_H_
#define PROCESSORSTATES_H_

unsigned int cpuidle_available();
unsigned int cpufreq_available();
int get_c_state_times(unsigned long int *c_states, int cpu_num, int c_states_num);
int get_available_c_states();
#endif /*PROCESSORSTATES_H_*/