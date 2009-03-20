package de.hd.pvs.TraceFormat;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Reads from all trace files.
 * @author julian
 *
 */
public class TraceFormatReader {
	final TraceFormatFileOpener files;
	
	public TraceFormatReader(String projectFile, boolean readNested) throws Exception{
		files = new TraceFormatFileOpener(projectFile, readNested);
	}
	
	public ProjectDescription getProjectDescription() {
		return files.getProjectDescription();
	}
	
	public String getProjectFileName(){
		return getProjectDescription().getProjectFilename();
	}
	
	public int getRankCount(){
		return files.getSize();
	}
	
	public int getThreadCount(int rank){
		return files.getTraceFilesPerRank().get(rank).getSize();
	}
	
	public XMLTraceEntry getNextTraceEntry(Epoch minTime, int rank, int thread){
		return files.getTraceFilesPerRank().get(rank).getFilesPerThread().get(thread).getTraceReader().getNextInputData();
	}

	public StatisticEntry getNextStatistic(Epoch minTime, String group, String hostname){
		return null;
	}
	
	public StatisticEntry getNextStatistic(Epoch minTime, String group, int rank){
		return null;
	}
	
	public StatisticEntry getNextStatistic(Epoch minTime, String group, int rank, int thread){
		return null;
	}	
}
