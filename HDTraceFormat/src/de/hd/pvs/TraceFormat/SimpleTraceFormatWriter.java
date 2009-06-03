
 /** Version Control Information $Id: HDTraceWriter.java 276 2009-05-04 16:59:13Z kunkel $
  * @lastmodified    $Date: 2009-05-04 18:59:13 +0200 (Mo, 04. Mai 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 276 $ 
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

package de.hd.pvs.TraceFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Allows to write to HDTraceFormat via a central API.
 * Compared to the (central) TraceFormatWriter this class simplifies the write process by
 * providing direct methods.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class SimpleTraceFormatWriter {	

	final TraceFormatWriter writer;
	
	/**
	 * Initialize the trace writer.
	 * 
	 * @param resultFile
	 * @param description
	 * @param applicationName
	 * @param labels
	 */
	public SimpleTraceFormatWriter(String projectfile, String description, String applicationName, String [] types) {
		writer = new TraceFormatWriter(projectfile, description, applicationName, types);
	}
	

	/**
	 * Create the topology in the project description. Uses the default labels for each topology node.
	 * All parent topologies required are created.
	 * @param topology
	 * @return
	 */
	public TopologyNode createInitalizeTopology(String [] topology){
		final ProjectDescription outProject = writer.getProjectDescription();
		
		TopologyNode cur = outProject.getTopologyRoot();
		
		if (topology.length > outProject.getTopologyTypes().getTypes().size()){
			throw new IllegalArgumentException("Topology labels are not as deep as the topology " +
					"you want to create: " + topology.length + " labels: " + 
					outProject.getTopologyTypes().getTypes().size() );
		}
		for(int p = 0 ; p < topology.length; p++){
			// check if parents exist
			final TopologyNode child = cur.getChild(topology[p]);
			if( child == null){
				// create all the next ones.
				for(int n = p ; n < topology.length; n++){
					cur = new TopologyNode(topology[n], cur, outProject.getTopologyType(p));
					writer.initalizeTopologyInternal(cur);
				}
				return cur;
			}
			cur = child;
		}
		return cur;
	}
	
	public void writeEvent(TopologyNode topology, String name, Epoch time)  throws IOException {
		writeEvent(topology, name, time, null, null);
	}
	
	public void writeEvent(TopologyNode topology, String name, Epoch time, HashMap<String, String> attributes,  final ArrayList<XMLTag> nestedData )  throws IOException {
		EventTraceEntry traceEntry = new EventTraceEntry(name, attributes, time, nestedData);
		writer.writeEvent(topology, traceEntry);
	}
	
	public void writeStateStart(TopologyNode topology, String name, Epoch startTime) throws IOException {
		writer.writeStateStart(topology, name, startTime);
	}
	
	public void writeStateEnd(TopologyNode topology, String name, Epoch endTime, HashMap<String, String> attributes, ArrayList<XMLTag> xmldata)  throws IOException {
		writer.writeStateEnd(topology, name, endTime, attributes, xmldata);
	}
	
	public void writeStateEnd(TopologyNode topology, String name, Epoch endTime)  throws IOException {
		writeStateEnd(topology, name, endTime, null, null);
	}


	public void writeStatisticsTimestamp(TopologyNode topology, StatisticsGroupDescription group, Epoch time) throws IOException{
		// add the group to the writer if necessary:
		if(! writer.getProjectDescription().getStatisticsGroupNames().contains(group.getName())){
			writer.addStatisticGroup(group.getName());
		}

		writer.writeStatisticsTimestamp(topology, group, time);
	}

	public void writeStatisticValue(TopologyNode topology, StatisticsDescription statistic, Object value) throws IOException {
		writer.writeStatisticValue(topology, statistic, value);
	}
	
	public void finalizeTrace() throws IOException{
		writer.finalizeTrace();
	}

}
