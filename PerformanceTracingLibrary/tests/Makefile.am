include $(top_srcdir)/common.am

check_PROGRAMS = ptlTest

ptlTest_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS) $(GLIBTOP_CFLAGS) $(HDTWLIB_CFLAGS)
ptlTest_LDADD = $(LIB_DIR)/libPTL.la $(HDTWLIB_LDADD) $(GLIB_LDADD) $(GLIBTOP_LDADD)
ptlTest_LDFLAGS = $(GLIB_LIBS) $(GLIBTOP_LIBS) $(HDTWLIB_LDFLAGS) $(HDTWLIB_LIBS) -static

TESTS = ptlTest

# Build the tests already with target all
# This is especially needed for eclipse autobuild function
all: $(check_PROGRAMS)
