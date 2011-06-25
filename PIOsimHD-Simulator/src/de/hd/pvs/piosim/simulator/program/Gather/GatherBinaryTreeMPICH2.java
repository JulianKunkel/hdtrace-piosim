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

		final int commSize = cmd.getCommunicator().getSize() -1;
		int receiveFrom = -1, sendTo = -1;
		int dataMultiplicator = 1;

		if(clientRankInComm == 0){
			// calculate from whom to receive
			receiveFrom = 1<<step;

			// check if receiveFrom is greater than the number of nodes
			// if not => receive data
			if(receiveFrom <= commSize){
				System.out.println("Step: " + step + " # " + "root <- " + receiveFrom);
				OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom) , 30303, cmd.getCommunicator());
				OUTResults.setNextStep(++step);
			}else{
				// no one to receive from
				OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}
		}else{
			if(Integer.bitCount(clientRankInComm) == 1){
				// I am the one who has to send data to root when the time is right (2^step == myRank)
				if ((1<<step) == clientRankInComm){
					// My turn to send data to root

					dataMultiplicator = (((clientRankInComm << 1) <= commSize) ? clientRankInComm : (commSize - clientRankInComm + 1));

					System.out.println("Step: " + step + " # " + clientRankInComm + " -> root" + " # dm = " + dataMultiplicator);
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, 0), new NetworkSimpleData(cmd.getSize() * dataMultiplicator + 20), 30303, cmd.getCommunicator());
					// finalize
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					// Not my turn to send to root
					// so receive data from higher ranks instead

					// receive from direct neighbour first and then from even ranks only
					receiveFrom = (int) (clientRankInComm + (1<<step));

					if(receiveFrom <= commSize){
						System.out.println("Step: " + step + " # " + clientRankInComm + " <- " + receiveFrom);
						OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom), 30303, cmd.getCommunicator());
					}
					OUTResults.setNextStep(++step);
				}
			}else{
				if(clientRankInComm%2 == 0){
					// im am an even rank but not Po2
					if(Integer.bitCount(clientRankInComm - (1<<step)) == 1 ){
						// my turn to send to Po2 rank
						sendTo = (int) (clientRankInComm - (1<<step));

						int leftPo2 = 1 << (Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(clientRankInComm) - 1);
						int rightPo2 = leftPo2 << 1;
						dataMultiplicator = ((rightPo2 <= commSize) ? (rightPo2 - clientRankInComm) : (commSize - clientRankInComm + 1));
						System.out.println("Step: " + step + " # " + clientRankInComm + " -> " + sendTo + " # dm = " + dataMultiplicator);
						OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, sendTo), new NetworkSimpleData(cmd.getSize() * dataMultiplicator + 20), 30303, cmd.getCommunicator());
						OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
					}else{
						// my turn to receive from rank + 1<<step
						receiveFrom = (int)(clientRankInComm + (1<<step));
						if(receiveFrom <= commSize){
							System.out.println("Step: " + step + " # " + clientRankInComm + " <- " + receiveFrom);
							OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom), 30303, cmd.getCommunicator());
						}
						OUTResults.setNextStep(++step);
					}
				}else if(clientRankInComm % 2 != 0 && step == CommandProcessing.STEP_START){
					// odd ranks send to even left neighbour (rank - 1)
					sendTo = (int)(clientRankInComm - 1);
					System.out.println("Step: " + step + " # " + clientRankInComm + " -> " + sendTo);
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, sendTo), new NetworkSimpleData(cmd.getSize() + 20), 30303, cmd.getCommunicator());
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}
			}
		}
	}
}

