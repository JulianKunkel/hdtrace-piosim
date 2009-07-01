SUBDIRS = . src lib
ACLOCAL_AMFLAGS = -I config

BUILT_SOURCES = src/hdTraceMPIWrapper.c

include $(top_srcdir)/config/common.am

include_HEADERS = $(SRC_DIR)/*.h

.PHONY: force

doc: devdoc

doxyINPUT = $(SRC_DIR) $(INC_DIR) include

devdoc: force
	OUTDIR=$(DOC_DIR) INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile

src/hdTraceMPIWrapper.c: $(SRC_DIR)/hdTraceMPIWrapper.src.c $(INC_DIR)/hdMPITracer.h.in $(top_srcdir)/scripts/create_sim-wrapper.py
	@echo "[PY] create"
	@cat $(SRC_DIR)/hdTraceMPIWrapper.src.c > src/hdTraceMPIWrapper.c || exit 1
	@cat $(INC_DIR)/hdMPITracer.h.in > include/hdMPITracer.h || exit 1
	@$(top_srcdir)/scripts/create_sim-wrapper.py $(top_srcdir)/interesting_funcs.h src/hdTraceMPIWrapper.c  include/hdMPITracer.h include/hdMPITracerCodeLocator.h
