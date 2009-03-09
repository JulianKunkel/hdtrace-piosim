package de.hd.pvs.traceConverter.Input;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

/**
 * An {@link AbstractTraceProcessor} fetches data from a trace source i.e. a particular file and
 * manufactures trace events from the trace file for a {@link TraceOutputConverter}.
 * 
 * Trace events processed by a {@link AbstractTraceProcessor} must be returned with an increasing start time.
 * 
 * @author julian
 *
 */
abstract public class AbstractTraceProcessor implements Comparable<AbstractTraceProcessor> {
	/**
	 * The implementation of the {@link TraceOutputConverter} i.e. the file format and spec.
	 */
	private TraceOutputConverter outputConverter;
	
	/**
	 * Are there more events to process
	 * @return
	 */
	abstract public boolean isFinished();
	
	/**
	 * Return the next "trace event" to process.
	 */
	abstract public void processEarliestEvent();
	
	/**
	 * Return the next event in the trace source.
	 * 
	 * @return
	 */
	abstract public Epoch peekEarliestTime();
	
	/**
	 * Set the implementation of the {@link TraceOutputConverter} i.e. the file format and spec.
	 * @param outputConverter
	 */
	public void setOutputConverter(TraceOutputConverter outputConverter) {
		this.outputConverter = outputConverter;
	}
	
	@Override
	public int compareTo(AbstractTraceProcessor o) {	
		return this.peekEarliestTime().compareTo(o.peekEarliestTime());
	}
	
}
