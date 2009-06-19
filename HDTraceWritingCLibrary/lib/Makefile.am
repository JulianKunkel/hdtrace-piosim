include $(top_srcdir)/common.am

lib_LTLIBRARIES = libhdTrace.la libhdStats.la libhdRelation.la libhdTracing.la

libhdTrace_la_SOURCES = $(SRC_DIR)/hdTrace.c $(SRC_DIR)/hdTopo.c $(SRC_DIR)/common.c $(SRC_DIR)/util.c
libhdTrace_la_CFLAGS = -I$(INC_DIR)

libhdStats_la_SOURCES = $(SRC_DIR)/hdStats.c $(SRC_DIR)/hdTopo.c $(SRC_DIR)/common.c $(SRC_DIR)/util.c
libhdStats_la_CFLAGS = -I$(INC_DIR)

libhdRelation_la_SOURCES = $(SRC_DIR)/hdRelation.c $(SRC_DIR)/hdTopo.c $(SRC_DIR)/common.c $(SRC_DIR)/util.c
libhdRelation_la_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS)

libhdTracing_la_SOURCES = $(SRC_DIR)/hdTrace.c  $(SRC_DIR)/hdStats.c  $(SRC_DIR)/hdRelation.c $(SRC_DIR)/common.c $(SRC_DIR)/util.c
libhdTracing_la_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS)
 
