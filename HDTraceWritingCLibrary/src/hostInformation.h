#ifndef HD_HOST_INFORMATION_
#define HD_HOST_INFORMATION_

/**
 * Determine the actual processor speed in MHZ on the system.
 */
unsigned processorCPUspeedinMHZ(void);

/**
 * This function returns the processor model name as a simple string.
 * The return string must be freed afterwards.
 */
char * processorModelName();

#endif /* HD_HOST_INFORMATION_ */
