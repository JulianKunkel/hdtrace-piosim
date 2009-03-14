package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.Trace.EventTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.StateTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.XMLTag;

/**
 * Write a single trace file.
 * 
 * @author julian
 * 
 */
public class TraceWriter {

	private final FileWriter file;

	public TraceWriter(String filename) throws IOException {
		file = new FileWriter(filename);
		
		file.write("<Program>\n");
	}

	public void finalize() {
		try {
			file.write("</Program>\n");
			file.close();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void writeAttributes(EventTraceEntry traceEntry) throws IOException{
		writeAttributes(traceEntry.getAttributes()); 
	}

	private void writeAttributes(final HashMap<String, String> attr) throws IOException{
		if(attr == null|| attr.size() == 0)
			return;
		
		for(String key: attr.keySet()){
			file.write(" " + key + "=\"" + attr.get(key) + "\"");
		}
	}
	
	public void Event(Epoch time, EventTraceEntry traceEntry) throws IOException{
		file.write("<Event name=\"" + traceEntry.getName() + "\" time=\"" + time + "\"");

		writeAttributes(traceEntry);
		
		if(traceEntry.getNestedXMLTags() != null && traceEntry.getNestedXMLTags().size() != 0 ){
			file.write(">");
			for(XMLTag nested: traceEntry.getNestedXMLTags()){
				file.write(nested.toString());
			}
			file.write("</Event>\n");
		}else{
			file.write("/>\n");
		}
	}

	/**
	 * Compute the duration for the state end.
	 * 
	 * @param time
	 * @param traceEntry
	 * @throws IOException
	 */
	public void StateEnd(Epoch time, StateTraceEntry traceEntry) throws IOException{
	}

	public void StateStart(Epoch time, StateTraceEntry traceEntry) throws IOException {
		file.write("<" + traceEntry.getName() + " time=\"" + time + "\" duration=\"" 
			+ traceEntry.getDuration() + "\"");
		writeAttributes(traceEntry);			

		if(traceEntry.getNestedXMLTags() != null && traceEntry.getNestedXMLTags().size() != 0 ){
			file.write(">");
			for(XMLTag nested: traceEntry.getNestedXMLTags()){
				file.write(nested.toString());
			}
			file.write("</" + traceEntry.getName() + ">\n");
		}else{
			file.write("/>\n");
		}
	}
}
