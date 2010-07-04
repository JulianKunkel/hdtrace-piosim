
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


	final private IInterProcessNetworkJobCallback callbacks;

	/**
	 * Creates a new NewNetworkJob which receives data from the source.
	 * @return
	 */
	static public InterProcessNetworkJob createReceiveOperation(MessageMatchingCriterion matchingCriterion, IInterProcessNetworkJobCallback callback)
	{
		return new InterProcessNetworkJob(InterProcessNetworkJobType.RECEIVE, null, matchingCriterion, callback);
	}

	static public InterProcessNetworkJob createSendOperation(MessageMatchingCriterion matchingCriterion, IMessageUserData jobData, IInterProcessNetworkJobCallback callback)
	{
		return new InterProcessNetworkJob(InterProcessNetworkJobType.SEND, jobData, matchingCriterion, callback);
	}

	/**
	 * Create a new SingleNetwork Job, private to reduce the error-prone creation of new send
	 * or receive jobs.
	 *
	 */
	protected InterProcessNetworkJob(InterProcessNetworkJobType operation, IMessageUserData jobData,
			MessageMatchingCriterion matchingCriterion,
			IInterProcessNetworkJobCallback callbacks)
	{

		assert(matchingCriterion != null);

		this.operation = operation;
		this.matchingCriterion = matchingCriterion;


		this.jobData = jobData;
		this.callbacks = callbacks;
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
		return getJobOperation() + "<" + matchingCriterion.toString() + " data:" + jobData  + ">";
	}

	@Override
	public long getSize() {
		return jobData.getSize();
	}

	public IInterProcessNetworkJobCallback getCallbacks() {
		return callbacks;
	}
}