/* A Bison parser, made by GNU Bison 1.875c.  */

/* Skeleton parser for Yacc-like parsing with Bison,
   Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003 Free Software Foundation, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

/* As a special exception, when this file is copied by Bison into a
   Bison output file, you may use that output file without restriction.
   This special exception was added by the Free Software Foundation
   in version 1.24 of Bison.  */

/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     MACHINE = 258,
     NESTED = 259,
     STATE = 260,
     RUN = 261,
     PJMP = 262,
     JUMP = 263,
     STATE_RETURN = 264,
     STATE_TERMINATE = 265,
     SUCCESS = 266,
     DEFAULT = 267,
     LBRACE = 268,
     RBRACE = 269,
     SEMICOLON = 270,
     ARROW = 271,
     IDENTIFIER = 272
   };
#endif
#define MACHINE 258
#define NESTED 259
#define STATE 260
#define RUN 261
#define PJMP 262
#define JUMP 263
#define STATE_RETURN 264
#define STATE_TERMINATE 265
#define SUCCESS 266
#define DEFAULT 267
#define LBRACE 268
#define RBRACE 269
#define SEMICOLON 270
#define ARROW 271
#define IDENTIFIER 272




#if ! defined (YYSTYPE) && ! defined (YYSTYPE_IS_DECLARED)
#line 55 "src/common/statecomp/parser.y"
typedef union YYSTYPE {
    int i;
    char *c;
} YYSTYPE;
/* Line 1268 of yacc.c.  */
#line 76 "src/common/statecomp/parser.h"
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
# define YYSTYPE_IS_TRIVIAL 1
#endif

extern YYSTYPE yylval;



