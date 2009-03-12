package de.hd.pvs.traceConverter.Output.Text;

import java.util.Properties;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
import de.hd.pvs.traceConverter.Input.Trace.EventTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.StateTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.XMLTag;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

public class TextConverter extends TraceOutputConverter {
	
	/**
	 * Print detailed information about states and events during state start / event.
	 */
	boolean printDetails = false;

	@Override
	public void Event(ProcessIdentifier id, Epoch time,
			EventTraceEntry traceEntry) {
		System.out.println(time.getFullDigitString() + " E " + id + " " + traceEntry.getName());
		
		if(printDetails && traceEntry.getNestedXMLTags() != null){
			for(XMLTag nested: traceEntry.getNestedXMLTags()){
				System.out.print(nested);
			}
		}
	}
	
	@Override
	public void StateEnd(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		System.out.println(time.getFullDigitString() + " E " + id + " " + traceEntry.getName());		
	}
	
	@Override
	public void StateStart(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		System.out.println(time.getFullDigitString() + " < " + id + " " + traceEntry.getName());

		if(printDetails && traceEntry.getNestedXMLTags() != null){
			for(XMLTag nested: traceEntry.getNestedXMLTags()){
				System.out.print(nested);
			}
		}
	}

	@Override
	public void Statistics(ProcessIdentifier id, Epoch time, String group,
			String name, StatisticType type, Object value) {
		System.out.println(time.getFullDigitString() + " S " + id + " " + group + " " + name + " " + value);
	}

	@Override
	public void addTimeline(int rank, int thread, String name) {
		
	}
	
	@Override
	public void finalizeTrace() {

	}

	@Override
	public void initializeTrace(Properties commandLineArguments,
			String resultFile) 
	{
		if(commandLineArguments.get("-h") != null || commandLineArguments.get("-?") != null){
			System.err.println("TextConverter Arguments: ");
			System.err.println(" -v Print content of events/states on start");
			System.exit(1);
		}
		
		if(commandLineArguments.get("-v") != null){
			printDetails = true;
		}
	}

}
