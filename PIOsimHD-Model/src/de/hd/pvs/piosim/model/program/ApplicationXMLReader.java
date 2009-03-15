
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
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.TraceFileNames;
import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
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

		final AttributeAnnotationHandler commonHandler = new AttributeAnnotationHandler();

		LinkedList<XMLTag>  elements;
		XMLTag applicationNode = rootTag;

		/* read file list */
		elements = applicationNode.getNestedXMLTagsWithName("FileList");
		if (elements.size() != 1) {
			throw new InvalidParameterException("Invalid XML, wrong FileList tags found!");
		}
		elements = elements.get(0).getNestedXMLTagsWithName("File");
		for (int i = 0; i < elements.size(); i++) {
			MPIFile f = new MPIFile();

			commonHandler.readSimpleAttributes(elements.get(i), f);
			f.setDistribution(Distribution.readDistributionFromXML(
					elements.get(i).getFirstNestedXMLTagWithName("Distribution")));

			app.getFiles().put(f.getId(), f);
		}

		/* read communicator list */
		elements =  applicationNode.getNestedXMLTagsWithName("CommunicatorList");
		if (elements.size() != 1) {
			throw new InvalidParameterException(
			"Invalid XML, wrong CommunicatorList tags defined (at least WORLD must exist)!");
		}
		elements = elements.get(0).getNestedXMLTagsWithName("Communicator");
		for (int i = 0; i < elements.size(); i++) {
			Communicator c = new Communicator();
			c.readXML(elements.get(i));
			if(c.getName().equals("WORLD")) {
				continue;
			}
			app.getCommunicators().put(c.getName(), c);
		}

		// now read Programs:
		Program [][] programs = new Program[app.getRankCount()] [];

		final XMLReaderToRAM reader = new XMLReaderToRAM();
		
		for (int rank = 0; rank < app.getRankCount(); rank++) {
			final int threadCnt =  app.getProcessThreadCount(rank);

			programs[rank] = new Program[threadCnt]; 

			for(int thread = 0 ; thread < threadCnt; thread++){

				// for each program open the corresponding file				
				final String file = TraceFileNames.getFilenameXML(app.getAbsoluteFilesPrefix(),
						rank, thread);

				if(!  (new File(file)).canRead() ){
					throw new IOException("File " + file + " is not readable!");
				}

				if(readCompleteProgram){
					// use DOM reader
					final XMLTag tag = reader.readXML(file);
					programs[rank][thread] = readProgramXMLDOM(rank, thread, tag, app);
				}else{ // use SAX Reader to read the file
					programs[rank][thread] = new ProgramReadXMLOnDemand(file, app);
					programs[rank][thread].setApplication(app, rank, thread);
				}
			}
		}

		app.setProcessThreadProgramMap(programs);

		return app;
	}

	/**
	 * Read the program from the XML node DOM. 
	 * 
	 * @param programmXMLnode
	 * @param program
	 * @throws Exception
	 */
	public Program readProgramXMLDOM(int rank, int thread, XMLTag processXML, Application app) throws Exception {

		final ProgramInMemory program = new ProgramInMemory();
		program.setApplication(app, rank, thread);

		LinkedList<XMLTag> elements = processXML.getNestedXMLTags();
		
		CommandXMLReader cmdReader = new CommandXMLReader(app);

		for (XMLTag xmlcmd: elements) {		
			// now read the particular command from the XML:
			Command cmd = cmdReader.readCommandXML(xmlcmd, program);

			program.getCommands().add(cmd);
		}

		return program;
	}

}
