
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

import java.util.ArrayList;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
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
	 * Manufactures top level XML entries. Removes nested elements and create real trace entries for them.
	 * 
	 * @param traceXML
	 * @param nestedData
	 * @return
	 */
	public static ITraceEntry manufactureXMLTraceObject(XMLTag traceXML, Epoch timeAdjustment){
		final ArrayList<ITraceEntry> children;
		final ArrayList<XMLTag> data;
		if(traceXML.getNestedXMLTags() != null){
			// type must be state:
			children = new ArrayList<ITraceEntry>(0);
			data = new ArrayList<XMLTag>(0);
			
			// create all traceXML children			
			for(XMLTag nestedTag: traceXML.getNestedXMLTags()){
				if(nestedTag.getName().equals("Nested")){				
					for(XMLTag child: nestedTag.getNestedXMLTags()){
						ITraceEntry childTraceEntry = manufactureXMLTraceObject(child, timeAdjustment);
						children.add(childTraceEntry);
					}
				}else{
					data.add(nestedTag);
				}
			}
		}else{
			children = null;
			data = null;
		}

		final ITraceEntry traceObject = manufactureXMLTraceObject(traceXML, data, timeAdjustment, children);

		return traceObject;
	}


	public static ITraceEntry manufactureXMLTraceObject(XMLTag traceXML, ArrayList<XMLTag> data, Epoch timeAdjustment, ArrayList<ITraceEntry> children){
		// determine type
		final TraceObjectType type = getType(traceXML.getName());

		final HashMap<String, String>  attributes = traceXML.getAttributes();

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

			StateTraceEntry traceObj = new StateTraceEntry(traceXML.getName(), 
					attributes, time, endTime, children, data);

			return traceObj;

		}else if (type == TraceObjectType.EVENT){

			// strip of the real name
			String name = traceXML.getAttributes().remove("name");
			if( name == null || name.length() < 2){
				throw new IllegalArgumentException("Event invalid");
			}
			EventTraceEntry traceObj= new EventTraceEntry(name, attributes, time, data);
			return traceObj;
		}else{
			return null;
		}
	}
}
