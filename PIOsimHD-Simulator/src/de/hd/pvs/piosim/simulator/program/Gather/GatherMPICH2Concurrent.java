package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanksRemapRoot;

/**
 * MPICH2 Gather Algorithm:
 * 	simple binary tree algorithm
 *
 * @author Julian
 * Overhauled existing method, since it did not work for larger process counts.
 *
 */
public class GatherMPICH2Concurrent
extends CommandImplementationWithCommunicatorLocalRanksRemapRoot<Gather>
{
	final int tag = 20000;

	final int RECEIVED_FROM_LEAF = 1;

	@Override
	public int getSingleTargetWorldRank(Gather cmd) {
		return cmd.getRootRank();
	}

	private int children(int rank, int size){
		final int trailingZeros = Integer.numberOfTrailingZeros(rank);
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(size-1);
		final int phaseStart = iterations - trailingZeros;
		int maxIter = iterations  - phaseStart - 1;

		int gathered = 1;
		for (int iter = 0 ; iter <= maxIter; iter++){
			final int targetRank = (1<<iter | rank);
			if (targetRank >= size )
				continue;
			gathered += children(targetRank, size);
		}

		return gathered;
	}

	@Override
	public void processWithLocalRanks(Gather cmd,
			ICommandProcessingMapped OUTresults, Communicator comm,
			int clientRankInComm, int singleRankInComm, GClientProcess client,
			long step, NetworkJobs compNetJobs) {


		final int commSize = cmd.getCommunicator().getSize();

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);

		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int phaseStart = iterations - trailingZeros;
		int maxIter = iterations  - phaseStart - 1;


		if(clientRankInComm != 0){
			// root rank is a special case...
			maxIter = maxIter + 1;
		}

		if(step == CommandProcessing.STEP_START){
			// receive messages from all leaf ranks
			for (int iter = 0 ; iter < maxIter; iter++){
				final int targetRank = (1<<iter | clientRankInComm);
				if (targetRank >= commSize )
					continue;

				//System.out.println(" to leaf:" + targetRank );
				OUTresults.addNetReceive(getLocalRankExchangeRoot(singleRankInComm, targetRank), tag, cmd.getCommunicator());
			}

			OUTresults.setNextStep(RECEIVED_FROM_LEAF);
			return;

		}else if(step == RECEIVED_FROM_LEAF){

			if(clientRankInComm == 0){ // root node
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				return;

			}else{ // non-root process => broadcast message towards root.
				// count the number of gathered receives
				int gathered = children(clientRankInComm, commSize); // my data

				int sendTo = (clientRankInComm ^ 1<<trailingZeros);
				System.out.println(" to root: " + sendTo + " " + gathered);
				OUTresults.addNetSend(getLocalRankExchangeRoot(singleRankInComm, sendTo),
						new NetworkSimpleData(cmd.getSize() * gathered), tag, cmd.getCommunicator());
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				return;
			}
		}
	}
}