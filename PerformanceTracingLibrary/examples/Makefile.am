# $Id$

include $(top_srcdir)/common.am

noinst_PROGRAMS = ptLoop

ptLoop_CFLAGS = -I$(INC_DIR) $(HDTWLIB_CFLAGS)
ptLoop_LDADD = -lpt $(HDTWLIB_LDADD)
ptLoop_LDFLAGS = $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -L$(LIB_DIR)/.libs -static

######################
# Run configurations #
######################

run_ptLoop: ptLoop
	./ptLoop ../pt.cfg
