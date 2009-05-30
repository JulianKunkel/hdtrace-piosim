
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

package de.hd.pvs.TraceFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsWriter;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceWriter;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Allows to write to HDTraceFormat via a central API.
 * The project description is not explicitly managed. 
 * 
 * @author Julian M. Kunkel
 * 
 */
public class TraceFormatWriter {

	static class OutFiles{
		TraceWriter traceWriter;
		HashMap<StatisticsGroupDescription, StatisticsWriter> registeredStatisticWriter = new HashMap<StatisticsGroupDescription, StatisticsWriter>();
	}

	// map a single process id to the corresponding trace writer.
	final HashMap<TopologyNode, OutFiles> traceWriterMap = new HashMap<TopologyNode, OutFiles>();

	final ProjectDescription outProject = new ProjectDescription();

	List<XMLTag> unparsedTagsToWrite = null;

	public void setUnparsedTagsToWrite(List<XMLTag> unparsedTagsToWrite){
		this.unparsedTagsToWrite = unparsedTagsToWrite;
	}

	public TraceFormatWriter(String resultFile, TopologyTypes labels) {
		outProject.setProjectFilename(resultFile);
		
		outProject.setTopologyRoot( new TopologyNode(outProject.getFilesPrefix(), null, "root"));
		outProject.setTopologyTypes(labels);
	}
	
