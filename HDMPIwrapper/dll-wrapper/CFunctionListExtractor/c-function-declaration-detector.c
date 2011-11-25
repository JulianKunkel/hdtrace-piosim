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
#include <string.h>

#include "c-function-declaration-detector.h"
#include "c-character-classifier.h"

/*
typedef enum {kCfddBeforeParameterList, kCfddInParameterList, kCfddAfterParameterList, kCfddAtEnd, kCfddBusted} CfddState;

typedef struct {
	FILE* curStream, *returnStream, *outputStream;
	char* curString, *returnString;
	int nextChar, inWhitespace, spaceNeeded;
	size_t curStringSize, returnStringSize;
	CfddState state;
	CCharacterClassifier* classifier;
} CFunctionDeclarationDetector;
*/

CFunctionDeclarationDetector* cfdd_make(FILE* outputStream) {
	CFunctionDeclarationDetector* result = (CFunctionDeclarationDetector*)malloc(sizeof(CFunctionDeclarationDetector));
	if(result) cfdd_init(result, outputStream);
	return result;
}

void
cfdd_init(   CFunctionDeclarationDetector*   self, FILE* outputStream   )
 {
	self->outputStream = outputStream;
	self->nextChar = EOF;
	self->state = kCfddBeforeParameterList;
	self->inWhitespace = 1;
	self->spaceNeeded = 0;
	self->curString = self->returnString = NULL;
	self->curStringSize = self->returnStringSize = 0l;
	self->curStream = open_memstream(&self->curString, &self->curStringSize);
	self->returnStream = open_memstream(&self->returnString, &self->returnStringSize);
	self->classifier = ccc_make();
}

//Private method. Appends nextChar to curStream iff it is no extra whitespace. Also all whitespace is converted to spaces.
static void cfdd_writeChar(CFunctionDeclarationDetector* self) {
	if(self->nextChar == EOF) return;
	if(self->nextChar == ' ' || self->nextChar == '\t' || self->nextChar == '\n' || self->nextChar == '\r') {
		self->inWhitespace = 1;
	} else if(self->nextChar == ')') {	//Ignore all space in front of a ')'.
		putc(')', self->curStream);
		self->inWhitespace = self->spaceNeeded = 0;	//Preserve whitespace after a ')'.
	} else {
		if(self->inWhitespace && self->spaceNeeded) {
			putc(' ', self->curStream);
		}
		putc(self->nextChar, self->curStream);
		self->inWhitespace = 0;
		self->spaceNeeded = 1;	//Preserve whitespace following other characters,
		if(self->nextChar == '(' || self->nextChar == '[') {	//iff they are not opening brackets.
			self->inWhitespace = 1;
			self->spaceNeeded = 0;
		}
	}
}

//Private method. Appends the contents of curStream to returnStream and resets curStream.
static void cfdd_flushCurStream(CFunctionDeclarationDetector* self) {
	fclose(self->curStream);
	fprintf(self->returnStream, "%s", self->curString);
	free(self->curString);
	self->curString = NULL;
	self->curStringSize = 0l;
	self->curStream = open_memstream(&self->curString, &self->curStringSize);
}

//Private method. Appends the contents of curStream to returnStream and resets curStream.
static void cfdd_resetCurStream(CFunctionDeclarationDetector* self) {
	fclose(self->curStream);
	free(self->curString);
	self->curString = NULL;
	self->curStringSize = 0l;
	self->curStream = open_memstream(&self->curString, &self->curStringSize);
}

//Private method. Appends the contents of returStream to outputStream, adds the string ";\n" and resets both curStream and returnStream.
static void cfdd_flushReturnStream(CFunctionDeclarationDetector* self) {
	fclose(self->returnStream);
	fprintf(self->outputStream, "%s;\n", self->returnString);
	free(self->curString);
	free(self->returnString);
	self->curString = self->returnString = NULL;
	self->curStringSize = self->returnStringSize = 0l;
	self->curStream = open_memstream(&self->curString, &self->curStringSize);
	self->returnStream = open_memstream(&self->returnString, &self->returnStringSize);
	self->inWhitespace = 1;
	self->spaceNeeded = 0;
}

//Private method. Resets both curStream and returnStream without making any output.
static void cfdd_resetStreams(CFunctionDeclarationDetector* self) {
	fclose(self->curStream);
	fclose(self->returnStream);
	free(self->curString);
	free(self->returnString);
	self->curString = self->returnString = NULL;
	self->curStringSize = self->returnStringSize = 0l;
	self->curStream = open_memstream(&self->curString, &self->curStringSize);
	self->returnStream = open_memstream(&self->returnString, &self->returnStringSize);
	self->inWhitespace = 1;
	self->spaceNeeded = 0;
}

