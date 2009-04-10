INCLUDE_DIR=../include

CC=gcc
MAKEDEPEND=gcc -MM

#CPPFLAGS=-DDEBUG
CPPFLAGS=
CFLAGS=-g -O -std=c99 -pedantic -Wall -Wextra -Wformat=2 \
	-Wold-style-definition -Wmissing-declarations -Wmissing-prototypes \
	-Wredundant-decls -Wmissing-noreturn -Wshadow -Wcast-align -Wwrite-strings \
	-Winline -Wswitch-enum -Wswitch-default -Winit-self -Wmissing-include-dirs \
	-Wundef -Waggregate-return -Wmissing-format-attribute -Wnested-externs \
	-Wstrict-prototypes -Wconversion \
	-D_POSIX_C_SOURCE -D_GNU_SOURCE -I$(INCLUDE_DIR)

# use large file compilation environment
CFLAGS+=$(shell getconf LFS_CFLAGS)
LDFLAGS+=$(shell getconf LFS_LDFLAGS)
LIBS+=$(shell getconf LFS_LIBS)

#prevent warnings at x64 (bug!?!)
CFLAGS+=-D_LARGEFILE_SOURCE

SRCS = $(wildcard *.c)

DEPDIR = .deps
df = $(DEPDIR)/$(*F)

.PHONY: all clean dist-clean

all: $(SRCS:%.c=%.o)

clean:
	rm -f *.o $(BINS)

dist-clean: clean
	rm -f .deps/*

%.o : %.c
	@echo "  DEP  $@"; \
	$(MAKEDEPEND) $(CPPFLAGS) $(CFLAGS) -o $(df).d $<; \
	cp $(df).d $(df).P; \
	sed -e 's/#.*//' -e 's/^[^:]*: *//' -e 's/ *\\$$//' \
	    -e '/^$$/ d' -e 's/$$/ :/' < $(df).d >> $(df).P; \
	rm -f $(df).d
	@echo "  CC   $@"; \
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $<

-include $(SRCS:%.c=$(DEPDIR)/%.P)
