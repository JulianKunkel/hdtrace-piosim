# $Id$

include $(top_srcdir)/common.am

bin_PROGRAMS = pt

pt_SOURCES = $(SRC_DIR)/ptmain.c $(SRC_DIR)/pt.c $(SRC_DIR)/tracing.c $(SRC_DIR)/trace.c $(SRC_DIR)/conf.c $(SRC_DIR)/LMG.c $(SRC_DIR)/serial.c $(SRC_DIR)/topo.c
pt_CFLAGS = -I$(SRC_DIR) -I$(INC_DIR) $(HDTWLIB_CFLAGS) $(GLIB_CFLAGS)
pt_LDADD = $(HDTWLIB_LDADD)
pt_LDFLAGS = -lpthread $(GLIB_LIBS)


######################
# Run configurations #
######################

run_pt: pt
	./pt
