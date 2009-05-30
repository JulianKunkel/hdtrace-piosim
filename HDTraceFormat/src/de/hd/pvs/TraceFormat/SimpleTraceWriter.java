
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
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
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
public class SimpleTraceWriter extends TraceFormatWriter {	

	/**
	 * Initialize the trace writer.
	 * 
	 * @param resultFile
	 * @param description
	 * @param applicationName
	 * @param labels
	 */
	public SimpleTraceWriter(String resultFile, String description, String applicationName, TopologyTypes topoType) {
		super(resultFile, topoType);
		init(resultFile, description, applicationName);
	}
	
	public SimpleTraceWriter(String resultFile, String description, String applicationName, String [] types) {
		super(resultFile, null);
		final TopologyTypes topoTypes = new TopologyTypes();
		topoTypes.setTopologyTypes(types);
		init(resultFile, description, applicationName);
	}
	
	private void init(String resultFile, String description, String applicationName){
		ProjectDescription outProject = getProjectDescription();		
		outProject.setDescription(description);
		outProject.setApplicationName(applicationName);
		
		outProject.setTopologyRoot(new TopologyNode("" ,null, "File"));
		outProject.setProjectFilename(resultFile);
	}

	@Override
	public void initalizeTopology(TopologyNode topology) {
		// check existence of parent topology in registered topology.		

		for(TopologyNode parent: topology.getParentTopologies()){
			try{
				super.initalizeTopology(parent);
			}catch(IllegalArgumentException e){
				// 
			}
		}
		super.initalizeTopology(topology);
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
		super.Event(topology, traceEntry);
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
		
		super.StateStart(topology, state);
		return state;
	}
	
	public void StateEnd(TopologyNode topology, Epoch endTime, StateTraceEntry traceEntry) {
		traceEntry.setEndTime(endTime);
		super.StateEnd(topology, traceEntry);
	}

	@Override
	public void writeStatisticValue(TopologyNode topology, StatisticDescription statistic, Object value) throws IOException {
		
		if(!getProjectDescription().getStatisticsGroupNames().contains(statistic.getGroup().getName())){
			super.addStatisticGroup(statistic.getGroup().getName());
		}

		super.writeStatisticValue(topology, statistic, value);
	}
	

	@Override
	public void writeStatistics(TopologyNode topology, StatisticGroupEntry entry) throws IOException{
		final StatisticsGroupDescription group = entry.getGroup();
		if(! getProjectDescription().getStatisticsGroupNames().contains(group.getName())){
			super.addStatisticGroup(group.getName());
		}
		
		super.writeStatistics(topology, entry);
	}
}
