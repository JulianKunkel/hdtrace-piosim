package de.hd.pvs.piosim.simulator.program.Allgather;

import de.hd.pvs.piosim.model.program.commands.Allgather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Allgather Algorithm
 *
 * Ring Algorithm:
 * 	1st Step: process i sends his data to process i + 1 and receives data from process i - 1
 *  further steps: process i sends data it received from process i - 1 to i + 1 and receives data from  process i - 1
 *
 * @author artur
 *
 */
public class AllgatherMPICH2 extends CommandImplementation<Allgather>{

	final int TAG = 40005;

	@Override
	public void process(Allgather cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		int sendTo, receiveFrom;

		// figure out who will receive data and to whom it will be send
		if(myRank == 0){
			sendTo = myRank + 1;
			receiveFrom = cmd.getCommunicator().getSize() - 1;
		}else if (myRank == (cmd.getCommunicator().getSize() - 1)){
			sendTo = 0;
			receiveFrom = myRank - 1;
		}else{
			sendTo = myRank + 1;
			receiveFrom = myRank - 1;
		}


		// since this simulator only cares about data size and not its content we will
		// simplify this procedure by not sending data received from i-1 but simply sending the same amount of data
		// thus for our purpose we do not need to distinguish between step 1 and further steps
		if (step < (cmd.getCommunicator().getSize() - 1)){
			// receive data from i - 1 and send data to i + 1
			OUTresults.addNetSend(sendTo, new NetworkSimpleData(cmd.getSize() + 20), TAG, cmd.getCommunicator());
			OUTresults.addNetReceive(receiveFrom, TAG, cmd.getCommunicator());

			OUTresults.setNextStep(++step);
		}else{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
		}
	}

}
