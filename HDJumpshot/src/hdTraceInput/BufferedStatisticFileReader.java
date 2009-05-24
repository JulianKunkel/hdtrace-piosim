
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedStatisticFileReader extends StatisticsReader implements IBufferedReader{

	private Epoch minTime;
	private Epoch maxTime;

	final StatisticStatistics [] statistics;

	ArrayList<StatisticGroupEntry> statEntries = new ArrayList<StatisticGroupEntry>();

	public BufferedStatisticFileReader(String filename, String group) throws Exception{
		super(filename, group);

		StatisticGroupEntry current = getNextInputEntry();

		minTime = current.getEarliestTime();		
		
		while(current != null){
			statEntries.add(current);
			current = getNextInputEntry();
		}

		maxTime = statEntries.get(statEntries.size()-1).getLatestTime();

		//  update local min/max value
		// check file:

		statistics = new StatisticStatistics [getGroup().getSize()];

		for(StatisticDescription desc: getGroup().getStatisticsOrdered()){
			if(! desc.isNumeric())
				continue;
			
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			

			// use bigdecimal to increase accuracy.			
			BigDecimal sum = new BigDecimal(0);			
			BigDecimal integratedSum = new BigDecimal(0);
			
			final int cnt = statEntries.size();
			
			final int groupNumber = desc.getNumberInGroup();
			
			for(StatisticGroupEntry entry: statEntries){
				double value = entry.getNumeric(groupNumber);
				
				if( value > max ) max = value;
				if( value < min ) min = value;
				
				sum = sum.add(new BigDecimal(value) );  
				
				integratedSum = integratedSum.add(
						new BigDecimal(value).multiply(	
								entry.getLatestTime().subtract(entry.getEarliestTime()).getBigDecimal())
						);
			}
			
			final double avg = sum.doubleValue() / cnt;
			
			BigDecimal stddev = new BigDecimal(0);
			for(StatisticGroupEntry entry: statEntries){
				double value = entry.getNumeric(groupNumber);
				stddev = stddev.add(new BigDecimal((value - avg) * (value - avg)));				
			}
				
			double stddevd = 0;
			if( cnt > 1){
				stddevd = Math.sqrt(stddev.divide( new BigDecimal (cnt - 1), RoundingMode.HALF_DOWN ).doubleValue());
			}
			
			statistics[groupNumber] = new StatisticStatistics(max, min, avg, stddevd, sum, integratedSum);
		}
	}

	public Enumeration<StatisticGroupEntry> enumerateStatistics(Epoch startTime, Epoch endTime){
		return new ReaderStatisticGroupEnumerator(this, getGroup(), startTime, endTime);
	}

	public int getStatisticPositionAfter(Epoch minEndTime){
		int min = 0; 
		int max = statEntries.size() - 1;
		
		while(true){						
			int cur = (min + max) / 2;
			StatisticGroupEntry entry = statEntries.get(cur);
			
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

	public StatisticGroupEntry getTraceEntryClosestToTime(Epoch dTime){
		int pos = getStatisticPositionAfter(dTime);
		if (pos == -1)
			pos = statEntries.size()-1;

		return statEntries.get(pos);
	}


	public ArrayList<StatisticGroupEntry> getStatEntries() {
		return statEntries;
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
}
