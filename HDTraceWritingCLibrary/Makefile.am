SUBDIRS = lib . tools tests
ACLOCAL_AMFLAGS = -I config

include $(top_srcdir)/config/common.am

include_HEADERS = $(INC_DIR)/*.h

.PHONY: force

doc: devdoc apidoc

doxyINPUT = $(SRC_DIR) $(INC_DIR) $(top_srcdir)/include

devdoc: force
	OUTDIR=$(DOC_DIR) INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile

apidoc: force
	mkdir -p $(DOC_DIR)/api
	OUTDIR=$(DOC_DIR)/api INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile.api_only

