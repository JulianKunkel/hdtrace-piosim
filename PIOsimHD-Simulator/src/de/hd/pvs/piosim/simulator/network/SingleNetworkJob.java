
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

/**
 * 
 */
package de.hd.pvs.piosim.simulator.network;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;

/**
 * Encapsulates a single desired data transfer between two endpoints (either receive or send).
 * Matching between the messages is done via communicator, tag and sender.
 * 
 * @author Julian M. Kunkel
 */

public class SingleNetworkJob{
	
	/**
	 * Receiver of the network message
	 */
	final private ISNodeHostedComponent targetComponent;
	
	/**
	 * Sender of the network message.
	 */
	final private ISNodeHostedComponent sourceComponent; 
		
	static final public int ANY_TAG = -1;
	
	/**
	 * Analog to MPI: tag, comm:
	 */
	final private int               tag; 	/* Matching of Jobs via TAG && senderIdentity && comm */
	final private Communicator      comm;
	
	/**
	 * Send or Receive.
	 */
	final private NetworkJobType  operation;
	
	/**
	 * Data contained in the Message.
	 */
	final private INetworkMessage   jobData;
	
	/**
	 * The parent set of network jobs.
	 * 
	 * null if this is just a single network job
	 */
	final private NetworkJobs   parentJobs; 
	
	/**
	 * if flowMode == false (default) then use one big message
	 * otherwise a message part callback is called for each received or sent part. 
	 */
	final private boolean       partialSendCallbackActive;
	final private boolean       partialRecvCallbackActive;
	
	/**
	 * Creates a new NetworkJob to send data from the source to the target.
	 * @param jobData
	 * @param sourceComponent
	 * @param targetComponent
	 * @param tag
	 * @param comm
	 * @param parentJobs
	 * @param partialSend
	 * @param shouldPartialRecv
	 * @return
	 */
	static public SingleNetworkJob createSendOperation(	
			INetworkMessage jobData, 
			ISNodeHostedComponent sourceComponent,
			ISNodeHostedComponent targetComponent, 
			int tag, Communicator comm, 
			NetworkJobs   parentJobs, 
			boolean partialSend, 
			boolean shouldPartialRecv) 
	{
		
		assert(sourceComponent != null); //sourceComponent == null if any source AND recv
		
		return new SingleNetworkJob(NetworkJobType.SEND, jobData , sourceComponent, targetComponent, tag, comm, parentJobs, 
				partialSend, shouldPartialRecv);		
	}
	
	/**
	 * Creates a new NewNetworkJob which receives data from the source.
	 * @param receiver
	 * @param senderSource can be null (MPI_ANY_SOURCE wildcard)
	 * @param tag
	 * @param comm
	 * @param parentJobs
	 * @return
	 */
	static public SingleNetworkJob createReceiveOperation(
			ISNodeHostedComponent receiver,
			ISNodeHostedComponent senderSource, 
			int tag, 
			Communicator comm, 
			NetworkJobs   parentJobs) 
	{
		return new SingleNetworkJob(NetworkJobType.RECEIVE, null, senderSource, receiver, tag, comm, parentJobs, 
				false, false);		
	}
	
	/**
	 * Create a new SingleNetwork Job, private to reduce the error-prone creation of new send
	 * or receive jobs.
	 * 
	 * @param operation
	 * @param jobData
	 * @param sourceComponent (can be NULL if receive and MPI_ANY_SOURCE tag).
	 * @param targetComponent
	 * @param tag
	 * @param comm
	 * @param parentJobs if the NIC should only signal if all jobs completed
	 * @param flowMode
	 */
	private SingleNetworkJob(NetworkJobType operation, INetworkMessage jobData, 
			ISNodeHostedComponent sourceComponent,
			ISNodeHostedComponent targetComponent, int tag, Communicator comm, NetworkJobs   parentJobs, 
			boolean partialSend, boolean shouldPartialRecv) {
			
		assert(targetComponent != null);
		
		this.targetComponent = targetComponent;
		this.operation = operation;
		this.sourceComponent = sourceComponent;
		
		this.jobData = jobData;
		this.tag = tag;
		this.comm = comm;
		this.parentJobs = parentJobs;
		this.partialSendCallbackActive = partialSend;
		this.partialRecvCallbackActive = shouldPartialRecv;
	}
	
	/**
	 * get the receiver of the message
	 * @return
	 */
	public ISNodeHostedComponent getTargetComponent() {
		return targetComponent;
	}
	
	/**
	 * Get the sender of the network message.
	 * @return
	 */
	public ISNodeHostedComponent getSourceComponent() {
		return sourceComponent;
	}
	
	/**
	 * Return the type of this operation (Send or Receive)
	 * @return
	 */
	public NetworkJobType getJobOperation() {
		return operation;
	}
	
	/**
	 * Return the data contained in this message.
	 * @return
	 */
	public INetworkMessage getJobData() {
		return jobData;
	}

	/**
	 * Return the matching tag.
	 * @return
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * @return the communicator
	 */
	public Communicator getCommunicator() {
		return comm;
	}
	
	/**
	 * @return the parentJobs (if any) to which this message belongs.
	 */
	public NetworkJobs getParentNetworkJobs() {
		return parentJobs;
	}
	
	@Override
	public String toString() {
		if(getSourceComponent() == null){
			return operation.toString() + " < null, " + 
				getTargetComponent().getIdentifier() + ", " + getTag() + "," + getCommunicator() +  ">";
		}else{
			return operation.toString() + " <" + getSourceComponent().getIdentifier() + ", " + 
				getTargetComponent().getIdentifier() + ", " + getTag() + "," + getCommunicator() +  ">";
		}
	}

	/**
	 * Should this send message invoke the sender callback on the sending component 
	 * for each successful message part.
	 * 
	 * @return the partialSend
	 */
	public boolean isPartialSendCallbackActive() {
		return partialSendCallbackActive;
	}
	
	/**
	 * Should this send message invoke the receiver callback on the target component 
	 * for each message part sucessfully received.
	 * 
	 * @return the shouldPartialRecv
	 */
	public boolean isPartialRecvCallbackActive() {
		return partialRecvCallbackActive;
	}
}