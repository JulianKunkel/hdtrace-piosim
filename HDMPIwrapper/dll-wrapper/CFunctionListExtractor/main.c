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

#include <stdio.h>
#include <string.h>

#include "c-function-declaration-detector.h"

void printUsage(char* invocation) {
	printf("%s\n\n", invocation);
	printf("This program reads a C source code file from stdin and outputs a function\n");
	printf("declaration to stdout for each function definition encountered. The effect\n");
	printf("is like the creation of a header file from implementation code, but is much\n");
	printf("more restricted.\n");
}

#if(0)
//Wrapper for getc(stream). Completely strips all comments.
int getcStrippingComments(FILE* stream) {
	static int hadStar;
	int curChar = getc(stream);
	if(curChar == EOF) return EOF;
	if(curChar == '/') {	//Start of a comment?
		int nextChar = getc(stream);
		switch(nextChar) {
			case '/': {	//Line comment.
				int hadBackslash = 0;
				while(1) {
					nextChar = getc(stream);
					if(nextChar == EOF) return EOF;
					if(!hadBackslash && nextChar == '\n') return '\n';
					hadBackslash = (nextChar == '\\');
				}
			} break; case '*': {	//Block comment.
				int done = 0;
				hadStar = 0;
				while(!done && (nextChar = getc(stream)) != EOF) {
					if(hadStar) {
						if(nextChar == '/') {
							nextChar = getc(stream);
							done = 1;
						}
					}
					hadStar = (nextChar == '*');
				}
				return nextChar;
			} break; case EOF: {
			} break; default: {
				ungetc(nextChar, stream);
			}
		}
	}
	return curChar;
}

//Wrapper for getcStrippingComments(stream) that also kicks all strings and character constants from the stream.
int getcStrippingStringsAndComments(FILE* stream) {
	int curChar = getcStrippingComments(stream);
	if(curChar == EOF) return EOF;
	if(curChar == '\'') {	//Start of a character constant?
		while(1) {
			curChar = getcStrippingComments(stream);
			if(curChar == '\\') {
				//Ignore the backslash and whatever it's escaping.
				curChar = getcStrippingComments(stream);
			} else {
				//Have we reached the end of the character constant?
				if(curChar == '\'') return getcStrippingStringsAndComments(stream);
			}
			if(curChar == EOF) return EOF;
		}
	}
	if(curChar == '\"') {	//Start of a string?
		while(1) {
			curChar = getcStrippingComments(stream);
			if(curChar == '\\') {
				//Ignore the backslash and whatever it's escaping.
				curChar = getcStrippingComments(stream);
			} else {
				//Have we reached the end of the string?
				if(curChar == '\"') return getcStrippingStringsAndComments(stream);
			}
			if(curChar == EOF) return EOF;
		}
	}
	return curChar;
}

//Wrapper for getcStrippingStringsAndComments(stream). All top level blocks are collapsed to "{};" The semicolon is added, so that all remaining statements end in a semicolon. Of course this behavior will split some statements like struct typedefs, but we are only interested in function definitions.
int getcStrippingBlocksAndComments(FILE* stream) {
	static int hadOpenBrace = 0, hadCloseBrace = 0, hadSemicolon = 0;
	if(hadCloseBrace) {	//Output pending semicolons before anything is read.
		hadCloseBrace = hadOpenBrace = 0;
		hadSemicolon = 1;
		return ';';
	}
	if(hadSemicolon) {	//Output pending newlines before anything is read.
		hadSemicolon = hadCloseBrace = hadOpenBrace = 0;
		return '\n';
	}
	int curChar = getcStrippingStringsAndComments(stream);
	if(curChar == EOF) return EOF;
	if(hadOpenBrace) {
		//When this point is reached, the open brace has already been forwarded, so it's just the time to skip the contents of the braces.
		int braceLevel = 1;
		while(braceLevel) {
			if(curChar == '{') braceLevel++;
			if(curChar == '}') braceLevel--;
			curChar = getcStrippingStringsAndComments(stream);
			if(curChar == EOF) return EOF;
		}
		//Output the closing brace.
		hadOpenBrace = 0;
		hadCloseBrace = 1;
		return '}';
	}
	hadOpenBrace = (curChar == '{');
	return curChar;
}
#endif

void doExtraction(FILE* stream) {
	CFunctionDeclarationDetector* detector = cfdd_make(stdout);
	int curChar;
	while((curChar = getc(stream)) != EOF) cfdd_pushChar(detector, curChar);
	cfdd_delete(detector);
}

int main(int argc, char** argv) {
	FILE* stream = stdin;
	if(argc >= 2) {
		if(!strcmp("-h", argv[1]) || !strcmp("--help",argv[1])) {
			printUsage(*argv);
			return 0;
		}
	}
//	stream = fopen("main.c", "r");
	doExtraction(stream);
	return 0;
}
