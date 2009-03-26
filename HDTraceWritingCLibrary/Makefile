PWD=$(shell pwd)

SRC_DIR=$(PWD)/src
LIB_DIR=$(PWD)/lib

AR=ar
TAGS=ctags

ARFLAGS=rcs


BINS=lib/libhdTrace.a lib/libhdStats.a

.PHONY: all clean dist-clean

all: $(BINS)

clean:
	$(MAKE) -C $(SRC_DIR) $@
	rm -f *.o $(BINS)
	
dist-clean: clean
	$(MAKE) -C $(SRC_DIR) $@
	rm -f tags
	rm -rf doc/*

lib/libhdTrace.a: src/hdTrace.o src/hdTopo.o src/util.o
	@echo "  AR   $@"; \
	$(AR) $(ARFLAGS) $@ $^

lib/libhdStats.a: src/hdStats.o src/hdTopo.o src/util.o
	@echo "  AR   $@"; \
	$(AR) $(ARFLAGS) $@ $^

.PHONY: src doc

src/%o:
	$(MAKE) -C $(SRC_DIR)

doc:
	doxygen Doxyfile
	
tags: $(wildcard *.[ch])
	@echo "  TAGS"; \
	$(TAGS) $^
