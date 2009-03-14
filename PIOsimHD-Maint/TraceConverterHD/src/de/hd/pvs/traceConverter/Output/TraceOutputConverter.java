package de.hd.pvs.traceConverter.Output;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.HDTraceConverter;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Trace.EventTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.StateTraceEntry;

/**
 * An implementation of the TraceOutputConverter decides how to 
 * use the data provided inside the XML trace.
 * The methods of the Converter are called to preserve increasing time in the trace.
 * 
 * @author julian
 *
 */
abstract public class TraceOutputConverter {
		
	/**
	 * Initialize the resulting trace output, called by the  {@link HDTraceConverter}
	 * 
	 * @param commandLineArguments Method specific Command Line Arguments.
	 * @param resultFile
	 * @param extStat
	 */
	abstract public void initializeTrace(
			RunParameters parameters,
			String resultFile
			);

	/**
	 * Called by the {@link HDTraceConverter} once all input is processed.
	 */
	abstract public void finalizeTrace();
	
	
	/**
	 * Announce the existence of a rank/thread line for events. Called by the TraceProcessor
	 */
	abstract public void addTimeline(ProcessIdentifier pid);
	
	/**
	 * Add the normal timeline.
	 * @param pid
	 */
	final public void addNormalTimeline(ProcessIdentifier pid){
		addTimeline(pid);
	}

	
	// handle states == default case
	abstract public void StateStart(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);
	abstract public void StateEnd(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);

	// handle events
	abstract public void Event(ProcessIdentifier id,Epoch time, EventTraceEntry traceEntry);
	
	// handle statistics
	abstract public void Statistics(ProcessIdentifier id, Epoch time, String statistic, ExternalStatisticsGroup group, Object value);
	
}
