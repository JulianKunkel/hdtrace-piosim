# $Id$

include $(top_srcdir)/common.am

noinst_PROGRAMS = traceAll traceLoop traceLoop2

traceAll_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS) $(GLIBTOP_CFLAGS) $(HDTWLIB_CFLAGS)
traceAll_LDADD = -lPTL $(HDTWLIB_LDADD) $(GLIB_LDADD) $(GLIBTOP_LDADD)
traceAll_LDFLAGS = $(GLIB_LIBS) $(GLIBTOP_LIBS) $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -L$(LIB_DIR)/.libs -static

traceLoop_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS) $(GLIBTOP_CFLAGS) $(HDTWLIB_CFLAGS)
traceLoop_LDADD = -lPTL $(HDTWLIB_LDADD) $(GLIB_LDADD) $(GLIBTOP_LDADD)
traceLoop_LDFLAGS = $(GLIB_LIBS) $(GLIBTOP_LIBS) $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -L$(LIB_DIR)/.libs -static

traceLoop2_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS) $(GLIBTOP_CFLAGS) $(HDTWLIB_CFLAGS)
traceLoop2_LDADD = -lPTL $(HDTWLIB_LDADD) $(GLIB_LDADD) $(GLIBTOP_LDADD)
traceLoop2_LDFLAGS = $(GLIB_LIBS) $(GLIBTOP_LIBS) $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -L$(LIB_DIR)/.libs -static

######################
# Run configurations #
######################

run_traceAll: traceAll
	./traceAll

run_traceLoop: traceLoop
	./traceLoop ALL
