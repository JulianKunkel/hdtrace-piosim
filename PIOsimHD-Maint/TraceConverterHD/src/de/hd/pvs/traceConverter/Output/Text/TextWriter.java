
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.traceConverter.Output.Text;

import java.util.Properties;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;

public class TextWriter extends TraceOutputWriter {
	
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
	public void Statistics(ProcessIdentifier id, Epoch time, String name,
			ExternalStatisticsGroup group, Object value) {
		String unit = "";
		if(group.getStatistic(name).getUnit() != null){
			unit = " " + group.getStatistic(name).getUnit(); 
		}
		System.out.println(time.getFullDigitString() + " S " + id + " " + group.getName() + " " + name + " " + value + unit);
	}

	@Override
	public void addTimeline(ProcessIdentifier pid) {
	}
	
	@Override
	public void finalizeTrace() {

	}

	@Override
	public void initializeTrace(RunParameters parameters,
			String resultFile) 
	{
		Properties commandLineArguments = parameters.getOutputFileSpecificOptions();
		
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
