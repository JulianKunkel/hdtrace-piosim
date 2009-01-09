
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
package de.hd.pvs.piosim.simulator.event;

import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

/**
 * Data contained in the event is part of a network transfer which has a 
 * defined target component it should get delivered to.
 * 
 * @author Julian M. Kunkel
 *
 */
abstract class NetworkEventType implements FlowEvent{
	abstract public SingleNetworkJob getNetworkJob();
	
	@Override
	final public ISNodeHostedComponent getFinalTarget() {		
		return getNetworkJob().getTargetComponent();
	}
}