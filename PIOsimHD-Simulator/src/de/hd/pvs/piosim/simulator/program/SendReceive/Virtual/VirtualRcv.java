
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
package de.hd.pvs.piosim.simulator.program.SendReceive.Virtual;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Recv;
import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.DynamicImplementationLoader;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * @author Julian M. Kunkel
 *
 */

public class VirtualRcv extends CommandImplementation<Recv>
{
	HashMap<MessageMatchingCriterion, LinkedList<ICommandProcessing>> earlySendersPerTag = new HashMap<MessageMatchingCriterion, LinkedList<ICommandProcessing>>();

	HashMap<MessageMatchingCriterion, LinkedList<ICommandProcessing>> earlyReceiversPerTag = new HashMap<MessageMatchingCriterion, LinkedList<ICommandProcessing>>();
	// TODO implement anyTag or anySource

	VirtualSend senderImpl = (VirtualSend) DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(Send.class);


	public void process(Recv cmd,  ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		MessageMatchingCriterion crit = new MessageMatchingCriterion(getTargetfromRank(client, cmd.getFromRank(), cmd.getCommunicator()),client.getModelComponent(),
				cmd.getFromTag(), cmd.getCommunicator(),  senderImpl, this);

		LinkedList<ICommandProcessing> pendingClientsList =  earlySendersPerTag.get(crit);

		if(pendingClientsList != null && pendingClientsList.size() > 0){
			// message matches
			ICommandProcessing pendingClient = pendingClientsList.pollFirst();
			if(pendingClient != null){
				// rendezvous mode => activate sender
				pendingClient.getInvokingComponent().activateBlockedCommand(pendingClient);
			}
			// we can continue
		}else{
			// no matching tag => block

			pendingClientsList = earlyReceiversPerTag.get(crit);
			if(pendingClientsList == null){
				pendingClientsList = new LinkedList<ICommandProcessing>();
				earlyReceiversPerTag.put(crit, pendingClientsList);
			}

			pendingClientsList.add(OUTresults);

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			OUTresults.setBlocking();
		}

		return;
	}

	@Override
	public String[] getAdditionalTraceAttributes(Recv cmd) {
		return new String[] { "fromTag", "" + cmd.getFromTag(), "fromRank", "" + cmd.getFromRank() };
	}

	final INodeHostedComponent getTargetfromRank(GClientProcess client, int rank, Communicator comm){
		assert(rank >= 0);
		rank = comm.getWorldRank(rank);
		String app = client.getModelComponent().getApplication();
		GClientProcess c = client.getSimulator().getApplicationMap().getClient( app,  rank);

		return c.getModelComponent();
	}
}
