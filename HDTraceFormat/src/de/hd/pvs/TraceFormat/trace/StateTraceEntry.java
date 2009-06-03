
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * State, contains a start and end time and might be nested.
 * I.e. it is allowed to contain child entries. 
 * 
 * @author julian
 */
public class StateTraceEntry extends TraceEntry implements IStateTraceEntry{

	private final Epoch endTime;

	/**
	 * Child trace entries.
	 */
	private final ArrayList<ITraceEntry> nestedTraceChildren;

	@Override
	public ForwardStateEnumeration childForwardEnumeration(){
		return new ForwardStateEnumeration(this);
	}

	@Override
	public ForwardStateEnumerationStartTime childForwardEnumeration(Epoch startTime){
		return new ForwardStateEnumerationStartTime(this, startTime);
	}

	@Override
	public Enumeration<ITraceEntry> childBackwardEnumeration(){
		return new BackwardStateEnumeration(this);
	}

	/**
	 * Constructor from XML
	 * @param name
	 * @param attributes
	 */
	public StateTraceEntry(String name, final HashMap<String, String> attributes, 
			Epoch start, Epoch end, ArrayList<ITraceEntry> nestedTraceChildren, ArrayList<XMLTag> nestedData) {
		super(name, attributes, start, nestedData);
		
		this.endTime = end;
		this.nestedTraceChildren = nestedTraceChildren;
	}
	
	@Override
	public TraceObjectType getType() {		
		return TraceObjectType.STATE;
	}

	@Override
	public Epoch getLatestTime() {
		return endTime;
	}

	@Override
	public boolean hasNestedTraceChildren(){
		return nestedTraceChildren != null && ! nestedTraceChildren.isEmpty();
	}

	@Override
	public ArrayList<ITraceEntry> getNestedTraceChildren() {
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
			for(ITraceEntry child: nestedTraceChildren){
				buff.append(child);
			}
		}

		return getEarliestTime() + " " + super.toString() + " " + buff.toString() + "\n";
	}
}
