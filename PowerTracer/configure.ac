#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
#
# $Id$
#

AC_PREREQ(2.61)
AC_INIT([PowerTracer], [0.9], [stephan.krempel@pvs.informatik.uni-heidelberg.de])
AC_CONFIG_SRCDIR([src/pt.c])
AC_CONFIG_HEADER([src/pkgconfig.h])

# Initialize automake
AM_INIT_AUTOMAKE([-Wall -Werror foreign 1.10])

AC_USE_SYSTEM_EXTENSIONS

# Checks for programs.
AC_PROG_CC_C99
AC_PROG_INSTALL
AC_PROG_AWK
AC_PROG_MKDIR_P
AC_PROG_LIBTOOL


# Checks for libraries.
AC_CHECK_LIB([pthread], [pthread_create])


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
AC_FUNC_MALLOC
AC_FUNC_REALLOC
AC_FUNC_SELECT_ARGTYPES
AC_CHECK_FUNCS([gettimeofday memset regcomp select strerror])

AC_CONFIG_FILES([Makefile
                 lib/Makefile
                 bin/Makefile
                 examples/Makefile])
AC_OUTPUT
