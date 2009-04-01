INCLUDE_DIR=../include

CC=gcc
MAKEDEPEND=gcc -MM

#CPPFLAGS=-DDEBUG
CPPFLAGS=
CFLAGS=-W -Wall -g -std=c99 -pedantic -D_POSIX_C_SOURCE -D_GNU_SOURCE -I$(INCLUDE_DIR)

# use large file compilation environment
CFLAGS+=$(shell getconf LFS_CFLAGS)
LDFLAGS+=$(shell getconf LFS_LDFLAGS)
LIBS+=$(shell getconf LFS_LIBS)

#prevent warnings at x64 (bug!?!)
CFLAGS+=-D_LARGEFILE_SOURCE

SRCS = $(wildcard *.c)

DEPDIR = .deps
df = $(DEPDIR)/$(*F)

BINS=hdTopoTest

.PHONY: all clean dist-clean force

all: $(BINS)

clean:
	rm -f *.o $(BINS)

dist-clean: clean
	rm -f .deps/*

../src/%o: force
	$(MAKE) -C ../src $(notdir $@)
	
%.o : %.c
	@echo "  DEP  $@"; \
	$(MAKEDEPEND) $(CPPFLAGS) $(CFLAGS) -o $(df).d $<; \
	cp $(df).d $(df).P; \
	sed -e 's/#.*//' -e 's/^[^:]*: *//' -e 's/ *\\$$//' \
	    -e '/^$$/ d' -e 's/$$/ :/' < $(df).d >> $(df).P; \
	rm -f $(df).d
	@echo "  CC   $@"; \
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $<

hdTopoTest: hdTopoTest.o ../src/hdTopo.o
	@echo "  LD  $@"; \
	$(CC) $(LDFLAGS) -o $@ $^
	
-include $(SRCS:%.c=$(DEPDIR)/%.P)
