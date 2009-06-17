
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
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime (latest object is one starting before endTime)
 * 
 * @author Julian M. Kunkel
 */
public class ReaderTraceElementEnumerator implements ITraceElementEnumerator{

	protected int curPos;
	
	final protected ArrayList<ITraceEntry> entries;	
	final protected Epoch endTime;
	
	/**
	 * Read all trace elements
	 * @param reader
	 */
	public ReaderTraceElementEnumerator(BufferedTraceFileReader reader) {
		this.endTime = new Epoch(Integer.MAX_VALUE, Integer.MAX_VALUE);
		this.curPos = 0;
		this.entries = reader.getTraceEntries();
	}
	
	public ReaderTraceElementEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		this.curPos = reader.getFirstTraceEntryPositionOverlappingOrLaterThan(startTime);
		
		this.entries = reader.getTraceEntries();
		this.endTime = endTime;
		
		if(this.curPos < 0){
			this.curPos = entries.size();
		}
	}

	@Override
	public boolean hasMoreElements() {
		return  curPos < entries.size() && (entries.get(curPos).getEarliestTime().compareTo(endTime) < 0);
	}
	
	@Override
	public ITraceEntry nextElement() {
		return entries.get(curPos++);	
	}
	
	@Override
	public ITraceEntry peekNextElement(){
		return entries.get(curPos);
	}
	
	@Override
	public int getNestingDepthOfNextElement(){
		return 0;
	}
}
