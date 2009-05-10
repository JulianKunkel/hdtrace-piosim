
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

package de.hd.pvs.TraceFormat.xml;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class XMLTraceEntryFactory {
	private static TraceObjectType getType(String name){
		if(name.equals("Event")){
			return TraceObjectType.EVENT;
		}else{
			return TraceObjectType.STATE;
		}
	}
	
	/**
	 * Manufactures top level XML entries.
	 * 
	 * @param data
	 * @param nestedData
	 * @return
	 */
	public static TraceEntry manufactureXMLTraceObject(XMLTag data, StateTraceEntry parent, XMLTag nestedData, Epoch timeAdjustment){		
		final TraceEntry traceObject = manufactureXMLTraceObject(data, parent, timeAdjustment);
		
		if(nestedData != null){
			// type must be state:
			StateTraceEntry traceObj = (StateTraceEntry) traceObject;
			// create all traceXML children
			XMLTag newNestedData = null;
			for(XMLTag child: nestedData.getNestedXMLTags()){
				if(child.getName().equals("Nested")){
					newNestedData = child;
				}else{					
					TraceEntry childTraceEntry = manufactureXMLTraceObject(child, traceObj, newNestedData, timeAdjustment);
					newNestedData = null;
					traceObj.addTraceChild(childTraceEntry);
				}
			}
			
			if(newNestedData != null){
				throw new IllegalArgumentException("Nested data, but no parent to attach to: " + newNestedData + " inside data " + data );
			}
		}
		
		return traceObject;
	}
	 

	public static TraceEntry manufactureXMLTraceObject(XMLTag data, StateTraceEntry parent, Epoch timeAdjustment){
		// determine type
		final TraceObjectType type = getType(data.getName());

		final HashMap<String, String>  attributes = data.getAttributes();

		// parse common time value
		final String timeStr = attributes.get("time");
		final Epoch time;
		if(timeStr != null){
			time = Epoch.parseTime(timeStr).add(timeAdjustment);
		}else{
			throw new IllegalArgumentException("Trace invalid, no time given");
		}

		if(type == TraceObjectType.STATE ){
			// parse common time value
			final String endTimeStr = attributes.get("end");
			final Epoch endTime;
			if(endTimeStr != null){
				endTime = Epoch.parseTime(endTimeStr).add(timeAdjustment);
			}else{
				throw new IllegalArgumentException("Trace invalid, no end time given");
			}
			
			StateTraceEntry traceObj = new StateTraceEntry(data.getName(), attributes, time, endTime);
			traceObj.setNestedXMLTags(data.getNestedXMLTags());
			
			return traceObj;
			
		}else if (type == TraceObjectType.EVENT){
			
			// strip of the real name
			String name = data.getAttributes().remove("name");
			if( name == null || name.length() < 2){
				throw new IllegalArgumentException("Event invalid");
			}
			EventTraceEntry traceObj= new EventTraceEntry(name, attributes, time);
			traceObj.setNestedXMLTags(data.getNestedXMLTags());
			return traceObj;
		}else{
			return null;
		}
	}
}
