
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

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;

/**
 * walks through a list of XMLTraceEntries and also through nested elements.
 * 
 * @author Julian M. Kunkel
 */
public class ReaderTraceElementNestedEnumerator implements Enumeration<TraceEntry>{

	protected ForwardStateEnumeration stateChildEnumeration = null;
	final protected ArrayList<TraceEntry> list;
	int curPos = 0;

	public ReaderTraceElementNestedEnumerator(BufferedTraceFileReader reader) {		
		list = reader.getTraceEntries();
		
		if(list.size() == 0)
			return;		
	}

	@Override
	public TraceEntry nextElement() {
		if( stateChildEnumeration == null ){ 
			final TraceEntry current = list.get(curPos++);
			
			if(current.getType() == TraceObjectType.STATE){
				final StateTraceEntry state = (StateTraceEntry) current;
				if(! state.hasNestedTraceChildren())
					return current;
				
				stateChildEnumeration = state.childForwardEnumeration();
				return current;
			}
			
			stateChildEnumeration = null;
			
			return current;
		}
		
		// now stateChildEnumeration != null
		final TraceEntry current = stateChildEnumeration.nextElement();
		
		if(! stateChildEnumeration.hasMoreElements()){
			stateChildEnumeration = null;
		}
		
		return current;
	}

	@Override
	public boolean hasMoreElements() {
		return stateChildEnumeration != null || curPos < list.size();
	}
	
	public int getNestingDepthOfNextElement() {
		if (stateChildEnumeration == null)
			return 0;
		return stateChildEnumeration.getNestingDepthOfNextElement();
	}
}
