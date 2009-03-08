package de.hd.pvs.traceConverter.Input;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

abstract public class AbstractTraceProcessor implements Comparable<AbstractTraceProcessor> {
	private TraceOutputConverter outputConverter;
	
	abstract public boolean isFinished();
	
	abstract public void processEarliestEvent();
	
	abstract public Epoch peekEarliestTime();
	
	public void setOutputConverter(TraceOutputConverter outputConverter) {
		this.outputConverter = outputConverter;
	}
	
	@Override
	public int compareTo(AbstractTraceProcessor o) {	
		return this.peekEarliestTime().compareTo(o.peekEarliestTime());
	}
	
}
