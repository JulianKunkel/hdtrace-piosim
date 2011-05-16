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
 * In this simulator we use example data and to simplify the problem, this class will simply faktorize the amount of data sent by the amount of nodes needing it.
 *
 * @author artur
 *
 */
public class ScatterMPICH2 extends CommandImplementation<Scatter>{

	@Override
	public void process(Scatter cmd, CommandProcessing OUTresults,
			GClientProcess client, long step, NetworkJobs compNetJobs) {
		final int RECEIVED = 2;

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		int clientRankInComm = myRank;

		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = rootRank;
		}

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
		final int phaseStart = iterations - trailingZeros;



		if(clientRankInComm != 0){
			// recv first, then send.

			if (step == CommandProcessing.STEP_START){
				OUTresults.setNextStep(RECEIVED);


				int recvFrom = (clientRankInComm ^ 1<<trailingZeros);

				if(recvFrom == 0){
					recvFrom = rootRank;
				}else if(recvFrom == rootRank){
					recvFrom = 0;
				}


				OUTresults.addNetReceive(recvFrom, 30000, cmd.getCommunicator());

			}else if(step == RECEIVED){
				// send
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					int targetRank = (1<<iter | clientRankInComm);
					if (targetRank >= commSize) continue;
					//System.out.println(clientRankInComm +" to " + (1<<iter | clientRankInComm) );
					OUTresults.addNetSend(((targetRank != rootRank) ? targetRank : 0),
							new NetworkSimpleData(cmd.getSize()*iter + 20), 30000, cmd.getCommunicator());
				}
			}
		}else{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			// send to all receivers
			for (int iter = iterations-1 ; iter >= 0 ; iter--){
				final int targetRank =  1<<iter;
				//System.out.println(clientRankInComm +" to " + ((targetRank != rootRank) ? targetRank : 0) );
				OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0,
						new NetworkSimpleData(cmd.getSize()*iter + 20), 30000, cmd.getCommunicator());
			}
		}

	}

}
