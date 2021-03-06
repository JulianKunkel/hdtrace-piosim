
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

import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSend extends CommandImplementation<Send>
{
	/**
	 * Overhead for every communication:
	 */
	final static int MESSAGE_HEADER_OVERHEAD = 4+4;

	public void process(Send cmd,  ICommandProcessing OUTresults, GClientProcess client, long step,  NetworkJobs compNetJobs) {
		final int RECV_ACK = 2;
		/* second step ?, receive whole data */

		if(step == CommandProcessing.STEP_START){

			if(cmd.getSize() <= client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize()){
				//eager send completes immediately

				/* data to transfer depends on actual command size, but is defined in send */
//				client.debug("eager send to " +  cmd.getToRank() );

				OUTresults.addNetSend(cmd.getToRank(),
						new NetworkMessageRendezvousMsg( cmd.getSize() + MESSAGE_HEADER_OVERHEAD, false ), cmd.getToTag(), cmd.getCommunicator(), RendezvousSend.class, RendezvousSend.class);

				return;
			}else{
				//rendezvous protocol
				/* determine application */
				OUTresults.setNextStep(RECV_ACK);

				OUTresults.addNetSend(cmd.getToRank(), new NetworkMessageRendezvousMsg(MESSAGE_HEADER_OVERHEAD, true), cmd.getToTag(), cmd.getCommunicator(), RendezvousSend.class, RendezvousSend.class);

				/* wait for incoming msg (send ready) */
				OUTresults.addNetReceive(cmd.getToRank(),  cmd.getToTag(), cmd.getCommunicator(), RendezvousRcv.class, RendezvousRcv.class);
				return;
			}
		}else if(RECV_ACK == step){
			/* data to transfer depends on actual command size, but is defined in send */
//			client.debug("SEND got ACK from " +  cmd.getToRank() );
			OUTresults.addNetSend(cmd.getToRank(),
					new NetworkMessageRendezvousMsg( cmd.getSize() + MESSAGE_HEADER_OVERHEAD , false ), cmd.getToTag(), cmd.getCommunicator(), RendezvousSend.class, RendezvousRcv.class);

			return;
		}

		return;
	}

	@Override
	public String[] getAdditionalTraceAttributes(Send cmd) {
		return new String[] { "toRank",  cmd.getToRank() + "", "size", cmd.getSize() + "", "toTag", ""+ cmd.getToTag() };
	}
}
