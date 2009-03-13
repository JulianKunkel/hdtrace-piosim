package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticDescription;

public class StatisticWriter {
	private final DataOutputStream file;	
	private final ExternalStatisticsGroup group;
	
	private Epoch lastTimeStamp = null;

	private Iterator<StatisticDescription> nextExpectedStatisticIter = null;
	
	public StatisticWriter(String filename, ExternalStatisticsGroup newGroup) throws Exception {
		this.group = newGroup;
		this.file = new DataOutputStream(new FileOutputStream(filename));
	}

	public void writeStatisticEntry(Epoch time, String statistic, Object value){
		if(lastTimeStamp != null && lastTimeStamp.compareTo(time) > 0){
			throw new IllegalArgumentException("New timestamp is before old timestamp! " + lastTimeStamp + " new: " + time);
		}
				
		// write timestamp, if necessary
		if(nextExpectedStatisticIter == null || ! nextExpectedStatisticIter.hasNext()){
			nextExpectedStatisticIter = group.getStatisticsOrdered().iterator();
		}
		
		final StatisticDescription expectedStat = nextExpectedStatisticIter.next();
		
		if(expectedStat == null || !expectedStat.getName().equals(statistic)){
			throw new IllegalArgumentException("Expected to get statistics in the correct order! Expected: " + expectedStat.getName() + " but got"
					+ statistic);
		}
		
		// write data:
		
		// TODO
		
		lastTimeStamp = time;
	}
	
	public void finalize(){
		try{
			file.close();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ExternalStatisticsGroup getOutputGroup() {
		return group;
	}
}
