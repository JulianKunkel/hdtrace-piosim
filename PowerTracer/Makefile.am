# $Id$

SUBDIRS = lib bin examples .

include $(top_srcdir)/common.am

include_HEADERS = $(INC_DIR)/*.h

.PHONY: force

doc: devdoc apidoc

doxyINPUT = $(SRC_DIR) $(INC_DIR)

devdoc: force
	OUTDIR=$(DOC_DIR) INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile

apidoc: force
	mkdir -p $(DOC_DIR)/api
	OUTDIR=$(DOC_DIR)/api INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile.api_only

