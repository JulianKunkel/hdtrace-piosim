#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
#
# $Id$
#

AC_PREREQ(2.61)
AC_INIT([hdStatsDaemon], [beta], [stephan.krempel@pvs.informatik.uni-heidelberg.de])
AC_CONFIG_SRCDIR([src/hdStatsDaemon.c])
AC_CONFIG_HEADER([src/hdStatsDaemon-pkg.h])

# Initialize automake
AM_INIT_AUTOMAKE([-Wall -Werror foreign 1.10])

AC_USE_SYSTEM_EXTENSIONS

# Checks for programs.
AC_PROG_CC_C99
# This sucks cause it uses gnu99 instead of real C99
#CC=`echo $CC | sed -e 's/-std=gnu99/-std=c99/'`

AC_PROG_INSTALL
AC_PROG_AWK
AC_PROG_MKDIR_P

AM_PROG_LIBTOOL

dnl ----------------------------------------------------------------------
dnl Define configure commandline flags
dnl ----------------------------------------------------------------------

# Enable debugging flags when enabled
AC_ARG_ENABLE(debugging,
	      AC_HELP_STRING([--enable-debugging],[enable debugging flags and output (overrides CFLAGS argument)]),
	      [
	       CFLAGS="-g -O0 -ggdb -DDEBUG"
	      ]
	     )

# Enable compiler warnings when enabled and using gcc
AC_ARG_ENABLE(warnings,
	      AC_HELP_STRING([--enable-warnings],[enable lots of compiler warnings (only with GCC)]),
	      [
	       if test "$GCC" != "yes"
	       then
		       CFLAGS="${CFLAGS} -pedantic -Wall -Wextra -Waggregate-return -Wcast-align -Wcast-qual \
		               -Wconversion -Wfloat-equal -Wformat=2 -Winit-self -Winline -Wmissing-declarations \
			       -Wmissing-format-attribute -Wmissing-include-dirs -Wmissing-noreturn \
			       -Wmissing-prototypes -Wnested-externs -Wold-style-definition -Wredundant-decls \
			       -Wshadow -Wstrict-prototypes -Wswitch-default -Wswitch-enum -Wundef -Wwrite-strings"
		else
			AC_MSG_WARN(["--enable-warnings" only does something when using gcc])
		fi
	       ]
	      )

# Disable assert macro for faster code
AC_ARG_ENABLE(asserts,
	      AC_HELP_STRING([--disable-asserts],[disable assertion code (should produce faster code)]),
	      [
	       if test "$enableval" == "no"
	       then
		       CPPFLAGS="${CPPFLAGS} -DNDEBUG"
	       fi
	      ]
	     )


dnl ----------------------------------------------------------------------
dnl Check for HDTraceWritingLibrary
dnl ----------------------------------------------------------------------

ac_save_LDFLAGS=$LDFLAGS
LDFLAGS="$LDFLAGS $HDTWLIB_LDFLAGS"
AC_CHECK_LIB([hdStats], [hdS_createGroup], [HDTWLIB_PRESENT=YES], [HDTWLIB_PRESENT=NO])
LDFLAGS=$ac_save_LDFLAGS


if test "$HDTWLIB_PRESENT" = "YES"
then
	HDTWLIB_CFLAGS=""
	HDTWLIB_LIBS="-lhdStats"
	HDTWLIB_LDADD=""
fi


AC_ARG_WITH(hdtwlib-static,
	    AC_HELP_STRING([--with-hdtwlib-static=prefix],[use static hdtwlib tree (prefix is REQUIRED)]),
	    [
	     if ! echo "${withval}" | grep '^/'
	     then
	         withval="$(pwd)/${withval}"
	     fi
	     HDTWLIB_STATIC_LIBHDSTATS_A="${withval}/lib/libhdStats.a"
	     HDTWLIB_STATIC_INCLUDE="${withval}/include"
	     if test -f ${HDTWLIB_STATIC_LIBHDSTATS_A}
    	     then
		     if test -f "${HDTWLIB_STATIC_INCLUDE}/hdStats.h"
		     then
			     HDTWLIB_CFLAGS="-I${HDTWLIB_STATIC_INCLUDE}"
			     HDTWLIB_LDADD="${HDTWLIB_STATIC_LIBHDSTATS_A}"
			     HDTWLIB_LIBS=""
			     HDTWLIB_PRESENT=YES
			     AC_MSG_NOTICE([assuming static hdtwlib in ${withval} is good...])
		     fi
	     fi
	    ]
	   )

if test "$HDTWLIB_PRESENT" != "YES"
then
	AC_MSG_ERROR([Cannot find hdStats library])
fi

AC_SUBST(HDTWLIB_CFLAGS)
AC_SUBST(HDTWLIB_LDFLAGS)
AC_SUBST(HDTWLIB_LIBS)
AC_SUBST(HDTWLIB_LDADD)


dnl ----------------------------------------------------------------------
dnl Check for PowerTracer Library
dnl ----------------------------------------------------------------------


ac_save_LDFLAGS=$LDFLAGS
LDFLAGS="$LDFLAGS $PT_LDFLAGS"
AC_CHECK_LIB([pt], [pt_createTrace], [PT_PRESENT=YES], [PT_PRESENT=NO])
LDFLAGS=$ac_save_LDFLAGS


