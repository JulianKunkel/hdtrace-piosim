#===========================================================================
#
#     Filename:  Makefile
#
#     Description:  Central Makefile
#
#     Version:  <VERSION>
#     Created:  <DATE>
#
#     Author:  Jan Treibig (jt), jan.treibig@gmail.com
#     Company:  RRZE Erlangen
#     Project:  likwid
#     Copyright:  Copyright (c) 2010, Jan Treibig
#
#     This program is free software; you can redistribute it and/or modify
#     it under the terms of the GNU General Public License, v2, as
#     published by the Free Software Foundation
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with this program; if not, write to the Free Software
#     Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
#===========================================================================

SRC_DIR    = ./src
DOC_DIR    = ./doc
BENCH_DIR  = ./bench
GROUP_DIR  = ./groups
MAKE_DIR   = ./

#DO NOT EDIT BELOW

# Dependency chains:
# *.[ch] -> *.o -> executables
# *.ptt -> *.pas -> *.s -> *.o -> executables
# *.txt -> *.h (generated)

include $(MAKE_DIR)/config.mk
include $(MAKE_DIR)/include_$(COMPILER).mk
INCLUDES  += -I./src/includes  -I$(BUILD_DIR) 
DEFINES   += -DVERSION=$(VERSION)                 \
			 -DRELEASE=$(RELEASE)                 \
			 -DMAX_NUM_THREADS=$(MAX_NUM_THREADS) \
			 -DMAX_NUM_SOCKETS=$(MAX_NUM_SOCKETS) \
			 -DLIBLIKWIDPIN=$(LIBLIKWIDPIN)

#CONFIGURE BUILD SYSTEM
BUILD_DIR  = ./$(COMPILER)
Q         ?= @
GENGROUPLOCK = .gengroup

ifneq ($(COLOR),NONE)
DEFINES += -DCOLOR=$(COLOR)
endif

TARGET_LIB = liblikwid.a
PINLIB  = liblikwidpin.so

