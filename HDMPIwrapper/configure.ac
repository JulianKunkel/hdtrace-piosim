#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
# Initialy manufactured by autoscan, refined by JK

AC_PREREQ(2.61)
AC_INIT(FULL-PACKAGE-NAME, VERSION, BUG-REPORT-ADDRESS)

AC_CONFIG_SRCDIR([src/])
AC_CONFIG_HEADER([include/hdmpi-wrapper-pkg.h])

# additional m4 files are found here:
AC_CONFIG_AUX_DIR([config])
AC_CONFIG_MACRO_DIR([config])

# Initialize automake
AM_INIT_AUTOMAKE([-Wall -Werror foreign 1.10])

# Enable libtool programming and disable shared libraries
AM_DISABLE_SHARED
AM_PROG_LIBTOOL

AS_BOURNE_COMPATIBLE

# Checks for programs.
AC_PROG_CC
AC_PROG_MAKE_SET
AC_PROG_CC_C99

# Checks for libraries.

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([limits.h stdlib.h string.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_C_INLINE
AC_TYPE_SIZE_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_FUNC_VPRINTF
AC_CHECK_FUNCS([gethostname strrchr])

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
	      
AC_ARG_ENABLE(asserts,
	      AC_HELP_STRING([--disable-asserts],[disable assertion code (should produce faster code)]),
	      [
	       if test "$enableval" == "no"
	       then
		       CPPFLAGS="${CPPFLAGS} -DNDEBUG"
	       fi
	      ]
	     )


       
# PVFS2 path:
dnl set the pvfs2 library path
AC_ARG_WITH(hdPVFS2,
[  --with-hdPVFS2=path         Use modified (hd)-PVFS2 library installed in "path" to enable client-internal PVFS2 tracing],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-PVFS2 must be given a pathname)
    else
       CFLAGS="$CFLAGS -I$withval/include"
       CPPFLAGS="$CPPFLAGS -I$withval/include"
       LDFLAGS="$LDFLAGS -L$withval/lib"
       
       AC_DEFINE(ENABLE_PVFS2_INTERNAL_TRACING, 1, [Define if PVFS2 enabled tracing shall be used])

    fi
)

# HDTrace path:
dnl set the hdtrace library path
AC_ARG_WITH(hdtrace,
[  --with-hdtrace=path         Use HDTrace library installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-hdtrace must be given a pathname)
    else
       CFLAGS="$CFLAGS -I$withval/include"
       CPPFLAGS="$CPPFLAGS -I$withval/include"
       LDFLAGS="$LDFLAGS -L$withval/lib"
    fi
)

dnl set the hdtrace library path
AC_ARG_WITH(mpi,
[  --with-mpi=path         Use MPI library installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-mpi must be given a pathname)
    else
       CFLAGS+=" -I$withval/include"
       CPPFLAGS+=" -I$withval/include"
       LDFLAGS+=" -L$withval/lib"
    fi
)
AC_CHECK_HEADERS(mpi.h,, AC_MSG_ERROR(Cannot find mpi.h use --with-mpi))

LIBS="$LIBS -lhdStats -lhdTrace"

# Check whether we can build with hdtrace.
# http://www.gnu.org/software/autoconf/manual/autoconf.html#AC_005fCHECK_005fHEADERS
AC_CHECK_HEADERS(hdTrace.h,, AC_MSG_ERROR(Cannot find hdTrace.h use --with-hdtrace))
AC_CHECK_HEADERS(hdStats.h,, AC_MSG_ERROR(Cannot find hdStats.h use --with-hdtrace))

AC_TRY_LINK([
		#include <stdio.h>
		#include <hdTrace.h>
		#include <hdStats.h>
		],
		[return (0);], , [AC_MSG_ERROR("Cannot link with hdtrace")])

dnl set the performancetrace library path
AC_ARG_WITH(hdutiltrace,
[  --with-hdutiltrace=path         Use libRUT installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-hdutiltrace must be given a pathname)
    else
       CFLAGS="$CFLAGS -I$withval/include"
       CPPFLAGS="$CPPFLAGS -I$withval/include"

       AC_DEFINE(ENABLE_UTILIZATION_TRACE, 1, [Define if resources utilization tracing shall be enabled])

       LDFLAGS="-L$withval/lib $LDFLAGS"
       AC_TRY_LINK([
		#include <RUT.h>
		],
		[return (0);], , [AC_MSG_ERROR("Cannot link with libRUT")])   
    fi
)

dnl set the power trace library path
AC_ARG_WITH(hdpowertrace,
[  --with-hdpowertrace=path         Use libPT installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-hdpowertrace must be given a pathname)
    else
       CFLAGS="$CFLAGS -I$withval/include"
       CPPFLAGS="$CPPFLAGS -I$withval/include"

       AC_DEFINE(ENABLE_POWER_TRACE, 1, [Define if power tracing shall be enabled])

       LDFLAGS="-L$withval/lib $LDFLAGS"
       AC_TRY_LINK([
		#include <pt.h>
		],
		[return (0);], , [AC_MSG_ERROR("Cannot link with libPT")])   
    fi
)



ENABLE_FUNCTION_WRAPPER=1
AC_ARG_ENABLE(function-wrapper,
	      AC_HELP_STRING([--disable-function-wrapper],[Do not wrap common MPI functions, just trigger PVFS2 and PTL upon MPI_init and MPI_finalize]),
	      [
                ENABLE_FUNCTION_WRAPPER=0
	      ]
	     )

if test "${ENABLE_FUNCTION_WRAPPER}" == "1"
then
  AC_DEFINE(ENABLE_FUNCTION_WRAPPER, 1, [Enable the common MPI function call wrapper and tracing])
fi

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


CFLAGS+=" $(getconf LFS_CFLAGS)"


# create sub directories in build folder:
AS_MKDIR_P(doc/doxygen)
AS_MKDIR_P(src)
AS_MKDIR_P(include)
AS_MKDIR_P(lib)


SHAVE_INIT([config], [enable])

AC_CONFIG_FILES([config/shave
		 config/shave-libtool
		 Makefile
		 lib/Makefile
		 src/Makefile
         test/HDTests/Makefile
         test/Makefile])

AC_OUTPUT