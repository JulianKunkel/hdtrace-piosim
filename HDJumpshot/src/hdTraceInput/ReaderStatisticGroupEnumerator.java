
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
import de.hd.pvs.TraceFormat.util.Epoch;

public class ReaderStatisticGroupEnumerator implements Enumeration<StatisticGroupEntry> {

	int currentPos;
	final ArrayList<StatisticGroupEntry> entries;
	final Epoch endTime;

	StatisticGroupEntry current;
	
	// read one more as required, i.e. to cover length of statistic
	boolean isFinalOne = false;

	public ReaderStatisticGroupEnumerator(BufferedStatisticFileReader reader, StatisticsGroupDescription group, Epoch startTime, Epoch endTime) {		
		entries = reader.getStatEntries();
		currentPos = reader.getStatisticPositionAfter(startTime) ;

		this.endTime = endTime;
		
		if(currentPos < 0){
			currentPos = entries.size() - 1;
		}
		current = entries.get(currentPos);
		currentPos++;
		
		if(current.getEarliestTime().compareTo(endTime) > 0){
			if(currentPos <= entries.size()){
				isFinalOne = true;
			}else{
				current = null;
			}
		}		
	}

	@Override
	public boolean hasMoreElements() {
		return current != null;
	}

	@Override
	public StatisticGroupEntry nextElement() {
		StatisticGroupEntry old = current;
	
		if(currentPos < entries.size()){
			current = entries.get(currentPos);
			
			currentPos++;
			
			if(isFinalOne){
				current = null;
			}else if(current.getEarliestTime().compareTo(endTime) > 0){
				isFinalOne = true;
			}
		}else{
			current = null;
		}

		return old;
	}


}
