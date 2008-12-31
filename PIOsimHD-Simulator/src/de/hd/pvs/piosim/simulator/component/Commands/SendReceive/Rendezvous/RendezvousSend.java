
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

import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.simulator.component.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.component.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.component.Commands.CommandImplementation;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSend extends CommandImplementation<Send>
{
	public CommandStepResults process(Send cmd, GClientProcess client, int step,  NetworkJobs compNetJobs) {
		final int RECV_ACK = 2;
		/* second step ?, receive whole data */

		ISNodeHostedComponent target = getTargetfromRank(client, cmd.getToRank());

		switch(step){
		case(STEP_START):{

			if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
				//eager send:
				CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, STEP_COMPLETED);

				/* data to transfer depends on actual command size, but is defined in send */
				client.debug("eager send to " +  target.getIdentifier() );

				netAddSend(jobs, target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getTag(), cmd.getCommunicator());

				return jobs;
			}else{
				//rendezvous protocol
				/* determine application */		
				CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, RECV_ACK);

				/* data to transfer depends on actual command size, but is defined in send */

				netAddSend(jobs, target, new NetworkSimpleMessage(100), cmd.getTag(), cmd.getCommunicator());

				/* wait for incoming msg (send ready) */
				netAddReceive(jobs, target, cmd.getTag(), cmd.getCommunicator());			
				return jobs;
			}
		}case(RECV_ACK):{
			CommandStepResults jobs = prepareStepResultsForJobs(compNetJobs, STEP_COMPLETED);

			/* data to transfer depends on actual command size, but is defined in send */

			client.debug("SEND got ACK from " +  target.getIdentifier() );

			netAddSend(jobs, target,  new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getTag(), cmd.getCommunicator());

			return jobs;
		}
		}

		return null;
	}
}
