
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

package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.IOException;
import java.util.List;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;

/**
 * Exports the XML data to a HDTrace project.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class HDTraceWriter extends TraceOutputWriter {	
	TraceFormatWriter writer;

	public void initalizeProjectDescriptionWithOldValues(String resultFile, ProjectDescription oldDescription, List<XMLTag> unparsedTagsToWrite){
		writer = new TraceFormatWriter(resultFile,  oldDescription.getTopologyTypes());
		writer.setUnparsedTagsToWrite(unparsedTagsToWrite);

		ProjectDescription outProject = writer.getProjectDescription();		
		outProject.setDescription(oldDescription.getDescription());
		outProject.setApplicationName(oldDescription.getName());
		
		// TODO fix for on the fly generated topologies. Right now just use the old ones.
		outProject.setTopologyRoot(oldDescription.getTopologyRoot());		
	}

	@Override
	public void initializeTrace(RunParameters parameters, String resultFile) {
		parameters.setProcessAlsoComputeEvents(true);
	}

	@Override
	public void initalizeForTopology(TopologyNode topology) {
		// check existence of parent topology in registered topology.		

		for(TopologyNode parent: topology.getParentTopologies()){
			try{
				writer.initalizeTopology(parent);
			}catch(IllegalArgumentException e){
				// 
			}
		}
		writer.initalizeTopology(topology);
	}

	@Override
	public void Event(TopologyNode topology,	EventTraceEntry traceEntry) {
		writer.Event(topology, traceEntry);
	}

	@Override
	public void finalizeTrace() throws IOException {
		writer.finalizeTrace();
	}

	@Override
	public void StateEnd(TopologyNode topology, StateTraceEntry traceEntry) {
		writer.StateEnd(topology, traceEntry);
	}

	@Override
	public void StateStart(TopologyNode topology, StateTraceEntry traceEntry) {
		writer.StateStart(topology, traceEntry);
	}

	@Override
	public void Statistics(TopologyNode topology, Epoch time, StatisticDescription statistic, Object value) {
		
		if(! writer.getProjectDescription().getStatisticsGroupNames().contains(statistic.getGroup().getName())){
			writer.addStatisticGroup(statistic.getGroup().getName());
		}

		try{
			writer.Statistics(topology, time, statistic, value);
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}	
}
