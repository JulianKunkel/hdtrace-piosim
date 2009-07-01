
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2009 Michael Kuhn
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

package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct
extends CommandImplementation<Gather>
{
	@Override
	public void process(Gather cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		System.out.println("root " + rootRank);
		System.out.println("rank " + myRank);

		switch (step) {
		case (CommandProcessing.STEP_START): {
			if (myRank != rootRank) {
				OUTresults.addNetSend(rootRank, new NetworkSimpleMessage(cmd.getSize() + 20), 40000, Communicator.INTERNAL_MPI);
			} else {
				for (int rank : cmd.getCommunicator().getParticipatingRanks()) {
					if (rank != myRank) {
						OUTresults.addNetReceive(rank, 40000, Communicator.INTERNAL_MPI);
					}
				}
			}

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			return;
		}
		}
	}
}