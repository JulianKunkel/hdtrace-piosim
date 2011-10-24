package de.hd.pvs.piosim.simulator.program.Gather;

import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Gather Algorithm:
 * 	simple binary tree algorithm
 *
 *
 * @author artur
 *
 */
public class GatherMPICH2
extends CommandImplementation<Gather>
{
	static final int TAG = 40100;

	@Override
	public void process(Gather cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int rootRank = 0;
		final int clientRankInComm = client.getModelComponent().getRank();
		final int commSize = cmd.getCommunicator().getSize() -1;
		int receiveFrom = -1, sendTo = -1;

		// specifies the amount of data to be sent
		int dataMultiplicator = 1;

		if(clientRankInComm == rootRank){
			// root rank operations
			//		receive from (1 << step)
			receiveFrom = 1 << step;

			if (receiveFrom <= commSize){
				OUTresults.addNetReceive(receiveFrom, TAG, cmd.getCommunicator());
				OUTresults.setNextStep(++step);
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}
		}else if(Integer.bitCount(clientRankInComm) == 1){
			// rank is power-of-two
			if(clientRankInComm == (1 << step)){
				// rank's turn to send data to root
				sendTo = rootRank;
				dataMultiplicator = ((clientRankInComm << 1) <= commSize) ? ((clientRankInComm << 1) - clientRankInComm) : (commSize - clientRankInComm);
				if(dataMultiplicator == 0)
					dataMultiplicator = 1;
				OUTresults.addNetSend(sendTo, new NetworkSimpleData(dataMultiplicator * cmd.getSize() + 20), TAG, cmd.getCommunicator());
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}else{
				// receive data and send own data plus the newly received to root later
				receiveFrom = clientRankInComm + (1 << step);

				if (receiveFrom <= commSize){
					OUTresults.addNetReceive(receiveFrom, TAG, cmd.getCommunicator());
				}
				OUTresults.setNextStep(++step);
			}
		}else{
			if(clientRankInComm % 2 != 0){
				// odd
				sendTo = clientRankInComm - 1;
				dataMultiplicator = 1;
				OUTresults.addNetSend(sendTo, new NetworkSimpleData(dataMultiplicator * cmd.getSize() + 20), TAG, cmd.getCommunicator());
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}else{
				// even
				if(step == CommandProcessing.STEP_START){
					receiveFrom = clientRankInComm + 1;

					if (receiveFrom <= commSize){
						OUTresults.addNetReceive(receiveFrom, TAG, cmd.getCommunicator());
					}

					OUTresults.setNextStep(++step);
				}else{
					sendTo = clientRankInComm - (1 << step);
					dataMultiplicator = (clientRankInComm == commSize) ? 1 : 2;

					if(Integer.bitCount(sendTo) == 1){
						OUTresults.addNetSend(sendTo, new NetworkSimpleData(dataMultiplicator * cmd.getSize() + 20), TAG, cmd.getCommunicator());
						OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
					}else{
						OUTresults.setNextStep(++step);
					}
				}
			}
		}
	}
}