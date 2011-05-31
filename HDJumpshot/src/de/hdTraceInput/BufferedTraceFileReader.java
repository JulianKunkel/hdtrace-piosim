
 /** Version Control Information $Id: BufferedTraceFileReader.java 417 2009-06-17 18:47:01Z kunkel $
  * @lastmodified    $Date: 2009-06-17 20:47:01 +0200 (Mi, 17. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 417 $ 
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

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedTraceFileReader extends StAXTraceFileReader implements IBufferedReader {

	final private Epoch minTime;
	final private Epoch maxTime;
	
	final private String filename;
	final private Epoch additionalTimeOffset;

	final ArrayList<ITraceEntry> traceEntries = new ArrayList<ITraceEntry>();
	
	public ReaderTraceElementEnumerator enumerateTraceEntries(boolean nested, Epoch startTime, Epoch endTime){
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
	
	public String getFilename() {
		return filename;
	}
	
	public BufferedTraceFileReader(String filename, boolean nested, Epoch timeOffset) throws Exception {
		super(filename, nested, timeOffset);
		
		this.additionalTimeOffset = timeOffset;
		this.filename = filename;

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
	
	public Epoch getAdditionalTimeOffset() {
		return additionalTimeOffset;
	}
}
