package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.util.Epoch;

/**
 * Container from data read from the trace file.
 * @author julian
 *
 */
public abstract class XMLTraceEntry {
	public static enum TYPE{
		STATE,
		EVENT
	};

	/**
	 * when did the event/state etc. occur
	 */
	final Epoch time;

	final HashMap<String, String> attributes;

	/** 
	 * DOM like for children.
	 */
	LinkedList<XMLTag> nestedXMLTags = null;

	private final String name;

	abstract public TYPE getType();


	private final XMLTraceEntry parentXMLData; 
	
	public XMLTraceEntry getParentTraceData() {
		return parentXMLData;
	}
	
	public boolean isTraceChild(){
		return parentXMLData != null;
	}
	
	public XMLTraceEntry(final String name, final HashMap<String, String> attributes, XMLTraceEntry parentXMLData) {
		this.attributes = attributes;
		this.name = name;
		this.parentXMLData  = parentXMLData;


		// parse common time value
		String value = attributes.remove("time");
		if(value != null){
			time = Epoch.parseTime(value);
		}else{
			throw new IllegalArgumentException("Trace invalid, no time given");
		}

	}

	public void setNestedXMLTags(LinkedList<XMLTag> nestedXMLTags) {
		this.nestedXMLTags = nestedXMLTags;
	}
	
	public LinkedList<XMLTag> getNestedXMLTags() {
		return nestedXMLTags;
	}


	public Epoch getTime() {
		return time;
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public String getAttribute(String attribute){
		return attributes.get(attribute);
	}

	public String getName() {
		return name;
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
				buff.append(child);
			}
		}
		
		return getTime() + " " + getName() + buff.toString() + "/" + getName() + "\n";
	}
}
