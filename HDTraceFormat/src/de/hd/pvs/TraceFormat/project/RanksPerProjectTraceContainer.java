package de.hd.pvs.TraceFormat.project;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsReader;

public class RanksPerProjectTraceContainer {
	final int rank;
	
	final HashMap<Integer, ThreadsPerRankTraceContainer> filesPerThread = new HashMap<Integer, ThreadsPerRankTraceContainer>();
	
	// maps group => reader
	final HashMap<String, StatisticsReader> statisticReaders = new HashMap<String, StatisticsReader>(); 
	
	public void setThread(int thread, ThreadsPerRankTraceContainer files){
		filesPerThread.put(thread, files);
	}
	
	public void setStatisticReader(String group, StatisticsReader reader){
		statisticReaders.put(group, reader);
	}
	
	public RanksPerProjectTraceContainer(int rank) {
		this.rank = rank;
	}
	
	public HashMap<Integer, ThreadsPerRankTraceContainer> getFilesPerThread() {
		return filesPerThread;
	}
	
	public int getSize(){
		return filesPerThread.size();
	}
}
