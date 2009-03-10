package de.hd.pvs.traceConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Stores all trace file names
 * 
 * @author julian
 *
 */
public class ExistingTraceFiles {
	File filePrefix;
	
	// maps process rank, thread id and identifier 
	final HashMap<Integer, HashMap<Integer, ArrayList<String>>> map;
	
	public ExistingTraceFiles(String filePrefix, 
			HashMap<Integer, HashMap<Integer, ArrayList<String>>> files) {		
		this.filePrefix = new File(filePrefix);
		this.map = files;
	}
		
	public String getFilenameXML(int rank, int thread){
		return filePrefix.toString() + rank +"_" + thread +".xml";
	}
	
	public String getFilenameStatistics(int rank, int thread, String stat){		
		return filePrefix.toString() + rank +"_" + thread + "_stat_" + stat + ".dat";
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