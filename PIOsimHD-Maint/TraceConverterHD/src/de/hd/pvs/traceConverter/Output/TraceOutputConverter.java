package de.hd.pvs.traceConverter.Output;

import java.util.ArrayList;
import java.util.Properties;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.HDTraceConverter;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
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
			Properties commandLineArguments,
			String resultFile
			);

	/**
	 * Called by the {@link HDTraceConverter} once all input is processed.
	 */
	abstract public void finalizeTrace();
	
	
	/**
	 * Announce the existence of a rank/timeline(thread) line for events. Called by the TraceProcessor
	 * 
	 * @param suggestedRank process
	 * @param suggestedTimeline aka vthread.
	 * 
	 * @param name The id of the new timeline
	 */
	abstract public void addTimeline(int rank, int thread, String name);
	
	/**
	 * Add the normal timeline.
	 * @param pid
	 */
	final public void addNormalTimeline(ProcessIdentifier pid){
		addTimeline(pid.getRank(), pid.getVthread(),
				pid.getRank() + "-" + pid.getVthread() );
	}

	
	// handle states == default case
	abstract public void StateStart(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);
	abstract public void StateEnd(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);

	// handle events
	abstract public void Event(ProcessIdentifier id,Epoch time, EventTraceEntry traceEntry);
	
	// handle statistics
	abstract public void Statistics(ProcessIdentifier id, Epoch time, String group, String name, StatisticType type, Object value);
	
}
