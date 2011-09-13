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
public class CopyOfReduceScatterMPICH2 extends CommandImplementation<ReduceScatter>{
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

		final int commSize = cmd.getCommunicator().getSize() - 1;
		final int highestPowerOfTwo = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize);
		final int numberOfOneWayCommunications = cmd.getCommunicator().getSize() - (highestPowerOfTwo);
		final int limitForHalfingAlgorithm = highestPowerOfTwo + numberOfOneWayCommunications;

		final int myRank = client.getModelComponent().getRank();
		int targetRank;

		if(Integer.bitCount(cmd.getCommunicator().getSize()) == 1){
			// special case: po2
			if(myRank < limitForHalfingAlgorithm){
				targetRank = myRank + 1<<step;
			}else{
				targetRank = myRank - 1<<step;
			}

			if(targetRank < 0 || targetRank > commSize){
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				return;
			}

			final long sendCnt = cmd.getRecvcounts().get(targetRank);
			System.out.println("perfect case! " + step + " # " + myRank + " <-> " + targetRank);
			OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
			OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
			OUTresults.setNextStep(++step);
		}else{
			// other case => dropping to the nearest po2
			if(step == CommandProcessing.STEP_START){
				System.out.println(numberOfOneWayCommunications);
				if(myRank <= (numberOfOneWayCommunications * 2)){
					// one way communication
					if(myRank % 2 != 0){
						targetRank = myRank - 1;
						final long sendCnt = cmd.getRecvcounts().get(targetRank);
						System.out.println(step + " # " + myRank + " -> " + targetRank);
						OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
						OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
					}else{
						targetRank = myRank + 1;

						if(myRank < commSize){
							System.out.println(step + " # " + myRank + " <- " + targetRank);
							OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
						}
						OUTresults.setNextStep(++step);
					}
				}else{
					// rest does nothing in this step
					OUTresults.setNextStep(++step);
				}
			}else{
				// biderectional non po2 case
				if(1<<step < 1<<highestPowerOfTwo){
					// intra half
					if(myRank < limitForHalfingAlgorithm){
						// left half
						if((myRank/2) % 2 == 0){
							// sender
							targetRank = myRank + 1<<step;
							if(targetRank < numberOfOneWayCommunications*2 + 1){
								targetRank++;
							}
						}else{
							// receiver
							targetRank = myRank - 1<<step;
							if(targetRank < numberOfOneWayCommunications*2 + 1){
								targetRank--;
							}
						}
					}else{
						//right half
						if((myRank - limitForHalfingAlgorithm) % 2 == 0){
							//sender
							targetRank = myRank + 1<<step;
						}else{
							//receiver
							targetRank = myRank - 1<<step;
						}
					}

					if(targetRank < 0 || targetRank > commSize){
						OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
						return;
					}

					System.out.println(step + " # " + myRank + " <-> " + targetRank);
					final long sendCnt = cmd.getRecvcounts().get(targetRank);

					OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
					OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
					OUTresults.setNextStep(++step);
				}else{
					// inter half
					if(myRank < limitForHalfingAlgorithm){
						targetRank = myRank + highestPowerOfTwo + numberOfOneWayCommunications;
					}else{
						targetRank = myRank - highestPowerOfTwo - numberOfOneWayCommunications;
						if(targetRank < numberOfOneWayCommunications*2 + 1){
							targetRank--;
						}
					}
					final long sendCnt = cmd.getRecvcounts().get(targetRank);
					System.out.println(step + " # " + myRank + " <-> " + targetRank);
					OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
					OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}
			}
		}
	}
}
