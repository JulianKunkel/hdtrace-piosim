
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

package de.hd.pvs.piosim.model.components.Switch;

import java.util.ArrayList;
import java.util.Collection;

import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.components.PaketRoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.NetworkComponent;


/**
 * A Switch contains of several Ports and allows to transfer data between two arbitrary
 * Ports.
 *
 * @author Julian M. Kunkel
 *
 * @has 1 - 0..n de.hd.pvs.piosim.model.components.Port.Port
 */
abstract public class Switch<PortType extends Port> extends BasicComponent {

	@ChildComponents
	private PaketRoutingAlgorithm routingAlgorithm = null;

	/**
	 * Return a list of connected Components.
	 */
	abstract public Collection<NetworkComponent> getConnectedComponents();

	/**
	 * Return the list of ports part of this switch.
	 * @return
	 */
	abstract public ArrayList<PortType> getPorts();

	/**
	 * Add a new port to the switch
	 * @param port
	 */
	abstract public void addNewPort(PortType port);

	@Override
	public String getComponentType() {
		return Switch.class.getSimpleName();
	}

	public PaketRoutingAlgorithm getRoutingAlgorithm() {
		return routingAlgorithm;
	}

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

}
