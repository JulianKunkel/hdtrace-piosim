
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat;

/**
 * Central class to report errors, warnings and debugging messages 
 * @author julian
 */
public class SimpleConsoleLogger {

	/**
	 * If true then all debug messages are printed.
	 */
	static boolean debugEverything = false;

	/**
	 * Enable or disable output of debugging messages
	 * @param debugEverythin
	 */
	static public void setDebugEverything(boolean debugEverythin) {
		debugEverything = debugEverythin;
	}

	static public boolean isDebugEverything() {
		return debugEverything;
	}
	
	static private StringBuffer getStackTrace(int ignoreParent){
		StringBuffer buff = new StringBuffer();
		StackTraceElement [] elems = new Throwable().getStackTrace();						

		buff.append("\t-- ");
		buff.append(elems[ignoreParent + 1].getMethodName());
		buff.append(" ");		

		for ( int i= ignoreParent + 1; i < elems.length ; i++){
			buff.append ( " [" +  i +  "] " + elems[i]);
		}
		return buff;
	}
	
	static public void DebugWithStackTrace(String what, int ignoreParent){
		if(!debugEverything)
			return;

		StringBuffer buff = getStackTrace(ignoreParent);		
		System.err.println(buff.toString());
	}

	/**
	 * Report the debugging information if it is enabled
	 * @param what
	 */
	static public void Debug(String what){
		if(debugEverything)
			System.err.println(what);
	}

	static public void Warning(String what){
		System.err.println(what);
	}

	static public void Error(String what){
		final StringBuffer buff = getStackTrace(2);
		System.err.println(what + "\n StackTrace: " + buff.toString());
	}
}
