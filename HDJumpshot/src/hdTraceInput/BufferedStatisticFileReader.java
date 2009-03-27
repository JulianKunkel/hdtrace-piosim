
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

import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedStatisticFileReader extends StatisticsReader implements IBufferedReader{

	private Epoch minTime;
	private Epoch maxTime;
	
	ArrayList<StatisticGroupEntry> statEntries = new ArrayList<StatisticGroupEntry>();
	
	public BufferedStatisticFileReader(String filename, StatisticsGroupDescription group) throws Exception{
		super(filename, group);
		
		StatisticGroupEntry current = getNextInputEntry();
		
		minTime = current.getEarliestTime();
		
		while(current != null){
			statEntries.add(current);

			current = getNextInputEntry();
		}
		
		maxTime = statEntries.get(statEntries.size()-1).getEarliestTime();
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
				if(entry.getLatestTime().compareTo(minEndTime) > 0 && cur == 0){
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
}
