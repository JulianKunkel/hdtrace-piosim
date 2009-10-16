
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

package de.hd.pvs.piosim.simulator.network.jobs.requests;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;

/**
 * Superclass for all different types of I/O requests a server can perform.
 *
 * @author Julian M. Kunkel
 */
public class RequestIO implements IMessageUserData {
	/// Internal states of the server.

	/**
	 * An initial request gets this tag:
	 */
	public static final int INITIAL_REQUEST_TAG = 0;
	/**
	 * Data transfer is started.
	 */
	public static final int IO_DATA_TAG = 1;
	/**
	 * I/O is completed.
	 */
	public static final int IO_COMPLETION_TAG = 2;

	/**
	 * List of <size, offset> tuple for the I/O
	 */
	private final ListIO listIO;

	/**
	 * The file on which the I/O should be performed.
	 */
	private final MPIFile file;

	/**
	 * Return the total size of this request.
	 */
	@Override
	public long getSize() {
		return 20+listIO.getIOOperations().size() * 8 ;
	}

	/**
	 * Create a complete Request:
	 * @param listio
	 * @param file
	 */
	public RequestIO(ListIO listio, MPIFile file) {
		this.listIO = listio;
		this.file = file;
	}

	/**
	 * @return the listIO
	 */
	public ListIO getListIO() {
		return listIO;
	}

	/**
	 * @return the file to perform I/O on.
	 */
	public MPIFile getFile() {
		return file;
	}
}
