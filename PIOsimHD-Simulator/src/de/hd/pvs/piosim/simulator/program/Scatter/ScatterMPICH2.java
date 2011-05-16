package de.hd.pvs.piosim.simulator.program.Scatter;

import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 *
 * Binary Tree Algorithm, Root starts to propagate data. A node sends data to child nodes only when data transfer to the node is complete.
 * The sender transfers data to all recipients in one step, that means the senders NIC is multiplexed among all receivers.
 *
 * Difference to BinaryTreeBCast is the amount of data sent. Since each node needs to get his own unique data it cannot be sent once and just copied.
 * In this simulator we use example data and to simplify the problem, this class will simply factorize the amount of data sent by the amount of nodes needing it.
 *
 * @author artur, julian
 *
 */
public class ScatterMPICH2 extends CommandImplementation<Scatter>{

	@Override
	public void process(Scatter cmd, CommandProcessing OUTresults,
			GClientProcess client, long step, NetworkJobs compNetJobs) {
		final int RECEIVED = 1;

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);

		final int rootRank = cmd.getCommunicator().getLocalRank( cmd.getRootRank() );
		int clientRankInComm = cmd.getCommunicator().getLocalRank( client.getModelComponent().getRank() );

		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = rootRank;
		}

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
		final int phaseStart = iterations - trailingZeros;


		if(clientRankInComm != 0){
			// receive first, then send.

			final int maxPhase = iterations - 1  - phaseStart;

			if (step == CommandProcessing.STEP_START){


				int recvFrom = (clientRankInComm ^ 1<<trailingZeros);

				OUTresults.addNetReceive(((recvFrom != rootRank) ? recvFrom : 0), 30000, cmd.getCommunicator());

				if(maxPhase == -1){
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					// there is some work to do
					OUTresults.setNextStep(RECEIVED);
				}

			}else{ // if(step >= RECEIVED)
				// send
				int iter = (int) step - RECEIVED;

				if(iter < maxPhase){
					// stop after all clients got the data
					OUTresults.setNextStep(step + 1);
				}else{
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}

				int targetRank = (1<< (maxPhase - iter) | clientRankInComm);

				if (targetRank < commSize){

					// the number of packets depends on the processes which forward the data in the binary tree
					final int countPow =  1<<(maxPhase - iter);
					int count = targetRank + countPow > commSize ? commSize - targetRank : countPow;

					//System.out.println(client.getModelComponent().getRank() + " check to target: " + targetRank + " " + count );


					OUTresults.addNetSend(((targetRank != rootRank) ? targetRank : 0),
						new NetworkSimpleData(cmd.getSize() * count + 20), 30000, cmd.getCommunicator());
				}
			}

		}else{ // rank 0, we know data must be transmitted to at least one target

			if(step < iterations - 1){
				// stop after all clients got the data
				OUTresults.setNextStep(step + 1);
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			final int targetRank =  1<< (iterations - 1 - step);
			int count = 2 * targetRank  > commSize ? commSize - targetRank : targetRank;

			//System.out.println("Check to target: " + targetRank + " " + count );

			OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0,
					new NetworkSimpleData(cmd.getSize() * count + 20), 30000, cmd.getCommunicator());
		}
	}

}
