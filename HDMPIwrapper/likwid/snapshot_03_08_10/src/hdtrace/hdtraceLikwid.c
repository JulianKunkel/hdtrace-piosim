#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sched.h>
#include <sys/types.h>
#include <unistd.h>
#include <ctype.h>

#include <error.h>
#include <types.h>
#include <msr.h>
#include <timer.h>
#include <cpuid.h>
#include <cpuFeatures.h>
#include <perfmon.h>
#include <strUtil.h>
#include <bstrlib.h>

#include "hdtraceLikwid.h"

// Intel 64 and IA-32 Architectures Software Developer's Manual, Volume 3B, Page 316 ff
// http://www.intel.com/products/processor/manuals/
// 48 Bit width of the counters.

#define VERSION_MSG \
printf("likwid-perfCtr  %d.%d \n\n",VERSION, RELEASE);

//#define DEBUG

#ifdef DEBUG
#warning "Debug is enabled!"
#endif

static int myCoreNumber = 0;
static CyclesData timeData;

extern PerfmonThread* perfmon_threadData;


// copied from likwid-pin.c
static void pinPid(int cpuid)
{
	cpu_set_t cpuset;

	CPU_ZERO(&cpuset);
	CPU_SET(cpuid, &cpuset);
	printf("[likwid-pin] Main PID -> core %d - ",  cpuid);
	if (sched_setaffinity(0, sizeof(cpu_set_t), &cpuset) == -1)
	{
		printf("sched_setaffinity failed : %s \n",strerror(errno));
	}
	else
	{
		printf("OK\n");
	}
}




void hdLikwid_init    (int coreNumber){
  myCoreNumber = coreNumber;

  int threads[1] = {myCoreNumber};

  timer_init();

  // init monitor only for the local thread.
  perfmon_init(1, threads);


  if( coreNumber == 0){
    printf("CPU type:\t%s \n",cpuid_info.name);
    printf("CPU clock:\t%3.2f GHz \n",  (float) timer_getCpuClock() * 1.E-09);
  }

  perfmon_setupEventSet(bformat("JK"));
  //perfmon_setupEventSet(bformat("MEM"));

  // pin PID to coreNumber
  pinPid(coreNumber);
}

void hdLikwid_start   (void){
   perfmon_startCountersThread(0);
   timer_startCycles(&timeData);
}

void hdLikwid_end     (hdLikwidResults * measured){
   timer_stopCycles(&timeData);
   perfmon_stopCountersThread(0);

#ifdef DEBUG
  perfmon_printCounterResults();
#endif
  uint64_t FIXC0 = perfmon_threadData[0].counters[0].counterData; // FIXC0 see nehalem_counter_map
  uint64_t FIXC1 = perfmon_threadData[0].counters[1].counterData;
  uint64_t FIXC2 = perfmon_threadData[0].counters[2].counterData;
  uint64_t PMC0 = perfmon_threadData[0].counters[3].counterData;
  uint64_t PMC1 = perfmon_threadData[0].counters[4].counterData;
  uint64_t PMC2 = perfmon_threadData[0].counters[5].counterData;
  uint64_t PMC3 = perfmon_threadData[0].counters[6].counterData;
  uint64_t UPMC0 = perfmon_threadData[0].counters[7].counterData;
  uint64_t UPMC1 = perfmon_threadData[0].counters[8].counterData;
  uint64_t UPMC2 = perfmon_threadData[0].counters[9].counterData;
  uint64_t UPMC3 = perfmon_threadData[0].counters[10].counterData;

  double iClock = 1.0 / timer_getCpuClock();
  double time =  FIXC1 * iClock;  // real CPU runtime!

  measured->runtime = FIXC1 * iClock; // real CPU runtime!
  measured->wallclocktime = (double) timer_printCycles(& timeData) / timer_getCpuClock();
  double revTime = 1 / measured->wallclocktime;
  measured->IPC 		= (double) FIXC0 / FIXC1;
  measured->clock 		= (double) 1.0E-6 * FIXC1 / FIXC2 / iClock;
  measured->memBandwidth 	= (double) 1.0E-6 * (UPMC0 + UPMC1) * 64 *revTime ;
  measured->remReadBW 		= (double) 1.0E-6 * UPMC2 * 64 * revTime;
  measured->sse_packed 		= (double) 1.0E-6 * PMC0 * revTime;
  measured->sse_scalar 		= (double) 1.0E-6 * PMC1 * revTime;
  measured->sse_sp 		= (double) 1.0E-6 * PMC2 * revTime;
  measured->sse_dp 		= (double) 1.0E-6 * PMC3 * revTime;
}

void hdLikwid_finalize(){
    perfmon_finalize();
}

int hdLikwidTest()
{
    int c;
    int coreNum = 0;

    // the rank of the process on this node: nodeRank_i < nodeRank_j  <=>  world_rank_i < world_rank_j, but enumerated from 0.
    int processNodeRank = 0;

    hdLikwid_init(coreNum);

    hdLikwidResults results;

    for(c=0; c < 1 ; c++){
	hdLikwid_start();

	int i;
	double a=1;
	double b=0;
	for (i=0; i < (c+1)*100000 ; i++){
	  a=b+i * a;
	  b=a;
	}

	//sleep (1);

	hdLikwid_end(& results);

#ifdef DEBUG
	printf("wc: %f - runtime: %f  ipc: %f  cpu: %f mem: %f remMem: %f scal: %f packed: %f sp: %f dp: %f \n", results.wallclocktime, results.runtime, results.IPC, results.clock, results.memBandwidth, results.remReadBW, results.sse_scalar, results.sse_packed, results.sse_sp, results.sse_dp);
#endif
    }

    hdLikwid_finalize();
    return EXIT_SUCCESS;
}

