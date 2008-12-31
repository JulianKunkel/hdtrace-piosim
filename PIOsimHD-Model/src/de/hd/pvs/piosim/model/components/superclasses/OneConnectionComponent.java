
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
package de.hd.pvs.piosim.model.components.superclasses;

import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.components.Connection.Connection;


/**
 * A Superclass for network components which have exactly one connection (like a NIC or a Port). 
 * 
 * @author Julian M. Kunkel
 */
abstract public class OneConnectionComponent<ParentType extends BasicComponent>
extends NetworkComponent<ParentType>
{
	
	@ChildComponents
	private Connection connection = new Connection();
	
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	
	public String toString() {
		return super.toString() + " <" + connection + ">";
	}
	
	public NetworkComponent getConnectedComponent() {
		return connection.getConnectedComponent();
	}

	/**
	 * Set the whole connection from outside. 
	 * WARNING: You should know what you do.
	 * 
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public void setConnectedComponent(NetworkComponent connectedComponent) {
		connection.setConnectedComponent(connectedComponent);
	}	
}