	/**
	 * Create the topology in the project description. Uses the default labels for each topology node.
	 * All parent topologies required are created.
	 * @param topology
	 * @return
	 */
	public TopologyNode createInitalizeTopology(String [] topology){
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
					initalizeTopologyInternal(cur);
				}
				return cur;
			}
			cur = child;
		}
		return cur;
	}

	void initalizeTopologyInternal(TopologyNode topology) {
		OutFiles files = traceWriterMap.get(topology);
		if(files != null){
			throw new IllegalArgumentException("Topology already initalized! " + topology.toRecursiveString() );
		}

		files = new OutFiles();
		traceWriterMap.put(topology, files);
	}
	
	private TraceWriter getOrCreateTraceTopology(TopologyNode topology, Epoch time){
		//lookup topology in project description and create missing topologies				
		final String file = outProject.getParentDir() + "/" + topology.getTraceFileName();

		OutFiles files = traceWriterMap.get(topology);
		if(files.traceWriter != null){
			return files.traceWriter;
		}
		try {
			files.traceWriter =  new TraceWriter(file, time);
			return files.traceWriter;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Trace file could not be created: " + file);
		}		
	}
	
	void initalizeTopologyIfNeeded(TopologyNode topology) {
		if(traceWriterMap.containsKey(topology)){
			return;
		}
		initalizeTopologyInternal(topology);
	}

	
	/**
	 * Announce that an already existing topology will create some statistics or a trace.
	 * The parent topologies must be added before.
	 * 
	 * @param topology
	 */
	public void initalizeTopology(TopologyNode topology) {		
		// check existence of parent topology in registered topology.
		for (TopologyNode parent: topology.getParentTopologies()){
			if(! traceWriterMap.containsKey(parent)){
				throw new IllegalArgumentException("Parent topology " + parent.toRecursiveString() + " not registered!");
			}
		}
		initalizeTopologyInternal(topology);
	}

	/**
	 * Add a statistic group for output. 
	 * @param group
	 */
	public void addStatisticGroup(String group){
		outProject.addStatisticsGroup(group);
	}


	public void Event(TopologyNode topology, EventTraceEntry traceEntry) {
		final TraceWriter writer = getOrCreateTraceTopology(topology, traceEntry.getEarliestTime());
		try {
			writer.Event(traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	/**
	 * Finish a state.
	 * 
	 * Note that the end time of the traceEntry must be set correctly!
	 * @param topology
	 * @param traceEntry
	 */
	public void StateEnd(TopologyNode topology, 	StateTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(topology).traceWriter;
		try {
			writer.StateEnd(traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	public void StateStart(TopologyNode topology, 
			StateTraceEntry traceEntry) {
		final TraceWriter writer = getOrCreateTraceTopology(topology, traceEntry.getEarliestTime());
		
		try {
			writer.StateStart(traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	/**
	 * Optional function, initializes the statistics file. If this function is not used 
	 * then the first statistic gets removed during the write process.  
	 * 
	 * @param topology
	 * @param group
	 * @param time
	 */
	public void initStatisticsTopology(TopologyNode topology, StatisticsGroupDescription group, Epoch time){
		final HashMap<StatisticsGroupDescription, StatisticsWriter> stats =  traceWriterMap.get(topology).registeredStatisticWriter;
		StatisticsWriter outWriter = stats.get(group);

		if (outWriter == null) {
			getOrCreateStatisticsTopologyInternal(topology, group, time);
		}else{
			throw new IllegalArgumentException("Statistic already initalized " + topology + " " + group );
		}
	}
	
	private StatisticsWriter getOrCreateStatisticsTopologyInternal(TopologyNode topology, StatisticsGroupDescription group, Epoch time){
		final HashMap<StatisticsGroupDescription, StatisticsWriter> stats =  traceWriterMap.get(topology).registeredStatisticWriter;
		

		StatisticsWriter outWriter = stats.get(group);

		if (outWriter != null){
			return outWriter;
		}
		
		final String file = outProject.getParentDir() + "/" + topology.getStatisticFileName(group.getName());
		
		if(time == null){
			throw new IllegalArgumentException("Invalid argument, should not be called with null time");
		}
		
		try {
			// generate a new output writer			
			outWriter = new StatisticsWriter(file, topology, group, time);
			stats.put(group, outWriter);
			return outWriter;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Statistic file could not be created: " + file);
		}
	}
	
	public void writeStatisticsTimestamp(TopologyNode topology, StatisticsGroupDescription group, Epoch time) throws IOException{
		StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, time);
		outWriter.writeStatisticsTimestamp(time);
	}
	
	public void writeStatisticValue(TopologyNode topology, StatisticDescription statistic, Object value) throws IOException{
		final StatisticsGroupDescription group = statistic.getGroup();
		StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, null);
		outWriter.writeStatisticEntry(statistic, value);
	}
	
	public void writeStatisticValue(TopologyNode topology, Epoch time, StatisticDescription statistic, Object value) throws IOException{
		final StatisticsGroupDescription group = statistic.getGroup();
		StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, time);
		
		if(outWriter.isStatisticIntervalFinished()){
			outWriter.writeStatisticsTimestamp(time);
		}
		
		outWriter.writeStatisticEntry(statistic, value);
	}

	/**
	 * Write a complete statistic entry.
	 * @param topology The topology the entry belongs to.
	 * @param entry Actual data.
	 * @throws IOException
	 */
	public void writeStatistics(TopologyNode topology, StatisticGroupEntry entry) throws IOException{
		final StatisticsGroupDescription group = entry.getGroup();
		writeStatisticsTimestamp(topology, group, entry.getLatestTime());
		
		final StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, entry.getEarliestTime());
		
		// use output group and NOT input group for checking.
		final ArrayList<StatisticDescription> descs = outWriter.getOutputGroup().getStatisticsOrdered();		
		int cur = 0;
		for(Object val: entry.getValues()){
			outWriter.writeStatisticEntry(descs.get(cur), val);
			cur++;
		}
	}
	
	public void finalizeTrace() throws IOException{
		for (OutFiles files : traceWriterMap.values()) {
			if(files.traceWriter != null)
				files.traceWriter.finalize();

			for (StatisticsWriter writer : files.registeredStatisticWriter.values()) {
				writer.finalize();
			}				
		}

		final ProjectDescriptionXMLWriter projectWriter = new ProjectDescriptionXMLWriter();		
		projectWriter.writeXMLToProjectFile(outProject, unparsedTagsToWrite);
	}

	public ProjectDescription getProjectDescription(){
		return outProject;
	}

}
