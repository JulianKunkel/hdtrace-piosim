
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

package de.hd.pvs.piosim.model.program;

import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.TraceFormat.project.MPICommunicator;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileCommand;
import de.hd.pvs.piosim.model.program.commands.superclasses.ICommunicatorCommand;

/**
 * Simple facade class which makes the building of an application easier
 * @author Julian M. Kunkel
 *
 */
public class ApplicationBuilder {
	private Application app = new Application();
	private Communicator commWorld;

	private static int lastUsedFileID = 0;

	private static int lastUsedCommID = 0;

	private void build(String applicationName, String desc, int processes, int [] threadsPerProcess) {
		app.setApplicationName(applicationName);
		app.setDescription(desc);
		// shall be set from outside.
		app.setProjectFilename(applicationName + ".xml");

		setProcessNumber(processes, threadsPerProcess);
	}

	public ApplicationBuilder(String applicationName, String desc, int processes, int [] threadsPerProcess) {
		build(applicationName, desc, processes, threadsPerProcess);
	}

	/**
	 * Build an application with an equal number of threads per process
	 *
	 * @param applicationName
	 * @param desc
	 * @param processes
	 * @param threadForAllProcesses
	 */
	public ApplicationBuilder(String applicationName, String desc, int processes, int threadForAllProcesses) {
		int [] threadsPerProcess =  new int[processes];
		for (int i=0; i  < processes; i++)
			threadsPerProcess[i] = threadForAllProcesses;
		build(applicationName, desc, processes, threadsPerProcess);
	}


	private void setProcessNumber(int num, int [] threadsPerProcess){
		 if (num <= 0){
			 throw new IllegalArgumentException("Size of the program must be > 0");
		 }

		 final Program [][] map = new Program[num] [];

		 final ArrayList<Integer> ranks = new ArrayList<Integer>();

		 for(int p=0; p < num; p++){
			 map[p] = new Program[threadsPerProcess[p]];

			 for(int t=0; t < threadsPerProcess[p]; t++){
				 	Program prog = new ProgramInMemory();
			 		prog.setApplication(app, p, t);
			 		map[p][t] = prog;
			 }

			 ranks.add(p);
		 }

		 commWorld = createCommunicator("WORLD", ranks);

		 app.setProcessThreadProgramMap(map);
	}

	public Communicator createCommunicator(String name, List<Integer> ranks){
		final Communicator comm = new Communicator(name);

		for(int rank: ranks){
			comm.addRank(rank, comm.getSize(), lastUsedCommID);
		}

		lastUsedCommID++;

		for(MPICommunicator curComm : app.getCommunicators()){
			if (curComm.getName().equals(name)){
				throw new IllegalArgumentException("Communicator " + name + " already exists in application " + app.getName());
			}
		}

		app.addCommunicator(comm);

		return comm;
	}

	public FileMetadata createFile(String name, long initialSize, Distribution distribution){
		final FileMetadata file = new FileMetadata();
		file.setSize(initialSize);
		file.setName(name);
		file.setDistribution(distribution);

		app.addFile(file);

		return file;
	}

	// for all depending programs add the command directly.
	public void addCommand(int rank, Command command){
		if( rank >= app.getProcessCount() ){
			throw new IllegalArgumentException("Invalid rank: " + rank + " application size is " + app.getProcessCount());
		}

		Program program = app.getClientProgram(rank, 0);
		((ProgramInMemory) program).getCommands().add(command);
		command.setProgram(program);
	}

	public void addCommand(Communicator comm, ICommunicatorCommand cmd){
		cmd.setCommunicator(comm);

		// add communicator for all participating programs.
		for(Integer i:  comm.getParticipatingRanks()){
			addCommand(i, (Command) cmd);
		}
	}



	public void addCommand(int rank, FileCommand cmd, FileDescriptor fd){
		if (! app.getFiles().contains(fd.getFile())){
			throw new IllegalArgumentException("File " + fd.getFile() + " not contained in application");
		}

		cmd.setFileDescriptor(fd);

		addCommand(rank, cmd);
	}


	public Communicator getWorldCommunicator(){
		return commWorld;
	}

	public Application getApplication() {
		return app;
	}
}
