
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

package de.hd.pvs.piosim.simulator.interfaces;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntryCallbacks;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExitCallbacks;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;

/**
 * Interface to simulate a NodeHostedComponent from the Model.
 *
 * @author Julian M. Kunkel
 *
 * @param <Type>
 */
public interface ISNodeHostedComponent<Type extends SPassiveComponent>
	extends IGNetworkEntryCallbacks, IGNetworkExitCallbacks
{
	/**
	 * Get the simulated node hosting this component.
	 * @return
	 */
	public GNode getAttachedNode();

	/**
	 * Return the identifier of this component
	 * @return
	 */
	public ComponentIdentifier getIdentifier();

	/**
	 * Return the Simulated Component implementing this interface itself.
	 * @return
	 */
	public Type getSimulatorObject();


	/**
	 * This callback is executed once the NetworkJobs completed.
	 * @param jobs
	 * @param endTime
	 */
	public void jobsCompletedCB(NetworkJobs jobs, Epoch endTime);

	/**
	 * Gets called if a compute job is finished
	 * @param job
	 */
	public void computeJobCompletedCV(ComputeJob job);
}
