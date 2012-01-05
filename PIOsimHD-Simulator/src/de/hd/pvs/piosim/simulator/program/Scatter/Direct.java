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

package de.hd.pvs.piosim.simulator.program.Scatter;

import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct
extends CommandImplementation<Scatter>
{
	@Override
	public void process(Scatter cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		if (step == CommandProcessing.STEP_START){
			if (myRank != rootRank) {
				OUTresults.addNetReceive(rootRank, 40001, cmd.getCommunicator());
			} else {
				for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
					if (rank != myRank) {
						OUTresults.addNetSend(rank, new NetworkSimpleData(cmd.getSize() + 20), 40001, cmd.getCommunicator());
					}
				}
			}

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			return;
		}
	}
}