/*
* Copyright (c) 2011 Nathanael Hübbe
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
* OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
*/

#ifndef C_FUNCTION_DECLARATION_DETECTOR_INCLUDE_GUARD
#define C_FUNCTION_DECLARATION_DETECTOR_INCLUDE_GUARD

#include <stdio.h>

#include "c-character-classifier.h"	//pre 4.6 gcc does not allow typedef redefinition (even matching ones).
//typedef struct CCharacterClassifier CCharacterClassifier;

typedef enum {kCfddBeforeParameterList, kCfddInParameterList, kCfddAfterParameterList, kCfddAtEnd, kCfddBusted} CfddState;

///A class that extracts function declarations from function definitions in C-code. It recognizes the format:
//"return type and function name"("parameter list"){"code"}
//The whole statement is discarded, if "return type and function name" contains the "static" keyword.
//"parameter list" is taken exactly as it is.
//"code" is discarded.
//The whole statement is discarded, if there is a "(", "{", "\"", "'" or ";" at an unexpected location.
//All whitespaces encountered are collapsed to single spaces or removed.
//So all output statements are single lines of the form:
//"return type and function name"("parameter list");
struct CFunctionDeclarationDetector {
	FILE* curStream, *returnStream, *outputStream;	//curStream/curString accumulate the current part, return Stream accumulates the current output line until it is either copied to the outputStream or discarded.
	char* curString, *returnString;
	int nextChar, inWhitespace, spaceNeeded;	//nextChar buffers the last char pushed in, so that pairs of chars can be passed on to the classifier.
	size_t curStringSize, returnStringSize;
	CfddState state;
	CCharacterClassifier* classifier;
};
typedef struct CFunctionDeclarationDetector CFunctionDeclarationDetector;

CFunctionDeclarationDetector* cfdd_make(FILE* outputStream);
void cfdd_init(CFunctionDeclarationDetector* self, FILE* outputStream);
void cfdd_pushChar(CFunctionDeclarationDetector* self, int curChar);
void cfdd_delete(CFunctionDeclarationDetector* self);

#endif
