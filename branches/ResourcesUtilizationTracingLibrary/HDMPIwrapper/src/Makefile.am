hdTraceMPIWrapper.c: $(SRC_DIR)/hdTraceMPIWrapper.src.c $(INC_DIR)/hdMPITracer.h.in $(top_srcdir)/scripts/create_sim-wrapper.py
	@echo "[PY] create"
	@cat $(SRC_DIR)/hdTraceMPIWrapper.src.c > src/hdTraceMPIWrapper.c || exit 1
	@cat $(INC_DIR)/hdMPITracer.h.in > include/hdMPITracer.h || exit 1
	@$(top_srcdir)/scripts/create_sim-wrapper.py $(top_srcdir)/interesting_funcs.h src/hdTraceMPIWrapper.c  include/hdMPITracer.h include/hdMPITracerCodeLocator.h
