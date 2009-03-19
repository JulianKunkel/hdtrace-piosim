
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

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

public class CommandXMLReader {
	final private Program program;
	final private AttributeAnnotationHandler myCommonAttributeHandler;

	final static CommandFactory factory = new CommandFactory();


	class MyAttributeAnnotationHandler extends AttributeAnnotationHandler{
		public MyAttributeAnnotationHandler() {
			setDefaultXMLType(AttributeXMLType.ATTRIBUTE);
		}		

		// extend reader.
		public Object parseXMLString(java.lang.Class<?> type, String what) throws IllegalArgumentException {
			if (type == MPIFile.class) {
				return getFile(what);
			}else if (type == Communicator.class) {
				return getCommunicator(what);
			}
			return super.parseXMLString(type, what);
		};
	}

	public CommandXMLReader(Program program) {
		this.program = program;
		this.myCommonAttributeHandler = new MyAttributeAnnotationHandler();
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
	public Command readCommandXML(XMLTag commandXMLElement, Program program) throws Exception {
		Command cmd = factory.createCommand(commandXMLElement.getName().toLowerCase());

		// read non-standard attributes:
		cmd.readXML(commandXMLElement);

		// now try to fill all command fields as specified by the Annotations.
		myCommonAttributeHandler.readSimpleAttributes(commandXMLElement, cmd);

		// read default parameters for all programs from XML:
		String aid = commandXMLElement.getAttribute("aid");
		if (aid != null){
			cmd.setAsynchronousID( Integer.parseInt( aid ) );
		}
		cmd.setProgram(program);
		return cmd;
	}


	/**
	 * Helper function, returns the communicator as specified in the string.
	 *  
	 * @return
	 */
	private Communicator getCommunicator(String which){
		Integer number = Integer.parseInt(which);

		Communicator communicator;		
		communicator = program.getCommunicator(number);
		if (communicator == null){
			throw new IllegalArgumentException("Invalid Communicator with cid: " + which);
		}

		return communicator;
	}

	/**
	 * Helper function, returns the MPI_File as specified in the string.
	 * 
	 * @param which
	 * @return
	 */
	private MPIFile getFile(String which){		
		if (which == null){
			throw new IllegalArgumentException("No file given for command! But a file parameter is necessary!");
		}
		return program.getApplication().getFile(Integer.parseInt(which));		
	}

}
