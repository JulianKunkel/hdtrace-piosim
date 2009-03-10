package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;

public class EventTraceEntry extends XMLTraceEntry{

	public EventTraceEntry(String name, final HashMap<String, String> attributes,
			XMLTraceEntry parentXMLData) {
		super(name, attributes, parentXMLData);		
	}
	
	@Override
	public TYPE getType() {		
		return TYPE.EVENT;
	}
	
	
}
