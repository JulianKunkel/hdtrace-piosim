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
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Simple synchronisation algorithm:
 * Each process sends a message to root, then root sends a message to each process.
 *
 * @author Julian M. Kunkel
 */

public class Direct
extends CommandImplementation<Barrier>
{
	final int tag = 20000;
	final static IMessageUserData data = new NetworkSimpleData(30);

	@Override
	public void process(Barrier cmd, CommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();

		if(myRank == cmd.getCommunicator().getLocalRank(0)){
			// you are rank 0 in this communicator

			if( step == 0 ){

				// receive a message from all other ranks
				for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
					if(rank != myRank)
						OUTresults.addNetReceive(rank, tag, cmd.getCommunicator());
				}

				OUTresults.setNextStep(1);
				return;

			}else if(step == 1){
				// send a message to all other ranks
				for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
					if(rank != myRank)
						OUTresults.addNetSend(rank, data, tag, cmd.getCommunicator());
				}

				return;
			}
		}else{ // rank != 0

			// send a message to the root rank and wait for response message.
			int targetRank = cmd.getCommunicator().getWorldRank(0);
			OUTresults.addNetSend(targetRank, data, tag, cmd.getCommunicator());
			OUTresults.addNetReceive(targetRank, tag, cmd.getCommunicator());
		}
	}
}
