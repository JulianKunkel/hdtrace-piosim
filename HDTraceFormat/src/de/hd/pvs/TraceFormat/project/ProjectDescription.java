
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.project;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;

public class ProjectDescription {
	// describes the project file:
	private String parentDir;
	private String filePrefix;
	private String projectFilename;

	private TopologyNode topologyRoot;
	private TopologyTypes topologyTypes;

	private String applicationName = "";

	private String alias = "";

	private String description = "";	

	final private LinkedList<MPICommunicator> communicators = new LinkedList<MPICommunicator>();

	/**
	 * map rank to cid map to communicator client
	 */
	final private HashMap<Integer, HashMap<Integer, CommunicatorInformation>> rankCIDMap = new HashMap<Integer, HashMap<Integer,CommunicatorInformation>>();

	/**
	 * Contains named communicators.
	 */
	final private HashMap<String, MPICommunicator>  commNameMap = new HashMap<String, MPICommunicator>();
	
	/**
	 * Map datatypes for each rank and each tid.
	 */
	final private HashMap<Integer, HashMap<Long, Datatype>> datatypeMap = new HashMap<Integer, HashMap<Long,Datatype>>();

	// available statistics
	final HashSet<String> statisticsGroupDescriptions = new HashSet<String>();

	public void addStatisticsGroup(String name){
		statisticsGroupDescriptions.add(name);
	}

	public Collection<String> getStatisticsGroupNames(){
		return statisticsGroupDescriptions;
	}

	public String getName() {
		return applicationName;
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	public String getAbsoluteFilenameOfProject(){
		return getAbsoluteFilesPrefix() + ".proj";
	}	

	public String getFilesPrefix() {
		return filePrefix;
	}

	private String getAbsoluteFilesPrefix() {
		return parentDir + "/" + filePrefix;
	}

	public String getParentDir() {
		return parentDir;
	}

	public String getDescription() {
		return description;
	}


	public String getProjectFilename() {
		return projectFilename;
	}

	public void setProjectFilename(String projectFileName) {
		if (projectFileName.lastIndexOf(".proj") < 1){
			projectFileName = projectFileName + ".proj";
		}

		File projectFile = new File(projectFileName);

		// scan for the trace files
		String prefix = projectFile.getName().toString();
		prefix = prefix.substring(0, prefix.lastIndexOf('.'));		

		File parent = projectFile.getParentFile();
		if(parent == null){
			parent = new File(".");
		}

		this.parentDir = parent.getAbsolutePath();
		this.projectFilename = projectFile.getName();
		this.filePrefix = prefix;

		if(topologyRoot != null){
			topologyRoot.setText(this.projectFilename.substring(0, projectFilename.length() - (".proj").length()));
		}
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTopologyTypes(TopologyTypes topologyLabels) {
		this.topologyTypes = topologyLabels;
	}

	public void setTopologyRoot(TopologyNode topologyRoot) {
		this.topologyRoot = topologyRoot;
	}

	public TopologyTypes getTopologyTypes() {
		return topologyTypes;
	}
	
	public String getTopologyType(int depth) {
		return topologyTypes.getTypeFor(depth);
	}	

	public TopologyNode getTopologyRoot() {
		return topologyRoot;
	}

	public void addCommunicator(MPICommunicator comm){
		communicators.add(comm);

		final HashMap<Integer, CommunicatorInformation> cidMap = comm.getParticipiants();
		
		for(Integer rank: comm.getParticipatingRanks()){
			HashMap<Integer, CommunicatorInformation> map = rankCIDMap.get(rank);
			if(map == null){		
				map = new HashMap<Integer, CommunicatorInformation>();
				rankCIDMap.put(rank, map);				
			}
			
			final CommunicatorInformation cinfo = cidMap.get(rank);
			map.put(cinfo.getCid(), cinfo);
		}
		
		if( comm.getName() != "" ){
			if (commNameMap.containsKey(comm.getName()) ){
				System.err.println("Warning: Communicator with name \"" + comm.getName() + "\" defined multiple times.");				
			}else{
				commNameMap.put(comm.getName(), comm);
			}
			
		}
	}

	public List<MPICommunicator> getCommunicators() {
		return communicators;
	}
	
	public MPICommunicator getCommunicator(String name){
		MPICommunicator c = commNameMap.get(name);
		if (c == null){
			throw new IllegalArgumentException("Communicator with name " + name + " not found.");
		}
		return c;
	}

	public CommunicatorInformation getCommunicator(int rank, int cid){
		HashMap<Integer, CommunicatorInformation> map = rankCIDMap.get(rank);
		if(map == null)
			return null;
		return map.get(cid);
	}
	
	public HashMap<Long, Datatype> getDatatypeMap(int rank){
		return datatypeMap.get(rank);
	}
	
	public void setDatatypeMap(int rank, HashMap<Long, Datatype> map){
		datatypeMap.put(rank, map);
	}
}