if test "$PT_PRESENT" = "YES"
then
	PT_CFLAGS=""
	PT_LIBS="-lpt"
	PT_LDADD=""
fi


AC_ARG_WITH(pt-static,
	    AC_HELP_STRING([--with-pt-static=prefix],[use static power tracer tree (prefix is REQUIRED)]),
	    [
	     if ! echo "${withval}" | grep '^/'
	     then
	         withval="$(pwd)/${withval}"
	     fi
	     PT_STATIC_LIBPT_A="${withval}/lib/libpt.a"
	     PT_STATIC_INCLUDE="${withval}/include"
	     if test -f ${PT_STATIC_LIBPT_A}
    	     then
		     if test -f "${PT_STATIC_INCLUDE}/pt.h"
		     then
			     PT_CFLAGS="-I${PT_STATIC_INCLUDE}"
			     PT_LDADD="${PT_STATIC_LIBPT_A}"
			     PT_LIBS=""
			     PT_PRESENT=YES
			     AC_MSG_NOTICE([assuming static pt in ${withval} is good...])
		     fi
	     fi
	    ]
	   )

if test "$PT_PRESENT" != "YES"
then
	AC_MSG_ERROR([Cannot find power tracer library])
fi

AC_SUBST(PT_CFLAGS)
AC_SUBST(PT_LDFLAGS)
AC_SUBST(PT_LIBS)
AC_SUBST(PT_LDADD)


dnl ----------------------------------------------------------------------
dnl Check for a suitable glib
dnl ----------------------------------------------------------------------

AM_PATH_GLIB_2_0([2.16.6],[GLIB_PRESENT=YES],[GLIB_PRESENT=NO],[gthread])


dnl ----------------------------------------------------------------------
dnl Check for libgtop
dnl ----------------------------------------------------------------------

GLIBTOP_PRESENT=NO

# we trust in pkg-config
if pkg-config libgtop-2.0
then
	GLIBTOP_CFLAGS="$(pkg-config --cflags libgtop-2.0)"
	GLIBTOP_LIBS="$(pkg-config --libs libgtop-2.0)"
	GLIBTOP_PRESENT=YES
fi


dnl ----------------------------------------------------------------------
dnl Check for PowerTracer Library
dnl ----------------------------------------------------------------------


ac_save_LDFLAGS=$LDFLAGS
LDFLAGS="$LDFLAGS $PTL_LDFLAGS"
AC_CHECK_LIB([PTL], [ptl_createTrace], [PTL_PRESENT=YES], [PTL_PRESENT=NO])
LDFLAGS=$ac_save_LDFLAGS


if test "$PTL_PRESENT" = "YES"
then
	PTL_CFLAGS=""
	PTL_LIBS="-lPTL"
	PTL_LDADD=""
fi


AC_ARG_WITH(ptl-static,
	    AC_HELP_STRING([--with-ptl-static=prefix],[use static PTL tree (prefix is REQUIRED)]),
	    [
	     if ! echo "${withval}" | grep '^/'
	     then
	         withval="$(pwd)/${withval}"
	     fi
	     PTL_STATIC_LIBPTL_A="${withval}/lib/libPTL.a"
	     PTL_STATIC_INCLUDE="${withval}/include"
	     if test -f ${PTL_STATIC_LIBPTL_A}
    	     then
		     if test -f "${PTL_STATIC_INCLUDE}/PTL.h"
		     then
			     PTL_CFLAGS="-I${PTL_STATIC_INCLUDE}"
			     PTL_LDADD="${PTL_STATIC_LIBPTL_A}"
			     PTL_LIBS=""
			     PTL_PRESENT=YES
			     AC_MSG_NOTICE([assuming static PTL in ${withval} is good...])
		     fi
	     fi
	    ]
	   )

if test "$PTL_PRESENT" != "YES"
then
	AC_MSG_ERROR([Cannot find performance tracer library])
fi

AC_SUBST(PTL_CFLAGS)
AC_SUBST(PTL_LDFLAGS)
AC_SUBST(PTL_LIBS)
AC_SUBST(PTL_LDADD)

CFLAGS+=" $GLIB_CFLAGS $GLIBTOP_CFLAGS $HDTWLIB_CFLAGS $PT_CFLAGS $PTL_CFLAGS"
LDFLAGS+=" $GLIB_LIBS $GLIBTOP_LIBS $HDTWLIB_LDFLAGS $PT_LDFLAGS $PTL_LDFLAGS"
LIBS+=" $HDTWLIB_LIBS $PT_LIBS $PTL_LIBS"
LDADD=" $PT_LDADD $PTL_LDADD $HDTWLIB_LDADD $GLIBTOP_LDADD $GLIB_LDADD"
AC_SUBST(LDADD)


dnl ------------------------------------------------------------------------
dnl Check for C capabilities
dnl ------------------------------------------------------------------------

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([arpa/inet.h fcntl.h stdlib.h string.h strings.h sys/time.h termios.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_TYPE_SIZE_T
AC_HEADER_TIME
AC_HEADER_STDBOOL
AC_TYPE_UINT32_T

# Checks for library functions.
AC_FUNC_SELECT_ARGTYPES
AC_CHECK_FUNCS([mkfifo select])

AC_CONFIG_FILES([Makefile
                 bin/Makefile])
                 
AC_OUTPUT
