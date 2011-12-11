#ifndef _READXML_H_
#define _READXML_H_

#include "expat.h"

#if defined(__amigaos__) && defined(__USE_INLINE__)
#endif
#ifdef XML_LARGE_SIZE
#if defined(XML_USE_MSC_EXTENSIONS) && _MSC_VER < 1400
#define XML_FMT_INT_MOD "I64"
#else
#define XML_FMT_INT_MOD "ll"
#endif
#else
#define XML_FMT_INT_MOD "l"
#endif

#endif
