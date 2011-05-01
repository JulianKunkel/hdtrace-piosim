
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
package de.hd.pvs.piosim.simulator.program.SendReceive.Rendezvous;

import de.hd.pvs.piosim.model.program.commands.Recv;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousRcv extends CommandImplementation<Recv>
{
	public void process(Recv cmd,  CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {

		final int ACK_RECVD = 1;
		final int LAST = 2;

		/* second step ?, receive whole data */
		switch(step){
		case(CommandProcessing.STEP_START):{
			/* determine application */
			OUTresults.setNextStep(ACK_RECVD);

			if (cmd.getFromRank() >= 0){
				OUTresults.addNetReceive(cmd.getFromRank(), cmd.getFromTag(), cmd.getCommunicator(), NetworkMessageRendezvousMsg.class);
			}else{
				OUTresults.addNetReceiveAnySource(cmd.getFromTag(), cmd.getCommunicator(), NetworkMessageRendezvousMsg.class);
			}

			return;
		}case(ACK_RECVD):{
			InterProcessNetworkJob response = compNetJobs.getResponses().get(0);

			//System.out.println("Receive got ACK from " +  response.getMatchingCriterion().getSourceComponent().getIdentifier() );

			client.debug("Receive got ACK from " +  response.getMatchingCriterion().getSourceComponent().getIdentifier() );

			if( ((NetworkMessageRendezvousMsg)response.getJobData()).isRequestRendezvous() ){
				//rendezvous protocol
				/* identify the sender from the source */

				/* Acknowledge sender to startup transfer */
				OUTresults.addNetSend(response.getMatchingCriterion().getSourceComponent(), new Acknowledge(100), response.getMatchingCriterion().getTag() , cmd.getCommunicator());

				OUTresults.addNetReceive(response.getMatchingCriterion().getSourceComponent(), response.getMatchingCriterion().getTag() , cmd.getCommunicator(), NetworkMessageRendezvousMsg.class);

				return;
			}else{
				client.debugFollowUpLine("Eager");
				// eager protocol
				return;
			}
		}
		default:
			throw new IllegalArgumentException("Unknown step");
		}
	}
}
