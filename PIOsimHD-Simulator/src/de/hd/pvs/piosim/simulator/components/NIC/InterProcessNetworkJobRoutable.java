
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

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;


/**
 * Encapsulates a single desired data transfer between two endpoints (either receive or send).
 * Matching between the messages is done via communicator, tag and sender.
 *
 * @author Julian M. Kunkel
 */

public class InterProcessNetworkJobRoutable extends InterProcessNetworkJob{
	final private INodeHostedComponent originalSource;
	final private INodeHostedComponent finalTarget;

	static public InterProcessNetworkJobRoutable createRoutableSendOperation(MessageMatchingCriterion matchingCriterion, IMessageUserData jobData, IInterProcessNetworkJobCallback callback, INodeHostedComponent originalSource, INodeHostedComponent finalTarget, RelationToken relationToken)
	{
		return new InterProcessNetworkJobRoutable(InterProcessNetworkJobType.SEND, jobData, matchingCriterion, callback, originalSource, finalTarget, relationToken);
	}

	public InterProcessNetworkJobRoutable(InterProcessNetworkJobType operation, IMessageUserData jobData,
			MessageMatchingCriterion matchingCriterion,	IInterProcessNetworkJobCallback callback, INodeHostedComponent originalSource, INodeHostedComponent finalTarget, RelationToken relationToken) {
		super(operation, jobData, matchingCriterion, callback, relationToken);
		this.originalSource = originalSource;
		this.finalTarget = finalTarget;
	}

	public INodeHostedComponent getFinalTarget() {
		return finalTarget;
	}

	public INodeHostedComponent getOriginalSource() {
		return originalSource;
	}
}