
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

import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Create a pipe between the processors, one processor sends data to the next and so forth until all data has been transmitted.
 * Data is partitioned into blocks of smaller size.
 *
 * @author Julian M. Kunkel
 */
public class PipedBlockwise
extends CommandImplementation<Bcast>
{
	final long splitSize = 1 * 1024*1024;
	final int msgHeader = 20;

	@Override
	public void process(Bcast cmd, CommandProcessing OUTresults,
			GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		int clientRankInComm = myRank;

		final int iterationsOfBlocks = (int)((cmd.getSize() - 1)/splitSize);	 // minus 1 Byte

		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = rootRank;
		}

		if(clientRankInComm != 0){
			// receive first, then start to send data


			if (step % 2 == 0){ //  receive step, receive data from the previous rank
				OUTresults.setNextStep(step + 1); // always also send received data.
				OUTresults.addNetReceive(clientRankInComm - 1, 30000, cmd.getCommunicator());

			}else{
				// send same amount of data to the next rank (if necessary)
				if( step / 2 < iterationsOfBlocks){
					OUTresults.setNextStep(step + 1); // receive again.
				}else{
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}

				final long commSize = cmd.getCommunicator().getSize();
				// the last process must not send data to the next
				if (clientRankInComm == commSize - 1){
					return;
				}

				// pass data to the next process:
				final IMessageUserData data = compNetJobs.getResponses().get(0).getJobData();

				int targetRank = clientRankInComm + 1 ;
				OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0, data, 30000, cmd.getCommunicator());
			}
		}else{ // RANK == 0

			final long dataRemains = cmd.getSize() - (splitSize * step);
			long amountToTransfer;

			if (dataRemains > splitSize){
				OUTresults.setNextStep(step + 1);
				amountToTransfer = splitSize;
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				amountToTransfer = dataRemains;
			}

			IMessageUserData data= new NetworkSimpleData(amountToTransfer + msgHeader);

			int targetRank = 1;
			OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0 , data, 30000, cmd.getCommunicator());
		}
	}

}
