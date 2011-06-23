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
		int dataMultiplicator;

		if(clientRankInComm == 0){
			// calculate from whom to receive
			receiveFrom = (int) Math.pow(2, step);

			// check if receiveFrom is greater than the number of nodes
			// if not => receive data
			if(receiveFrom <= commSize){
				System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);
				OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom) , 30303, cmd.getCommunicator());
				OUTResults.setNextStep(++step);
			}else{
				// no one to receive from
				OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}
		}else{
			if(Integer.bitCount(clientRankInComm) == 1){
				// I am the one who has to send data to root when the time is right (2^step == myRank)
				if (Math.pow(2, step) == clientRankInComm){
					// My turn to send data to root

					// correct the amount of data
					dataMultiplicator = (clientRankInComm*2 <= commSize) ? clientRankInComm : commSize - clientRankInComm;


					System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, 0), new NetworkSimpleData(cmd.getSize() * dataMultiplicator + 20), 30303, cmd.getCommunicator());
					// finalize
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					// Not my turn to send to root
					// so receive data from higher ranks instead

					// receive from direct neighbour first and then from even ranks only
					receiveFrom = (int) (clientRankInComm + ((step == 0) ? 1 : 2*step));

					if(receiveFrom <= commSize){
						System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);
						OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom), 30303, cmd.getCommunicator());
					}
					OUTResults.setNextStep(++step);
				}
			}else{
				if(clientRankInComm%2 == 0){
					if(step == CommandProcessing.STEP_START){
						receiveFrom = (int)(clientRankInComm + Math.pow(2, step));
						if(receiveFrom <= commSize){
							System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);
							OUTResults.addNetReceive(getLocalRankExchangeRoot(rootRank, receiveFrom), 30303, cmd.getCommunicator());
						}
						OUTResults.setNextStep(++step);
					}else{
						sendTo = (int)(clientRankInComm - 2*step);
						if(Integer.bitCount(sendTo) == 1){
							System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);

							// correct amount of data
							if (clientRankInComm + 2 == commSize)
								dataMultiplicator = 3;
							else
								dataMultiplicator = 2;

							OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, sendTo), new NetworkSimpleData(cmd.getSize() * dataMultiplicator + 20), 30303, cmd.getCommunicator());
							OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
						}else{
							OUTResults.setNextStep(++step);
						}
					}
				}else if(clientRankInComm % 2 != 0 && step == CommandProcessing.STEP_START){
					sendTo = (int)(clientRankInComm - 1);
					System.out.println("Rank: " + clientRankInComm + " sendTo: " + sendTo + " receiveFrom: " + receiveFrom + " Step: " +  step);
					OUTResults.addNetSend(getLocalRankExchangeRoot(rootRank, sendTo), new NetworkSimpleData(cmd.getSize() + 20), 30303, cmd.getCommunicator());
					OUTResults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}
			}
		}
	}
}
