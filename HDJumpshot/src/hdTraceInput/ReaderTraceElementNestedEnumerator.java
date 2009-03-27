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

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime and also through nested elements.
 * 
 * @author Julian M. Kunkel
 */
public class ReaderTraceElementNestedEnumerator extends ReaderTraceElementEnumerator {

	protected ForwardStateEnumeration stateChildEnumeration = null;

	public ReaderTraceElementNestedEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		super(reader, startTime, endTime);
		if (currentEntry == null || currentPos == 0)
			return;

		// scan if the state before has nested elements later than the given time.
		final XMLTraceEntry before = entries.get(currentPos -1 );

		if(before.getLatestTime().compareTo(startTime) <= 0){
			// then there might be no overlapping of the potential previous state.
			return;
		}

		if(before.getType() == TraceObjectType.STATE){
			// now its a state
			final StateTraceEntry state = (StateTraceEntry) before;
			if(! state.hasNestedTraceChildren())
				return;
			stateChildEnumeration = state.childForwardEnumeration(startTime);
			if(! stateChildEnumeration.hasMoreElements()){
				stateChildEnumeration = null;
			}else{
				// now we have found an event in the state directly before this one.
				currentPos--;
			}
		}
	}

	@Override
	public XMLTraceEntry nextElement() {
		if( stateChildEnumeration == null){ 
			final XMLTraceEntry current = super.nextElement();
			
			if(current.getType() == TraceObjectType.STATE){
				final StateTraceEntry state = (StateTraceEntry) current;
				if(! state.hasNestedTraceChildren())
					return current;
				
				stateChildEnumeration = state.childForwardEnumeration();
				return current;
			}
			
			return current;
		}
		
		// now stateChildEnumeration != null
		final XMLTraceEntry current = stateChildEnumeration.nextElement();
		
		if(! stateChildEnumeration.hasMoreElements()){
			stateChildEnumeration = null;
		}
		
		return current;
	}

	@Override
	public boolean hasMoreElements() {
		return stateChildEnumeration != null || super.hasMoreElements();
	}
	
	@Override
	public int getNestingDepthOfNextElement() {
		if (stateChildEnumeration == null)
			return 0;
		return stateChildEnumeration.getNestingDepthOfNextElement();
	}
}
