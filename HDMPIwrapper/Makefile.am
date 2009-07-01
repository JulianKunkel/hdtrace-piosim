SUBDIRS = lib .
ACLOCAL_AMFLAGS = -I config

include $(top_srcdir)/config/common.am

include_HEADERS = $(INC_DIR)/*.h

.PHONY: force

doc: devdoc

doxyINPUT = $(SRC_DIR) $(INC_DIR) include

devdoc: force
	OUTDIR=$(DOC_DIR) INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile
