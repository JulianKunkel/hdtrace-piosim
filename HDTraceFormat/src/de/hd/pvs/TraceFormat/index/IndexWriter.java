
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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hd.pvs.TraceFormat.index.IndexReader.IndexData;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Write an index file. 
 * An index contains of a sorted array of times and corresponding file offsets in the indexed file. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class IndexWriter {	
	final static public int VERSION = 1;
	final static public char INTERNAL_NODE = 'i';
	
	public final static int DEFAULT_SPLIT_FACTOR = 100; // BTREE_ENTRIES_PER_NODE

	final DataOutputStream file;
	
	Epoch lastEpoch = null;
	
	public IndexWriter(String filename) throws IOException{
		this.file = new DataOutputStream(new FileOutputStream(filename));
		
		file.writeInt(VERSION);				
	}
	
	public void writeNextEntry(Epoch time, long offset) throws IOException{
		if(lastEpoch != null && lastEpoch.compareTo(time) > 0){
			throw new IllegalArgumentException("Time is decreasing!");
		}
			
		file.writeInt(time.getSeconds());
		file.writeInt(time.getNanoSeconds());
		file.writeLong(offset);
		
		lastEpoch = time;
	}
	

	public void finalize() throws IOException{
		file.close();
	}

	
	/**
	 * Test index creation.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		IndexWriter tmp = new IndexWriter("/tmp/idx");
		
		for (int i=0; i < 10000; i++){
			tmp.writeNextEntry(new Epoch(i*10, 0), (i*10));
		}
		
		tmp.finalize();	
		
		
		IndexReader reader = new IndexReader("/home/julian/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0_stat_Energy.idx");
		
		for(int i=0; i < 100; i++){
			IndexData data =  reader.getFirstInfoWithTime(new Epoch(i*10, 0));
			if(data != null)
				System.out.println(data.getNextTime() + " " + data.getPosition());
		}
		
		reader.close();
	}
}
