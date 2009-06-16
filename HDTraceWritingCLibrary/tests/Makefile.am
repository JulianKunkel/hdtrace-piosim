include $(top_srcdir)/common.am

check_PROGRAMS = hdTopoTest hdStatsTest hdRelationTest hdTraceTest

hdTopoTest_SOURCES = hdTopoTest.c $(SRC_DIR)/hdTopo.c
hdTopoTest_CFLAGS = -I$(INC_DIR)

hdStatsTest_SOURCES = hdStatsTest.c
hdStatsTest_CFLAGS = -I$(SRC_DIR) -I$(INC_DIR)
hdStatsTest_LDADD = $(LIB_DIR)/libhdStats.la
hdStatsTest_LDFLAGS = -static

hdTraceTest_SOURCES = hdTraceTest.c
hdTraceTest_CFLAGS = -I$(INC_DIR)
hdTraceTest_LDADD = $(LIB_DIR)/libhdTrace.la

hdRelationTest_SOURCES = hdRelationTest.c
hdRelationTest_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS)
hdRelationTest_LDADD = $(LIB_DIR)/libhdRelation.la $(GLIB_LDADD)
hdRelationTest_LDFLAGS = $(GLIB_LIBS)

TESTS = hdTopoTest hdStatsTest hdRelationTest
