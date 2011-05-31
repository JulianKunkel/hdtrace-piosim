
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

/**
 *
 */
package de.hd.pvs.piosim.model.program.commands.superclasses;

import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.program.Communicator;

/**
 * Superclass for all I/O related operations.
 *
 * @author Julian M. Kunkel
 */
abstract public class FileCommand
	extends Command implements ICommunicatorCommand
{

	/**
	 * The filedescriptor this file was opened with.
	 * If it is a file open or close, then the communicator is set explicitly.
	 */
	private FileDescriptor fd;

	public void setFileDescriptor(FileDescriptor fd) {
		this.fd = fd;
	}

	public FileDescriptor getFileDescriptor() {
		return fd;
	}

	public FileMetadata getFile(){
		return fd.getFile();
	}

	public Communicator getCommunicator(){
		return fd.getCommunicator();
	}

	public void setCommunicator(Communicator communicator) {
		throw new IllegalArgumentException("Invalid to change the communicator!");
	}
}
