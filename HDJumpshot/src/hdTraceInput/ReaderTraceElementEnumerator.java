
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

import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime (latest object is one starting before endTime)
 * 
 * @author Julian M. Kunkel
 */
public class ReaderTraceElementEnumerator implements Enumeration<XMLTraceEntry>{

	protected int currentPos;
	protected XMLTraceEntry currentEntry;
	
	final protected ArrayList<XMLTraceEntry> entries;	
	final protected Epoch endTime;
	boolean hasMoreElements;
	
	private void updateHasMoreElements(){
		hasMoreElements = currentPos >= 0 && currentPos < entries.size() && (entries.get(currentPos).getEarliestTime().compareTo(endTime) < 0);
	}
	
	public ReaderTraceElementEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		this.currentPos = reader.getTraceEntryPositionLaterThan(startTime);
		
		this.entries = reader.getTraceEntries();
		this.endTime = endTime;
	

		updateHasMoreElements();	
		
		if(hasMoreElements)
			currentEntry = entries.get(currentPos++);
	}

	@Override
	public boolean hasMoreElements() {		
		return hasMoreElements;
	}
	
	@Override
	public XMLTraceEntry nextElement() {
		final XMLTraceEntry old = currentEntry;

		updateHasMoreElements();	
		if(hasMoreElements)
			currentEntry = entries.get(currentPos++);
		
		return old;
	}
	
	/**
	 * Return the nesting depth of the next element (does only work for a nested enumerator) 
	 * @return 0 (by default)
	 */
	public int getNestingDepthOfNextElement(){
		return 0;
	}
}
