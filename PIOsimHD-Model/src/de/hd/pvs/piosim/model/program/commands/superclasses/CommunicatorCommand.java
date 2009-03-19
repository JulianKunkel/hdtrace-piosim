
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

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.program.Communicator;

/**
 * A Communicator Command is a Command several clients issue together in a collective fashion, 
 * e.g. Barrier
 * 
 * @author Julian M. Kunkel
 */
abstract public class CommunicatorCommand 
extends Command 
implements ICommunicatorCommand
{

	@NotNull
	@Attribute(xmlName="cid")
	protected Communicator communicator;

	/**
	 * Get the Communicator the Command should work on
	 * @return
	 */
	public Communicator getCommunicator(){
		return communicator;
	}
	
	/**
	 * Set the Communicator the Command should work on
	 * @param communicator
	 */
	public void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
	}

}