void cfdd_pushChar(CFunctionDeclarationDetector* self, int curChar) {
	int ignoreChar = 0;
	if(self->nextChar == EOF) ignoreChar = 1;	//Ignore the first call, we need to see the second character first.
	if(!ignoreChar) {
		long oldBlockLevel = ccc_getBlockLevel(self->classifier);
		CccCharacterClass cc = ccc_classify(self->classifier, self->nextChar, curChar);
		switch(self->state) {
			case kCfddBeforeParameterList: {
				switch (cc) {
					case kCccNormal: {
						if(self->nextChar == ' ' || self->nextChar == '\t' || self->nextChar == '\n' || self->nextChar == '\r') {
							//Flush after every word ...
							fflush(self->curStream);
							if(strstr(self->curString, "extern")) {	//... so that we can filter out the extern keyword.
								cfdd_resetCurStream(self);
								self->inWhitespace = 1;
								self->spaceNeeded = 1;	//Preserve a space. Usually this is an extra space, but it may be the only one, so it is needed. And I don't think it's worthwhile to check for these conditions.
							}
							cfdd_flushCurStream(self);
						}
						if(self->nextChar == '{') {
							self->state = kCfddBusted;
						}
						if(self->nextChar == '(') {
							self->state = kCfddInParameterList;
							//Check if this statement should be ignored altogether.
							cfdd_flushCurStream(self);
							fflush(self->returnStream);
							if(strstr(self->returnString, "static")
								|| strstr(self->returnString, "inline"))
							{
								self->state = kCfddBusted;
							}
						}
						cfdd_writeChar(self);
					} break;

					case kCccCharacterConstant:
					case kCccString: {
						self->state = kCfddBusted;
					} break;

					case kCccPreprocessorDirective: break;
					case kCccLineComment: break;
					case kCccBlockComment: break;
				}
			} break; case kCfddInParameterList: {
				switch (cc) {
					case kCccNormal: {
						if(self->nextChar == '{') {
							self->state = kCfddBusted;
						}
						cfdd_writeChar(self);
						if(!ccc_getCallLevel(self->classifier)) {
							self->state = kCfddAfterParameterList;
							cfdd_flushCurStream(self);
						}
					} break;

					case kCccCharacterConstant:
					case kCccString: {
						self->state = kCfddBusted;
					} break;

					case kCccPreprocessorDirective: break;
					case kCccLineComment: break;
					case kCccBlockComment: break;
				}
			} break; case kCfddAfterParameterList: {
				switch (cc) {
					case kCccNormal: {
						if(oldBlockLevel) {
							if(!ccc_getBlockLevel(self->classifier)) {
								self->state = kCfddBeforeParameterList;
								cfdd_flushReturnStream(self);
							}
						} else {
							if(!(self->nextChar == ' ' || self->nextChar == '\t'
								|| self->nextChar == '\n' || self->nextChar == '\r'
								|| self->nextChar == '{'))
							{
								self->state = kCfddBusted;
							}
						}
					} break;

					case kCccCharacterConstant: break;
					case kCccString: break;
					case kCccPreprocessorDirective: break;
					case kCccLineComment: break;
					case kCccBlockComment: break;
				}
			} break; case kCfddBusted: default: {
			} break;
		}
		if(cc == kCccNormal) {
			if((!oldBlockLevel && self->nextChar == ';') || (oldBlockLevel && !ccc_getBlockLevel(self->classifier))) {
				self->state = kCfddBeforeParameterList;
				cfdd_resetStreams(self);
			}
		}
/*		if(self->nextChar == '\n') {
			switch(cc) {
				case kCccNormal: printf("%ld: Normal\n", ccc_getBlockLevel(self->classifier)); break;
				case kCccCharacterConstant: printf("%ld: Character constant\n", ccc_getBlockLevel(self->classifier)); break;
				case kCccString: printf("%ld: String\n", ccc_getBlockLevel(self->classifier)); break;
				case kCccPreprocessorDirective: printf("%ld: Preprocessor\n", ccc_getBlockLevel(self->classifier)); break;
				case kCccLineComment: printf("%ld: Line comment\n", ccc_getBlockLevel(self->classifier)); break;
				case kCccBlockComment: printf("%ld: Block comment\n", ccc_getBlockLevel(self->classifier)); break;
			}
		}*/
	}
	self->nextChar = curChar;
}

void cfdd_delete(CFunctionDeclarationDetector* self) {
	cfdd_pushChar(self, EOF);	//Process the last character.

	ccc_delete(self->classifier);
	fclose(self->curStream);
	fclose(self->returnStream);
	free(self->curString);
	free(self->returnString);
	free(self);
}
