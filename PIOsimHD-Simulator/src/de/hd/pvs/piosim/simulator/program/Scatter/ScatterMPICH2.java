package de.hd.pvs.piosim.simulator.program.Scatter;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanksRemapRoot;

/**
 *
 * Binary Tree Algorithm, Root starts to propagate data. A node sends data to child nodes only when data transfer to the node is complete.
 * The sender transfers data to all recipients in one step, that means the senders NIC is multiplexed among all receivers.
 *
 * Difference to BinaryTreeBCast is the amount of data sent. Since each node needs to get his own unique data it cannot be sent once and just copied.
 * In this simulator we use example data and to simplify the problem, this class will simply factorize the amount of data sent by the amount of nodes needing it.
 *
 * The file is based on the Broadcast implementation BinaryTreeMultiplex
 * To ensure the receiver is ready a small message is sent from any receiver to the source of the larger message.
 *
 * @author artur, julian
 *
 */
public class ScatterMPICH2 extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Scatter>{

	@Override
	public int getSingleTargetWorldRank(Scatter cmd) {
		return cmd.getRootRank();
	}

	@Override
	public void processWithLocalRanks(Scatter cmd, ICommandProcessingMapped OUTresults, Communicator comm, int clientRankInComm, int singleRankInComm, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		final int WAITING_FOR_ACK = 1;
		final int RECEIVED_ACK = 2;


		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = comm.getSize();
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
		final int phaseStart = iterations - trailingZeros;


		if(clientRankInComm != 0){
			// receive first, then send.

			final int maxPhase = iterations - 1  - phaseStart;

			if (step == CommandProcessing.STEP_START){

				int recvFrom = (clientRankInComm ^ 1<<trailingZeros);

				int target = getLocalRankExchangeRoot(singleRankInComm, recvFrom);
				OUTresults.addNetSend(target, new NetworkSimpleData(20), 30001, cmd.getCommunicator());
				OUTresults.addNetReceive(target, 30000, cmd.getCommunicator());

				if(maxPhase == -1){
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					// there is some work to do
					OUTresults.setNextStep(WAITING_FOR_ACK);
				}

			}else if (step == WAITING_FOR_ACK){
				// wait for an ack from the first process we want to sent data to.
				final int waitforAckFrom =  (1<< (maxPhase) | clientRankInComm);
				if (waitforAckFrom < commSize){
					int target =  getLocalRankExchangeRoot(singleRankInComm, waitforAckFrom);

					OUTresults.addNetReceive(target, 30001, cmd.getCommunicator());

					OUTresults.setNextStep(RECEIVED_ACK);

					return;
				}else{
					OUTresults.setNextStep(RECEIVED_ACK);
					return;
				}
			}else{// if(step >= RECEIVED_ACK)
				// send
				int iter = (int) step - RECEIVED_ACK;
				int targetRank = (1<< (maxPhase - iter) | clientRankInComm);


				if(iter < maxPhase){
					// stop after all clients got the data
					OUTresults.setNextStep(step + 1);

					final int waitforAckFrom =  (1<< (maxPhase - iter - 1) | clientRankInComm);
					if (waitforAckFrom < commSize){
						OUTresults.addNetReceive( getLocalRankExchangeRoot(singleRankInComm, waitforAckFrom), 30001, cmd.getCommunicator());
					}
				}else{
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}


				if (targetRank < commSize){

					// the number of packets depends on the processes which forward the data in the binary tree
					final int countPow =  1<<(maxPhase - iter);
					int count = targetRank + countPow > commSize ? commSize - targetRank : countPow;

					//System.out.println(client.getModelComponent().getRank() + " check to target: " + targetRank + " " + count );


					OUTresults.addNetSend( getLocalRankExchangeRoot(singleRankInComm, targetRank), new NetworkSimpleData(cmd.getSize() * count + 20), 30000, cmd.getCommunicator());
				}
			}

		}else{ // rank 0, we know data must be transmitted to at least one target

			// step 0 is to wait for the receiver to be ready!
			if(step == CommandProcessing.STEP_START){
				final int targetRank =  1<< (iterations - 1);
				OUTresults.addNetReceive((targetRank != singleRankInComm) ? targetRank : 0, 30001, cmd.getCommunicator());
				OUTresults.setNextStep(1);

				return;
			}

			// step > 0
			OUTresults.setNextStep(step + 1);

			if(step < iterations){
				// stop after all clients got the data
				OUTresults.setNextStep(step + 1);
				final int waitforAckFrom =  1<< (iterations - step - 1);
				OUTresults.addNetReceive((waitforAckFrom != singleRankInComm) ? waitforAckFrom : 0, 30001, cmd.getCommunicator());
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			final int targetRank =  1<< (iterations - step);
			int count = 2 * targetRank  > commSize ? commSize - targetRank : targetRank;

			//System.out.println("Check to target: " + targetRank + " " + count );

			OUTresults.addNetSend( (targetRank != singleRankInComm) ? targetRank : 0,
					new NetworkSimpleData(cmd.getSize() * count + 20), 30000, cmd.getCommunicator());
		}
	}

}
