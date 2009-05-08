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
package de.hd.pvs.piosim.model.program.commands;

import java.util.HashMap;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;

public class Filereadall extends FileIOCommand {
	Communicator comm;
	HashMap<Integer,ListIO> io = new HashMap<Integer,ListIO>();

	public Filereadall(Communicator comm) {
		this.comm = comm;
	}

	public Communicator getCommunicator() {
		return this.comm;
	}

	public ListIO getIOList(Integer rank) {
		return this.io.get(rank);
	}

	public void setListIO(Integer rank, ListIO io) {
		this.io.put(rank, io);
	}
}
