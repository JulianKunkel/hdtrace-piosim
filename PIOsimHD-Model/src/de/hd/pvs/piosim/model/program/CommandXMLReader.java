
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

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.AttributeAnnotationHandler;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.commands.Fileclose;
import de.hd.pvs.piosim.model.program.commands.Fileopen;
import de.hd.pvs.piosim.model.program.commands.Filesetview;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileCommand;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.model.program.fileView.FileView;

public class CommandXMLReader {
	final private Program program;

	/**
	 * Maps the file id to the correct file.
	 * It gets populated by File_open and removed by File_close
	 */
	final private static class LocalFileStructure{
		final FileDescriptor fd;
		// Maps the file id to the corresponding file view (if any)
		FileView currentView = null;

		public LocalFileStructure(FileDescriptor fd) {
			this.fd = fd;
		}
	}

	/**
	 * Maps the file id to the corresponding process local Data
	 */
	final private HashMap<Integer, LocalFileStructure> fidToFileMap = new HashMap<Integer, LocalFileStructure>();

	final private AttributeAnnotationHandler myCommonAttributeHandler;

	final static CommandFactory factory = new CommandFactory();


	class MyAttributeAnnotationHandler extends AttributeAnnotationHandler{
		public MyAttributeAnnotationHandler() {
			setDefaultXMLType(AttributeXMLType.ATTRIBUTE);
		}

		// extend reader.
		public Object parseXMLString(java.lang.Class<?> type, String what) throws IllegalArgumentException {
			if (type == Communicator.class) {
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
			final FileMetadata file = program.getApplication().getFile(name);
			final int fid = Integer.parseInt(commandXMLElement.getAttribute("fid"));
			final Communicator comm = getCommunicator(commandXMLElement.getAttribute("cid"));
			fidToFileMap.put(fid, new LocalFileStructure(new FileDescriptor(file, comm)));
		}

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

		if(FileCommand.class.isAssignableFrom(cmd.getClass())){
			final String fids = commandXMLElement.getAttribute("fid");
			final int fid = Integer.parseInt(fids);

			LocalFileStructure openendFilestructure = fidToFileMap.get(fid);
			if(openendFilestructure == null){
				throw new IllegalArgumentException("Error fid " + fid + ", but file was not opened previously!");
			}
			((FileCommand) cmd).setFileDescriptor(openendFilestructure.fd);

			// special care for file open / close to update fids
			if(cmd.getClass() == Fileclose.class){

				final LocalFileStructure openend = fidToFileMap.remove(fid);
				if(openend != null){
					// error checking that fid specified on close matches proper open
					String cCid = commandXMLElement.getAttribute("cid");
					if(cCid != null){
						final Communicator comm = getCommunicator(cCid);
						if(openend.fd.getCommunicator() != comm){
							System.err.println("Warning close of fid: " + fid + ", but is open with a different communicator." );
						}
					}
				}else{
					System.err.println("Warning: fid: " + fid + " not open, but closed !");
				}
			}else if(cmd.getClass() == Filesetview.class){
				//final long etid = Long.parseLong(commandXMLElement.getAttribute("etid"));
				final long filetid = Long.parseLong(commandXMLElement.getAttribute("filetid"));
				final int displacement = Integer.parseInt(commandXMLElement.getAttribute("offset"));
				Datatype datatype = program.getApplication().getDatatypeMap(program.getRank()).get(filetid);
				assert(datatype != null);

				final LocalFileStructure openend = fidToFileMap.get(fid);
				if(openend != null){
					FileView view = new FileView(datatype, displacement);
					openend.currentView = view;
				}else{
					System.err.println("Warning: " + fid + " not open but setView !");
				}
			}

			// parse File I/O command type id:
			if(FileIOCommand.class.isAssignableFrom(cmd.getClass())){
				final FileIOCommand fcmd = (FileIOCommand) cmd;
				final long offset = Long.parseLong(commandXMLElement.getAttribute("offset"));
				final long size = Long.parseLong(commandXMLElement.getAttribute("size"));

				if(openendFilestructure != null){
					final ListIO list =  new ListIO();

					if(openendFilestructure.currentView == null){
						list.addIOOperation(offset, size);
					}else{
						openendFilestructure.currentView.createIOOperation(list, offset, size);
						fcmd.setFileView(openendFilestructure.currentView);
					}

					fcmd.setListIO(list);
				}else{
					System.err.println("Warning: " + fid + " not open but I/O should be done !");
				}
			}
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
