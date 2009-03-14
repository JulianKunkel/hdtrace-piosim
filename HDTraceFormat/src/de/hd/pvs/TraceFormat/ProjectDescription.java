
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;

public class ProjectDescription {
	// describes the project file:
	private String parentDir;
	private String filePrefix;
	private String projectFilename;

	
	private String applicationName = "";

	private String alias = "";
	
	private String description = "";	

	private int processCount = 0;	

	// maps process rank and existing thread ids  
	final ArrayList<Integer> processThreadCount = new ArrayList<Integer>();  
	
	// available statistics
	final HashMap<String, ExternalStatisticsGroup> statisticGroupDescriptions = new HashMap<String, ExternalStatisticsGroup>();
	
	void addExternalStatisticsGroup(ExternalStatisticsGroup group){
		statisticGroupDescriptions.put(group.getName(), group);
	}
	
	public Collection<String> getExternalStatisticGroups(){
		return statisticGroupDescriptions.keySet();
	}
	
	public ExternalStatisticsGroup getExternalStatisticsDescription(String groupName){
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

	public String getDescription() {
		return description;
	}


	public String getProjectFilename() {
		return parentDir + "/" + projectFilename;
	}

	public void setProjectFilename(String projectFileName) {
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
	}

	public String getFilesPrefix() {
		return filePrefix;
	}
	
	public String getAbsoluteFilesPrefix() {
		return parentDir + "/" + filePrefix;
	}
	
	public String getParentDir() {
		return parentDir;
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
		// remove old values if too many present:
		for (int i=this.processCount ; i < processThreadCount.size(); i++){
			processThreadCount.remove(i);				
		}
		
		// add new values if less present:
		processThreadCount.ensureCapacity(processCount);
		for (int i= this.processThreadCount.size() ; i < processCount; i++){
			processThreadCount.add(0);
		}
		
		this.processCount = processCount;
	}
	
	/**
	 * Set the number of threads of a particular process,
	 * the threads are numbered with 0..(threadCount - 1)
	 * 
	 * @param process
	 * @param threadCount
	 */
	public void setProcessThreadCount(final int process, int threadCount) {
		processThreadCount.set(process, threadCount);
	}
	
	public int getProcessThreadCount(int process){
		return processThreadCount.get(process);
	}
}
