package de.hd.pvs.traceConverter.Input.Trace;


import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;

/**
 * Reads data from a XML trace and triggers the appropriate Start/Stop Event/State calls. 
 * 
 * @author julian
 *
 */
public class TraceProcessor extends AbstractTraceProcessor{
	final SaxTraceFileReader reader;
	
	private XMLTraceEntry nextTraceEntry = null;
	
	public TraceProcessor(final SaxTraceFileReader reader) {
		this.reader = reader;
		
		nextTraceEntry = reader.getNextInputData();
	}
		
	@Override
	public void processEarliestEvent() {
		System.out.println("processing " + nextTraceEntry.getName() + " t " + nextTraceEntry.getTime());
		
		nextTraceEntry = reader.getNextInputData();
	}
	
	@Override
	public Epoch peekEarliestTime() {		
		return nextTraceEntry.getTime();
	}
	
	@Override
	public boolean isFinished() {
		return nextTraceEntry == null;
	}
	
}