VPATH     = $(SRC_DIR)
OBJ       = $(patsubst $(SRC_DIR)/%.c, $(BUILD_DIR)/%.o,$(wildcard $(SRC_DIR)/*.c))
ifeq ($(MAKECMDGOALS),likwid-bench)
OBJ      += $(patsubst $(BENCH_DIR)/%.ptt, $(BUILD_DIR)/%.o,$(wildcard $(BENCH_DIR)/*.ptt))
endif
APPS      = likwid-perfCtr  \
			likwid-features \
			likwid-topology \
			likwid-pin      \
            likwid-bench


CPPFLAGS := $(CPPFLAGS) $(DEFINES) $(INCLUDES)
CFLAGS := $(CFLAGS) -fPIC

all: $(BUILD_DIR) $(GENGROUPLOCK) $(OBJ) $(filter-out likwid-bench,$(APPS)) $(TARGET_LIB)  $(PINLIB) libhdtraceLikwid.so

tags:
	@echo "===>  GENERATE  TAGS"
	$(Q)ctags -R

hdtraceLikwid: $(SRC_DIR)/hdtrace/hdtraceLikwid.c
	$(Q)${CC} $(CFLAGS) $(ANSI_CFLAGS) $(CPPFLAGS) ${LFLAGS} -o hdtraceLikwid $(SRC_DIR)/hdtrace/hdtraceLikwid.c $(LIBS) $(OBJ)

libhdtraceLikwid.so:
	@echo "===>  CREATE LIB libhdtraceLikwid.so"
	$(Q)${CC} $(CFLAGS) $(ANSI_CFLAGS) $(CPPFLAGS) ${LFLAGS} -fPIC -o libhdtraceLikwid.so $(SRC_DIR)/hdtrace/hdtraceLikwid.c $(LIBS) $(OBJ) -shared -Wl,-soname,libhdtraceLikwid.so


$(APPS):  $(addprefix $(SRC_DIR)/applications/,$(addsuffix  .c,$(APPS))) $(BUILD_DIR)  $(OBJ)
	@echo "===>  LINKING  $@"
	$(Q)${CC} $(CFLAGS) $(ANSI_CFLAGS) $(CPPFLAGS) ${LFLAGS} -o $@  $(addprefix $(SRC_DIR)/applications/,$(addsuffix  .c,$@)) $(OBJ) $(LIBS)

$(TARGET_LIB): $(OBJ)
	@echo "===>  CREATE LIB  $(TARGET_LIB)"
	$(Q)${AR} -cq $(TARGET_LIB) $(filter-out $(BUILD_DIR)/main.o,$(OBJ))

$(BUILD_DIR):
	@mkdir $(BUILD_DIR)

$(PINLIB):
	@echo "===>  CREATE LIB  $(PINLIB)"
	$(Q)$(MAKE) -s -C src/pthread-overload/ $(PINLIB)

$(GENGROUPLOCK): $(foreach directory,$(shell ls $(GROUP_DIR)), $(wildcard $(GROUP_DIR)/$(directory)/*.txt))
	@echo "===>  GENERATE GROUP HEADERS"
	$(Q)$(GEN_GROUPS) ./groups  $(BUILD_DIR) ./perl/templates
	$(Q)touch $(GENGROUPLOCK)


#PATTERN RULES
$(BUILD_DIR)/%.o:  %.c
	@echo "===>  COMPILE  $@"
	$(Q)$(CC) -c  $(CFLAGS) $(ANSI_CFLAGS) $(CPPFLAGS) $< -o $@
	$(Q)$(CC) $(CPPFLAGS) -MT $(@:.d=.o) -MM  $< > $(BUILD_DIR)/$*.d

$(BUILD_DIR)/%.pas:  $(BENCH_DIR)/%.ptt
	@echo "===>  GENERATE BENCHMARKS"
	$(Q)$(GEN_PAS) ./bench  $(BUILD_DIR) ./perl/templates

$(BUILD_DIR)/%.o:  $(BUILD_DIR)/%.pas
	@echo "===>  ASSEMBLE  $@"
	$(Q)$(PAS) -i x86-64 -o $(BUILD_DIR)/$*.s $<  '$(DEFINES)'
	$(Q)$(AS) $(ASFLAGS)  $(BUILD_DIR)/$*.s -o $@

ifeq ($(findstring $(MAKECMDGOALS),clean),)
-include $(OBJ:.o=.d)
endif

.PHONY: clean distclean install uninstall

.PRECIOUS: $(BUILD_DIR)/%.pas

clean:
	@echo "===>  CLEAN"
	@rm -rf $(BUILD_DIR)/*.o $(BUILD_DIR)/*.so $(BUILD_DIR)/*.a *.so

distclean: clean
	@echo "===>  DIST CLEAN"
	@rm -f likwid-*
	@rm -f $(TARGET_LIB)
	@rm -f $(PINLIB)
	@rm -f $(GENGROUPLOCK)
	@rm -f libhdtraceLikwid*

install:
	@echo "===> INSTALL applications to $(PREFIX)/bin"
	@mkdir -p $(PREFIX)/bin
	@cp -f likwid-*  $(PREFIX)/bin
	@chmod 755 $(PREFIX)/bin/likwid-*
	@echo "===> INSTALL man pages to $(MANPREFIX)/man1"
	@mkdir -p $(MANPREFIX)/man1
	@sed -e "s/<VERSION>/$(VERSION)/g" -e "s/<DATE>/$(DATE)/g" < $(DOC_DIR)/likwid-topology.1 > $(MANPREFIX)/man1/likwid-topology.1
	@sed -e "s/<VERSION>/$(VERSION)/g" -e "s/<DATE>/$(DATE)/g" < $(DOC_DIR)/likwid-features.1 > $(MANPREFIX)/man1/likwid-features.1
	@sed -e "s/<VERSION>/$(VERSION)/g" -e "s/<DATE>/$(DATE)/g" < $(DOC_DIR)/likwid-perfCtr.1 > $(MANPREFIX)/man1/likwid-perfCtr.1
	@sed -e "s/<VERSION>/$(VERSION)/g" -e "s/<DATE>/$(DATE)/g" < $(DOC_DIR)/likwid-pin.1 > $(MANPREFIX)/man1/likwid-pin.1
	@echo "===> INSTALL header to $(PREFIX)/include"
	@mkdir -p $(PREFIX)/include
	@cp -f src/includes/likwid.h  $(PREFIX)/include
	@cp -f src/includes/hdtraceLikwid.h  $(PREFIX)/include
	@chmod 644 $(MANPREFIX)/man1/likwid-*
	@echo "===> INSTALL libraries to $(PREFIX)/lib"
	@mkdir -p $(PREFIX)/lib
	@cp -f liblikwid* libhdtraceLikwid* $(PREFIX)/lib
	@chmod 755 $(PREFIX)/lib/$(PINLIB)


uninstall:
	@echo "===> REMOVING applications from $(PREFIX)/bin"
	@rm -f $(addprefix $(PREFIX)/bin/,$(APPS))
	@echo "===> REMOVING man pages from $(MANPREFIX)/man1"
	@rm -f $(addprefix $(MANPREFIX)/man1/,$(addsuffix  .1,$(APPS)))
	@echo "===> REMOVING libs from $(PREFIX)/lib"
	@rm -f $(PREFIX)/lib/$(TARGET_LIB) $(PREFIX)/lib/$(PINLIB) $(PREFIX)/lib/libhdtraceLikwid.so



