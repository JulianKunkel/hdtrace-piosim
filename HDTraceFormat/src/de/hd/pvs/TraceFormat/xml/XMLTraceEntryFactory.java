
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

package de.hd.pvs.TraceFormat.xml;

import java.util.HashMap;
import java.util.Iterator;

import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry.TYPE;
import de.hd.pvs.TraceFormat.util.Epoch;

public class XMLTraceEntryFactory {
	private static XMLTraceEntry.TYPE getType(String name){
		if(name.equals("Event")){
			return TYPE.EVENT;
		}else{
			return TYPE.STATE;
		}
	}
	
	/**
	 * Manufactures top level XML entries.
	 * 
	 * @param data
	 * @param nestedData
	 * @return
	 */
	public static XMLTraceEntry manufactureXMLTraceObject(XMLTag data, StateTraceEntry parent, XMLTag nestedData){		
		//System.out.println(data.getName() + " ");
		//if(parent != null)
		//	System.out.println(" p: " + parent.getName());
		
		XMLTraceEntry traceObject = manufactureXMLTraceObject(data, parent);
		
		if(nestedData != null){
			// type must be state:
			StateTraceEntry traceObj = (StateTraceEntry) traceObject;
			// create all traceXML children
			XMLTag newNestedData = null;
			for(XMLTag child: nestedData.getNestedXMLTags()){
				if(child.getName().equals("Nested")){
					newNestedData = child;
				}else{
					XMLTraceEntry childTraceEntry = manufactureXMLTraceObject(child, traceObj, newNestedData);
					newNestedData = null;
					traceObj.addXMLTraceChild(childTraceEntry);
				}
			}
			
			if(newNestedData != null){
				throw new IllegalArgumentException("Nested data, but no parent to attach to: " + newNestedData + " inside data " + data );
			}
		}
		
		return traceObject;
	}
	 

	public static XMLTraceEntry manufactureXMLTraceObject(XMLTag data, StateTraceEntry parent){
		// determine type
		final XMLTraceEntry.TYPE type = getType(data.getName());

		if(type == TYPE.STATE ){
			final HashMap<String, String>  attributes = data.getAttributes();
			StateTraceEntry traceObj = new StateTraceEntry(data.getName(), attributes, parent);
			traceObj.setNestedXMLTags(data.getNestedXMLTags());

			return traceObj;
		}else if (type == TYPE.EVENT){
			// strip of the real name
			String name = data.getAttributes().remove("name");
			if( name == null || name.length() < 2){
				throw new IllegalArgumentException("Event invalid");
			}
			EventTraceEntry traceObj= new EventTraceEntry(name, data.getAttributes(), parent);
			traceObj.setNestedXMLTags(data.getNestedXMLTags());
			return traceObj;
		}else{
			return null;
		}
	}
}
