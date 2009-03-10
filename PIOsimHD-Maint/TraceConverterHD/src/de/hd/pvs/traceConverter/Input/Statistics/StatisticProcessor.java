package de.hd.pvs.traceConverter.Input.Statistics;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Trace.SaxTraceFileReader;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

public class StatisticProcessor  extends AbstractTraceProcessor{
	final StatisticsReader reader;
	
	public StatisticProcessor(StatisticsReader reader) {
		this.reader = reader;
	}

	@Override
	public void processEarliestEvent(Epoch now) {
		
	}
	
	@Override
	public void initalize() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Epoch peekEarliestTime() {
		// TODO Auto-generated method stub
		return Epoch.ZERO;
	}	
	
	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
