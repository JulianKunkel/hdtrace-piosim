
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

import java.util.LinkedList;

import de.hd.pvs.piosim.model.program.commands.Recv;
import de.hd.pvs.piosim.model.program.commands.Send;
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

public class VirtualSend extends CommandImplementation<Send>
{

	VirtualRcv receiverImpl = null;

	/**
	 * Overhead for every communication:
	 */
	final static int MESSAGE_HEADER_OVERHEAD = 4+4;

	public void process(Send cmd,  ICommandProcessing OUTresults, GClientProcess client, long step,  NetworkJobs compNetJobs) {


		if(receiverImpl == null){
			receiverImpl = (VirtualRcv) DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(Recv.class);
		}

		/* data to transfer depends on actual command size, but is defined in send */
		MessageMatchingCriterion crit = new MessageMatchingCriterion(client.getModelComponent(), receiverImpl.getTargetfromRank(client, cmd.getToRank(), cmd.getCommunicator()),
				cmd.getToTag(), cmd.getCommunicator(),  this, receiverImpl);

		LinkedList<ICommandProcessing> pendingClientsList =  receiverImpl.earlyReceiversPerTag.get(crit);

		if(pendingClientsList != null && pendingClientsList.size() > 0){
			// message matches
			ICommandProcessing pendingClient = pendingClientsList.pollFirst();
			pendingClient.getInvokingComponent().activateBlockedCommand(pendingClient);
			return;
		}

		pendingClientsList = receiverImpl.earlySendersPerTag.get(crit);
		if(pendingClientsList == null){
			pendingClientsList = new LinkedList<ICommandProcessing>();
			receiverImpl.earlySendersPerTag.put(crit, pendingClientsList);
		}

		if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
			//eager send completes immediately
			// we can continue already since it is a eager message
			pendingClientsList.add(null);
		}else{
			//rendezvous protocol, block now since we cannot pair
			pendingClientsList.add(OUTresults);
			OUTresults.setBlocking();
		}
	}

	@Override
	public String[] getAdditionalTraceAttributes(Send cmd) {
		return new String[] { "toRank",  cmd.getToRank() + "", "size", cmd.getSize() + "", "toTag", ""+ cmd.getToTag() };
	}
}
