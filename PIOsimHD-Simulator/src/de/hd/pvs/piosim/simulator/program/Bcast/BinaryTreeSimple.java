
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

package de.hd.pvs.piosim.simulator.program.Bcast;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanksRemapRoot;

/**
 * Binary Tree Algorithm, Root starts to propagate data. A node sends data to child nodes only when data transfer to the node is complete.
 * The sender transfers data to all recipients in one step, that means the senders NIC is multiplexed among all receivers.
 *
 * @author Julian M. Kunkel
 */
public class BinaryTreeSimple
extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Bcast>
{

	@Override
	public int getSingleTargetWorldRank(Bcast cmd) {
		return cmd.getRootRank();
	}

	@Override
	public void processWithLocalRanks(Bcast cmd, ICommandProcessingMapped OUTresults, Communicator comm, 	int clientRankInComm, int rootRank, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		final int RECEIVED = 2;

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
		final int phaseStart = iterations - trailingZeros;


		if(clientRankInComm != 0){
			// recv first, then send.

			if (step == CommandProcessing.STEP_START){
				OUTresults.setNextStep(RECEIVED);


				int recvFrom = getLocalRankExchangeRoot(rootRank, (clientRankInComm ^ 1<<trailingZeros));

				OUTresults.addNetReceive(recvFrom, 30000, cmd.getCommunicator());

			}else if(step == RECEIVED){
				// send
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					int targetRank = (1<<iter | clientRankInComm);
					if (targetRank >= commSize) continue;
					//System.out.println(clientRankInComm +" to " + (1<<iter | clientRankInComm) );
					OUTresults.addNetSend( getLocalRankExchangeRoot(rootRank, targetRank),
							new NetworkSimpleData(cmd.getSize() + 20), 30000, cmd.getCommunicator());
				}
			}
		}else{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			// send to all receivers
			for (int iter = iterations-1 ; iter >= 0 ; iter--){
				final int targetRank =  1<<iter;
				//System.out.println(clientRankInComm +" to " + ((targetRank != rootRank) ? targetRank : 0) );
				OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0,
						new NetworkSimpleData(cmd.getSize() + 20), 30000, cmd.getCommunicator());
			}
		}
	}

}
