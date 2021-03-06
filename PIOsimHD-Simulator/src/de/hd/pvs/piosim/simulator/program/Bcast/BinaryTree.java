//	Copyright (C) 2008, 2009, 2011 Julian M. Kunkel
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
 * Binary Tree Algorithm, Root starts to propagate data. A node sends data to child processes only when the process is ready.
 *
 * @author Julian M. Kunkel
 */
public class BinaryTree extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Bcast>
{


	@Override
	public int getSingleTargetWorldRank(Bcast cmd) {
		return cmd.getRootRank();
	}

	@Override
	public void processWithLocalRanks(Bcast cmd, ICommandProcessingMapped OUTresults, Communicator comm, 	int clientRankInComm, int rootRank, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int RENDEVOUZ = 10;

		if (step == CommandProcessing.STEP_START){
			long eagerSize = client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize();
			if (cmd.getSize() <= eagerSize){
				// run the eager protocol:
				OUTresults.invokeChildOperation(cmd, CommandProcessing.STEP_COMPLETED,
						de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple.class);
				return;
			}

			// not eager
			OUTresults.setNextStep(RENDEVOUZ);

			//wait for receives from all "to receive data"

			final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);

			final int commSize = cmd.getCommunicator().getSize();
			final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
			final int phaseStart = iterations - trailingZeros;

			if(clientRankInComm != 0){
				int sendTo = getLocalRankExchangeRoot(rootRank, (clientRankInComm ^ 1<<trailingZeros));

				//System.out.println(myRank + " phaseStart: " + phaseStart +" tz:" + trailingZeros + " send to: " +  sendTo);

				// receive confirmation from all descending clients i send data to.

				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					final int targetRank = (1<<iter | clientRankInComm);
					if (targetRank >= commSize)
						continue;
					OUTresults.addNetReceive( getLocalRankExchangeRoot(rootRank, targetRank) , 30002, cmd.getCommunicator());
				}


				// send confirmation to receive data to parent node.

				OUTresults.addNetSend(sendTo,
						new NetworkSimpleData(20), // just 20 Bytes or something.
						30002, cmd.getCommunicator());
			}else{
				// receive from all descending nodes that they accept data.
				for (int iter = iterations-1 ; iter >= 0 ; iter--){
					final int targetRank =  1<<iter;
					//System.out.println(myRank +" from " + ((targetRank != rootRank) ? targetRank : 0) );
					OUTresults.addNetReceive( (targetRank != rootRank) ? targetRank : 0, 30002, cmd.getCommunicator());
				}
			}


		}else if(step == RENDEVOUZ){
			OUTresults.invokeChildOperation(cmd, CommandProcessing.STEP_COMPLETED,
					de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple.class);
		}
	}

}
