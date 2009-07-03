
 /** Version Control Information $Id: PVFS2OpTypeParser.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


package de.viewer.pvfs2;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julian M. Kunkel
 *
 */
public class PVFS2OpTypeParser {
	private HashMap<Integer, String> mappingOp = new  HashMap<Integer, String> ();
	private HashMap<Integer, String> mappingEvent = new  HashMap<Integer, String> ();

	private static PVFS2OpTypeParser instance = new PVFS2OpTypeParser();

	public static PVFS2OpTypeParser getInstance(){
		return instance;
	}

	public void loadConfig(){
		loadConfig("pvfs2-server-ops.cfg");
	}

	public void loadConfig(String file){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			loadConfig(reader);

		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 operations from file "
					+ file + " error was: " + e.getMessage());
		}
	}

	public void loadConfig(StringReader sreader){
		try{
			BufferedReader reader = new BufferedReader(sreader);
			loadConfig(reader);

		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 operations " +
					" error was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadConfig(BufferedReader reader) throws IOException{
		StringBuffer buffer = new StringBuffer();
		while(reader.ready()){
			String line = reader.readLine();
			if (line == null) break;
			buffer.append( line + "\n");
		}

		String fileContent = buffer.toString();
		Pattern pattern =  Pattern.compile("enum[ \n\t]*PVFS_server_op[ \n\t]*\\{([^}]*)\\}", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = pattern.matcher( fileContent );

		if(! m.find()){
			throw new IOException("Warning could not find operation enum in file, cannot parse PVFS2 operations" );
		}

		String enumString = m.group(1);
		if( enumString != null){
			pattern = Pattern.compile("PVFS_SERV_([^ =]*).*=[^0-9]*([0-9]+)");
			m  = pattern.matcher(enumString);
			while(m.find()){
				String type = m.group(1);
				Integer val = Integer.parseInt(m.group(2));

				mappingOp.put(val, type);
			}
		}else{
			throw new IOException("Warning could not find operation enum in file, cannot parse PVFS2 operations" );
		}

		pattern = Pattern.compile("enum[ \n\t]*PVFS_event_op[ \n\t]*\\{([^}]*)\\}", Pattern.DOTALL | Pattern.MULTILINE);
		m = pattern.matcher( fileContent );

		if(! m.find()){
			throw new IOException("Warning could not find event enum, cannot parse PVFS2 operations" );
		}

		enumString = m.group(1);
		if( enumString != null){
			pattern = Pattern.compile("PVFS_EVENT_([^ =]*).*=[^0-9]*([0-9]+)");
			m  = pattern.matcher(enumString);
			while(m.find()){
				String type = m.group(1);
				Integer val = Integer.parseInt(m.group(2));

				mappingEvent.put(val, type);
			}
		}else{
			throw new IOException("Warning could not find operation enum, cannot parse PVFS2 operations" );
		}

		reader.close();
		buffer = null;
	}

	private PVFS2OpTypeParser(){

	}

	public String getOperationType(Integer type){
		return mappingOp.get(type);
	}

	public String getEventType(Integer type){
		return mappingEvent.get(type);
	}

	/**
	 * Testfunction parses input file.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Operation TYPE:" +  PVFS2OpTypeParser.getInstance().getOperationType(1));
		System.out.println("Event TYPE:" +  PVFS2OpTypeParser.getInstance().getEventType(1));
	}

}

