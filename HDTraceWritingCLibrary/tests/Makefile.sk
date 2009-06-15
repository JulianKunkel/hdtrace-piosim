INCLUDE_DIR=../include
LIB_DIR=../lib

CC=gcc
MAKEDEPEND=gcc -MM

#CPPFLAGS=-DDEBUG
CPPFLAGS=
CFLAGS=-g -O0 -ggdb -std=c99 -pedantic -Wall -Wextra -D_POSIX_C_SOURCE -D_GNU_SOURCE -I$(INCLUDE_DIR) -I../src \
	-Waggregate-return -Wcast-align -Wcast-qual -Wconversion -Wfloat-equal \
	-Wformat=2 -Winit-self -Winline -Wmissing-declarations \
	-Wmissing-format-attribute -Wmissing-include-dirs -Wmissing-noreturn \
	-Wmissing-prototypes -Wnested-externs -Wold-style-definition \
	-Wredundant-decls -Wshadow -Wstrict-prototypes -Wswitch-default \
	-Wswitch-enum -Wundef -Wwrite-strings 

LDFLAGS=-L$(LIB_DIR) $(shell pkg-config --libs glib-2.0)  

# use large file compilation environment
CFLAGS+=$(shell getconf LFS_CFLAGS)
LDFLAGS+=$(shell getconf LFS_LDFLAGS)
LIBS+=$(shell getconf LFS_LIBS)

#prevent warnings at x64 (bug!?!)
CFLAGS+=-D_LARGEFILE_SOURCE

SRCS = $(wildcard *.c)

DEPDIR = .deps
df = $(DEPDIR)/$(*F)

BINS=hdTopoTest hdStatsTest hdRelationTest hdTraceTest

.PHONY: all clean dist-clean force

all: $(BINS)

clean:
	rm -f *.o $(BINS)

dist-clean: clean
	rm -f .deps/*

../src/%o: force
	$(MAKE) -C ../src $(notdir $@)

../lib/%: force
	$(MAKE) -C .. lib/$(notdir $@)
	
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
	$(CC) $(LDFLAGS) $^ -o $@
	
hdStatsTest: hdStatsTest.o ../lib/libhdStats.a
	echo "  LD  $@"; \
	$(CC) $(LDFLAGS) $^ -lhdStats -o $@

hdTraceTest: hdTraceTest.o ../lib/libhdTrace.a
	echo "  LD  $@"; \
	$(CC) $(LDFLAGS) $^ -lhdTrace -o $@
	
hdRelationTest: hdRelationTest.o ../lib/libhdRelation.a
	echo "  LD  $@"; \
	$(CC) $(LDFLAGS) $^ -lhdRelation -o $@
	
-include $(SRCS:%.c=$(DEPDIR)/%.P)
