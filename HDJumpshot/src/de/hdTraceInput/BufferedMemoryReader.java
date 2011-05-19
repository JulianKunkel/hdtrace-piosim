package de.hdTraceInput;

import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedMemoryReader implements IBufferedStatisticsReader{

	/**
	 * Earliest statistics start time
	 */
	protected Epoch minTime;
	
	/**
	 * Latest statistics end time
	 */
	protected Epoch maxTime;
	
	/**
	 * The group we are reading.
	 */
	protected StatisticsGroupDescription group;

	/**
	 * Statistics about the contained values
	 */
	protected StatisticStatistics [] statistics;

	/**
	 * The actual contained entries.
	 */
	protected StatisticsGroupEntry [] entries;	
	
	/**
	 * Set the actual contained memory data.
	 * The group must be set first with setGroup
	 * 
	 * @param entries
	 */
	protected void setEntries(StatisticsGroupEntry[] entries) {
		this.entries = entries;
		
		this.maxTime = entries[entries.length-1].getLatestTime();
		this.minTime = entries[0].getEarliestTime();
		
		this.statistics = new StatisticStatistics [getGroup().getSize()];
		
		for(StatisticsDescription desc: getGroup().getStatisticsOrdered()){
			if(! desc.isNumeric())
				continue;
			
			statistics[desc.getNumberInGroup()] = StatisticsComputer.computeStatistics(
					this, desc, minTime, maxTime
					);
		}
	}
	
	protected void setGroup(StatisticsGroupDescription group) {
		this.group = group;
	}

	public Enumeration<StatisticsGroupEntry> enumerateStatistics(Epoch startTime, Epoch endTime){
		return new ReaderStatisticGroupEnumerator(this, getGroup(), startTime, endTime);
	}

	public int getStatisticPositionAfter(Epoch minEndTime){
		int min = 0; 
		int max = entries.length - 1;
		
		while(true){						
			int cur = (min + max) / 2;
			StatisticsGroupEntry entry = entries[cur];
			
			if(min == max){ // found entry or stopped.
				
				if(entry.getLatestTime().compareTo(minEndTime) < 0 && cur == 0){
					// there was no entry before!
					return -1;
				}

				return cur;
			} 
			// not found => continue bin search:

			if ( entry.getLatestTime().compareTo(minEndTime) >= 0 ){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}

	public StatisticsGroupEntry getTraceEntryClosestToTime(Epoch dTime){
		int pos = getStatisticPositionAfter(dTime);
		if (pos == -1)
			pos = entries.length -1;

		return entries[pos];
	}


	public StatisticsGroupEntry [] getStatEntries() {
		return entries;
	}

	public Epoch getMinTime() {
		return minTime;
	}

	public Epoch getMaxTime() {
		return maxTime;
	}

	public StatisticStatistics getStatisticsFor(int which) {
		return statistics[which];
	}
	
	public StatisticsGroupDescription getGroup() {
		return group;
	}
}
