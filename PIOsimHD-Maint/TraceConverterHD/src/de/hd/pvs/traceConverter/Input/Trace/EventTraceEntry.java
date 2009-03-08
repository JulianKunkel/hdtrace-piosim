package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;

public class EventTraceEntry extends XMLTraceEntry{

	private final XMLTraceEntry parentXMLData; 
	
	public EventTraceEntry(String name, final HashMap<String, String> attributes,
			XMLTraceEntry parentXMLData) {
		super(name, attributes);
		
		this.parentXMLData = parentXMLData;
	}
	
	@Override
	public TYPE getType() {		
		return TYPE.EVENT;
	}
	

	public XMLTraceEntry getParentTraceData() {
		return parentXMLData;
	}
	
	public boolean isTraceChild(){
		return parentXMLData != null;
	}
	
}
