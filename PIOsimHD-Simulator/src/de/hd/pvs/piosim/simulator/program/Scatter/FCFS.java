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
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


/**
 * First come first serve implementation.
 * With this method "root" waits for a response message from any process, then transfers all data
 * to this process and continues until all processes were served.
 *
 * To avoid wrong matching ...
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
	public void process(Scatter cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		if (myRank != rootRank) {
			if (step == CommandProcessing.STEP_START){
				// wait until root is ready to send data.
				OUTresults.addNetSend(rootRank, new MyAcknowledge(), tagNumber, Communicator.INTERNAL_MPI);
				OUTresults.addNetReceive(rootRank, tagNumber, Communicator.INTERNAL_MPI, MyData.class);
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

				return;
			}

		}else{  // root rank, the step encodes how many operations have been done
			if (step == CommandProcessing.STEP_START){
				OUTresults.addNetReceiveAnySource(tagNumber, Communicator.INTERNAL_MPI, MyAcknowledge.class);
				OUTresults.setNextStep(1);
			}else{

				final INodeHostedComponent target = compNetJobs.getResponses().get(0).getMatchingCriterion().getSourceComponent();


				if( step == cmd.getCommunicator().getSize() - 1 ){
					// we almost finished, we have to send data to the last process.
					// then complete.
					OUTresults.addNetSend(target, new MyData(cmd.getSize()), 40001, Communicator.INTERNAL_MPI);

					OUTresults.setNextStep(OUTresults.STEP_COMPLETED);
					return;
				}

				// transfer data to the next process and wait for an ACK of another one.
				OUTresults.addNetSend(target, new MyData(cmd.getSize()), 40001, Communicator.INTERNAL_MPI);
				OUTresults.addNetReceiveAnySource(tagNumber, Communicator.INTERNAL_MPI, MyAcknowledge.class);
				OUTresults.setNextStep(step + 1);
			}
		}
	}
}