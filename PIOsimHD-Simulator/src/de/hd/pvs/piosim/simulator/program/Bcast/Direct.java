package de.hd.pvs.piosim.simulator.program.Bcast;


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

import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Simple bcast algorithm:
 * Root sends the message to each process.
 *
 * @author Julian M. Kunkel
 */

public class Direct
extends CommandImplementation<Bcast>
{
	final int tag = 20000;

	@Override
	public void process(Bcast cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();

		if(myRank == cmd.getCommunicator().getLocalRank(0)){
			// You are rank 0 in this communicator

				// send a message to all other ranks
				for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
					if(rank != myRank)
						OUTresults.addNetSend(rank, new NetworkSimpleData(cmd.getSize()), tag, cmd.getCommunicator());
				}

		}else{ // rank != 0
			// Wait for the data.
			int targetRank = cmd.getCommunicator().getWorldRank(0);
			OUTresults.addNetReceive(targetRank, tag, cmd.getCommunicator());
		}
	}
}
