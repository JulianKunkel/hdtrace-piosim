package de.hd.pvs.TraceFormat.topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;

public class ThreadsPerRankTraceContainer{
	final int thread;

	final StAXTraceFileReader traceReader;
	
	// maps group => reader
	final HashMap<String, StatisticsReader> statisticReaders = new HashMap<String, StatisticsReader>(); 
	
	
	public ThreadsPerRankTraceContainer(int thread, StAXTraceFileReader traceReader) {
		this.thread = thread;
		this.traceReader = traceReader;
	}
	
	public StAXTraceFileReader getTraceFileReader() {
		return traceReader;
	}
	
	public HashMap<String, StatisticsReader> getStatisticReaders() {
		return statisticReaders;
	}
	
	public void setStatisticReader(String group, StatisticsReader reader){
		statisticReaders.put(group, reader);
	}
	
	public int getThread() {
		return thread;
	}
}
