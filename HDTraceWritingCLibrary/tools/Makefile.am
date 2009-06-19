include $(top_srcdir)/config/common.am

noinst_PROGRAMS = hdStatsPrint
dist_noinst_SCRIPTS = hdStatsReadHeader.pl

hdStatsPrint_CFLAGS = -I$(INC_DIR)

######################
# Run configurations #
######################

STATS_EXAMPLE=$(top_srcdir)/../PerformanceTracingLibrary/build/examples/MyProject_host0_Performance.stat
HEADER_READER=$(top_srcdir)/tools/hdStatsReadHeader.pl

run_hdStatsPrint: hdStatsPrint ${HEADER_READER}
	./hdStatsPrint $(shell ${HEADER_READER} $(STATS_EXAMPLE))
