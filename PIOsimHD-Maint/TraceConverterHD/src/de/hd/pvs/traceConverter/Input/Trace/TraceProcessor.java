package de.hd.pvs.traceConverter.Input.Trace;


import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;

public class TraceProcessor extends AbstractTraceProcessor{
	final SaxTraceFileReader reader;
	
	public TraceProcessor(final SaxTraceFileReader reader) {
		this.reader = reader;
	}
	
	@Override
	public void processEarliestEvent() {
		
	}
	
	@Override
	public Epoch peekEarliestTime() {		
		return Epoch.ZERO;
	}
	
	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
