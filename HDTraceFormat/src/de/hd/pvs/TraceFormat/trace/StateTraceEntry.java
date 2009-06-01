
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * State, contains a start and end time and might be nested.
 * I.e. it is allowed to contain child entries. 
 * 
 * @author julian
 */
public class StateTraceEntry extends TraceEntry{

	Epoch endTime;

	/**
	 * Child trace entries.
	 */
	LinkedList<TraceEntry> nestedTraceChildren = null;

	/**
	 * Walk through the children in correct time order, (Depth First Search)
	 * This includes not the parent state.
	 * @return
	 */
	public ForwardStateEnumeration childForwardEnumeration(){
		return new ForwardStateEnumeration(this);
	}
	
	/**
	 * Walk through the children in correct time order, (Depth First Search)
	 * The least startTime the child might have is the startTime.
	 * This includes not the parent state. 
	 * @return
	 */
	public ForwardStateEnumerationStartTime childForwardEnumeration(Epoch startTime){
		return new ForwardStateEnumerationStartTime(this, startTime);
	}


	/**
	 * Walk through the children in reversed time order
	 * @return
	 */
	public Enumeration<TraceEntry> childBackwardEnumeration(){
		return new BackwardStateEnumeration(this);
	}

	/**
	 * Constructor from XML
	 * @param name
	 * @param attributes
	 */
	public StateTraceEntry(String name, final HashMap<String, String> attributes, Epoch start, Epoch end) {
		super(name, attributes, start);
		this.endTime = end;
	}
	
	/**
	 * Create a new state trace entry.
	 * @param name
	 * @param start
	 */
	public StateTraceEntry(String name, Epoch start){
		super(name, start);
	}
	
	/**
	 * Set the end time.
	 * @param endTime
	 */
	public void setEndTime(Epoch endTime) {
		this.endTime = endTime;
	}

	/**
	 * Add a nested child
	 * @param child
	 */
	public void addTraceChild(TraceEntry child){
		if(nestedTraceChildren == null){
			nestedTraceChildren = new LinkedList<TraceEntry>();
		}

		nestedTraceChildren.add(child);
	}

	@Override
	public TraceObjectType getType() {		
		return TraceObjectType.STATE;
	}

	@Override
	public Epoch getLatestTime() {
		return endTime;
	}

	public boolean hasNestedTraceChildren(){
		return nestedTraceChildren != null && ! nestedTraceChildren.isEmpty();
	}

	public LinkedList<TraceEntry> getNestedTraceChildren() {
		return nestedTraceChildren;
	}

	/**
	 * Create an informative string for the state and all contained children. 
	 * @return
	 */
	public String toStringWithChildren() {
		StringBuffer buff = new StringBuffer();

		if(nestedTraceChildren != null){
			// print nestedXMLTags
			for(TraceEntry child: nestedTraceChildren){
				buff.append(child);
			}
		}

		return getEarliestTime() + " " + super.toString() + " " + buff.toString() + "\n";
	}
}
