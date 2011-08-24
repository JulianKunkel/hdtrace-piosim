package de.hd.pvs.piosim.simulator.program.ReduceScatter;

import de.hd.pvs.piosim.model.program.commands.ReduceScatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Algorithm:
 *
 * A recursive-halving algorithm (beginning with processes that are
 * distance 1 apart) is used for the reduce-scatter.
 * The non-power-of-two case is
 * handled by dropping to the nearest lower power-of-two: the first
 * few odd-numbered processes send their data to their left neighbors
 * (rank-1), and the reduce-scatter happens among the remaining
 * power-of-two processes. If the root is one of the excluded
 * processes, then after the reduce-scatter, rank 0 sends its result to
 * the root and exits;
 *
 * @author artur
 *
 */
public class ReduceScatterMPICH2 extends CommandImplementation<ReduceScatter>{
	final int TAG = 40002;
	final int RECEIVED = 2;

	@Override
	public long getInstructionCount(ReduceScatter cmd, GClientProcess client, long step) {
		final int myRank = client.getModelComponent().getRank();

		if(step == RECEIVED){
			// aggregate data received from all clients, i.e. we have a comm of a given size and the number of elements dedicated to us...
			return cmd.getRecvcounts().get(myRank) * (cmd.getCommunicator().getSize() - 1) + 1;
		}else{
			return 1;
		}
	}

	@Override
	public void process(ReduceScatter cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int highestPowerOfTwo = Integer.bitCount(cmd.getCommunicator().getSize() - 1);
		final int numberOfOneWayCommunications = cmd.getCommunicator().getSize() - (1 << highestPowerOfTwo);
		final int myRank = client.getModelComponent().getRank();
		int targetRank;


		if(step == CommandProcessing.STEP_START){

			// odd sends, even receives
			// does not apply when commSize is a power of two
			if(myRank <= numberOfOneWayCommunications * 2 && numberOfOneWayCommunications != 0){
				if(myRank % 2 != 0){
					targetRank = myRank - 1;
					final long sendCnt = cmd.getRecvcounts().get(targetRank);
					OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					targetRank = myRank + 1;
					OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
					OUTresults.setNextStep(++step);
				}
			}else{
				// bidirect communication
				if(myRank % 2 == 0){
					targetRank = myRank + 1;
				}else{
					targetRank = myRank - 1;
				}

				// catch edge case
				if(targetRank > cmd.getCommunicator().getSize()){
					OUTresults.setNextStep(++step);
				}

				final long sendCnt = cmd.getRecvcounts().get(targetRank);
				OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
				OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
				OUTresults.setNextStep(++step);

			}
		}else{
			// ignore all odd ranks below NumberOfOneWayCommunication*2
			// from now on only SendReceives

			final int devider = 1 << step;

			if (myRank < devider){
				targetRank = myRank + devider;
			}else{
				targetRank = myRank - devider;
			}

			if(targetRank > cmd.getCommunicator().getSize()){
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				return;
			}

			final long sendCnt = cmd.getRecvcounts().get(targetRank);
			OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
			OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
			OUTresults.setNextStep(++step);
		}
	}
}
