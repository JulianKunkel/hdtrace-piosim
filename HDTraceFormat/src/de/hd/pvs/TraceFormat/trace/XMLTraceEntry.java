
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

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Container from data read from the trace file.
 * @author Julian M. Kunkel
 *
 */
public abstract class XMLTraceEntry extends XMLTag {
	public static enum TYPE{
		STATE,
		EVENT
	};

	/**
	 * when did the event/state etc. occur
	 */
	final Epoch time;

	abstract public TYPE getType();

	private final XMLTraceEntry parentXMLData; 
	
	public XMLTraceEntry getParentTraceData() {
		return parentXMLData;
	}
	
	public boolean isTraceChild(){
		return parentXMLData != null;
	}
	
	public XMLTraceEntry(final String name, final HashMap<String, String> attributes, XMLTraceEntry parentXMLData) {
		super(name, attributes, null);		
		this.parentXMLData  = parentXMLData;

		// parse common time value
		String value = attributes.remove("time");
		if(value != null){
			time = Epoch.parseTime(value);
		}else{
			throw new IllegalArgumentException("Trace invalid, no time given");
		}

	}


	public Epoch getTime() {
		return time;
	}

}
