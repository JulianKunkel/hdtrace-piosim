
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
package de.hd.pvs.piosim.model.inputOutput.distribution;

import java.util.HashMap;
import java.util.List;

import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.interfaces.IDynamicModelComponent;

/**
 * A distribution describes how data get distributed among a set of servers.
 *
 * @author Julian M. Kunkel
 */
abstract public class Distribution implements IDynamicModelComponent {
	/**
	 * Computes on which servers the data should be read/written. Similar to RAID
	 * concepts.
	 * @param serverList the existing servers
	 * @param iolist the input I/O which is split among the servers
	 * @return A map which contains for each server the actual I/O operations to perform
	 */
	abstract public HashMap<Server, ListIO> distributeIOOperation(
			ListIO iolist,
			List<Server> serverList
	);
}
