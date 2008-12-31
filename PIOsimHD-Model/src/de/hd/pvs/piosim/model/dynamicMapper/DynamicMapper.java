
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

package de.hd.pvs.piosim.model.dynamicMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Superclass which loads a dynamic mapping between model or command to simulation classes.
 * 
 * @author Julian M. Kunkel
 */
abstract public class DynamicMapper {

	/**
	 * Read all lines from the file.
	 * @param filename
	 * @return
	 */
	static public String [] readLines(String filename) {
		File file = new File(filename);
		
		ArrayList<String> lines = new ArrayList<String>();
		
		try {
			BufferedReader inp = new BufferedReader( new FileReader(file));	  
			
			String line;
			
			while ( (line = inp.readLine()) != null) {
				int commentPos = line.indexOf('#');
				if(commentPos >= 0) {
					line = line.substring(0, commentPos);
				}
				
				line = line.replaceAll("[ \n\t]+", "");				
				
				if(line.length() > 0) {
					lines.add(line);
				}
			}
			
			inp.close();
		}catch(IOException ex) {
			System.err.println("Looking for mapping in: " + file.getAbsolutePath());
			throw new RuntimeException(ex);
		}		
		
		return lines.toArray(new String[0]);
	}
	

	/**
	 * Return the simple class name of the string. 
	 * @param canonicalClassName
	 * @return
	 */
	protected String getImplementationClassName(String canonicalClassName) {
		int lastPos = canonicalClassName.lastIndexOf('.');
		return canonicalClassName.substring(lastPos+1, canonicalClassName.length());
	}
	
	/**
	 * Try to load the class or throw an Exception
	 * @param className
	 */
	protected void tryToLoadClass(String className){
		try{
			Class.forName(className, false, ClassLoader.getSystemClassLoader());
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException(e);							
		}
	}
	
}
