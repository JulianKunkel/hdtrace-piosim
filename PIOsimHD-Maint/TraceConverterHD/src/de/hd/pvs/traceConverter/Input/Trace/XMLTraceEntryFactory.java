package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;
import java.util.Iterator;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.Trace.XMLTraceEntry.TYPE;

public class XMLTraceEntryFactory {
	private static XMLTraceEntry.TYPE getType(String name){
		if(name.equals("Event")){
			return TYPE.EVENT;
		}else{
			return TYPE.STATE;
		}
	}
	
	public static XMLTraceEntry manufactureXMLTraceObject(XMLTag data, StateTraceEntry parent){
		// determine type
		final XMLTraceEntry.TYPE type = getType(data.getName());
				
		if(type == TYPE.STATE ){
			final HashMap<String, String>  attributes = data.getAttributes();
			StateTraceEntry traceObj = new StateTraceEntry(data.getName(), attributes, null);
			// parse common duration value
			String value = attributes.remove("duration");
			if(value != null)
			traceObj.setDuration(Epoch.parseTime(value));
			
			if(data.getNestedXMLTags() != null){
				//determine whether it has nested states or events:
				Iterator<XMLTag> it = data.getNestedXMLTags().iterator();
				while(it.hasNext()){
					XMLTag cur = it.next();
					if(cur.getName().equals("Nested")){
						Epoch duration = null; 
							
						it.remove();						
						// create all traceXML children & compute duration of this call:						
						for(XMLTag child: cur.getNestedXMLTags()){
							XMLTraceEntry childTraceEntry = manufactureXMLTraceObject(child, traceObj);
							traceObj.addXMLTraceChild(childTraceEntry);
							if(childTraceEntry.getType() == TYPE.STATE){
								final StateTraceEntry stateEntry = (StateTraceEntry) childTraceEntry;
								duration = stateEntry.getTime().add(stateEntry.getDuration()); 
							}
						}
						
						traceObj.setDuration(duration);						
					}else{
						traceObj.addXMLChildTag(cur);
					}
				}
			}
			if(traceObj.getTime() == null){
				throw new IllegalArgumentException("State invalid, no duration given");
			}
			
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
