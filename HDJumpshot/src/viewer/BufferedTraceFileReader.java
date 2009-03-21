package viewer;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedTraceFileReader extends StAXTraceFileReader implements IBufferedReader {

	private Epoch minTime;
	private Epoch maxTime;
	
	ArrayList<XMLTraceEntry> traceEntries = new ArrayList<XMLTraceEntry>();
	
	public BufferedTraceFileReader(String filename, boolean nested) throws Exception {
		super(filename, nested);
		
		XMLTraceEntry current = readNextInputEntry();
		
		minTime = current.getTimeStamp();
		
		while(current != null){
			traceEntries.add(current);

			current = readNextInputEntry();
		}
		
		maxTime = traceEntries.get(traceEntries.size()-1).getTimeStamp();
	}
	
	public ArrayList<XMLTraceEntry> getTraceEntries() {
		return traceEntries;
	}
	
	public Epoch getMinTime() {
		return minTime;
	}
	
	public Epoch getMaxTime() {
		return maxTime;
	}
}
