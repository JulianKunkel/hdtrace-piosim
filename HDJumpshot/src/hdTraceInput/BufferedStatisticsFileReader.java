
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


package hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Buffers all statistics of a given file.
 * 
 * @author julian
 *
 */
public class BufferedStatisticsFileReader implements IBufferedReader, StatisticsSource {

	/**
	 * Earliest statistics start time
	 */
	private final Epoch minTime;
	
	/**
	 * Latest statistics end time
	 */
	private final Epoch maxTime;
	
	/**
	 * The group we are reading.
	 */
	private final StatisticsGroupDescription group;

	/**
	 * Statistics about the contained values
	 */
	private final StatisticStatistics [] statistics;

	/**
	 * The actual contained entries.
	 */
	private final StatisticsGroupEntry [] entries;

	public BufferedStatisticsFileReader(String filename, String expectedGroupName) throws Exception{
		final StatisticsReader reader = new StatisticsReader(filename, expectedGroupName);

		this.group = reader.getGroup();
		
		StatisticsGroupEntry current = reader.getNextInputEntry();
		
		this.minTime = current.getEarliestTime();	
		
		final ArrayList<StatisticsGroupEntry> statEntries = new ArrayList<StatisticsGroupEntry>();
		
		while(current != null){
			statEntries.add(current);
			current = reader.getNextInputEntry();
		}

		this.maxTime = statEntries.get(statEntries.size()-1).getLatestTime();
		
		this.entries = statEntries.toArray(new StatisticsGroupEntry[]{});

		
		this.statistics = new StatisticStatistics [getGroup().getSize()];
		for(StatisticsDescription desc: getGroup().getStatisticsOrdered()){
			if(! desc.isNumeric())
				continue;
			
			statistics[desc.getNumberInGroup()] = StatisticsComputer.computeStatistics(
					this, desc, minTime, maxTime
					);
		}
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
