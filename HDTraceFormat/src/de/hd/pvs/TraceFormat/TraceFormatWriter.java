
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
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceWriter;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Exports the XML data to a HDTrace project.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class TraceFormatWriter {

	static class OutFiles{
		TraceWriter traceWriter;
		HashMap<StatisticsGroupDescription, StatisticWriter> registeredStatisticWriter = new HashMap<StatisticsGroupDescription, StatisticWriter>();
	}

	// map a single process id to the corresponding trace writer.
	final HashMap<TopologyEntry, OutFiles> traceWriterMap = new HashMap<TopologyEntry, OutFiles>();

	final ProjectDescription outProject = new ProjectDescription();

	List<XMLTag> unparsedTagsToWrite = null;

	public void setUnparsedTagsToWrite(List<XMLTag> unparsedTagsToWrite){
		this.unparsedTagsToWrite = unparsedTagsToWrite;
	}

	public TraceFormatWriter(String resultFile, TopologyLabels labels) {
		outProject.setProjectFilename(resultFile);
		
		outProject.setTopologyRoot( new TopologyEntry(outProject.getFilesPrefix(), null));
		outProject.setTopologyLabels(labels);
	}
	
	/**
	 * Create the topology in the project description. 
	 * All parent topologies required are created.
	 * @param topology
	 * @return
	 */
	public TopologyEntry createTopology(String [] topology){
		TopologyEntry cur = outProject.getTopologyRoot();
		
		if (topology.length >= outProject.getTopologyLabels().getLabels().size()){
			throw new IllegalArgumentException("Topology labels are not as deep as the topology " +
					"you want to create");
		}
		for(int p = 0 ; p < topology.length; p++){
			// check if parents exist
			final TopologyEntry child = cur.getChild(topology[p]);
			if( child == null){
				// create all the next ones.
				for(int n = p ; n < topology.length; n++){
					cur = new TopologyEntry(topology[n], cur);
				}
				return cur;
			}
			cur = child;
		}
		throw new IllegalArgumentException("Topology exists already!");
	}

	/**
	 * Announce that an already existing topology will create some statistics or a trace.
	 * 
	 * @param topology
	 */
	public void initalizeTopology(TopologyEntry topology) {		
		//lookup topology in project description and create missing topologies				
		final String file = outProject.getParentDir() + "/" + topology.getTraceFileName();

		OutFiles files = traceWriterMap.get(topology);
		if(files != null){
			throw new IllegalArgumentException("Topology already initalized! " + topology.toString() );
		}
		
		// check existence of parent topology in registered topology.
		for (TopologyEntry parent: topology.getParentTopologies()){
			if(! traceWriterMap.containsKey(parent)){
				throw new IllegalArgumentException("Parent topology " + parent.toString() + " not registered!");
			}
		}
		
		files = new OutFiles();
		traceWriterMap.put(topology, files);

		try {
			files.traceWriter =  new TraceWriter(file);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Trace file could not be created: " + file);
		}
	}

	/**
	 * Add a statistic group for output. 
	 * @param group
	 */
	public void addStatisticGroup(StatisticsGroupDescription group){
		outProject.addExternalStatisticsGroup(group);
	}


	public void Event(TopologyEntry topology, Epoch time,
			EventTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(topology).traceWriter;
		try {
			writer.Event(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	public void StateEnd(TopologyEntry topology, Epoch time,
			StateTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(topology).traceWriter;
		try {
			writer.StateEnd(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	public void StateStart(TopologyEntry topology, Epoch time,
			StateTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(topology).traceWriter;
		
		try {
			writer.StateStart(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id " + topology);
		}
	}

	public void Statistics(TopologyEntry topology, Epoch time, String statistic,
			StatisticsGroupDescription group, Object value) {
		final HashMap<StatisticsGroupDescription, StatisticWriter> stats =  traceWriterMap.get(topology).registeredStatisticWriter;

		StatisticWriter outWriter = stats.get(group);

		if (outWriter == null) {			
			final String file = outProject.getParentDir() + "/" + topology.getStatisticFileName(group.getName());

			try {
				// generate a new output writer
				outWriter = new StatisticWriter(file, group);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Statistic file could not be created: " + file);
			}

			stats.put(group, outWriter);
		}

		try {
			outWriter.writeStatisticEntry(time, statistic, value);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Error during write in statistic file", e);
		}
	}

	public void finalizeTrace() throws IOException{
		for (OutFiles files : traceWriterMap.values()) {
			if(files.traceWriter != null)
				files.traceWriter.finalize();

			for (StatisticWriter writer : files.registeredStatisticWriter.values()) {
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
