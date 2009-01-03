
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

import de.hd.pvs.piosim.model.program.commands.Receive;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousRcv extends CommandImplementation<Receive>
{
	public CommandStepResults process(Receive cmd, GClientProcess client, int step, NetworkJobs compNetJobs) {		

		final int ACK_RECVD = 1;
		final int LAST = 2;

		/* second step ?, receive whole data */
		switch(step){
		case(STEP_START):{				
			/* determine application */
			CommandStepResults jobs = prepareStepResultsForJobs(client,cmd, ACK_RECVD);

			ISNodeHostedComponent src = null;
			/* MATCH any Source */
			if (cmd.getFromRank() >= 0){
				src = getTargetfromRank(client,cmd.getFromRank());
				assert(src != null);
			}

			/* wait for incoming msg (send ready) */
			netAddReceive(jobs, src, cmd.getTag(), cmd.getCommunicator());

			return jobs;
		}case(LAST):{
			CommandStepResults jobs = prepareStepResultsForJobs(compNetJobs, STEP_COMPLETED);

			netAddReceive(jobs, compNetJobs.getNetworkJobs().get(0).getTargetComponent(), 
					compNetJobs.getNetworkJobs().get(0).getTag(), cmd.getCommunicator());

			return jobs;
		}case(ACK_RECVD):{
			SingleNetworkJob response = compNetJobs.getResponses().get(0);

			client.debug("Receive got ACK from " +  response.getSourceComponent().getIdentifier() );

			if( response.getJobData().getClass() == NetworkMessageRendezvousSendData.class ){
				client.debugFollowUpLine("Eager");
				// eager protocoll
				return null;
			}else{
				//rendezvous protocol
				/* identify the sender from the source */
				CommandStepResults jobs = prepareStepResultsForJobs(compNetJobs, LAST);

				/* Acknowledge sender to startup transfer */
				netAddSend(jobs, response.getSourceComponent(), new NetworkSimpleMessage(100), response.getTag() , cmd.getCommunicator());

				return jobs;
			}
		}
		default:
			throw new IllegalArgumentException("Unknown step");
		}
	}
}
