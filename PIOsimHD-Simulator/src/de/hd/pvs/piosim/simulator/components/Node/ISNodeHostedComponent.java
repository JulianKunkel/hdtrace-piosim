
 /** Version Control Information $Id: ISNodeHostedComponent.java 707 2009-10-08 22:19:00Z kunkel $
  * @lastmodified    $Date: 2009-10-09 00:19:00 +0200 (Fr, 09. Okt 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 707 $
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

package de.hd.pvs.piosim.simulator.components.Node;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Interface to simulate a NodeHostedComponent from the Model.
 *
 * @author Julian M. Kunkel
 *
 * @param <Type>
 */
public interface ISNodeHostedComponent<Type extends SPassiveComponent>
{

	public void setNetworkInterface(IProcessNetworkInterface nic);
	public IProcessNetworkInterface getNetworkInterface();

	public void setNodeRessources(INodeRessources ressources);
	public INodeRessources getNodeRessources();

	/**
	 * Return the identifier of this component
	 * @return
	 */
	public ComponentIdentifier getIdentifier();

	/**
	 * This callback is executed once the NetworkJobs completed.
	 * @param jobs
	 * @param endTime
	 */
	public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime);

	public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime);

	public void messagePartSendCB(MessagePart part, InterProcessNetworkJob myJob, Epoch endTime);

	public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime);

	public boolean mayIReceiveMessagePart(MessagePart part, InterProcessNetworkJob job);

	/**
	 * Gets called if a compute job is finished
	 * @param job
	 */
	public void computeJobCompletedCV(ComputeJob job);
}
