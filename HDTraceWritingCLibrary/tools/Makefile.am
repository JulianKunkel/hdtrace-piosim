include $(top_srcdir)/common.am

noinst_PROGRAMS = hdStatsPrint
dist_noinst_SCRIPTS = hdStatsReadHeader.pl

hdStatsPrint_CFLAGS = -I$(INC_DIR)

######################
# Run configurations #
######################

STATS_EXAMPLE=../../PerformanceTracingLibrary/examples/MyProject_host0_Performance.stat

run_hdStatsPrint: hdStatsPrint hdStatsReadHeader.pl
	./hdStatsPrint $(shell ./hdStatsReadHeader.pl $(STATS_EXAMPLE))
