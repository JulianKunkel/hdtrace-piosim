
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

package de.hd.pvs.TraceFormat.index;

import java.io.IOException;
import java.io.RandomAccessFile;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * Reads an index of an trace/statistic file.
 * This is done on demand, i.e. no caching (except for root node)
 * Caching is implicitly done inside the OS.  
 * 
 * @author Julian M. Kunkel
 *
 */
public class IndexReader {
	final static public int VERSION = 1;
	final static public char INTERNAL_NODE = 'i';

	final RandomAccessFile file;	
	final long rootOffset;

	final long entries;
	
	final private static int RECORD_LENGTH = 4+4+4; // three ints.

	public static class IndexData{
		final Epoch nextTime;
		final int  position;

		public IndexData(Epoch nextTime, int position) {
			this.nextTime = nextTime;
			this.position = position;
		}

		/**
		 * @return the epoch which comes next, or null if EOF
		 */
		public Epoch getNextTime() {
			return nextTime;
		}

		/**
		 * @return the position inside the file which is the smallest time before the searched event or -1 if there is no event before.
		 */
		public int getPosition() {
			return position;
		}
	}

	public IndexReader(String filename) throws IOException{
		this.file = new RandomAccessFile(filename, "r");

		final int version = file.readInt();
		if(version != VERSION){
			throw new IllegalArgumentException("File has version " + version + ", but I understand only " + VERSION);
		}
		rootOffset = file.getFilePointer();		

		entries = (file.length() - 4) / RECORD_LENGTH;
	}

	public void close() throws IOException{
		file.close();
	}


	/**
	 * Return a valid position which is the closest time before the event/statistic. 
	 * returns null if there is no such a position in the file. 
	 */
	public IndexData getFirstInfoWithTime(final Epoch time) throws IOException{		
		long minEntry = 0;
		long maxEntry = entries;
		
		// do a binary search on the data:
		while(true){
			final long curEntry = (maxEntry + minEntry) / 2;
			
			//System.out.println(curEntry + " " +  minEntry + " " + maxEntry);
			
			file.seek(curEntry* RECORD_LENGTH + 4);

			// read epoch:
			final int seconds = file.readInt();
			final int nanoSeconds = file.readInt();

			if(minEntry >= maxEntry){
				// got the entry after the one we are are looking for.
				if(curEntry == 0){
					return new IndexData(new Epoch(seconds, nanoSeconds), 0);
				}
				
				// seek to the position before:
				file.seek((curEntry-1)* RECORD_LENGTH + 4 + 8);
				
				return new IndexData(new Epoch(seconds, nanoSeconds), file.readInt());
			}
			
			if(seconds == time.getSeconds() && nanoSeconds == time.getNanoSeconds()){
				return new IndexData(time, file.readInt());
			}if(seconds > time.getSeconds() || (seconds == time.getSeconds() && nanoSeconds > time.getNanoSeconds())){
				// time read is later than the one we search for:
				maxEntry = curEntry ;
			}else{
				// time read is smaller OR equal than the one we search for.
				minEntry = curEntry + 1;
				
				if(minEntry == entries){
					return null;
				}
			}
		}
	}
}
