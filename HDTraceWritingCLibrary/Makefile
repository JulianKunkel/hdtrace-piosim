PWD=$(shell pwd)

SRC_DIR=src
LIB_DIR=lib
DOC_DIR=doc
TESTS_DIR=tests

AR=ar
TAGS=ctags

ARFLAGS=rcs


BINS=$(LIB_DIR)/libhdTrace.a $(LIB_DIR)/libhdStats.a

.PHONY: all clean dist-clean

all: $(BINS)

clean:
	$(MAKE) -C $(SRC_DIR) $@
	$(MAKE) -C $(TESTS_DIR) $@
	rm -f *.o $(BINS)
	
dist-clean: clean
	$(MAKE) -C $(SRC_DIR) $@
	$(MAKE) -C $(TESTS_DIR) $@
	rm -f tags
	rm -rf $(DOC_DIR)/*

$(LIB_DIR)/libhdTrace.a: $(SRC_DIR)/hdTrace.o $(SRC_DIR)/hdTopo.o $(SRC_DIR)/common.o $(SRC_DIR)/util.o
	@echo "  AR   $@"; \
	$(AR) $(ARFLAGS) $@ $^

$(LIB_DIR)/libhdStats.a: $(SRC_DIR)/hdStats.o $(SRC_DIR)/hdTopo.o $(SRC_DIR)/common.o $(SRC_DIR)/util.o
	@echo "  AR   $@"; \
	$(AR) $(ARFLAGS) $@ $^

.PHONY: force

$(SRC_DIR)/%o: force
	$(MAKE) -C $(SRC_DIR) $(notdir $@)
	
tests: force all
	$(MAKE) -C $(TESTS_DIR)

doc: force
	doxygen Doxyfile
	
tags: $(wildcard *.[ch])
	@echo "  TAGS"; \
	$(TAGS) $^
