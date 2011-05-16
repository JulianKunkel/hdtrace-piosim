package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class GatherBinaryTreeMPICH2 extends CommandImplementation<Gather>{

	@Override
	public void process(Gather cmd, CommandProcessing OUTResults,
			GClientProcess client, long step, NetworkJobs compNetJobs) {

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();
		final int pof2Offset = commSize - pof2(commSize);
		final int iterations = Integer.numberOfTrailingZeros(commSize);

		int clientRankInComm = myRank;

		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = rootRank;
		}



		if(myRank == rootRank){
			// calculate from whom to receive
			int receiveFrom = (int) Math.pow(2, step);

			// check if receiveFrom is greater than the number of nodes
			// if not => receive data
			if(receiveFrom < commSize){
				OUTResults.addNetReceive(receiveFrom, 30303, cmd.getCommunicator());
				OUTResults.setNextStep(step++);
			}else{
				// no one to receive from
				OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}
			// finalize
		}else{
			// who will be next receiver? => (myrank - 2^step)
			int sendTo = (int) (clientRankInComm - Math.pow(2, step));
			int receiveFrom = (int) (clientRankInComm + Math.pow(2, step/-1));

			// am i leaf node?
			if(step == CommandProcessing.STEP_START && Integer.numberOfTrailingZeros(clientRankInComm) == 0){
				// i am a leaf node so send data to direct neighbor
				OUTResults.addNetSend(sendTo, new NetworkSimpleData(cmd.getSize() + 20), 30303, cmd.getCommunicator());
				OUTResults.setNextStep(step++);
			}else{
				// i am not a leaf node

				// receive data
				OUTResults.addNetReceive(receiveFrom, 30303, cmd.getCommunicator());
				// thus I send my data + the data i received to the next node (myrank - 2^step)
				// unless this receiver is out of range (< 0)
				if(sendTo >= 0){
					OUTResults.addNetSend(sendTo, new NetworkSimpleData(cmd.getSize() * (int)Math.pow(2, step) + 20), 30303, cmd.getCommunicator());
					OUTResults.setNextStep(step++);
				}else{
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}

			}
		}
	}

	/**
	 * Power of 2
	 *
	 * Determines what number is a power of 2 and is closest to the value given
	 *
	 * @param value 	Number to check
	 * @return			Closest number to pof2 of value
	 */
	private int pof2(int value){
		int temp = 1;

		while (temp <= value){
			temp *= 2;
		}

		return temp/2;
	}

}
