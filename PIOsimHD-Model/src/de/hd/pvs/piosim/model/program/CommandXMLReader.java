
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

import java.util.HashMap;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.Fileclose;
import de.hd.pvs.piosim.model.program.commands.Fileopen;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;

public class CommandXMLReader {
	final private Program program;

	/**
	 * Maps the file id to the correct file.
	 * It gets populated by File_open and removed by File_close
	 */
	final private HashMap<Integer, MPIFile> fidToFileMap = new HashMap<Integer, MPIFile>();

	final private AttributeAnnotationHandler myCommonAttributeHandler;

	final static CommandFactory factory = new CommandFactory();


	class MyAttributeAnnotationHandler extends AttributeAnnotationHandler{
		public MyAttributeAnnotationHandler() {
			setDefaultXMLType(AttributeXMLType.ATTRIBUTE);
		}

		// extend reader.
		public Object parseXMLString(java.lang.Class<?> type, String what) throws IllegalArgumentException {
			if (type == MPIFile.class) {
				return fidToFileMap.get(Integer.parseInt(what));
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
	public Command parseCommandXML(XMLTag commandXMLElement) throws Exception {
		final Command cmd = factory.createCommand(commandXMLElement.getName());

		// special care for file open / close to update fids
		if(cmd.getClass() == Fileopen.class){
			final String name = commandXMLElement.getAttribute("name");
			final MPIFile file = program.getApplication().getFile(name);

			fidToFileMap.put(Integer.parseInt(commandXMLElement.getAttribute("fid")), file);
		}

		// TODO: handle file set view (!)

		cmd.setXMLTag(commandXMLElement);

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

		// special care for file open / close to update fids
		if(cmd.getClass() == Fileclose.class){
			fidToFileMap.remove(Integer.parseInt(commandXMLElement.getAttribute("fid")));
		}

		// parse File I/O command type id:
		if(FileIOCommand.class.isAssignableFrom(cmd.getClass())){
			final FileIOCommand fcmd = (FileIOCommand) cmd;

			final long typeID = Long.parseLong(commandXMLElement.getAttribute("tid"));
			final long offset = Long.parseLong(commandXMLElement.getAttribute("offset"));
			final long size = Long.parseLong(commandXMLElement.getAttribute("size"));

			ListIO list = new ListIO();
			list.addIOOperation(offset, size);
			fcmd.setListIO(list);
		}

		return cmd;
	}

	public Command parseCommandXML(ITraceEntry command) throws Exception {
		return parseCommandXML((XMLTag) command);
	}



	/**
	 * Helper function, returns the communicator as specified in the string.
	 *
	 * @return
	 */
	private Communicator getCommunicator(String which){
		final Integer number = Integer.parseInt(which);

		final Communicator communicator = program.getCommunicator(number);
		if (communicator == null){
			throw new IllegalArgumentException("Invalid Communicator with cid: " + which);
		}

		return communicator;
	}

}
