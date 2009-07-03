
 /** Version Control Information $Id: ReaderTraceElementNestedTimeEnumerator.java 406 2009-06-16 14:18:45Z kunkel $
  * @lastmodified    $Date: 2009-06-16 16:18:45 +0200 (Di, 16. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 406 $ 
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

import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime and also through nested elements.
 * 
 * @author Julian M. Kunkel
 */
public class ReaderTraceElementNestedTimeEnumerator extends ReaderTraceElementEnumerator {

	protected ForwardStateEnumeration stateChildEnumeration = null;

	public ReaderTraceElementNestedTimeEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		super(reader, startTime, endTime);		
	}

	@Override
	public ITraceEntry nextElement() {		
		if( stateChildEnumeration == null){ 
			final ITraceEntry current = super.nextElement();
			if(current.getType() == TracableObjectType.STATE){
				final IStateTraceEntry state = (IStateTraceEntry) current;
				if(! state.hasNestedTraceChildren())
					return current;
				
				stateChildEnumeration = state.childForwardEnumeration();
								
				return current;
			}
			
			stateChildEnumeration = null;
			
			return current;
		}
				
		// now stateChildEnumeration != null
		final ITraceEntry current = stateChildEnumeration.nextElement();
		
		if(! stateChildEnumeration.hasMoreElements()){
			stateChildEnumeration = null;
		}
		
		return current;
	}

	@Override
	public boolean hasMoreElements() {
		return (stateChildEnumeration != null && stateChildEnumeration.hasMoreElements()) || super.hasMoreElements();
	}
	
	@Override
	public int getNestingDepthOfNextElement() {
		if (stateChildEnumeration == null)
			return 0;
		return stateChildEnumeration.getNestingDepthOfNextElement();
	}
}
