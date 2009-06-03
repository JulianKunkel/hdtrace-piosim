
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.


package de.hd.pvs.TraceFormat.trace;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * Enumerate the children in correct time order, start with an initial time. 
 * The enumeration allows to look ahead one element (via peek).
 * 
 * @author Julian M. Kunkel
 */
public class ForwardStateEnumerationStartTime extends ForwardStateEnumeration{
	
	ITraceEntry current = null;
		
	public ForwardStateEnumerationStartTime(IStateTraceEntry owner, Epoch startTime) {
		super(owner);
		while(super.hasMoreElements){
			current = super.nextElement();
			if (current.getEarliestTime().compareTo(startTime) >= 0){ // TODO could be optimized with bin tree algorithm.
				return;
			}
		}
		if(! super.hasMoreElements){
			current = null;
		}
	}
	
	/**
	 * Look at the next element without extracting it.
	 * @return
	 */
	public ITraceEntry peek(){
		return current;
	}
	
	@Override
	public ITraceEntry nextElement() {
		ITraceEntry old = current;
		
		if(super.hasMoreElements){
			current = super.nextElement();
		}else{
			current = null;
		}
		
		return old;
	}
	
	@Override
	public boolean hasMoreElements() {		
		return current != null;
	}
}