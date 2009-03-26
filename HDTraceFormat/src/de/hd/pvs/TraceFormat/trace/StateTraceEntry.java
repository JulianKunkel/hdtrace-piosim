
//	Copyright (C) 2008, 2009 Julian M. Kunkel
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;


public class StateTraceEntry extends XMLTraceEntry{

	final Epoch endTime;

	LinkedList<XMLTraceEntry> nestedTraceChildren = null;

	/**
	 * Enumerate the children in correct time order
	 * @author julian
	 */
	private class ForwardEnumeration implements Enumeration<XMLTraceEntry>{
		Stack<StateTraceEntry> nestedChildren = new Stack<StateTraceEntry>();
		Stack<Iterator<XMLTraceEntry>> nestedIterator = new Stack<Iterator<XMLTraceEntry>>();

		protected boolean hasMoreElements;

		public ForwardEnumeration(StateTraceEntry owner) {			
			pushOnStackIfPossible(owner);
			hasMoreElements = (nestedIterator.size() != 0);			
		}

		private void pushOnStackIfPossible(StateTraceEntry entry){
			if(entry.hasNestedTraceChildren()){
				nestedChildren.push(entry);
				nestedIterator.push( iterator(entry) );
			}			
		}
		
		protected Iterator<XMLTraceEntry> iterator(StateTraceEntry entry){
			return entry.getNestedTraceChildren().iterator();
		}

		@Override
		public final boolean hasMoreElements() {
			return hasMoreElements;
		}

		@Override
		public final XMLTraceEntry nextElement() {
			Iterator<XMLTraceEntry> iter = nestedIterator.peek();

			XMLTraceEntry obj = iter.next();

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
	}

	private class BackwardEnumeration extends ForwardEnumeration{
		public BackwardEnumeration(StateTraceEntry owner) {
			super(owner);
		}
		
		@Override
		protected Iterator<XMLTraceEntry> iterator(StateTraceEntry state) {		
			return state.getNestedTraceChildren().descendingIterator();
		}
	}

	/**
	 * Walk through the children in correct time order, aka Depth First Search
	 * This includes not the parent state.
	 * @return
	 */
	public Enumeration<XMLTraceEntry> childForwardEnumeration(){
		return new ForwardEnumeration(this);
	}

	/**
	 * Walk through the children in reversed time order
	 * @return
	 */
	public Enumeration<XMLTraceEntry> childBackwardEnumeration(){
		return new BackwardEnumeration(this);
	}

	public StateTraceEntry(String name, final HashMap<String, String> attributes,
			XMLTraceEntry parentXMLData) {
		super(name, attributes, parentXMLData);

		// parse common time value
		String value = attributes.remove("end");
		if(value != null){
			endTime = Epoch.parseTime(value);
		}else{
			throw new IllegalArgumentException("Trace invalid, no time given");
		}
	}

	public void addXMLTraceChild(XMLTraceEntry child){
		if(nestedTraceChildren == null){
			nestedTraceChildren = new LinkedList<XMLTraceEntry>();
		}

		nestedTraceChildren.add(child);
	}

	@Override
	public TraceObjectType getType() {		
		return TraceObjectType.STATE;
	}

	public Epoch getDurationTime() {
		return endTime.subtract(getEarliestTime());
	}

	public double getDurationTimeDouble() {
		return endTime.getDouble() - getEarliestTime().getDouble();
	}

	public Epoch getLatestTime() {
		return endTime;
	}

	public boolean hasNestedTraceChildren(){
		return nestedTraceChildren != null && ! nestedTraceChildren.isEmpty();
	}

	public LinkedList<XMLTraceEntry> getNestedTraceChildren() {
		return nestedTraceChildren;
	}

	public String toStringWithChildren() {
		StringBuffer buff = new StringBuffer();

		if(nestedTraceChildren != null){
			// print nestedXMLTags
			for(XMLTraceEntry child: nestedTraceChildren){
				buff.append(child);
			}
		}

		return getEarliestTime() + " " + super.toString() + " " + buff.toString() + "\n";
	}
}
