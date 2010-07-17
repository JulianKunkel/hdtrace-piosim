
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

/**
 * Superclass for all different types of I/O requests a server can perform.
 *
 * @author Julian M. Kunkel
 */
public class RequestIO extends FileRequest {
	/// Internal states of the server.
	/**
	 * List of <size, offset> tuple for the I/O
	 */
	private final ListIO listIO;

	/**
	 * Create a complete Request:
	 * @param listio
	 * @param file
	 */
	public RequestIO(ListIO listio, MPIFile file) {
		super(file, 20 + listio.getIOOperations().size() * 8);
		this.listIO = listio;
	}

	/**
	 * @return the listIO
	 */
	public ListIO getListIO() {
		return listIO;
	}
}
