
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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;

import de.hd.pvs.TraceFormat.TraceObjectType;

/**
 * Enumerate the nested elements of a state in correct time order. 
 * 
 * @author Julian M. Kunkel
 */
public class ForwardStateEnumeration implements Enumeration<TraceEntry>{
	private Stack<StateTraceEntry> nestedChildren = new Stack<StateTraceEntry>();
	private Stack<Iterator<TraceEntry>> nestedIterator = new Stack<Iterator<TraceEntry>>();
	
	protected boolean hasMoreElements;

	public ForwardStateEnumeration(StateTraceEntry owner) {			
		pushOnStackIfPossible(owner);
		hasMoreElements = (nestedIterator.size() != 0);
	}

	private void pushOnStackIfPossible(StateTraceEntry entry){
		if(entry.hasNestedTraceChildren()){
			nestedChildren.push(entry);
			nestedIterator.push( iterator(entry) );
		}			
	}
	
	protected Iterator<TraceEntry> iterator(StateTraceEntry entry){
		return entry.getNestedTraceChildren().iterator();
	}

	@Override
	public boolean hasMoreElements() {
		return hasMoreElements;
	}

	
	@Override
	public TraceEntry nextElement() {
		Iterator<TraceEntry> iter = nestedIterator.peek();

		TraceEntry obj = iter.next();

		if(obj.getType() == TraceObjectType.STATE){
			// we have to do a DFS, therefore go into this one:
			StateTraceEntry state = (StateTraceEntry) obj;
			pushOnStackIfPossible(state);
		}

		// update iter value:
		iter = nestedIterator.peek();			
		while(! iter.hasNext()){
			// pop from stack
			nestedIterator.pop();
			nestedChildren.pop();
			if(nestedChildren.size() == 0){
				hasMoreElements = false;
				break;
			}

			iter = nestedIterator.peek();				
		}

		return obj;
	}

	/**
	 * Return the nesting depth of the element which will be returned next by <code>nextElement()</code> 
	 * @return
	 */
	public int getNestingDepthOfNextElement() {
		return nestedChildren.size();
	}
}