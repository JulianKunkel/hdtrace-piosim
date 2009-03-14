
//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat;

import java.io.File;
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
	String parentDir;
	String filePrefix;
	String projectFilename;

	// maps process rank, thread id and identifier 
	final HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();

	public ExistingTraceFiles(String projectFileName, int processCount) {		
		File projectFile = new File(projectFileName);

		// scan for the trace files
		String prefix = projectFile.getName().toString();
		prefix = prefix.substring(0, prefix.lastIndexOf('.'));		

		for (int i=0 ; i < processCount; i++){
			final ArrayList<Integer> list = new ArrayList<Integer>();
			map.put(i, list);				
		}

		File parent = projectFile.getParentFile();
		if(parent == null){
			parent = new File(".");
		}

		this.parentDir = parent.getAbsolutePath();
		this.projectFilename = projectFile.getName();
		this.filePrefix = prefix;

		// scan for available threads:
		for (String file: parent.list()){
			if(file.startsWith(prefix + "_")){
				String remainder = file.substring(prefix.length() + 1, file.lastIndexOf('.'));				
				String[] splits = remainder.split("_");				
				if(splits.length == 2 && file.endsWith(".xml")){
					// rank, thread id
					int rank = Integer.parseInt(splits[0]);
					int thread = Integer.parseInt(splits[1]);

					map.get(rank).add(thread);
				}
			}
		}
	}

	public String getFilenameXML(int rank, int thread){
		return parentDir + "/" +  TraceFileNames.getFilenameXML(filePrefix, rank, thread);
	}

	public String getFilenameStatistics(int rank, int thread, String stat){		
		return parentDir + "/" + TraceFileNames.getFilenameStatistics(filePrefix, rank, thread, stat);
	}	

	public Collection<Integer> getExistingThreads(int rank){
		return map.get(rank);
	}

	/**
	 * Get number of processes.
	 * @return
	 */
	public int getSize(){
		return map.size();
	}
}
