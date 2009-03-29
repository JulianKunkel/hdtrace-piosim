
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;

public class TextWriter extends TraceOutputWriter {

	/**
	 * Print detailed information about states and events during state start / event.
	 */
	boolean printDetails = false;

	private BufferedWriter writer;

	@Override
	public void Event(TopologyEntry topology, EventTraceEntry traceEntry) {
		try {
			writer.append(traceEntry.getEarliestTime().getFullDigitString() + 
					" E " + topology.toRecursiveString() + " " + traceEntry.getName() + "\n");

			if(printDetails && traceEntry.getNestedXMLTags() != null){
				for(XMLTag nested: traceEntry.getNestedXMLTags()){
					writer.append(nested + "\n"); 
				}
			}

		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}


	}

	@Override
	public void StateEnd(TopologyEntry topology, StateTraceEntry traceEntry) {
		try {
			writer.append(traceEntry.getLatestTime().getFullDigitString() + " > " + 
					topology.toRecursiveString() + " " + traceEntry.getName() + "\n");
		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void StateStart(TopologyEntry topology,
			StateTraceEntry traceEntry) {
		try {
			writer.append(traceEntry.getEarliestTime().getFullDigitString() + " < " + topology.toRecursiveString() + " " + traceEntry.getName() + "\n");

			if(printDetails && traceEntry.getNestedXMLTags() != null){
				for(XMLTag nested: traceEntry.getNestedXMLTags()){
					writer.append("\tD: " + nested + "\n");
				}
			}
		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void Statistics(TopologyEntry topology, Epoch time, String name,
			StatisticsGroupDescription group, Object value) {
		String unit = "";
		if(group.getStatistic(name).getUnit() != null){
			unit = " " + group.getStatistic(name).getUnit(); 
		}
		try {
			writer.append(time.getFullDigitString() + " S " + topology.toRecursiveString() + " " + group.getName() + " " + name + " " + value + unit + "\n");
		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void initalizeForTopology(TopologyEntry topology) {
	}

	@Override
	public void finalizeTrace() throws IOException{
		writer.close();
	}

	@Override
	public void initializeTrace(RunParameters parameters,
			String resultFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(resultFile));

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
