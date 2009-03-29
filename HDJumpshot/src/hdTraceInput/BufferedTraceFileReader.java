
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

import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.DrawObjects;

public class BufferedTraceFileReader extends StAXTraceFileReader implements IBufferedReader {

	private Epoch minTime;
	private Epoch maxTime;

	ArrayList<TraceEntry> traceEntries = new ArrayList<TraceEntry>();
	
	/**
	 * Return an enumeration of the contained trace entries between start & endTime.
	 * 
	 * @param nested
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public ReaderTraceElementEnumerator enumerateTraceEntry(boolean nested, Epoch startTime, Epoch endTime){
		if(! nested)
			return new ReaderTraceElementEnumerator(this, startTime, endTime);
		else
			return new ReaderTraceElementNestedEnumerator(this, startTime, endTime);
	}

	public BufferedTraceFileReader(String filename, boolean nested) throws Exception {
		super(filename, nested);

		TraceEntry current = getNextInputEntry();

		minTime = current.getEarliestTime();

		while(current != null){
			traceEntries.add(current);

			current = getNextInputEntry();
		}

		maxTime = traceEntries.get(traceEntries.size()-1).getLatestTime();
	}

	
	public ArrayList<TraceEntry> getTraceEntries() {
		return traceEntries;
	}

	public Epoch getMinTime() {
		return minTime;
	}

	public Epoch getMaxTime() {
		return maxTime;
	}

	/**
	 * Note: nested objects are not taken into account! 
	 * 
	 * @param laterThanTime
	 * @return
	 */
	public int getTraceEntryPositionLaterThan(Epoch laterThanTime){
		int min = 0; 
		int max = traceEntries.size() - 1;
		
		while(true){
			int cur = (min + max) / 2;
			TraceEntry entry = traceEntries.get(cur);
			
			if(min == max){ // found entry or stopped.
				if( traceEntries.get(cur).getLatestTime().compareTo(laterThanTime) <= 0 )
					return -1;				
				return cur;
			} 
			// not found => continue bin search:
			
			if ( entry.getLatestTime().compareTo(laterThanTime) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
	
	public int getTraceEntryClosestToTimePosition(Epoch dTime){
		int min = 0; 
		int max = traceEntries.size() - 1;
		
		while(true){
			int cur = (min + max) / 2;
			TraceEntry entry = traceEntries.get(cur);
			
			if(min == max){ // found entry or stopped.
				int best = cur;
				double bestDistance = DrawObjects.getTimeDistance(dTime, entry);
								
				// check entries left and right.
				if( min != 0){
					TraceEntry left = traceEntries.get(cur -1);
					double leftDistance = DrawObjects.getTimeDistance(dTime, left);
					
					if(leftDistance < bestDistance){
						bestDistance = leftDistance;
						best = cur-1;
					}
				}
				
				if(max != traceEntries.size() -1){
					// check right
					TraceEntry right = traceEntries.get(cur + 1);
					double rightDistance = DrawObjects.getTimeDistance(dTime, right);
					
					if(rightDistance < bestDistance){
						bestDistance = rightDistance;
						best = cur+1;
					}
				}

				return best;
			} 
			// not found => continue bin search:
			
			if ( entry.getEarliestTime().compareTo(dTime) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
	
	
	/**
	 * Return the XMLTrace entry which is covered or closest to this time.
	 * 
	 * @param time
	 * @return
	 */
	public TraceEntry getTraceEntryClosestToTime(Epoch dTime){
		int best = getTraceEntryClosestToTimePosition(dTime);
		return traceEntries.get(best);
	}
}
