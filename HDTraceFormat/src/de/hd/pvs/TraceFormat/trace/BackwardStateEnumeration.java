
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


/**
 * 
 */
package de.hd.pvs.TraceFormat.trace;

import java.util.Iterator;

/**
 * Iterate the nested elements of a state in reversed time order, i.e. the last started elements 
 * will be the first returned by the iterator.
 * 
 * @author julian
 */
class BackwardStateEnumeration extends ForwardStateEnumeration{
	public BackwardStateEnumeration(StateTraceEntry owner) {
		super(owner);
	}
	
	@Override
	protected Iterator<TraceEntry> iterator(StateTraceEntry state) {		
		return state.getNestedTraceChildren().descendingIterator();
	}
}