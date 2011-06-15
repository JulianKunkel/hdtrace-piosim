package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanksRemapRoot;

public class GatherBinaryTreeMPICH2 extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Gather>{


	@Override
	public int getSingleTargetWorldRank(Gather cmd) {
		return cmd.getRootRank();
	}

	@Override
	public void processWithLocalRanks(Gather cmd,
		ICommandProcessingMapped OUTResults, Communicator comm,
		int clientRankInComm, int rootRank, GClientProcess client,
		long step, NetworkJobs compNetJobs)
	{

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int pof2Offset = commSize - pof2(commSize);
		final int iterations = Integer.numberOfTrailingZeros(commSize);

		if(clientRankInComm == 0){
			// calculate from whom to receive
			int receiveFrom = (int) Math.pow(2, step);

			// check if receiveFrom is greater than the number of nodes
			// if not => receive data
			if(receiveFrom < commSize){
				OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom) , 30303, cmd.getCommunicator());
				OUTResults.setNextStep(++step);
			}else{
				// no one to receive from
				OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}
			// finalize
		}else{
			if(isValuePofTwo(clientRankInComm)){
				// I am the one who has to send data to root when the time is right (2^step == myRank)
				if (Math.pow(2, step) == clientRankInComm){
					// My turn to send data to root
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, 0), new NetworkSimpleData(cmd.getSize() + 20), 30303, cmd.getCommunicator());
					// finalize
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					// Not my turn to send to root
					if(clientRankInComm < cmd.getCommunicator().getSize()-1){
						// Receive some data first
						if(clientRankInComm/2 >= step && cmd.getCommunicator().getSize()-1 >= (clientRankInComm + Math.pow(2, step))){
							int receiveFrom = (int) (clientRankInComm + Math.pow(2, step));
							OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom), 30303, cmd.getCommunicator());
							OUTResults.setNextStep(++step);
						}
					}
				}
			}else{
				if(step == CommandProcessing.STEP_START && (clientRankInComm%2 == 0)){
					// Receive from neighbour
					OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, clientRankInComm + 1), 30303, cmd.getCommunicator());
					OUTResults.setNextStep(++step);
				}else if(step <= Math.sqrt(clientRankInComm)){
					int sendTo = (int)(clientRankInComm - Math.pow(2, step));
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, sendTo), new NetworkSimpleData(cmd.getSize() + 20), 30303, cmd.getCommunicator());
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

	private boolean isValuePofTwo(int value){
		int temp = 1;

		while (temp < value){
			temp *= 2;
		}

		if(temp == value)
			return true;
		else
			return false;
	}

}
