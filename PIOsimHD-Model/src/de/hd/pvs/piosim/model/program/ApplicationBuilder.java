
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

package de.hd.pvs.piosim.model.program;

import java.util.HashMap;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileCommand;

/**
 * Simple facade class which makes the building of an application easier
 * @author Julian M. Kunkel
 *
 */
public class ApplicationBuilder {
	private Application app = new Application();
	
	private static int lastUsedFileID = 0;
	
	public ApplicationBuilder(String applicationName, String desc, int size) {
		app.setApplicationName(applicationName);
		app.setDescription(desc);
		// shall be set from outside.
		app.setFilename(applicationName + ".xml");
		
		setRankSize(size);
	}
	
	private void setRankSize(int num){
		 if (num <= 0){
			 throw new IllegalArgumentException("Size of the program must be > 0");
		 }
		
		 Program [] map = new Program[num];
		 
		 for(int i=0; i < num; i++){
			 Program p = new Program(app,i);
			 map[i] = p;
		 }
		 
		 app.setRankProgramMap(map);
		 app.setProcessCount(num);
	}
	
	public Communicator createCommunicator(String name, int [] ranks){
		Communicator comm = new Communicator(name);
		comm.setWorldRanks(ranks);
		
		HashMap<String, Communicator> appComms = app.getCommunicators();
		
		if (appComms.containsKey(name)){
			throw new IllegalArgumentException("Communicator " + name + " already exists in application " + app.getName());
		}
		
		appComms.put(name, comm);
		app.setCommunicators(appComms);
		
		return comm;
	}
	
	public MPIFile createFile(String name, long initialSize, Distribution distribution){
		MPIFile file = new MPIFile();
		file.setSize(initialSize);
		file.setID(++lastUsedFileID);
		file.setName(name);
		file.setDistribution(distribution);
				
		app.getFiles().put(file.getId(), file);
				
		return file;
	}
	
	// for all depending programs add the command directly.
	
	public void addCommand(int rank, Command command){
		if( rank >= app.getProcessCount() ){
			throw new IllegalArgumentException("Invalid rank: " + rank + " application size is " + app.getProcessCount());
		}
		
		Program program = app.getClientProgram(rank);
		program.getCommands().add(command);
		command.setProgram(program);
	}	
	
	public void addCommand(Communicator comm, CommunicatorCommand cmd){
		if(! app.getCommunicators().values().contains(comm)){
			throw new IllegalArgumentException("Communicator " + comm + " not contained in application");
		}
		
		cmd.setCommunicator(comm);
		
		// add communicator for all participating programs.
		for(Integer i: app.getCommunicator(comm.getName()).getParticipantsWorldRank()){			
			addCommand(i, cmd);
		}
	}
	
	public void addCommand(int rank, FileCommand cmd, MPIFile file){
		if (! app.getFiles().values().contains(file)){
			throw new IllegalArgumentException("File " + file + " not contained in application");
		}
		
		cmd.setFile(file);
		
		addCommand(rank, cmd);
	}
	
	
	public Communicator getWorldCommunicator(){
		return app.getCommunicator("WORLD");
	}
	
	public Application getApplication() {
		return app;
	}
}
