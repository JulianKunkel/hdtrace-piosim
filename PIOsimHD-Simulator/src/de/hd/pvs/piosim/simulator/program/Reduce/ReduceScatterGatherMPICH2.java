//    Copyright (C) 2008, 2009, 2010, 2011 Julian M. Kunkel
//
//    This file is part of PIOsimHD.
//
//    PIOsimHD is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    PIOsimHD is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.simulator.program.Reduce;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.project.CommunicatorInformation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.model.program.commands.ReduceScatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Implementation, Scatter-Gather.
 *
 * This algorithm implements the reduce in two steps: first a
   reduce-scatter, followed by a gather to the root. A
   recursive-halving algorithm (beginning with processes that are
   distance 1 apart) is used for the reduce-scatter, and a binomial tree
   algorithm is used for the gather. The non-power-of-two case is
   handled by dropping to the nearest lower power-of-two: the first
   few odd-numbered processes send their data to their left neighbors
   (rank-1), and the reduce-scatter happens among the remaining
   power-of-two processes. If the root is one of the excluded
   processes, then after the reduce-scatter, rank 0 sends its result to
   the root and exits; the root now acts as rank 0 in the binomial tree
   algorithm for gather.
 *
 * @author artur
 */
public class ReduceScatterGatherMPICH2
extends CommandImplementation<Reduce>
{
    final int SCATTER_COMPLETED = 2;
    final int TAG = 41234;

    // Re-map communicator for non-power of two processes
    static HashMap<Integer,Communicator> communicators = new HashMap<Integer,Communicator>();


    @Override
    public void process(Reduce cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs)
    {
        if (cmd.getCommunicator().getSize() == 1) {
            return;
        }

        final int commSize = cmd.getCommunicator().getSize();

        if(Integer.bitCount(commSize) != 1){
            // number of processes is not power of two
            // The non-power-of-two case is
            // handled by dropping to the nearest lower power-of-two: the first
            // few odd-numbered processes send their data to their left neighbors
            // (rank-1)
            final int myRank = client.getModelComponent().getRank();
            final int rest = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize) - 1;
            // rest = nearest lower power-of-two
            if(myRank < 2*(commSize - (1<<rest))){
                // odd sends to even
                if(myRank % 2 == 0){
                    OUTresults.addNetReceive(myRank + 1, TAG, cmd.getCommunicator());
                }else{
                    OUTresults.addNetSend(myRank - 1, new NetworkSimpleData(cmd.getSize() / cmd.getCommunicator().getSize()), TAG, cmd.getCommunicator());
                }
            }
        }

        // spawn a communicator for the remaining power-of-two processes
        Communicator comm = communicators.get(cmd.getCommunicator().getIdentity());
        if(comm == null){
            comm = new Communicator("");

            communicators.put(cmd.getCommunicator().getIdentity(), comm);

            HashMap<Integer, CommunicatorInformation> m = cmd.getCommunicator().getParticipiants();

            if(Integer.bitCount(commSize) != 1){
                // skip processes handled above
                final int rest = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize);
                int localRankCounter = 0;
                for(Integer key : m.keySet()){
                    if(key < 2*(commSize - rest) && key % 2 != 0){
                        continue;
                    }
                    comm.addRank(key, localRankCounter, 0);
                    localRankCounter++;
                }
            }else{
                // nothing to skip
                for(Integer key : m.keySet()){
                    comm.addRank(key, key, 0);
                }
            }
        }

        if(step == CommandProcessing.STEP_START){
            // perform reduce scatter
            ReduceScatter scmd = new ReduceScatter();
            HashMap<Integer, Long> map = new HashMap<Integer, Long>();

            // default: split data equally.
            long sizePerRank = cmd.getSize() / cmd.getCommunicator().getSize();
            int restClients = (int) (cmd.getSize() % cmd.getCommunicator().getSize());

            for (int i=0; i < cmd.getCommunicator().getSize(); i++){
                if( i < restClients){
                    map.put(i, sizePerRank + 1);
                }else{
                    map.put(i, sizePerRank);
                }
            }

            scmd.setRecvcounts(map);

            scmd.setCommunicator(cmd.getCommunicator());

            OUTresults.invokeChildOperation(scmd, SCATTER_COMPLETED,
                de.hd.pvs.piosim.simulator.program.ReduceScatter.ReduceScatterPowerOfTwo.class);

        }else if(step == SCATTER_COMPLETED){
            // perform gather
            Gather gcmd = new Gather();

            gcmd.setRootRank(cmd.getRootRank());
            // the amount of data to gather from each node is the assigned data from the scatter, this can be approximated by the equation:
            long size = cmd.getSize() / cmd.getCommunicator().getSize() + cmd.getSize() % cmd.getCommunicator().getSize();
            gcmd.setSize(size);

            gcmd.setCommunicator(cmd.getCommunicator());

            OUTresults.invokeChildOperation(gcmd, CommandProcessing.STEP_COMPLETED,
                    de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2.class);
        }
    }

}