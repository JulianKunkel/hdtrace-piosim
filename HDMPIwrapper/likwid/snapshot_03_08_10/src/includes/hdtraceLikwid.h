#ifndef HAVE_LIKWID_HDTRACE
#define HAVE_LIKWID_HDTRACE

typedef struct hdLikwidResults{
  double wallclocktime;
  double runtime;
  double IPC; // per Cycle
  double clock; // avg Value in MHz
  double memBandwidth; // MiB/s
  double remReadBW; // MiB/s
  double sse_scalar; // Ops/s
  double sse_packed; // Ops/s
  double sse_sp;
  double sse_dp;
} hdLikwidResults;

void hdLikwid_init    (int coreNumber);

void hdLikwid_start   ();
void hdLikwid_end     (hdLikwidResults * measured);

void hdLikwid_finalize();

#endif