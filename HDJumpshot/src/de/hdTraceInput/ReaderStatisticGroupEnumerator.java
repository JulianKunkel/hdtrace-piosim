
 /** Version Control Information $Id: ReaderStatisticGroupEnumerator.java 325 2009-06-01 15:42:47Z kunkel $
  * @lastmodified    $Date: 2009-06-01 17:42:47 +0200 (Mo, 01. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 325 $ 
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


package de.hdTraceInput;

import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class ReaderStatisticGroupEnumerator implements Enumeration<StatisticsGroupEntry> {

	int currentPos;
	final StatisticsGroupEntry [] entries;
	final Epoch endTime;

	StatisticsGroupEntry current;
	
	// read one more as required, i.e. to cover length of statistic
	boolean isFinalOne = false;

	public ReaderStatisticGroupEnumerator(BufferedStatisticsFileReader reader, StatisticsGroupDescription group, Epoch startTime, Epoch endTime) {		
		entries = reader.getStatEntries();
		currentPos = reader.getStatisticPositionAfter(startTime) ;

		this.endTime = endTime;	
		
		if(currentPos < 0){
			currentPos = entries.length - 1;
		}
		current = entries[currentPos];
		currentPos++;
		
		if(current.getLatestTime().compareTo(endTime) > 0){
			if(currentPos <= entries.length){
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
	public StatisticsGroupEntry nextElement() {
		StatisticsGroupEntry old = current;
	
		if(currentPos < entries.length){
			current = entries[currentPos];
			
			currentPos++;
			
			if(isFinalOne){
				current = null;
			}else if(current.getLatestTime().compareTo(endTime) > 0){
				isFinalOne = true;
			}
		}else{
			current = null;
		}

		return old;
	}


}
