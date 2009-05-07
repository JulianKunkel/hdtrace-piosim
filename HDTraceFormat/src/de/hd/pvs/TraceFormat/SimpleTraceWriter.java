
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
import java.util.HashMap;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Allows to write to HDTraceFormat via a central API.
 * All project files are automatically managed by the SimpleTraceWriter.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class SimpleTraceWriter {	
	TraceFormatWriter writer;

	/**
	 * Initialize the trace writer.
	 * 
	 * @param resultFile
	 * @param description
	 * @param applicationName
	 * @param labels
	 */
	public SimpleTraceWriter(String resultFile, String description, String applicationName, TopologyTypes topoType) {
		init(resultFile, description, applicationName, topoType);
	}
	
	public SimpleTraceWriter(String resultFile, String description, String applicationName, String [] types) {
		final TopologyTypes topoType = new TopologyTypes();
		topoType.setTopologyTypes(types);
		init(resultFile, description, applicationName, topoType);
	}
	
	private void init(String resultFile, String description, String applicationName, TopologyTypes types){
		writer = new TraceFormatWriter(resultFile, types);

		ProjectDescription outProject = writer.getProjectDescription();		
		outProject.setDescription(description);
		outProject.setApplicationName(applicationName);
		
		outProject.setTopologyTypes(types);
		
		outProject.setTopologyRoot(new TopologyNode("" ,null, "File"));
		outProject.setProjectFilename(resultFile);
	}

	/**
	 * Announce the existence of a topology, this must be done before the topology can be used.
	 *  
	 * @param topology
	 */
	public void initalizeTopology(TopologyNode topology) {
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
	
	/**
	 * Create the topology and return the TopologyNode for the last level
	 * 
	 * @param topology, hierarchy
	 * @return
	 */
	public TopologyNode initializeTopology(String [] topology){
		return writer.createInitalizeTopology(topology);
	}

	public void finalizeTrace() throws IOException {
		writer.finalizeTrace();
	}

	public void Event(TopologyNode topology, EventTraceEntry traceEntry) {
		writer.Event(topology, traceEntry);
	}
	
	public void StateEnd(TopologyNode topology, StateTraceEntry traceEntry) {
		writer.StateEnd(topology, traceEntry);
	}

	public void StateStart(TopologyNode topology, StateTraceEntry traceEntry) {
		writer.StateStart(topology, traceEntry);
	}
	
	public void Event(TopologyNode topology, String name, Epoch time) {
		Event(topology, name, time, null);
	}
	
	public void Event(TopologyNode topology, String name, Epoch time, HashMap<String, String> attributes ) {
		EventTraceEntry traceEntry = new EventTraceEntry(name, time);
		if(attributes != null){
			for(String key: attributes.keySet()){
				traceEntry.addAttribute(key, attributes.get(key));
			}
		}
		writer.Event(topology, traceEntry);
	}

	public StateTraceEntry StateStart(TopologyNode topology, String name, Epoch startTime) {
		return StateStart(topology, name, startTime, null);
	}
	
	public StateTraceEntry StateStart(TopologyNode topology, String name, Epoch startTime, HashMap<String, String> attributes) {
		StateTraceEntry state = new StateTraceEntry(name, startTime);
		if(attributes != null){
			for(String key: attributes.keySet()){
				state.addAttribute(key, attributes.get(key));
			}
		}
		
		writer.StateStart(topology, state);
		return state;
	}
	
	public void StateEnd(TopologyNode topology, Epoch endTime, StateTraceEntry traceEntry) {
		traceEntry.setEndTime(endTime);
		writer.StateEnd(topology, traceEntry);
	}
	
	public void writeStatisticsTimestamp(TopologyNode topology, StatisticsGroupDescription group, Epoch time) throws IOException{
		writer.writeStatisticsTimestamp(topology, group, time);
	}
	
	public void writeStatistics(TopologyNode topology, StatisticDescription statistic, Object value) throws IOException {
		
		if(! writer.getProjectDescription().getStatisticsGroupNames().contains(statistic.getGroup().getName())){
			writer.addStatisticGroup(statistic.getGroup().getName());
		}

		writer.Statistics(topology, statistic, value);
	}	
}
