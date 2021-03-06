
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
import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Contains the information about a single trace entry. Superclass for StateTraceEntry and
 * EventTraceEntry.
 * 
 * @author Julian M. Kunkel
 *
 */
public abstract class TraceEntry extends XMLTag implements ITraceEntry{
	/**
	 * when did the event/state etc. occur
	 */
	final Epoch time;
		
	/**
	 * Constructor from XML.
	 * @param name
	 * @param attributes
	 */
	public TraceEntry(final String name, final HashMap<String, String> attributes, final Epoch time, final ArrayList<XMLTag> nestedData) {
		super(name, attributes, null, nestedData);
		this.time = time;
	}
	
	/**
	 * Constructor
	 * @param name
	 * @param attributes
	 */
	public TraceEntry(final String name, final Epoch time) {
		super(name, new HashMap<String, String>(), null, null);
		this.time = time;
	}

	@Override
	final public Epoch getEarliestTime() {
		return time;
	}
	
	@Override
	final public Epoch getDurationTime() {
		return getLatestTime().subtract(getEarliestTime());
	}
	
	@Override
	final public ArrayList<XMLTag> getContainedXMLData() {
		return super.getNestedXMLTags();
	}
}
