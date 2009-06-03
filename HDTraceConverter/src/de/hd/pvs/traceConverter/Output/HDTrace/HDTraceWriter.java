
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
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
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
		writer.setUnparsedProjectTagsToWrite(unparsedTagsToWrite);

		ProjectDescription outProject = writer.getProjectDescription();		
		outProject.setDescription(oldDescription.getDescription());
		outProject.setApplicationName(oldDescription.getName());
		
		// TODO fix for on the fly generated topologies. Right now just use the old ones.
		outProject.setTopologyRoot(oldDescription.getTopologyRoot());		
	}

	@Override
	public void initializeTrace(RunParameters parameters, String resultFile) {

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
		try{
			writer.initalizeTopology(topology);
		}catch(IllegalArgumentException e){
			//
		}
	}

	@Override
	public void Event(TopologyNode topology,	IEventTraceEntry traceEntry) throws IOException {
		writer.writeEvent(topology, traceEntry);
	}

	@Override
	public void finalizeTrace() throws IOException {
		writer.finalizeTrace();
	}

	@Override
	public void StateEnd(TopologyNode topology, IStateTraceEntry traceEntry)  throws IOException {
		writer.writeStateEnd(topology, traceEntry.getName(), traceEntry.getLatestTime(), traceEntry.getAttributes(), traceEntry.getContainedXMLData());
	}

	@Override
	public void StateStart(TopologyNode topology, String name, Epoch startTime)
			throws IOException {	
		writer.writeStateStart(topology, name, startTime);
	}

	@Override
	public void Statistics(TopologyNode topology, StatisticsGroupEntry entry) {		
		if(! writer.getProjectDescription().getStatisticsGroupNames().contains(entry.getGroup().getName())){
			writer.addStatisticGroup(entry.getGroup().getName());
		}

		try{
			writer.writeStatisticsGroupEntry(topology, entry);
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}	
}
