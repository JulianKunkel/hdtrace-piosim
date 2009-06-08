#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
# Initialy manufactured by autoscan, refined by JK

AC_PREREQ(2.61)
AC_INIT(FULL-PACKAGE-NAME, VERSION, BUG-REPORT-ADDRESS)
#AC_CONFIG_SRCDIR([src/common.c])

AS_BOURNE_COMPATIBLE

# Checks for programs.
AC_PROG_CC
AC_PROG_MAKE_SET

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

AC_CONFIG_FILES([Makefile
                 test/HDTests/Makefile
                 test/Makefile])

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

# add glib2 stuff
LIBS="$LIBS $(pkg-config --libs glib-2.0)"
CFLAGS="$CFLAGS $(pkg-config --cflags glib-2.0)"
CPPFLAGS="$CPPFLAGS $(pkg-config --cflags glib-2.0)"


AC_ARG_WITH(glib,
[  --with-glib=path         Use GLIB2 library installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-glib must be given a pathname)
    else
       CFLAGS+=" -I$withval/include"
       CPPFLAGS+=" -I$withval/include"
       LDFLAGS+=" -L$withval/lib"
    fi
)
AC_CHECK_HEADERS(glib.h,, AC_MSG_ERROR(Cannot find glib.h use --with-glib))


AC_TRY_LINK([
		#include <stdio.h>
		#include <hdTrace.h>
		#include <hdStats.h>
		],
		[return (0);], , [AC_MSG_ERROR("Cannot link with hdtrace")])


dnl set the performancetrace library path
AC_ARG_WITH(hdperftrace,
[  --with-hdperftrace=path         Use HDPerformanceTrace library installed in "path"],
    if test "x$withval" = "xyes" ; then
           AC_MSG_ERROR(--with-hdperftrace must be given a pathname)
    else
       CFLAGS="$CFLAGS -I$withval/include -DUSE_PERFORMANCE_TRACE"
       CPPFLAGS="$CPPFLAGS -I$withval/include -DUSE_PERFORMANCE_TRACE"
       LDFLAGS="-L$withval/lib $LDFLAGS"
       AC_TRY_LINK([
		#include <PTL.h>
		],
		[return (0);], , [AC_MSG_ERROR("Cannot link with performance trace library")])   
    fi
)

CFLAGS+=" $(getconf LFS_CFLAGS)"

# create sub directories in build folder:
AS_MKDIR_P(doc/doxygen)
AS_MKDIR_P(src)
AS_MKDIR_P(include)
AS_MKDIR_P(lib)

AC_OUTPUT
