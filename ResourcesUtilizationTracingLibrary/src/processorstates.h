#ifndef PROCESSORSTATES_H_
#define PROCESSORSTATES_H_

unsigned int cpuidle_available();
unsigned int cpufreq_available();
unsigned int cpufreq_stats_available();
int get_available_c_states();
int get_available_p_states();
int get_c_state_times(unsigned long int *c_states, int cpu_num, int c_states_num);
int get_p_state_times(unsigned long int *p_states, int cpu_num, int p_states_num);
#endif /*PROCESSORSTATES_H_*/
