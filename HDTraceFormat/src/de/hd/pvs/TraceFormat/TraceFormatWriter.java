
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLWriter;
import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.TraceFormat.relation.RelationXMLWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsWriter;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.NestedTraceWriter;
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

	/**
	 * Contains all files to create for a given topology.
	 * @author julian
	 */
	static class OutFiles{
		NestedTraceWriter traceWriter;
		HashMap<StatisticsGroupDescription, StatisticsWriter> registeredStatisticWriter = new HashMap<StatisticsGroupDescription, StatisticsWriter>();
		RelationXMLWriter relationWriter;
	}

	/**
	 * For a relation, all topologies within a local node have a unique id.
	 * Track the last topology number used. 
	 */
	int lastTopologyNumber = 0;
	
	/**
	 *  map a single topology to the corresponding output files.
	 */
	final HashMap<TopologyNode, OutFiles> traceWriterMap = new HashMap<TopologyNode, OutFiles>();

	/**
	 * The output project description
	 */
	final ProjectDescription outProject = new ProjectDescription();

	/**
	 * List of XML elements which are simply appended to the project file. 
	 */
	List<XMLTag> unparsedTagsToWrite = null;

	/**
	 * Set the list of XML elements which are appended to the project file.
	 * I.e. they are not interpreted by the trace format writer.
	 * @param unparsedTagsToWrite
	 */
	public void setUnparsedProjectTagsToWrite(List<XMLTag> unparsedTagsToWrite){
		this.unparsedTagsToWrite = unparsedTagsToWrite;
	}

	/**
	 * Create a trace project with the given topology labels.
	 * @param projectfile
	 * @param labels
	 */
	public TraceFormatWriter(String projectfile, TopologyTypes labels) {
		init(projectfile, "", "");
		outProject.setTopologyTypes(labels);
	}

	public TraceFormatWriter(String projectfile, String description, String applicationName, String [] types) {
		final TopologyTypes topoTypes = new TopologyTypes();

		init(projectfile, description, applicationName);		

		outProject.setTopologyTypes(topoTypes);
		topoTypes.setTopologyTypes(types);

		// delete all files with the same prefix.
		final File file = new File(projectfile).getAbsoluteFile();
		final String projPrefix = file.getName();
		if(projPrefix.length() < 3){
			throw new IllegalArgumentException("Project file name must be at least 3 characters.");
		}

		final File folder = file.getParentFile();
		for(String curName: folder.list(new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(projPrefix) && name.endsWith(".trc");
			} })
		){
			(new File(folder, curName)).delete();
		}

	}

	private void init(String projectfile, String description, String applicationName){
		outProject.setDescription(description);
		outProject.setApplicationName(applicationName);
		outProject.setProjectFilename(projectfile);

		outProject.setTopologyRoot(new TopologyNode(outProject.getFilesPrefix(), null, "File"));
	}

	/**
	 * Create the topology in the project description. Uses the default labels for each topology node.
	 * All parent topologies required are created.
	 * @param topology
	 * @return
	 */
	public TopologyNode createInitalizeTopology(String [] topology){
		final ProjectDescription outProject = getProjectDescription();

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

	private NestedTraceWriter getOrCreateTraceForTopology(TopologyNode topology, Epoch time){
		//lookup topology in project description and create missing topologies				

		OutFiles files = traceWriterMap.get(topology);
		if(files.traceWriter != null){
			return files.traceWriter;
		}
		final String file = outProject.getParentDir() + "/" + topology.getTraceFileName();

		try {
			files.traceWriter =  new NestedTraceWriter(file, time);
			return files.traceWriter;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Trace file could not be created: " + file);
		}		
	}
	
	public RelationXMLWriter getRelationForTopology(TopologyNode topology){
		//lookup topology in project description and create missing topologies				

		OutFiles files = traceWriterMap.get(topology);
		if(files.relationWriter != null){
			return files.relationWriter;
		}
		final String file = outProject.getParentDir() + "/" + topology.getRelationFileName();

		try {
			// because we know all the topologies, we do not need any local or remote tokens.
			files.relationWriter =  new RelationXMLWriter(file, "", 
					"", lastTopologyNumber, Epoch.ZERO);
			lastTopologyNumber++;
			return files.relationWriter;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Relation file could not be created: " + file);
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

	/**
	 * Write a single event.
	 * 
	 * @param topology
	 */
	public void writeEvent(TopologyNode topology, String name, Epoch time, HashMap<String, String> attributes, ArrayList<XMLTag> xmldat) throws IOException {
		final NestedTraceWriter writer = getOrCreateTraceForTopology(topology, time);
		final EventTraceEntry traceEntry = new EventTraceEntry(name, attributes, time, xmldat);
		writer.writeEvent(traceEntry);
	}

	/**
	 * Write a single event.
	 * 
	 * @param topology
	 */
	public void writeEvent(TopologyNode topology, String name, Epoch time) throws IOException {
		final NestedTraceWriter writer = getOrCreateTraceForTopology(topology, time);
		final EventTraceEntry traceEntry = new EventTraceEntry(name, time);
		writer.writeEvent(traceEntry);
	}

	public void writeStateEnd(TopologyNode topology, String name, Epoch endTime)  throws IOException {
		this.writeStateEnd(topology, name, endTime, null, null);
	}


	/**
	 * Finish a state.
	 *
	 * @param topology
	 * @param traceEntry
	 */
	public void writeStateEnd(TopologyNode topology, String name, Epoch endTime, HashMap<String, String> attributes, ArrayList<XMLTag> xmldata)  throws IOException {
		NestedTraceWriter writer = traceWriterMap.get(topology).traceWriter;
		writer.writeStateEnd(name, endTime, attributes, xmldata);
	}

	/**
	 * Start a state.
	 * @param topology
	 * @param traceEntry
	 */
	public void writeStateStart(TopologyNode topology, String name, Epoch startTime)  throws IOException {
		final NestedTraceWriter writer = getOrCreateTraceForTopology(topology, startTime);	
		writer.writeStateStart(name, startTime);
	}

	/**
	 * Initializes the statistics file. If this function is not used, then the first statistic gets removed during the write process.
	 * The function also registers the statistics group to the project description if necessary.
	 * 
	 * @param topology
	 * @param group
	 * @param time
	 */
	public void initStatisticsTopology(TopologyNode topology, StatisticsGroupDescription group, Epoch time){
		final HashMap<StatisticsGroupDescription, StatisticsWriter> stats =  traceWriterMap.get(topology).registeredStatisticWriter;
		StatisticsWriter outWriter = stats.get(group);

		if(! getProjectDescription().getStatisticsGroupNames().contains(group.getName())){
			addStatisticGroup(group.getName());
		}

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

	public void writeStatisticValue(TopologyNode topology, StatisticsDescription statistic, Object value) throws IOException{
		final StatisticsGroupDescription group = statistic.getGroup();
		StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, null);
		outWriter.writeStatisticEntry(statistic, value);
	}

	public void writeStatisticValue(TopologyNode topology, Epoch time, StatisticsDescription statistic, Object value) throws IOException{
		final StatisticsGroupDescription group = statistic.getGroup();
		StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, time);

		if(outWriter.isStatisticIntervalFinished()){
			outWriter.writeStatisticsTimestamp(time);
		}

		outWriter.writeStatisticEntry(statistic, value);
	}

	/**
	 * Write a complete statistic group entry.
	 * @param topology The topology the entry belongs to.
	 * @param entry Actual data.
	 * @throws IOException
	 */
	public void writeStatisticsGroupEntry(TopologyNode topology, StatisticsGroupEntry entry) throws IOException{
		final StatisticsGroupDescription group = entry.getGroup();
		final StatisticsWriter outWriter = getOrCreateStatisticsTopologyInternal(topology, group, entry.getEarliestTime());

		// use output group and NOT input group for checking.
		outWriter.writeStatisticsGroupEntry(entry);
	}

	/**
	 * Finalize and close the trace file(s).
	 * @throws IOException
	 */
	public void finalizeTrace() throws IOException{
		for (OutFiles files : traceWriterMap.values()) {
			if(files.traceWriter != null)
				files.traceWriter.finalize();
			
			if(files.relationWriter != null){
				files.relationWriter.finalize();
			}

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
