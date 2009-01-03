
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
package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.event.Message;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

/**
 * Simulates a Network Interface Card.
 * 
 * A NIC is split into NIC upload (TX - realized by subclasses) and NIC download (RX) 
 * (directly incorporated in the main class).
 * 
 * @author Julian M. Kunkel
 */
abstract public class GNIC<Type extends NIC>  extends SNetworkComponent<Type>{
	
	/**
	 * This class encapsulates the matching of receive network jobs with send jobs.
	 * It compares jobs, jobs are considered to be equal if they can be matched.
	 * A send and recv is considered to match if the source, tag and communicator matches.  
	 * 
	 * @author Julian M. Kunkel
	 *
	 */
	public static class MSGMatchingCriterion{
		SingleNetworkJob job;
		
		public MSGMatchingCriterion(SingleNetworkJob job){
			this.job = job;
		}
		
		@Override
		public boolean equals(Object obj) {		
			MSGMatchingCriterion c = (MSGMatchingCriterion) obj;
			boolean ret = (c.job.getCommunicator() == this.job.getCommunicator());
			ret &=(c.job.getTag() == this.job.getTag());
			
			ret &= job.getSourceComponent() == c.job.getSourceComponent();
			ret &= job.getTargetComponent() == c.job.getTargetComponent();
			
			return ret;
		}

		@Override
		public String toString() {
			return "CRIT: " +  job.getSourceComponent().getIdentifier() + " - " + job.getTargetComponent().getIdentifier() + " " + job.getTag() + " " + job.getCommunicator();
		}
		
		@Override
		public int hashCode() {
			return job.getTargetComponent().hashCode() + job.getSourceComponent().hashCode() + job.getCommunicator().hashCode();
		}
	}
	
	/**
	 * The node owning this NIC.
	 */
	private GNode attachedNode;
	
	final public void setAttachedNode(GNode attachedNode) {
		this.attachedNode = attachedNode;
	}
		
	
	/**
	 * Append some new data to a message i.e. new data gets available for a message and can be
	 * transferred.
	 * 
	 * @param message
	 * @param count
	 */
	abstract public void appendAvailableDataToIncompleteSend(Message message, long count);
	
	/**
	 * Start a new network job.
	 * 
	 * The method will delegate the job either to the upload queue or register the job on the 
	 * download queue
	 */
	abstract public void submitNewNetworkJob(SingleNetworkJob job);
	
	final public GNode getAttachedNode() {
		return attachedNode;
	}
	
	@Override
	final protected boolean isDirectlyControlledByBlockUnblock() {		
		return true;
	}
}
