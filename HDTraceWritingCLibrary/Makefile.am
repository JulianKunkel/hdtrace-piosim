SUBDIRS = lib . tools tests

include $(top_srcdir)/common.am

include_HEADERS = $(INC_DIR)/hd*.h

.PHONY: force

doc: devdoc apidoc

devdoc: force
	OUTDIR=$(DOC_DIR) doxygen $(top_srcdir)/Doxyfile

apidoc: force
	mkdir -p $(DOC_DIR)/api
	OUTDIR=$(DOC_DIR)/api doxygen $(top_srcdir)/Doxyfile
