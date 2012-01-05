
//	Copyright (C) 2011 Julian M. Kunkel
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
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanksRemapRoot;

/**
 * Create a pipe between the processors, one processor sends data to the next and so forth until all data has been transmitted.
 * Data is partitioned into blocks of smaller size.
 *
 * @author Julian M. Kunkel
 */
public class PipedBlockwise extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Bcast>
{
	/* Define the size in which data is fragmented */
	final long splitSize = 1 * 1024*1024;

	/* Size of the message header. */
	final int msgHeader = 20;

	final int READY_TAG = 5;
	final int DATA_TAG = 30;

	@Override
	public int getSingleTargetWorldRank(Bcast cmd) {
		return cmd.getRootRank();
	}

	@Override
	public void processWithLocalRanks(Bcast cmd, ICommandProcessingMapped OUTresults, Communicator comm, 	int clientRankInComm, int rootRank, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		if (cmd.getCommunicator().getSize() == 1){
			// Nothing to do.
			return;
		}


		if(clientRankInComm == 0){
			// Rank 0 starts the pipeline.
			// Amount of data which must be send.
			final long dataRemains = cmd.getSize() - (splitSize * step);
			// Amount of data to actually send in this step.
			long amountToTransfer;

			if (dataRemains > splitSize){
				// Another block of data must be transmitted.
				OUTresults.setNextStep(step + 1);
				amountToTransfer = splitSize;
			}else{
				// This is the last block to transmit.
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				amountToTransfer = dataRemains;
			}

			IMessageUserData data= new NetworkSimpleData(amountToTransfer + msgHeader);

			// The rank to send data to.
			int targetRank = getLocalRankExchangeRoot(rootRank, 1);

			// Send the first packet already without notification.
			OUTresults.addNetSend( targetRank , data, DATA_TAG, cmd.getCommunicator());

			if(step == 0){
				// Wait for an acceptance notification from the other rank not to overwhelm its buffer.
				OUTresults.addNetReceive(targetRank, READY_TAG, comm);
			}
		}else if(clientRankInComm == (comm.getSize() - 1) ){
			// Last rank.
			if( step == 0){
				// Announce to be ready to the previous rank.
				OUTresults.addNetSend( getLocalRankExchangeRoot(rootRank, clientRankInComm - 1), new NetworkSimpleData(msgHeader), READY_TAG , cmd.getCommunicator());
			}
			// The number of iterations to make before all data is received.
			// Number of iterations == blocks to process.
			final int iterationsOfBlocks = (int)((cmd.getSize() - 1)/splitSize);

			// Decide if more data must be accepted.
			if( step < iterationsOfBlocks){
				OUTresults.setNextStep(step + 1);
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			// Post the receive operation to get data from the previous rank.
			OUTresults.addNetReceive( getLocalRankExchangeRoot(rootRank, clientRankInComm - 1) , DATA_TAG, cmd.getCommunicator());

		}else {
			// A rank which receives and forwards data.

			if(step == 0){
				// Transit to the next step.
				OUTresults.setNextStep(1);

				// Post the receive operation to get the first data packet from the previous rank.
				OUTresults.addNetReceive( getLocalRankExchangeRoot(rootRank, clientRankInComm - 1) , DATA_TAG, cmd.getCommunicator());

				// Announce to be ready to the previous rank.
				OUTresults.addNetSend( getLocalRankExchangeRoot(rootRank, clientRankInComm - 1), new NetworkSimpleData(msgHeader), READY_TAG , cmd.getCommunicator());

				// Wait for the next process to be ready before transfer of data is started.
				OUTresults.addNetReceive( getLocalRankExchangeRoot(rootRank, clientRankInComm + 1), READY_TAG, cmd.getCommunicator());
				return;
			}

			final int iterationsOfBlocks = (int)((cmd.getSize() - 1)/splitSize) + 1;

			// Send same amount of data to the next rank.
			if( step < iterationsOfBlocks){
				OUTresults.setNextStep(step + 1); // Receive again.

				// Post the receive operation to get data from the previous rank.
				OUTresults.addNetReceive( getLocalRankExchangeRoot(rootRank, clientRankInComm - 1), DATA_TAG, cmd.getCommunicator());
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			// Pass the received data to the next process, just forward the message content.
			final IMessageUserData data = compNetJobs.getResponses()[0].getJobData();

			int targetRank = clientRankInComm + 1 ;
			OUTresults.addNetSend(  getLocalRankExchangeRoot(rootRank, targetRank), data, DATA_TAG, cmd.getCommunicator());
		}
	}

}
