
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2009 Michael Kuhn
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

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;

public class RequestFlush implements INetworkMessage {
	/// Internal states of the server.

	/**
	 * An initial request gets this tag:
	 */
	public static final int TAG = 0;

	/**
	 * The file on which the I/O should be performed.
	 */
	private final MPIFile file;

	/**
	 * Return the total size of this request.
	 */
	@Override
	public long getSize() {
		return 20;
	}

	/**
	 * Create a complete Request:
	 * @param listio
	 * @param file
	 */
	public RequestFlush(MPIFile file) {
		this.file = file;
	}

	/**
	 * @return the file to perform I/O on.
	 */
	public MPIFile getFile() {
		return file;
	}
}