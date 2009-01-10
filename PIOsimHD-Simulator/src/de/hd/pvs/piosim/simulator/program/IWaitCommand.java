
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

package de.hd.pvs.piosim.simulator.program;

import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;

/**
 * All implementations for WAIT commands must implement this interface.
 * 
 * @author Julian M. Kunkel
 *
 */
public interface IWaitCommand {
	/**
	 * Signal that a pending AIO operation finished.
	 * 
	 * @param which one finished
	 */
	public void pendingAIOfinished(Wait cmd, CommandProcessing prevStep, GClientProcess client, Integer which);

}
