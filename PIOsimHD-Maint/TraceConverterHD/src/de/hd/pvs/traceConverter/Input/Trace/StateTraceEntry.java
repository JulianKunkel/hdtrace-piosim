package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.util.Epoch;


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
		return nestedTraceChildren != null;
	}
	
	public LinkedList<XMLTraceEntry> getNestedTraceChildren() {
		return nestedTraceChildren;
	}
	

	public void addXMLChildTag(XMLTag tag){
		if (nestedXMLTags == null){
			nestedXMLTags = new LinkedList<XMLTag>();
		}

		nestedXMLTags.add(tag);
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
