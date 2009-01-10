
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

import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSend extends CommandImplementation<Send>
{
	public void process(Send cmd,  CommandProcessing OUTresults, GClientProcess client, int step,  NetworkJobs compNetJobs) {
		final int RECV_ACK = 2;
		/* second step ?, receive whole data */		

		switch(step){
		case(CommandProcessing.STEP_START):{

			if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
				//eager send completes immediately

				/* data to transfer depends on actual command size, but is defined in send */
				client.debug("eager send to " +  cmd.getToRank() );

				OUTresults.addNetSend(cmd.getToRank(), 
						new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getTag(), cmd.getCommunicator());

				return;
			}else{
				//rendezvous protocol
				/* determine application */
				OUTresults.setNextStep(RECV_ACK);
								
				/* data to transfer depends on actual command size, but is defined in send */
				OUTresults.addNetSend(cmd.getToRank(), 
						new NetworkSimpleMessage(100), cmd.getTag(), cmd.getCommunicator());

				/* wait for incoming msg (send ready) */
				OUTresults.addNetReceive(cmd.getToRank(),  cmd.getTag(), cmd.getCommunicator());			
				return;
			}
		}case(RECV_ACK):{
			/* data to transfer depends on actual command size, but is defined in send */

			client.debug("SEND got ACK from " +  cmd.getToRank() );
			OUTresults.addNetSend(cmd.getToRank(), 
					new NetworkMessageRendezvousSendData( cmd.getSize() ), cmd.getTag(), cmd.getCommunicator());

			return;
		}
		}

		return;
	}
}
