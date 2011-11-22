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

#define _GNU_SOURCE

#include <stdlib.h>

#include "c-character-classifier.h"

//typedef enum { kCccNormal, kCccPreprocessorDirective, kCccCharacterConstant, kCccString, kCccLineComment, kCccBlockComment } CccCharacterClass;

/*typedef struct {
	CccCharacterClass state;
	int atLineStart, inEscape;
	long backToNormal, blockLevel, callLevel, indexLevel;
} CCharacterClassifier;*/

CCharacterClassifier* ccc_make() {
	CCharacterClassifier* result = (CCharacterClassifier*)malloc(sizeof(CCharacterClassifier));
	if(result) ccc_init(result);
	return result;
}

void ccc_init(CCharacterClassifier* self) {
	self->state = kCccNormal;
	self->atLineStart = 1;
	self->inEscape = 0;
	self->backToNormal = 0;
	self->blockLevel = 0;
	self->callLevel = 0;
	self->indexLevel = 0;
}

void ccc_delete(CCharacterClassifier* self) {
	free(self);
}

CccCharacterClass ccc_classify(CCharacterClassifier* self, int curChar, int nextChar) {
	if(self->backToNormal) {
		self->backToNormal--;
		if(!self->backToNormal) self->state = kCccNormal;
	}
	if(!self->backToNormal) {
		switch(self->state) {
			case kCccNormal: {
				if(self->atLineStart && curChar == '#') self->state = kCccPreprocessorDirective;
				if(curChar == '/' && nextChar == '*') self->state = kCccBlockComment;
				if(curChar == '/' && nextChar == '/') self->state = kCccLineComment;
				if(curChar == '\'') self->state = kCccCharacterConstant;
				if(curChar == '\"') self->state = kCccString;
				if(curChar == '{') self->blockLevel++;
				if(curChar == '(') self->callLevel++;
				if(curChar == '[') self->indexLevel++;
				if(curChar == '}') self->blockLevel--;
				if(curChar == ')') self->callLevel--;
				if(curChar == ']') self->indexLevel--;
			} break; case kCccPreprocessorDirective: {
				if(!(self->inEscape) && curChar == '\n') self->state = kCccNormal;
			} break; case kCccLineComment: {
				if(!(self->inEscape) && curChar == '\n') self->state = kCccNormal;
			} break; case kCccCharacterConstant: {
				if(!(self->inEscape) && curChar == '\'') self->backToNormal = 1;
			} break; case kCccString: {
				if(!(self->inEscape) && curChar == '\"') self->backToNormal = 1;
			} break; case kCccBlockComment: {
				if(curChar == '*' && nextChar == '/') self->backToNormal = 2;
			} break;
		}
	}
	if(curChar != '\t' && curChar != ' ') self->atLineStart = 0;
	if(!(self->inEscape) && curChar == '\n') self->atLineStart = 1;
	if(self->inEscape) {
		self->inEscape = 0;
	} else {
		self->inEscape = (curChar == '\\');
	}
	return self->state;
}

void ccc_getLevels(CCharacterClassifier* self, long* blockLevel, long* callLevel, long* indexLevel) {
	if(blockLevel) *blockLevel = self->blockLevel;
	if(callLevel) *callLevel = self->callLevel;
	if(indexLevel) *indexLevel = self->indexLevel;
}

long ccc_getBlockLevel(CCharacterClassifier* self) { return self->blockLevel; }
long ccc_getCallLevel(CCharacterClassifier* self) { return self->callLevel; }
long ccc_getIndexLevel(CCharacterClassifier* self) { return self->indexLevel; }
