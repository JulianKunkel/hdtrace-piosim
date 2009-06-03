
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Iterate the nested elements of a state in reversed time order, i.e. the last started elements 
 * will be the first returned by the iterator.
 * 
 * @author julian
 */
class BackwardStateEnumeration extends ForwardStateEnumeration{

	/**
	 * Iterates backwards through an array
	 * @author julian
	 * @param <Type>
	 */
	private static class ArrayListBackwardsIterator<Type> implements Iterator<Type>{
		private int pos;
		private final ArrayList<Type> list;
		
		public ArrayListBackwardsIterator(ArrayList<Type> list) {
			pos = list.size() - 1;
			this.list = list;			
		}
		
		@Override
		public boolean hasNext() {
			return pos >= 0;
		}
		
		@Override
		public Type next() {
			return list.get(pos--);
		}
		
		@Override
		public void remove() {
			list.remove(pos);
		}
	}
	
	public BackwardStateEnumeration(IStateTraceEntry owner) {
		super(owner);
	}
	
	@Override
	protected Iterator<ITraceEntry> iterator(IStateTraceEntry state) {		
		return new ArrayListBackwardsIterator<ITraceEntry>(state.getNestedTraceChildren());
	}
}