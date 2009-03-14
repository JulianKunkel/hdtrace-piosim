
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
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hd.pvs.TraceFormat.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.xml.XMLutil;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.Distribution;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * This class is used to create an Application object from an XML description.
 *  
 * @author Julian M. Kunkel
 */

public class ApplicationXMLReader extends ProjectDescriptionXMLReader {

	class MyAttributeAnnotationHandler extends AttributeAnnotationHandler{
		final Application app ; 
		public MyAttributeAnnotationHandler(Application app) {
			this.app = app;
			setDefaultXMLType(AttributeXMLType.ATTRIBUTE);
		}		

		// extend reader.
		public Object parseXMLString(java.lang.Class<?> type, String what) throws IllegalArgumentException {
			if (type == MPIFile.class) {
				return getFile(what, app);
			}else if (type == Communicator.class) {
				return getCommunicator(what, app);
			}
			return super.parseXMLString(type, what);
		};
	}


	/**
	 * Parse an application of the file.
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public Application parseApplication(String filename) throws Exception{
		try {
			return parseApplication(new File(filename));
		}catch (Exception e) {
			throw new IllegalArgumentException( "Invalid application " + filename, e);
		}
	}

	/**
	 * Parse an application of the file.
	 * 
	 * @param XMLFile
	 * @return
	 * @throws Exception
	 */
	public Application parseApplication(File XMLFile) throws Exception {
		super.readProjectDescription(XMLFile.getAbsolutePath());
		
		if (! XMLFile.canRead()) {
			throw new IllegalArgumentException("Application not readable: " + XMLFile.getAbsolutePath());
		}
		AttributeAnnotationHandler commonHandler = new AttributeAnnotationHandler();

		Application app = new Application();

		app.setProjectFilename(XMLFile.getAbsolutePath());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(XMLFile);

		ArrayList<Element> elements;
		Element applicationNode = document.getDocumentElement();

		/* read file list */
		elements = XMLutil.getElementsByTag(applicationNode, "FileList");
		if (elements.size() != 1) {
			throw new InvalidParameterException("Invalid XML, wrong FileList tags found!");
		}
		elements = XMLutil.getElementsByTag(elements.get(0), "File");
		for (int i = 0; i < elements.size(); i++) {
			MPIFile f = new MPIFile();

			commonHandler.readSimpleAttributes(elements.get(i), f);
			f.setDistribution(Distribution.readDistributionFromXML(XMLutil.getFirstElementByTag(elements.get(i), 
			"Distribution")));

			app.getFiles().put(f.getId(), f);
		}

		/* read communicator list */
		elements =  XMLutil.getElementsByTag(applicationNode, "CommunicatorList");
		if (elements.size() != 1) {
			throw new InvalidParameterException(
			"Invalid XML, wrong CommunicatorList tags defined (at least WORLD must exist)!");
		}
		elements = XMLutil.getElementsByTag(elements.get(0), "Communicator");
		for (int i = 0; i < elements.size(); i++) {
			Communicator c = new Communicator();
			c.readXML(elements.get(i));
			if(c.getName().equals("WORLD")) {
				continue;
			}
			app.getCommunicators().put(c.getName(), c);
		}

		// now read Programs:
		Program [] programs = new Program[app.getProcessCount()];
			// todo use SAX for parsing.
			Document appXML = builder.parse(filename);
			programs[i] = readProgramXMLDOM(i, appXML.getDocumentElement(), app);

			
		app.setRankProgramMap(programs);

		return app;
	}

	/**
	 * Read the program from the XML node DOM. 
	 * 
	 * @param programmXMLnode
	 * @param program
	 * @throws Exception
	 */
	public Program readProgramXMLDOM(int expectedRank, Element processXML, Application app) throws Exception {

		String number_str = processXML.getAttribute("number");
		int rank = Integer.parseInt(number_str);
		if (rank != expectedRank) {
			throw new IllegalArgumentException(
					"Invalid XML, Wrong rank numbering in application " + app.getAlias() + " program rank:" + rank);
		}

		Program program = new Program(app, rank);		


		CommandFactory factory = new CommandFactory();

		ArrayList<Element> elements = XMLutil.getChildElements(XMLutil.getFirstElementByTag(processXML, "Program"));
		for (Element xmlcmd: elements) {

			Command cmd = factory.createCommand(xmlcmd.getNodeName().toLowerCase());		
			// read default parameters for all programs from XML:
			Node aid = xmlcmd.getAttributes().getNamedItem("aid");
			if (aid != null){
				cmd.setAsynchronousID( Integer.parseInt( aid.getNodeValue() ) );
			}
			cmd.setProgram(program);

			// now read the particular command from the XML:
			readCommandXML(xmlcmd, cmd, program.getApplication());

			program.getCommands().add(cmd);
		}

		return program;
	}

	/**
	 * Read the XML of a command from the DOM. Therefore Attribute annotated fields are filled with
	 * the XML content.
	 * 
	 * @param commandXMLElement
	 * @param cmd
	 * @param app
	 * @throws Exception
	 */
	private void readCommandXML(Element commandXMLElement, Command cmd, Application app) throws Exception {
		// read non-standard attributes:
		cmd.readXML(commandXMLElement);

		// now try to fill all command fields as specified by the Annotations.
		AttributeAnnotationHandler myCommonAttributeHandler = new MyAttributeAnnotationHandler(app);

		myCommonAttributeHandler.readSimpleAttributes(commandXMLElement, cmd);
	}


	/**
	 * Helper function, returns the communicator as specified in the string.
	 *  
	 * @return
	 */
	private Communicator getCommunicator(String which, Application app){
		Communicator communicator;		
		if(which != null){
			communicator = app.getCommunicator(which.toUpperCase());
			if (communicator == null){
				throw new IllegalArgumentException("Invalid Communicator with name " + which);
			}

		}else{
			communicator = app.getCommunicator("WORLD");
			if (communicator == null){
				throw new IllegalArgumentException("Invalid Communicator with name " + "WORLD");
			}
		}
		return communicator;
	}

	/**
	 * Helper function, returns the MPI_File as specified in the string.
	 * 
	 * @param which
	 * @param app
	 * @return
	 */
	private MPIFile getFile(String which, Application app){		
		if (which == null){
			throw new IllegalArgumentException("No file given for command! But a file parameter is necessary!");
		}
		return app.getFile(Integer.parseInt(which));		
	}
}
