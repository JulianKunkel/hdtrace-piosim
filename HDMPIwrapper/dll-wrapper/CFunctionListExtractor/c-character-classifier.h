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

#ifndef C_CHARACTER_CLASSIFIER_INCLUDE_GUARD
#define C_CHARACTER_CLASSIFIER_INCLUDE_GUARD


typedef enum { kCccNormal, kCccPreprocessorDirective, kCccCharacterConstant, kCccString, kCccLineComment, kCccBlockComment } CccCharacterClass;

///This class is designed to do a rudimentary syntax analysis of C-code, classifying each character correctly into the classes: preprocessor directive, character constant, string and comment. After initialization pass each character to ccc_classify() in sequence. This information can be used to strip the code off some or all of these character classes.
struct CCharacterClassifier {
	CccCharacterClass state;
	int atLineStart, inEscape;
	long backToNormal, blockLevel, callLevel, indexLevel;
};
typedef struct CCharacterClassifier CCharacterClassifier;

CCharacterClassifier* ccc_make();
void ccc_init(CCharacterClassifier* self);
void ccc_delete(CCharacterClassifier* self);

///The core method of CCharacterClassifier. Give it a character and the next one, and it will tell you which class the character belongs to. Make sure to pass every single character in correct sequence.
CccCharacterClass ccc_classify(CCharacterClassifier* self, int curChar, int nextChar);
///Get the current bracket levels. Outside everything is (0,0,0). Apart from self, all parameters are pure output parameters and may be null.
void ccc_getLevels(CCharacterClassifier* self, long* blockLevel, long* callLevel, long* indexLevel);
long ccc_getBlockLevel(CCharacterClassifier* self);
long ccc_getCallLevel(CCharacterClassifier* self);
long ccc_getIndexLevel(CCharacterClassifier* self);

#endif
