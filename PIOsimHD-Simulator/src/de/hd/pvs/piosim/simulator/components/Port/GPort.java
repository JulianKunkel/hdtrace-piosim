
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

package de.hd.pvs.piosim.simulator.components.Port;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.event.MessagePart;

/**
 * An simulated outgoing port.
 *
 * @author Julian M. Kunkel
 */
public class GPort extends SNetworkComponent<Port>{
	/**
	 * The simulated component this Port is connected to.
	 */
	private SNetworkComponent connectedComponent = null;

	@Override
	protected SNetworkComponent getTargetFlowComponent(MessagePart part) {
		return connectedComponent;
	}

	@Override
	protected Epoch getProcessingTime(MessagePart p) {
		return  new Epoch(p.getSize() / (double) getModelComponent().getConnection().getBandwidth() );
	}


	@Override
	protected Epoch getMaximumProcessingTime() {
		return new Epoch(getSimulator().getModel().getGlobalSettings().getTransferGranularity() /
				(double)  getModelComponent().getConnection().getBandwidth());
	}

	@Override
	protected Epoch getProcessingLatency() {
		return getModelComponent().getConnection().getLatency();
	}

	public SNetworkComponent getConnectedComponent() {
		return connectedComponent;
	}

	public void setConnectedComponent(SNetworkComponent connectedComponent) {
		this.connectedComponent = connectedComponent;
	}
}