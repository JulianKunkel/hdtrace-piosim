
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

package de.hd.pvs.piosim.simulator.component.Server;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.component.IGComponent;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;


public interface IGServer<Type extends SPassiveComponent> 
extends IGComponent<Type>, 
ISNodeHostedComponent<Type>
{
	/**
	 * If an receive got stalled by the IGServerCacheLayer implementation, the implementation
	 * should call this function to schedule a blocked one if it desires to. 
	 */
	public void startupBlockedIOReceiveIfPossible(Epoch startTime);
}
