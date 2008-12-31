
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
package de.hd.pvs.piosim.simulator.component.Commands.SendReceive.Rendezvous;

import de.hd.pvs.piosim.model.program.commands.Sendrecv;
import de.hd.pvs.piosim.simulator.component.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.component.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.component.Commands.CommandImplementation;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSendrecv extends CommandImplementation<Sendrecv>
{
	public CommandStepResults process(Sendrecv cmd, GClientProcess client, int step,  NetworkJobs compNetJobs) {
		final int EAGER_ACK = 2;
		final int RENDEZVOUS_ACK = 3;
		final int START_RENDEZVOUS_RECV = 4;
		/* second step ?, receive whole data */

		ISNodeHostedComponent target = getTargetfromRank(client, cmd.getToRank());

		switch(step){
		case(STEP_START):{

			CommandStepResults jobs;

			if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
				//eager send:
				jobs = prepareStepResultsForJobs(client, cmd, EAGER_ACK);

				/* data to transfer depends on actual command size, but is defined in send */
				client.debug("eager send to " +  target.getIdentifier() );

				netAddSend(jobs, target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getToTag(), cmd.getCommunicator());
			}else{
				//rendezvous protocol
				/* determine application */		
				jobs = prepareStepResultsForJobs(client, cmd, RENDEZVOUS_ACK);

				/* data to transfer depends on actual command size, but is defined in send */

				netAddSend(jobs, target, new NetworkSimpleMessage(100), cmd.getToTag(), cmd.getCommunicator());

				/* wait for incoming msg (send ready) */
				netAddReceive(jobs, target, cmd.getToTag(), cmd.getCommunicator());			
			}

			// receive from source:
			ISNodeHostedComponent src = null;
			/* MATCH any Source */
			if (cmd.getFromRank() >= 0){
				src = getTargetfromRank(client,cmd.getFromRank());
				assert(src != null);
			}

			/* wait for incoming msg (send ready) */
			netAddReceive(jobs, src, cmd.getFromTag(), cmd.getCommunicator());

			return jobs;
		}case(RENDEZVOUS_ACK):{
			CommandStepResults jobs;

			// receiver path
			SingleNetworkJob response = compNetJobs.getResponses().get(0);

			client.debug("Receive got ACK from " +  response.getSourceComponent().getIdentifier() );

			if( response.getJobData().getClass() == NetworkMessageRendezvousSendData.class ){
				jobs = prepareStepResultsForJobs(compNetJobs, STEP_COMPLETED);
				client.debugFollowUpLine("Eager");
				// eager protocoll
			}else{
				jobs = prepareStepResultsForJobs(compNetJobs, START_RENDEZVOUS_RECV);
				//rendezvous protocol
				/* identify the sender from the source */
				/* Acknowledge sender to startup transfer */
				netAddSend(jobs, response.getSourceComponent(), new NetworkSimpleMessage(100), response.getTag() , cmd.getCommunicator());
			}			

			/* data to transfer depends on actual command size, but is defined in send */

			client.debug("SEND got ACK from " +  target.getIdentifier() );

			netAddSend(jobs, target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getToTag(), cmd.getCommunicator());

			return jobs;
		}case(EAGER_ACK):{
			SingleNetworkJob response = compNetJobs.getResponses().get(0);

			client.debug("Receive got ACK from " +  response.getSourceComponent().getIdentifier() );

			if( response.getJobData().getClass() == NetworkMessageRendezvousSendData.class ){
				client.debugFollowUpLine("Eager");
				// eager protocol
				return null;
			}else{
				//rendezvous protocol
				/* identify the sender from the source */
				CommandStepResults jobs = prepareStepResultsForJobs(compNetJobs, START_RENDEZVOUS_RECV);

				/* Acknowledge sender to startup transfer */
				netAddSend(jobs, response.getSourceComponent(), new NetworkSimpleMessage(100), response.getTag() , cmd.getCommunicator());

				return jobs;
			}
		}case(START_RENDEZVOUS_RECV):{
			CommandStepResults jobs = prepareStepResultsForJobs(compNetJobs, STEP_COMPLETED);

			netAddReceive(jobs, compNetJobs.getNetworkJobs().get(0).getTargetComponent(), 
					compNetJobs.getNetworkJobs().get(0).getTag(), cmd.getCommunicator());

			return jobs;			
		}
		}

		return null;
	}
}

