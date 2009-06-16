
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

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedTraceFileReader extends StAXTraceFileReader implements IBufferedReader {

	private Epoch minTime;
	private Epoch maxTime;

	final ArrayList<ITraceEntry> traceEntries = new ArrayList<ITraceEntry>();
	
	
	/**
	 * Return an enumeration of the contained trace entries not finishing earlier than start and not
	 * starting after endTime (an overlapping of end or start is possible and must be checked by hand).
	 * 
	 * @param nested
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public ReaderTraceElementEnumerator enumerateTraceEntryLaterThan(boolean nested, Epoch startTime, Epoch endTime){
		if(! nested)
			return new ReaderTraceElementEnumerator(this, startTime, endTime);
		else
			return new ReaderTraceElementNestedTimeEnumerator(this, startTime, endTime);
	}
	
	public ReaderTraceElementNestedEnumerator enumerateNestedTraceEntry(){
		return new ReaderTraceElementNestedEnumerator(this);
	}
	
	public ReaderTraceElementEnumerator enumerateTraceEntry(){
		return new ReaderTraceElementEnumerator(this);
	}
	
	public BufferedTraceFileReader(String filename, boolean nested) throws Exception {
		super(filename, nested);

		ITraceEntry current = getNextInputEntry();

		minTime = current.getEarliestTime();

		while(current != null){
			traceEntries.add(current);

			current = getNextInputEntry();
		}

		maxTime = traceEntries.get(traceEntries.size()-1).getLatestTime();
	}

	
	public ArrayList<ITraceEntry> getTraceEntries() {
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
	public int getFirstTraceEntryPositionOverlappingOrLaterThan(Epoch laterThanTime){
		return ArraySearcher.getPositionEntryOverlappingOrLaterThan(traceEntries, laterThanTime);
	}
	
	public int getTraceEntryClosestToTimePosition(Epoch dTime){
		return ArraySearcher.getPositionEntryClosestToTime(traceEntries, dTime);
	}
	
	
	/**
	 * Return the XMLTrace entry which is covered or closest to this time.
	 * 
	 * @param time
	 * @return
	 */
	public ITraceEntry getTraceEntryClosestToTime(Epoch dTime){
		int best = getTraceEntryClosestToTimePosition(dTime);
		return traceEntries.get(best);
	}
}
