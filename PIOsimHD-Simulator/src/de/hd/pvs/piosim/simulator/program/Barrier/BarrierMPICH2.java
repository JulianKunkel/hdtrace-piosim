package de.hd.pvs.piosim.simulator.program.Barrier;

import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Algorithm
 *
 * In step k, 0 <= k <= (ceiling(lgp)-1),
 * process i sends to process (i + 2^k) % p and receives from process
 * (i - 2^k + p) % p.
 *
 * @author artur
 *
 */
public class BarrierMPICH2 extends CommandImplementation<Barrier>{

	final int TAG = 40006;

	@Override
	public void process(Barrier cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();

		if(step <= Math.ceil(Math.log(cmd.getCommunicator().getSize() - 1))){
			//receive from (i + 2^k) % p
			final int receiveFrom = (myRank + (1 << step)) % cmd.getCommunicator().getSize();

			// send to (i - 2^k + p) % p
			final int sendTo = (myRank - (1 << step) + cmd.getCommunicator().getSize()) % cmd.getCommunicator().getSize();

			OUTresults.addNetReceive(receiveFrom, TAG, cmd.getCommunicator());
			OUTresults.addNetSend(sendTo, new NetworkSimpleData(20), TAG, cmd.getCommunicator());

			OUTresults.setNextStep(++step);
		}else{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
		}
	}

}
