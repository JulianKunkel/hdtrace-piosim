
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

package de.hd.pvs.traceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Stores all trace file names
 * 
 * @author Julian M. Kunkel
 *
 */
public class ExistingTraceFiles {
	String filePrefixPath;
	
	// maps process rank, thread id and identifier 
	final HashMap<Integer, HashMap<Integer, ArrayList<String>>> map;
	
	public ExistingTraceFiles(String filePrefixPath, 
			HashMap<Integer, HashMap<Integer, ArrayList<String>>> files) {		
		this.filePrefixPath = filePrefixPath;
		
		this.map = files;
	}
		
	public String getFilenameXML(int rank, int thread){
		return FileNames.getFilenameXML(filePrefixPath, rank, thread);
	}
	
	public String getFilenameStatistics(int rank, int thread, String stat){		
		return FileNames.getFilenameStatistics(filePrefixPath, rank, thread, stat);
	}	
	
	public ArrayList<String> getStatisticsFiles(int rank, int thread){
		if(map.get(rank) == null) return null;		
		return map.get(rank).get(thread);
	}
	
	public Collection<Integer> getExistingThreads(int rank){
		return map.get(rank).keySet();
	}
	
	/**
	 * Get number of processes.
	 * @return
	 */
	public int getSize(){
		return map.size();
	}
}
