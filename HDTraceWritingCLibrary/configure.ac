#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.61)
AC_INIT([HDTraceCLibrary], [0svn20090617], [stephan.krempel@pvs.informatik.uni-heidelberg.de])
AC_CONFIG_SRCDIR([src/hdTopo.c])
AC_CONFIG_HEADER([src/pkgconfig.h])

# additional m4 files are found here:
AC_CONFIG_AUX_DIR([config])
AC_CONFIG_MACRO_DIR([config])

# Initialize automake
AM_INIT_AUTOMAKE([-Wall -Werror foreign 1.10])

# Checks for system capabilities
AC_USE_SYSTEM_EXTENSIONS
AC_SYS_LARGEFILE

# Checks for programs.
AC_PROG_CC_C99
# AC_PROG_RANLIB

# Enable libtool programming and disable shared libraries
AM_DISABLE_SHARED
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


# Checks for libraries.
# FIXME: Replace `main' with a function in `-lhdTrace':
# AC_CHECK_LIB([hdTrace], [main])

dnl ------------------------------------------------------------------------
dnl Check for C capabilities
dnl ------------------------------------------------------------------------

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([fcntl.h stdint.h stdlib.h string.h sys/time.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_C_INLINE
AC_TYPE_INT32_T
AC_TYPE_INT64_T
AC_TYPE_UINT8_T
AC_TYPE_UINT32_T
AC_TYPE_UINT64_T
AC_TYPE_OFF_T
AC_TYPE_SIZE_T
AC_TYPE_SSIZE_T
AC_HEADER_TIME

# Checks for library functions.
AC_FUNC_MALLOC
AC_FUNC_ALLOCA
AC_FUNC_MEMCMP
AC_FUNC_SELECT_ARGTYPES
AC_CHECK_FUNCS([gethostname gettimeofday isascii memset regcomp select strdup strerror])

SHAVE_INIT([config], [enable])

AC_CONFIG_FILES([config/shave
		 config/shave-libtool
		 Makefile
		 lib/Makefile
		 tools/Makefile
		 tests/Makefile])
AC_OUTPUT
