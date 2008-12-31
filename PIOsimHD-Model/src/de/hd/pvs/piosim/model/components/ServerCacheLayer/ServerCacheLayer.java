
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
package de.hd.pvs.piosim.model.components.ServerCacheLayer;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;

/**
 * Superclass for all model cache components.
 * Each server can pick a particular cache layer for itself and set its parameters.
 *  
 * @author Julian M. Kunkel
 */
abstract public class ServerCacheLayer 
extends BasicComponent<Server>{	
	/**
	 * Define the maximum number of concurrent operations which should be started on the
	 * I/O subsystem.
	 */
	@Attribute
	@NotNegativeOrZero
	int maxNumberOfConcurrentIOOps = -1;
	
	final public void setMaxNumberOfConcurrentIOOps(int maxNumberOfConcurrentIOOps) {
		this.maxNumberOfConcurrentIOOps = maxNumberOfConcurrentIOOps;
	}
	
	final public int getMaxNumberOfConcurrentIOOps() {
		return maxNumberOfConcurrentIOOps;
	}
	
	@Override
	final public String getComponentType() {		
		return ServerCacheLayer.class.getSimpleName();
	}
}
