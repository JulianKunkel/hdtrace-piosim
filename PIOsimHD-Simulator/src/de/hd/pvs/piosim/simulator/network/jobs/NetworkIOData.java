
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
package de.hd.pvs.piosim.simulator.network.jobs;

import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;


/**
 * Contains I/O data (read or write).
 *
 * @author Julian M. Kunkel
 *
 */
public class NetworkIOData extends NetworkSimpleData {
	private final RequestIO ioRequest;

	public NetworkIOData(RequestIO ioRequest) {
		super(ioRequest.getListIO().getTotalSize());
		this.ioRequest = ioRequest;
	}

	/**
	 * @return the ioRequest
	 */
	public RequestIO getIORequest() {
		return ioRequest;
	}
}
