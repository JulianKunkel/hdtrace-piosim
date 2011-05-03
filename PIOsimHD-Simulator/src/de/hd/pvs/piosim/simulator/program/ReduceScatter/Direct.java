//	Copyright (C) 2011 Julian Kunkel
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

package de.hd.pvs.piosim.simulator.program.ReduceScatter;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.ReduceScatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


/**
 * Very simple implementation,
 * every client sends all remote data directly to the receiver, which then applies the operator to data received from all clients + its own.
 *
 * @author julian
 */
public class Direct
extends CommandImplementation<ReduceScatter>
{
	final int RECEIVED = 2;

	@Override
	public long getInstructionCount(ReduceScatter cmd, GClientProcess client, long step) {
		final int myRank = client.getModelComponent().getRank();

		if(step == RECEIVED){
			// aggregate data received from all clients, i.e. we have a comm of a given size and the number of elements dedicated to us...
			return cmd.getRecvcounts().get(myRank) * (cmd.getCommunicator().getSize() - 1) + 1;
		}else{
			return 1;
		}
	}

	@Override
	public void process(ReduceScatter cmd, CommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}


		if(step == CommandProcessing.STEP_START){
			final int myRank = client.getModelComponent().getRank();

			// send local data to all other clients and wait for their data to the local part.
			//final long localPartToRecv = cmd.getRecvcounts().get(myRank);

			for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
				final long sendCnt = cmd.getRecvcounts().get(rank);
				OUTresults.addNetSend(rank, new NetworkSimpleData(sendCnt + 20), 40002, Communicator.INTERNAL_MPI);
				// receive a part....
				OUTresults.addNetReceive(rank, 40002, Communicator.INTERNAL_MPI);
			}

			OUTresults.setNextStep(RECEIVED);

			return;
		}
	}
}