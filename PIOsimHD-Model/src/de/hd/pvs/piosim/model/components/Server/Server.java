
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

package de.hd.pvs.piosim.model.components.Server;

import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.superclasses.NodeHostedComponent;

/**
 * The Server is an I/O process which manages data. 
 * 
 * @author Julian M. Kunkel
 *
 * @has 1 - 1 de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem
 * @has 1 - 1 de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer
 */
public class Server extends NodeHostedComponent {	
	/**
	 * The simple Server contains one IOsubsystem used to store data.
	 */
	@ChildComponents
	private IOSubsystem iosubsystem = null;
	
	/**
	 * The cache implementation to choose for this particular server.
	 */
	@NotNull
	@ChildComponents	
	private ServerCacheLayer cacheStrategy = null;

	/**
	 * @return the iosubsystem
	 */
	public IOSubsystem getIOsubsystem() {
		return iosubsystem;
	}
	
	public void setIOsubsystem(IOSubsystem iosubsystem) {
		this.iosubsystem = iosubsystem;
	}
	
	/**
	 * @return the cacheImplementation
	 */
	public ServerCacheLayer getCacheImplementation() {
		return cacheStrategy;
	}
	
	/**
	 * @param cacheImplementation the cacheImplementation to set
	 */
	public void setCacheImplementation(ServerCacheLayer cacheImplementation) {
		this.cacheStrategy = cacheImplementation;
	}

	@Override
	public String getComponentType() {
		return Server.class.getSimpleName();
	}
}
