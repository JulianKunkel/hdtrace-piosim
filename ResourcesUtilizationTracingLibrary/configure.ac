#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
#
# $Id$

AC_PREREQ(2.61)
AC_INIT([ResourcesUtilizationTracingLibrary], [0svn20090617], [stephan.krempel@pvs.informatik.uni-heidelberg.de])
AC_CONFIG_SRCDIR([src/RUT.c])
AC_CONFIG_HEADER([src/RUT-pkg.h])

# Initialize automake
AM_INIT_AUTOMAKE([-Wall -Werror foreign 1.10])

# Checks for system capabilities
AC_USE_SYSTEM_EXTENSIONS
AC_SYS_LARGEFILE

# Checks for programs.
AC_PROG_CC_C99
AC_PROG_LIBTOOL

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
dnl Check for a suitable glib
dnl ----------------------------------------------------------------------

GLIB_PRESENT=NO
GLIB_LDADD=

AM_PATH_GLIB_2_0([2.16.6],[GLIB_PRESENT=YES],[GLIB_PRESENT=NO],[gthread])

AC_ARG_WITH(glib-static,
	    AC_HELP_STRING([--with-glib-static=prefix],[use static glib tree (prefix is REQUIRED)]),
	    [
	     if ! echo "${withval}" | grep '^/'
	     then
	         withval="$(pwd)/${withval}"
	     fi
	     GLIB_STATIC_LIBGLIB2_A="${withval}/lib/libglib-2.0.a"
	     GLIB_STATIC_LIBGTHREAD2_A="${withval}/lib/libgthread-2.0.a"
	     GLIB_STATIC_INCLUDE="${withval}/include/glib-2.0"
	     GLIB_STATIC_LIBINCLUDE="${withval}/lib/glib-2.0/include"
	     if test -f ${GLIB_STATIC_LIBGLIB2_A} -a -f ${GLIB_STATIC_LIBGTHREAD2_A}
    	     then
		     if test -f "${GLIB_STATIC_INCLUDE}/glib.h"
		     then
			     GLIB_LDADD="${GLIB_STATIC_LIBGTHREAD2_A} ${GLIB_STATIC_LIBGLIB2_A}"
			     GLIB_CFLAGS="-I${GLIB_STATIC_INCLUDE} -I${GLIB_STATIC_LIBINCLUDE}"
			     # TODO: Do we need to do some pthreads stuff here?
			     AC_SUBST(GLIB_LIBS)   # override the one by AM_PATH_GLIB_2_0
			     AC_SUBST(GLIB_CFLAGS)
			     GLIB_PRESENT=YES
			     AC_MSG_NOTICE([assuming static glib in ${withval} is good...])
		     fi
	     fi
	    ]
	   )

if test "$GLIB_PRESENT" != "YES"
then
	AC_MSG_ERROR([Cannot find a suitable glib2 (>= 2.16.6)])
fi

AC_SUBST(GLIB_LDADD)

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

AC_ARG_WITH(glibtop-static,
	    AC_HELP_STRING([--with-glibtop-static=prefix],[use static glibtop tree (prefix is REQUIRED)]),
	    [
	     if ! echo "${withval}" | grep '^/'
	     then
	         withval="$(pwd)/${withval}"
	     fi
	     GLIBTOP_STATIC_LIBGTOP2_A="${withval}/lib/libgtop-2.0.a"
	     GLIBTOP_STATIC_INCLUDE="${withval}/include/libgtop-2.0"
	     if test -f ${GLIBTOP_STATIC_LIBGTOP2_A}
    	 then
		     if test -f "${GLIBTOP_STATIC_INCLUDE}/glibtop.h"
		     then
			     GLIBTOP_LDADD="${GLIBTOP_STATIC_LIBGTOP2_A} ${GLIB_LDADD}"
			     GLIBTOP_CFLAGS="-I${GLIBTOP_STATIC_INCLUDE} ${GLIB_CFLAGS}"
			     GLIBTOP_LIBS="${GLIB_LIBS}"
			     GLIBTOP_PRESENT=YES
			     AC_MSG_NOTICE([assuming static glibtop in ${withval} is good...])
		     fi
	     fi
	    ]
	   )

if test "$GLIBTOP_PRESENT" != "YES"
then
	AC_MSG_ERROR([Cannot find gtoplib2])
else
	AC_MSG_NOTICE([glibtop2 found but version not checked])
fi

AC_SUBST(GLIBTOP_CFLAGS)
AC_SUBST(GLIBTOP_LIBS)
AC_SUBST(GLIBTOP_LDADD)

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


dnl ------------------------------------------------------------------------
dnl Check for C capabilities
dnl ------------------------------------------------------------------------

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([stdint.h stdlib.h string.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_TYPE_SIZE_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_CHECK_FUNCS([bzero])

AC_CONFIG_FILES([Makefile
		 lib/Makefile
		 tests/Makefile
		 examples/Makefile])
AC_OUTPUT
