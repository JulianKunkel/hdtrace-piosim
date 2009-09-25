# $Id$

include $(top_srcdir)/common.am

check_PROGRAMS = rutTest

rutTest_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS) $(GLIBTOP_CFLAGS) $(HDTWLIB_CFLAGS)
rutTest_LDADD = $(LIB_DIR)/libRUT.la $(HDTWLIB_LDADD) $(GLIB_LDADD) $(GLIBTOP_LDADD)
rutTest_LDFLAGS = $(GLIB_LIBS) $(GLIBTOP_LIBS) $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -static

TESTS = rutTest

# Build the tests already with target all
# This is especially needed for eclipse autobuild function
all: $(check_PROGRAMS)
