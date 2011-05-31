package de.hd.pvs.piosim.simulator.program.Barrier;


//Copyright (C) 2011 Julian M. Kunkel
//
//This file is part of PIOsimHD.
//
//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Tree-Synchronization algorithm:
 * span a binary tree among the processes and to the communicator root.
 * Assume 8 processes.
 *
 * This implementation is not SMP-aware.
 *
 * @author Julian M. Kunkel
 */

public class BinaryTree
extends CommandImplementation<Barrier>
{
	final int tag = 20000;
	final static IMessageUserData data = new NetworkSimpleData(30);

	final int RECEIVED_FROM_LEAF = 1;
	final int RECEIVED_FROM_ROOT = 2;

	@Override
	public void process(Barrier cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}


		int clientRankInComm = cmd.getCommunicator().getLocalRank( client.getModelComponent().getRank() );
		final int commSize = cmd.getCommunicator().getSize();

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);

		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int phaseStart = iterations - trailingZeros;
		int maxIter = iterations  - phaseStart - 1;


		if(clientRankInComm != 0){
			// root rank is a special case...
			maxIter = maxIter + 1;
		}


		if(step == 0){

			// receive messages from all leaf ranks
			for (int iter = 0 ; iter < maxIter; iter++){
				final int targetRank = (1<<iter | clientRankInComm);
				if (targetRank >= commSize )
					continue;

				//System.out.println(" to leaf:" + targetRank );
				OUTresults.addNetReceive(targetRank, tag, cmd.getCommunicator());
			}

			OUTresults.setNextStep(RECEIVED_FROM_LEAF);
			return;

		}else if(step == RECEIVED_FROM_LEAF){

			if(clientRankInComm == 0){ // root node
				// return messages to leafs => send messages towards all leaf ranks
				// reverse the order in which packets are send data, to speed up process slightly -> first send data to the process which is has most work to do
				for (int iter = maxIter - 1 ; iter >= 0; iter--){
					final int targetRank = (1<<iter | clientRankInComm);
					if (targetRank >= commSize )
						continue;

					//System.out.println(" to leaf:" + targetRank );
					OUTresults.addNetSend(targetRank, data, tag, cmd.getCommunicator());
				}

				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				return;

			}else{ // non-root process => broadcast message towards root.
				int sendTo = (clientRankInComm ^ 1<<trailingZeros);
				//System.out.println(" to root: " + sendTo);
				OUTresults.addNetSend(sendTo, data, tag, cmd.getCommunicator());
				// wait from response message from node
				OUTresults.addNetReceive(sendTo, tag, cmd.getCommunicator());
				OUTresults.setNextStep(RECEIVED_FROM_ROOT);
				return;
			}
		}else if(step == RECEIVED_FROM_ROOT){
			// return messages to leafs => send messages towards all leaf ranks
			// reverse the order in which packets are send data, to speed up process slightly -> first send data to the process which is has most work to do
			for (int iter = maxIter - 1 ; iter >= 0; iter--){
				final int targetRank = (1<<iter | clientRankInComm);
				if (targetRank >= commSize )
					continue;

				//System.out.println(" to leaf:" + targetRank );
				OUTresults.addNetSend(targetRank, data, tag, cmd.getCommunicator());
			}

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			return;
		}
	}
}
