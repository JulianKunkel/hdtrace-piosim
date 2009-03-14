
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

package de.hd.pvs.piosim.simulator.interfaces;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.event.IOJob;

/**
 * This interface defines the callback for IOSubsystems.
 * It allows to return the state of the asychronous operations to the owning component. 
 * 
 * @author Julian M. Kunkel
 */
public interface IIOSubsystemCaller {
	
	/**
	 * Signals the completion of an particular IOJob. 
	 * @param endTime
	 * @param job must be the EXACT object submitted to the IOsubsystem.
	 */
	public void IOComplete(Epoch endTime, IOJob job);
}
