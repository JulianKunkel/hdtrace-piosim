
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

package de.hd.pvs.TraceFormat.project;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;

public class ProjectDescription {
	// describes the project file:
	private String parentDir;
	private String filePrefix;
	private String projectFilename;

	private TopologyInternalLevel topologyRoot;
	private TopologyLabels        topologyLabels;
	
	private String applicationName = "";

	private String alias = "";
	
	private String description = "";	

	private int processCount = 0;	

	// available statistics
	final HashMap<String, StatisticsGroupDescription> statisticGroupDescriptions = new HashMap<String, StatisticsGroupDescription>();
	
	public void addExternalStatisticsGroup(StatisticsGroupDescription group){
		statisticGroupDescriptions.put(group.getName(), group);
	}
	
	public Collection<String> getExternalStatisticGroupNames(){
		return statisticGroupDescriptions.keySet();
	}
	
	public Collection<StatisticsGroupDescription> getExternalStatisticGroups(){
		return statisticGroupDescriptions.values();
	}
	
	public StatisticsGroupDescription getExternalStatisticsGroup(String groupName){
		return statisticGroupDescriptions.get(groupName);
	}
	

	public String getName() {
		return applicationName;
	}

	public int getProcessCount() {
		return processCount;
	}

	public String getApplicationName() {
		return applicationName;
	}
	
	public String getAbsoluteFilenameOfProject(){
		return getAbsoluteFilesPrefix() + ".xml";
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
		if (projectFileName.lastIndexOf(".xml") < 1){
			projectFileName = projectFileName + ".xml";
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
		
		if(topologyRoot != null)
			topologyRoot.setLabel(this.projectFilename);
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

	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}
	
	public void setTopologyLabels(TopologyLabels topologyLabels) {
		this.topologyLabels = topologyLabels;
	}
	
	public void setTopologyRoot(TopologyInternalLevel topologyRoot) {
		this.topologyRoot = topologyRoot;
	}
	
	public TopologyLabels getTopologyLabels() {
		return topologyLabels;
	}
	
	public TopologyInternalLevel getTopologyRoot() {
		return topologyRoot;
	}
}
