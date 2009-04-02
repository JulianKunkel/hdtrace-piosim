
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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


package viewer.pvfs2;

import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.StringTokenizer;


/**
 * @author Julian M. Kunkel
 *
 */

public class PVFS2Slog2FileParser {

	static private void loadDefaults(){
		PVFS2SMParser.getInstance().loadConfig();
		PVFS2OpTypeParser.getInstance().loadConfig();
	}

	static public void parseSlog2(String slog2){
		/* Load information from file if available
		 * Last 4Bytes are a pointer to beginning of extra information
		 */
		try{
			long length;
			long pos;
			RandomAccessFile data = new RandomAccessFile(slog2, "r");
			length = data.length();
			data.seek(length - 8);

			pos = data.readLong();
			/* maximum of 1 MByte should be added */
			if( pos >= length || pos <= 0 || (length - pos) > 1024*1024){
				data.close();
				throw new Exception("File is not prepared");
			}

			data.seek(pos);
			byte [] buff = new byte[(int)(length-pos)];
			data.readFully(buff);
			data.close();

			String probe_version = new String(buff,0, 9);

			if ( ! probe_version.equals("<version>")  ){
				throw new Exception("PVFS2 version not found");
			}

			/* checkout if this file contains the information we want */
			String version = null;

			int start_buff = -1;
			int s_pos = 0;

			StringTokenizer tokenizer = new StringTokenizer(
					new String(buff), "\n", true);

			/* look for patterns */
			while(tokenizer.hasMoreElements()){
				String line = tokenizer.nextToken();
				if( line.contains("<version>") ){
					start_buff = s_pos + line.indexOf("<version>") +
					"<version>".length();
				}
				if( line.contains("</version>")){
					if ( start_buff == -1 ){
						throw new Exception("Could not find start for version string");
					}
					version = new String(buff, start_buff,
						s_pos - start_buff + line.indexOf("</version>")).trim();
					start_buff = -1;
				}
				if( line.contains("<SM-list>")){
					start_buff = s_pos + line.indexOf("<SM-list>") +
					"<SM-list>".length();
				}
				if( line.contains("</SM-list>")){
					if ( start_buff == -1 ){
						throw new Exception("Could not find start for SM-list string");
					}
					String sm_info = new String(buff, start_buff,
						s_pos - start_buff + line.indexOf("</SM-list>")).trim();

					PVFS2SMParser.getInstance().loadConfig(new StringReader(sm_info));
					start_buff = -1;
				}
				if( line.contains("<Headers>")){
					start_buff = s_pos + line.indexOf("<Headers>") +
					"<Headers>".length();
				}
				if( line.contains("</Headers>")){
					if ( start_buff == -1 ){
						throw new Exception("Could not find start for Headers string");
					}
					String headers = new String(buff, start_buff,
						s_pos - start_buff + line.indexOf("</Headers>")).trim();
					PVFS2OpTypeParser.getInstance().loadConfig(new StringReader(headers));
					start_buff = -1;
				}

				s_pos += line.length();
			}
			System.out.println("Logfile created with PVFS2 version: " + version);

		}catch(Exception e){
			System.err.println("Warning cannot find PIOviz marks in slog2 "
					+ slog2 + " error was: " + e.getMessage() + "\n" +
					"Using default values!");
			loadDefaults();
		}
	}
}
