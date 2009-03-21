package de.hd.pvs.TraceFormat.topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsReader;

/**
 * For a given host maps the ranks hosted on this host.
 * @author julian
 *
 * @param <StatisticsReader>
 * @param <StAXTraceFileReader>
 */
public class HostnamePerProjectContainer {
	final HashMap<Integer, RanksPerHostnameTraceContainer > traceFilesPerRank = new HashMap<Integer, RanksPerHostnameTraceContainer>();
	
	final String hostname;
	
	// maps group => reader
	final HashMap<String, StatisticsReader> statisticReaders = new HashMap<String, StatisticsReader>(); 
	
	public HostnamePerProjectContainer(String hostname) {
		this.hostname = hostname;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setStatisticReader(String group, StatisticsReader reader){
		statisticReaders.put(group, reader);
	}
	
	public HashMap<String, StatisticsReader> getStatisticReaders() {
		return statisticReaders;
	}
	
	public HashMap<Integer, RanksPerHostnameTraceContainer > getTraceFilesPerRank() {
		return traceFilesPerRank;
	}
	
	public void setRank(int rank, RanksPerHostnameTraceContainer rankContainer){
		traceFilesPerRank.put(rank, rankContainer);
	}
	
	public int getSize(){
		return traceFilesPerRank.size();
	}
}
