SUBDIRS = src .

include $(top_srcdir)/common.am

.PHONY: force

doc: devdoc

doxyINPUT = $(SRC_DIR)

devdoc: force
	OUTDIR=$(DOC_DIR) INPUT="$(doxyINPUT)" doxygen $(top_srcdir)/Doxyfile

