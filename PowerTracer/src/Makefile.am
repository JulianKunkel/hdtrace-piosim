include $(top_srcdir)/common.am

bin_PROGRAMS = pt

pt_SOURCES = pt.c conf.c trace.c tracing.c LMG.c serial.c
pt_CFLAGS = $(HDTWLIB_CFLAGS)
pt_LDADD = $(HDTWLIB_LDADD)
pt_LDFLAGS = $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -lpthread

lib_LTLIBRARIES = libPT.la

libPT_la_SOURCES = pt.c conf.c trace.c tracing.c LMG.c serial.c
libPT_la_CFLAGS = -DBUILD_LIB $(HDTWLIB_CFLAGS)


######################
# Run configurations #
######################

run_pt: pt
	./pt
