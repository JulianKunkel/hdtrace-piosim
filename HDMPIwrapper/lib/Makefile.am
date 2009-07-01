include $(top_srcdir)/config/common.am

lib_LTLIBRARIES = libhdTraceMPIWrapper.la

libhdTraceMPIWrapper_la_SOURCES = $(srcdir)/hdTraceMPIWrapper.c
libhdTraceMPIWrapper_la_CFLAGS = -I$(INC_DIR) $(GLIB_CFLAGS)

$(srcdir)/hdTraceMPIWrapper.c:
	@echo "[PY] create"
	@cat $(SRC_DIR)/hdTraceMPIWrapper.src.c > $(srcdir)/hdTraceMPIWrapper.c || exit 1
	@cat $(INC_DIR)/hdMPITracer.h.in > $(srcdir)/hdMPITracer.h || exit 1
	@$(SRC_DIR)/../scripts/create_sim-wrapper.py $(INC_DIR)/../interesting_funcs.h $(srcdir)/hdTraceMPIWrapper.c  $(srcdir)/hdMPITracer.h $(INC_DIR)/hdMPITracerCodeLocator.h