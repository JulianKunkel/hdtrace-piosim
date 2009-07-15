include $(top_srcdir)/config/common.am

lib_LTLIBRARIES = libhdTraceMPIWrapper.la

# do not use $(SRC_DIR) here since hdTraceMPIWrapper.c is build in build tree
libhdTraceMPIWrapper_la_SOURCES = $(top_builddir)/src/hdTraceMPIWrapper.c
libhdTraceMPIWrapper_la_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS)
