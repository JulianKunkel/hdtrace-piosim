
/** Version Control Information $Id: SingleNetworkJob.java 149 2009-03-27 13:55:56Z kunkel $
 * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 149 $
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
package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.simulator.network.IMessageUserData;

/**
 * Encapsulates a single desired data transfer between two endpoints (either receive or send).
 * Matching between the messages is done via communicator, tag and sender.
 *
 * @author Julian M. Kunkel
 */

public class InterProcessNetworkJob implements IMessageUserData{

	final private MessageMatchingCriterion matchingCriterion;

	/**
	 * Send or Receive.
	 */
	final private InterProcessNetworkJobType  operation;

	/**
	 * Data contained in the Message.
	 */
	final private IMessageUserData   jobData;

	/**
	 * if flowMode == false (default) then use one big message
	 * otherwise a message part callback is called for each received or sent part.
	 */
	private boolean       partialCallbackActive;

	/**
	 * If this is a send message, is all data available or is the data to be transferred later?
	 */
	private boolean       isDataAvailable = true;


	/**
	 * Creates a new NewNetworkJob which receives data from the source.
	 * @return
	 */
	static public InterProcessNetworkJob createReceiveOperation(MessageMatchingCriterion matchingCriterion, boolean partial)
	{
		return new InterProcessNetworkJob(InterProcessNetworkJobType.RECEIVE, null, matchingCriterion, partial);
	}

	static public InterProcessNetworkJob createSendOperation(MessageMatchingCriterion matchingCriterion, IMessageUserData jobData, boolean partial)
	{
		return new InterProcessNetworkJob(InterProcessNetworkJobType.SEND, jobData, matchingCriterion, partial);
	}

	static public InterProcessNetworkJob createEmptySendOperation(MessageMatchingCriterion matchingCriterion, IMessageUserData jobData, boolean partial)
	{
		final InterProcessNetworkJob job = new InterProcessNetworkJob(InterProcessNetworkJobType.SEND, jobData, matchingCriterion, partial);
		job.isDataAvailable = false;
		return job;
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
	private InterProcessNetworkJob(InterProcessNetworkJobType operation, IMessageUserData jobData,
			MessageMatchingCriterion matchingCriterion,
			boolean partial) {

		assert(matchingCriterion != null);

		this.operation = operation;
		this.matchingCriterion = matchingCriterion;


		this.jobData = jobData;
		this.partialCallbackActive = partial;
	}

	public MessageMatchingCriterion getMatchingCriterion() {
		return matchingCriterion;
	}

	/**
	 * Return the type of this operation (Send or Receive)
	 * @return
	 */
	public InterProcessNetworkJobType getJobOperation() {
		return operation;
	}

	/**
	 * Return the data contained in this message.
	 * @return
	 */
	public IMessageUserData getJobData() {
		return jobData;
	}

	@Override
	public String toString() {
		return matchingCriterion.toString();
	}

	/**
	 * Should this send message invoke the sender callback on the sending component
	 * for each successful message part.
	 *
	 * @return the partialSend
	 */

	public boolean isPartialCallbackActive() {
		return partialCallbackActive;
	}

	@Override
	public long getSize() {
		return jobData.getSize();
	}


	public boolean isDataAvailable() {
		return isDataAvailable;
	}
}