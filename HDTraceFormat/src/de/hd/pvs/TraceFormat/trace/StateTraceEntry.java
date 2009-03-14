
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

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;


public class StateTraceEntry extends EventTraceEntry{

	final Epoch duration;

	LinkedList<XMLTraceEntry> nestedTraceChildren = null;
	
	
	public StateTraceEntry(String name, final HashMap<String, String> attributes,
			XMLTraceEntry parentXMLData) {
		super(name, attributes, parentXMLData);
		
		// parse common time value
		String value = attributes.remove("duration");
		if(value != null){
			duration = Epoch.parseTime(value);
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
	public TYPE getType() {		
		return TYPE.STATE;
	}
	
	public Epoch getDuration() {
		return duration;
	}
	
	public boolean hasNestedTrace(){
		return nestedTraceChildren != null && ! nestedTraceChildren.isEmpty();
	}
	
	public LinkedList<XMLTraceEntry> getNestedTraceChildren() {
		return nestedTraceChildren;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for(String key: attributes.keySet()){
			buff.append(" " + key + "=\"" + attributes.get(key) + "\"");
		}		

		if(nestedXMLTags != null){
			// print nestedXMLTags
			for(XMLTag child: nestedXMLTags){
				buff.append(" " + child);
			}
		}
		
		if(nestedTraceChildren != null){
			// print nestedXMLTags
			for(XMLTraceEntry child: nestedTraceChildren){
				buff.append(child);
			}
		}

		return getTime() + " " + getName() + buff.toString() + " /" + getName() + "\n";
	}
}
