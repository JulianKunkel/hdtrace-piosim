
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

package de.hd.pvs.piosim.model.program;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.project.MPICommunicator;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.SerializationHandler;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicTraceEntryToCommandMapper;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.NoOperation;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * This class is used to create an Application object from an XML description.
 *
 * @author Julian M. Kunkel
 */

public class ApplicationXMLReader extends ProjectDescriptionXMLReader {


	/**
	 * Parse an application of the file.
	 *
	 * @param XMLFile
	 * @return
	 * @throws Exception
	 */
	public Application parseApplication(String filename, boolean readCompleteProgram) throws Exception {
		final File  xmlFile = new File(filename);
		final Application app = new Application();

		super.readProjectDescription(app, xmlFile.getAbsolutePath());

		final SerializationHandler serializationHandler = new SerializationHandler();

		LinkedList<XMLTag>  elements;
		XMLTag applicationNode = rootTag;

		/* read file list */
		elements = applicationNode.getNestedXMLTagsWithName("FileList");
		if (elements.size() != 1) {
			throw new InvalidParameterException("Invalid XML, wrong FileList tags found!");
		}

		// parse files
		elements = elements.get(0).getNestedXMLTagsWithName("File");
		for (int i = 0; i < elements.size(); i++) {
			final FileMetadata f = new FileMetadata();

			serializationHandler.readXML(elements.get(i), f);

			try{
				final Distribution dist = (Distribution) serializationHandler.createDynamicObjectFromXML(elements.get(i).getFirstNestedXMLTagWithName("Distribution"));
				f.setDistribution(dist);
			}catch(IllegalArgumentException e){
				System.err.println("Error on creation of distribution for file \"" + f.getName()+ "\"");
				throw e;
			}catch(NullPointerException e){
				System.err.println("Error on creation of distribution for file \"" + f.getName()+ "\"");
				throw e;
			}

			app.addFile(f);
		}

		MPICommunicator commWorld = null;

		// search for world communicator:
		for(MPICommunicator comm: app.getCommunicators()){
			if(comm.getName().equals("WORLD")){
				commWorld = comm;
				break;
			}
		}

		// now read Programs (TODO it could use the TraceFormat reader)
		final ArrayList<TopologyNode> [] threadNodesPerRank = new ArrayList[commWorld.getSize()];

		final XMLReaderToRAM reader = new XMLReaderToRAM();

		for(int rank=0; rank < commWorld.getSize(); rank++){
			threadNodesPerRank[rank] = new ArrayList<TopologyNode>();
		}

		for (TopologyNode node: app.getTopologyRoot().getChildrenRecursivly()) {
			final boolean isThread = node.getType().equalsIgnoreCase("thread");
			final TopologyNode parentRank = node.getParentNodeWithTopologyType("rank");

			if(parentRank == null || ! isThread){
				continue;
			}

			final int rank = Integer.parseInt(parentRank.getName());
			threadNodesPerRank[rank].add(node);
		}


		// for each anotherStringrank open the corresponding file if available!

		final Program [][] programs = new Program[commWorld.getSize()][];
		for(int rank=0; rank < commWorld.getSize(); rank++){
			final ArrayList<TopologyNode> threadNodes = threadNodesPerRank[rank];

			programs[rank] = new Program[threadNodes.size()];

			if(threadNodes.size() == 0){ // some programs are missing
				System.err.println("No program(s) found for rank: " + rank);
				continue;
			}

			for(int thread = 0 ; thread < threadNodes.size(); thread++){
				final TopologyNode threadNode = threadNodes.get(thread);

				final String file = app.getParentDir() + "/" + threadNode.getTraceFileName();

				if(!  (new File(file)).canRead() ){
					throw new IOException("File " + file + " is not readable!");
				}

				if(readCompleteProgram){
					// use DOM reader
					programs[rank][thread] = readProgramXMLDOM(rank, thread, file, app);
				}else{ // use SAX Reader to read the file
					programs[rank][thread] = new ProgramReadXMLOnDemand();
				}

				programs[rank][thread].setApplication(app, rank, thread);
				programs[rank][thread].setFilename(file);
				programs[rank][thread].restartWithFirstCommand();
			}
		}

		app.setProcessThreadProgramMap(programs);

		return app;
	}

	@Override
	protected MPICommunicator createCommunicator(String name) {
		return new Communicator(name);
	}

	/**
	 * Read the program from the XML node DOM.
	 *
	 * @param programmXMLnode
	 * @param program
	 * @throws Exception
	 */
	public Program readProgramXMLDOM(int rank, int thread, String filename, Application app) throws Exception {
		final StAXTraceFileReader traceFileReader = new StAXTraceFileReader(filename, false, Epoch.ZERO);

		// TODO move efficiency to another location...
		final double processingSpeedOfTheSystem = traceFileReader.getProcssorSpeedInMHz() * 1000 * 1000; // => ops/s
		assert(processingSpeedOfTheSystem > 0);

		final ProgramInMemory program = new ProgramInMemory();
		program.setApplication(app, rank, thread);

		final CommandXMLReader cmdReader = new CommandXMLReader(program);

		ITraceEntry entry = traceFileReader.getNextInputEntry();

		Epoch lastTimeForComputeJob = entry.getLatestTime();

		while(entry != null) {
			//System.out.println(entry);
			// now read the particular command from the XML:

			if (DynamicTraceEntryToCommandMapper.isCommandAvailable(entry.getName())){
				Command cmd = cmdReader.parseCommandXML(entry);
				if(cmd.getClass() != NoOperation.class){
					// add an appropriate compute job, depending on the speed of the system.
					long cycles = (long) (entry.getEarliestTime().subtract(lastTimeForComputeJob).getDouble() * processingSpeedOfTheSystem);

					assert(cycles >= 0);

					if(cycles > 0){
						Compute compute = new Compute();
						compute.setCycles( cycles );
						program.addCommand(compute);
					}

					lastTimeForComputeJob = entry.getLatestTime();
					program.addCommand(cmd);
				}
			}
			entry = traceFileReader.getNextInputEntry();
		}

		return program;
	}

}
