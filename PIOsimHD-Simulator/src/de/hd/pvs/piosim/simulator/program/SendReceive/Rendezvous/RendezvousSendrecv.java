
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

import de.hd.pvs.piosim.model.program.commands.Sendrecv;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 *
 * TODO: a bug leads to hung up for sendRecv with rendevouz protocol, recheck execution graph
 *
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSendrecv extends CommandImplementation<Sendrecv>
{
	public void process(Sendrecv cmd,  CommandProcessing OUTresults, GClientProcess client, int step,  NetworkJobs compNetJobs) {
		final int EAGER_ACK = 2;
		final int RENDEZVOUS_ACK = 3;
		final int START_RENDEZVOUS_RECV = 4;
		/* second step ?, receive whole data */

		int target = cmd.getToRank();

		switch(step){
		case(CommandProcessing.STEP_START):{
			System.out.println(client.getIdentifier() + " SCHUH will do START_RENDEZVOUS_RECV " + target);


			if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
				//eager send:
				OUTresults.setNextStep(EAGER_ACK);

				/* data to transfer depends on actual command size, but is defined in send */
				client.debug("eager send to " +  target );

				OUTresults.addNetSend(target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getToTag(), cmd.getCommunicator());
			}else{
				//rendezvous protocol
				/* determine application */
				OUTresults.setNextStep(RENDEZVOUS_ACK);

				/* data to transfer depends on actual command size, but is defined in send */
				OUTresults.addNetSend(target, new NetworkSimpleData(100), cmd.getToTag(), cmd.getCommunicator());

				/* wait for incoming msg (send ready) */
				OUTresults.addNetReceive(target, cmd.getToTag(), cmd.getCommunicator());
			}

			// receiving part:
			/* MATCH any Source */
			if (cmd.getFromRank() >= 0){
				OUTresults.addNetReceive(cmd.getFromRank(), cmd.getFromTag(), cmd.getCommunicator());
			}else{
				OUTresults.addNetReceive(null, cmd.getFromTag(), cmd.getCommunicator());
			}
			return;
		}case(RENDEZVOUS_ACK):{
			// receiver path
			InterProcessNetworkJob response = compNetJobs.getResponses().get(0);

			client.debug("Receive got ACK from " +  response.getMatchingCriterion().getSourceComponent().getIdentifier() );

			if( response.getJobData().getClass() == NetworkMessageRendezvousSendData.class ){
				client.debugFollowUpLine("Eager");
				// eager protocoll
			}else{

				OUTresults.setNextStep(START_RENDEZVOUS_RECV);
				//rendezvous protocol
				/* identify the sender from the source */
				/* Acknowledge sender to startup transfer */
				OUTresults.addNetSend(response.getMatchingCriterion().getSourceComponent(), new NetworkSimpleData(100), response.getMatchingCriterion().getTag() , cmd.getCommunicator());
			}

			/* data to transfer depends on actual command size, but is defined in send */

			client.debug("SEND got ACK from " +  target );

			OUTresults.addNetSend( target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getToTag(), cmd.getCommunicator());
			return;
		}case(EAGER_ACK):{
			InterProcessNetworkJob response = compNetJobs.getResponses().get(0);

			client.debug("Receive got ACK from " +  response.getMatchingCriterion().getSourceComponent().getIdentifier() );

			if( response.getJobData().getClass() == NetworkMessageRendezvousSendData.class ){
				client.debugFollowUpLine("Eager");
				// eager protocol
			}else{
				//rendezvous protocol
				/* identify the sender from the source */
				OUTresults.setNextStep(START_RENDEZVOUS_RECV);

				/* Acknowledge sender to startup transfer */
				OUTresults.addNetSend( response.getMatchingCriterion().getSourceComponent(), new NetworkSimpleData(100), response.getMatchingCriterion().getTag() , cmd.getCommunicator());
			}
			return;
		}case(START_RENDEZVOUS_RECV):{
			OUTresults.addNetReceive(compNetJobs.getNetworkJobs().get(0).getMatchingCriterion().getTargetComponent(),
					compNetJobs.getNetworkJobs().get(0).getMatchingCriterion().getTag(), cmd.getCommunicator());
			return;
		}
		}

		return;
	}
}

