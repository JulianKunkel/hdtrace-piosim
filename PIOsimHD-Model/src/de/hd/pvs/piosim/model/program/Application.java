
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

/**
 *
 */
package de.hd.pvs.piosim.model.program;

import java.util.HashMap;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;

/**
 * Contains the description of an application, i.e. a parallel program consisting of several
 * clients cooperating to perform a computation.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class Application{
	private String applicationName = "";

	private String alias = "";
	
	private String description = "";	

	private String filename = "";


	/**
	 * If true then each client process has its own XML file. (allows optimized reading with SAX).
	 */
	private boolean isSplitIntoSeveralFiles = true;
	
	private HashMap<String, Communicator> communicators = new HashMap<String, Communicator>();

	private Program [] rankProgramMap = null;

	private HashMap<Integer, MPIFile>  files = new HashMap<Integer, MPIFile>();

	private int processCount = 0;	


	public String getName() {
		return applicationName;
	}

	public int getProcessCount() {
		return processCount;
	}

	public Program getClientProgram(int rank) {
		if(rank >= rankProgramMap.length) 
			return null;
		
		return rankProgramMap[rank];
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getDescription() {
		return description;
	}

	public Communicator getCommunicator(String name) {
		return communicators.get(name);
	}

	/**
	 * @return a single file
	 */
	public MPIFile getFile(int fileid) {
		return files.get(fileid);
	}	

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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

	public HashMap<String, Communicator> getCommunicators() {
		return communicators;
	}

	public HashMap<Integer, MPIFile> getFiles() {
		return files;
	}

	public Program[] getRankProgramMap() {
		return rankProgramMap;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProcessCount(int processCount) {
		this.processCount = processCount;

		// create a new world communicator
		Communicator comm = new Communicator("WORLD");		
		/* world communicator put all ranks into */
		int [] ranks = new int[processCount];
		
		for(int i=0; i < processCount; i++){
			ranks[i] = i;
		}
		comm.setWorldRanks(ranks);

		communicators.put("WORLD", comm);
	}

	public void setRankProgramMap(Program[] rankProgramMap) {
		this.rankProgramMap = rankProgramMap;
	}

	public void setCommunicators(HashMap<String, Communicator> communicators) {
		this.communicators = communicators;
	}

	public void setFiles(HashMap<Integer, MPIFile> files) {
		this.files = files;
	}
	
	public boolean isSplitIntoSeveralFiles() {
		return isSplitIntoSeveralFiles;
	}
	
	public void setSplitIntoSeveralFiles(boolean isSplitIntoSeveralFiles) {
		this.isSplitIntoSeveralFiles = isSplitIntoSeveralFiles;
	}
}
