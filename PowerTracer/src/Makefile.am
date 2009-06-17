include $(top_srcdir)/common.am

bin_PROGRAMS = pt

pt_SOURCES = pt.c LMG.c serial.c
pt_CFLAGS = $(HDTWLIB_CFLAGS)
pt_LDADD = $(HDTWLIB_LDADD)
pt_LDFLAGS = $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS)


######################
# Run configurations #
######################

run_pt: pt
	./pt
