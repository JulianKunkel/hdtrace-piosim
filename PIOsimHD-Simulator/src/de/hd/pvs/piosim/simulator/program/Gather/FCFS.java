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

package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


/**
 * First come first serve implementation.
 * With this method "root" waits for a response message from any process, then sends
 * data from root to the responding process and continues until all processes were served.
 *
 * When multiple gathers are invoked on the same communicator, then on root a wrong matching
 * could occur with the ANY_SOURCE. This can be fixed by remembering the ranks which were already completed.
 * Yet this is not the case.
 * In a real implementation this could be done for a client by remembering the number of performed gathers
 * for each communicator, then the tag could be set to this number, thus for each communicator the right number
 * of gathers must have been performed.
 * Another solution is to post all receives upon startup of the root process and check in a loop for
 * completion of any of those receives.
 *
 * @see  de.hd.pvs.piosim.simulator.program.Scatter.FCFS as well
 * @author kunkel
 */
public class FCFS
extends CommandImplementation<Gather>
{
	static final int tagNumber = 40001;

	// first each process sends a small msg, then continues upon confirmation.
	static final int STEP_SEND = 1;

	static private class MyData extends NetworkSimpleData{
		public MyData(long size) {
			super(size + 20);
		}
	}

	static private class MyAcknowledge extends NetworkSimpleData{
		public MyAcknowledge() {
			super(20);
		}
	}

	@Override
	public void process(Gather cmd, CommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		if (myRank != rootRank) {
			if (step == CommandProcessing.STEP_START){
				// wait until root is ready to send data.
				OUTresults.addNetSend(rootRank, new MyAcknowledge(), tagNumber, cmd.getCommunicator());
				OUTresults.addNetReceive(rootRank, tagNumber, cmd.getCommunicator());
				OUTresults.setNextStep(STEP_SEND);

				return;
			}else if(step == STEP_SEND){
				OUTresults.addNetSend(rootRank,  new MyData(cmd.getSize()), tagNumber, cmd.getCommunicator());
				return;
			}

		}else{  // root rank, the step encodes how many operations have been done
			if (step == CommandProcessing.STEP_START){
				OUTresults.addNetReceive(ANY_SOURCE, tagNumber, cmd.getCommunicator());
				OUTresults.setNextStep(STEP_SEND);
			}else{

				final INodeHostedComponent target = compNetJobs.getResponses().get(0).getMatchingCriterion().getSourceComponent();

				// receive data from the next process and wait for an ACK of another one.
				OUTresults.addNetReceive(target, tagNumber, cmd.getCommunicator());
				OUTresults.addNetSend(target, new MyAcknowledge(), tagNumber, cmd.getCommunicator());

				if( step == cmd.getCommunicator().getSize() - 1 ){
					// we almost finished, we have to receive data from the last process.
					// then complete.

					OUTresults.setNextStep(OUTresults.STEP_COMPLETED);
					return;
				}

				OUTresults.addNetReceive(ANY_SOURCE, tagNumber, cmd.getCommunicator());
				OUTresults.setNextStep(step + 1);
			}
		}
	}
}