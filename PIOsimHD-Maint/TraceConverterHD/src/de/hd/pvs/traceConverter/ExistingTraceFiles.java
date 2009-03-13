package de.hd.pvs.traceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Stores all trace file names
 * 
 * @author julian
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
