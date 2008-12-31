
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

package de.hd.pvs.piosim.model.logging;

import java.io.File;
import java.util.HashSet;

import de.hd.pvs.piosim.model.dynamicMapper.DynamicMapper;

/**
 * This class contains all methods required to log particular messages on the console (or file).
 * A message includes the stacktrace. 
 * It can be configured for logging or debugging.
 * 
 * Log levels:
 * - warn (get's always printed)
 * - info (gets always printed)
 * - debug (get's only printed if enabled)
 * 
 * @author Julian M. Kunkel
 *
 */
public class ConsoleLogger {
	
	private static ConsoleLogger instance = new ConsoleLogger();
	
	/**
	 * If true then all debugging messages are active.
	 */
	protected boolean debugAll = false;
	
	/**
	 * Contains a list of canonical class names which classes should be debugged.
	 */
	protected HashSet<String> canonicalClassNamesToTrace = new HashSet<String>();
	
	/**
	 * Contains a list of component IDs which should be debugged. 
	 */
	protected HashSet<Integer> basicComponentIDsToTrace = new HashSet<Integer>();
	
	/**
	 * Return the actual logger object.
	 * @return
	 */
	public static ConsoleLogger getInstance() {
		return instance;
	}
	

	private void pinit(String loggerDefinitionFile, boolean debugEverything){
		debugAll = debugEverything;
		if (loggerDefinitionFile.length() == 0){
			return;
		}
		
		File file = new File(loggerDefinitionFile);
		
		if(! file.canRead()){
			throw new IllegalArgumentException("Logger definition file not found: " + loggerDefinitionFile);
		}
				
		// read contents from file
		String [] lines = DynamicMapper.readLines(file.getAbsolutePath());
		
		int mode = 0;
		
		for(int i=0; i < lines.length; i++){
			String line = lines[i];
			if (line.equals("ClassNamesToTrace")){
				mode = 1;
				continue;
			}else if(line.equals("ComponentIDsToTrace")){
				mode = 2;
				continue;
			}
			
			if(mode == 1){
				canonicalClassNamesToTrace.add(line);
			}else if (mode == 2){
				int val = Integer.parseInt(line);
				basicComponentIDsToTrace.add(val);
			}else{
				throw new IllegalArgumentException("I don't understand how to parse line " + i + " in file " + 
						file.getAbsolutePath() + " data: " + line);
			}			
		}
	}
	
	/**
	 * Parse the logger definition file and set internal values
	 * @param loggerDefinitionFile
	 */
	public static void init(String loggerDefinitionFile, boolean debugEverything){
		instance.pinit(loggerDefinitionFile, debugEverything);
	}
	
	protected void getStackTrace(StringBuffer buff, int depth){
		StackTraceElement [] elems = new Throwable().getStackTrace();						
		
		buff.append("\t-- ");
		buff.append(elems[depth].getMethodName());
		buff.append(" ");		
		
		for ( int i= 3; i < elems.length ; i++){
			buff.append ( " [" +  i +  "] " + elems[i]);
		}
		buff.append("\n");			
	}
	
	/**
	 * Print the message with the stack trace.
	 * @param what
	 */
	public void warn(String what){		
		StringBuffer buff = new StringBuffer();
		buff.append("[WARN] " + what);
		getStackTrace(buff, 2);
		
		System.out.print(buff.toString());
	}
	
	/**
	 * Print the message with the stack trace.
	 * @param what
	 */
	public void info(String what){		
		StringBuffer buff = new StringBuffer();
		buff.append(what );
		getStackTrace(buff, 2);
		
		System.out.print(buff.toString());
	}
		
	
	public boolean isDebuggable(Object obj){
		if(debugAll){
			return true;
		}
		return canonicalClassNamesToTrace.contains(obj.getClass().getCanonicalName());
	}

	/**
	 * Print the message with the stack trace.
	 *  
	 * @param object the calling object.
	 * @param what
	 */
	public boolean debug(Object object, String what){
		if (! isDebuggable(object)){
			return true;
		}
		
		StringBuffer buff = new StringBuffer();
		buff.append(what );
		getStackTrace(buff, 2);
		
		System.out.print(buff.toString());
		return true;
	}
	
	/**
	 * Print another line, but without stack trace.
	 * 
	 * @param object the calling object
	 * @param what
	 */
	public void debugFollowUpline(Object object, String what){
		if (! isDebuggable(object)){
			return;
		}

		System.out.println(" -> " + what);
	}
	
	public boolean isDebugAll() {
		return debugAll;
	}
}
