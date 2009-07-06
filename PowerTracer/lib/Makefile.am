# $Id: Makefile.am 498 2009-07-06 00:04:51Z krempel $

include $(top_srcdir)/common.am

lib_LTLIBRARIES = libpt.la

libpt_la_SOURCES = $(SRC_DIR)/pt.c $(SRC_DIR)/tracing.c $(SRC_DIR)/trace.c $(SRC_DIR)/conf.c $(SRC_DIR)/LMG.c $(SRC_DIR)/serial.c
libpt_la_CFLAGS = -I$(INC_DIR) $(HDTWLIB_CFLAGS)
