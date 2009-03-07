package de.hd.pvs.traceConverter.Output;

import java.util.ArrayList;
import java.util.Properties;

import de.hd.pvs.traceConverter.Input.ExternalStatisticsDescription;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;

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
	 * Initialize the resulting trace output
	 * 
	 * @param commandLineArguments Method specific Command Line Arguments.
	 * @param resultFile
	 * @param ranks
	 * @param vThreadsPerRank
	 * @param extStat
	 */
	abstract public void initializeTrace(
			Properties commandLineArguments,
			String resultFile, 
			int ranks, 
			int [] vThreadsPerRank, 
			ArrayList<ExternalStatisticsDescription> extStat
			);
	
	/**
	 * Called once all input is processed.
	 */
	abstract public void finalizeTrace();

	
	// handle states == default case
	abstract public void StateStart(ProcessIdentifier id, String stateName);
	abstract public void StateEnd(ProcessIdentifier id);

	// handle events
	abstract public void Event(ProcessIdentifier id, String eventName);
	
	// handle statistics
	abstract public void Statistics(ProcessIdentifier id, String group);
	
}
