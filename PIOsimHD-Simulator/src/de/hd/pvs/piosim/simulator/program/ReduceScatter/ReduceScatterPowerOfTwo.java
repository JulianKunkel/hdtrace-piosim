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
public class ReduceScatterPowerOfTwo extends CommandImplementation<ReduceScatter>{
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
        final int iterations = Integer.numberOfTrailingZeros(cmd.getCommunicator().getSize());
        final int range = cmd.getCommunicator().getSize() / (1<<(iterations-step));
        final int myRank = client.getModelComponent().getRank();

        int offset = 0;
        int targetRank;

        System.out.println("Iterations: " + iterations + " range: " + range);

        for(int i = 0; i*range < commSize+1; i++){
            // check what group the current rank belongs to
            if(myRank >= i*range && myRank < (i+1)*range){
                // place found, save to data
                offset = i;
                break;
            }else{
                continue;
            }
        }

        if(offset % 2 == 0){
            // send/receive to/from rank + range
            targetRank = myRank + range;
        }else{
            // send/receive to/from rank - range
            targetRank = myRank - range;
        }

        if(targetRank < 0 || targetRank > commSize){
            System.err.println("Range exceeded! Probably Non-PowerOfTwo number of processes used!");
            return;
        }else{
            System.out.println(step + ": " + myRank + " <-> " + targetRank);
            // data to be sent is halved each step
            final long sendCnt = cmd.getTotalSize() / (2<<step);
            OUTresults.addNetSend(targetRank, new NetworkSimpleData(sendCnt + 20), TAG, cmd.getCommunicator());
            OUTresults.addNetReceive(targetRank, TAG, cmd.getCommunicator());
            OUTresults.setNextStep(++step);
        }

        if(step > iterations){
            OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
        }
    }
}


