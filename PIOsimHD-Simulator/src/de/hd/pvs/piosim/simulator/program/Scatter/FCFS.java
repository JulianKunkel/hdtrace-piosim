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

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


/**
 * First come first serve implementation.
 * With this method "root" waits for a response message from any process, then root receives
 * data from responding process and continues until all processes were served.

 * When multiple scatters are invoked on the same communicator, then on root a wrong matching
 * could occur with the ANY_SOURCE.
 * @see  de.hd.pvs.piosim.simulator.program.Gather.FCFS for a discussion of this issue.
 *
 * @author kunkel
 */
public class FCFS
extends CommandImplementation<Scatter>
{
	static final int tagNumber = 40001;

	// first each process sends a small msg, then continues upon confirmation.
	static final int STEP_RECEIVE = 1;

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
	public void process(Scatter cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
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
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

				return;
			}

		}else{  // root rank, the step encodes how many operations have been done
			if (step == CommandProcessing.STEP_START){
				OUTresults.addNetReceive(ANY_SOURCE, tagNumber, cmd.getCommunicator());
				OUTresults.setNextStep(STEP_RECEIVE);
			}else{ // STEP_RECEIVE

				final INodeHostedComponent target = compNetJobs.getResponses()[0].getMatchingCriterion().getSourceComponent();


				if( step == cmd.getCommunicator().getSize() - 1 ){
					// we almost finished, we have to send data to the last process.
					// then complete.
					OUTresults.addNetSend(target, new MyData(cmd.getSize()), tagNumber, cmd.getCommunicator());

					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
					return;
				}

				// transfer data to the next process and wait for an ACK of another one.
				OUTresults.addNetSend(target, new MyData(cmd.getSize()), tagNumber, cmd.getCommunicator());
				OUTresults.addNetReceive(ANY_SOURCE, tagNumber, cmd.getCommunicator());
				OUTresults.setNextStep(step + 1);
			}
		}
	}
}