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
