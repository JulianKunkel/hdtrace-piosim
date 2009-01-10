
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.simulator.program.Allreduce;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Allreduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Complex Binary Tree Algorithm, Root collects and distributes again 
 * @author Julian M. Kunkel
 */
public class BinaryTree 
	extends CommandImplementation<Allreduce>
{
	
	@Override
	public void process( Allreduce cmd, CommandStepResults OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}
		
		final int RECV_DATA = 100000;
		
		int lastRank = cmd.getCommunicator().getSize()-1;
		int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(lastRank);
		
		int current_iteration;
		int mask = 0;
		
		int clientRankInComm = cmd.getCommunicator().getCommRank(client.getModelComponent().getRank());
		
		if (step >= RECV_DATA){
			current_iteration = step - RECV_DATA;
		}else{
			current_iteration = step;
		}
		
		boolean isPartnerAvail = false;
		boolean myBit = false;
		if (step < RECV_DATA){
			while(current_iteration < iterations){
				mask = 1 << current_iteration;
				myBit = (clientRankInComm & mask) > 0;
				isPartnerAvail = (clientRankInComm | mask) <= lastRank;
				if (isPartnerAvail)
					break;
				current_iteration++;
			}
		}else{
			while(current_iteration < iterations){
				mask = 1 << (iterations - current_iteration -1);
				myBit = (clientRankInComm & mask) > 0;
				isPartnerAvail = (clientRankInComm | mask) <= lastRank;
				if (isPartnerAvail)
					break;
				current_iteration++;
			}
		}

		if(current_iteration == iterations){
			if(step < RECV_DATA){
				current_iteration = 0;
				step = RECV_DATA;
				mask = 1 << (iterations -1);
			}else{
				return; // no more work to do
			}
		}
		
		int partner = -1;
		if( isPartnerAvail ){
			if( myBit ){
				//determine lower one
				partner = clientRankInComm & ~(mask);
			}else{
				//determine higher one
				partner = clientRankInComm | (mask);
			}
		}
		//System.out.println(Simulator.getSimulator().getCurrentTime() + " step: " + step + " rankInComm: " + clientRankInComm  + " " + myBit + " " + isPartnerAvail + " partner: " + partner);
			
		int targetRank = cmd.getCommunicator().getWorldRank(partner);

		if (step < RECV_DATA){
			// receives data
			if(myBit){
				OUTresults.setNextStep(iterations - current_iteration + RECV_DATA);
				OUTresults.addNetSend(targetRank, new NetworkSimpleMessage(cmd.getSize() + 20),  
						30000, Communicator.INTERNAL_MPI);
				
				OUTresults.addNetReceive(targetRank, 30000, Communicator.INTERNAL_MPI);
				
				return;
			}else{
				int nextIter;
				if(current_iteration + 1== iterations){
					nextIter = 0 + RECV_DATA;
				}else{
					nextIter = current_iteration + 1;
				}
				OUTresults.setNextStep(nextIter);
				OUTresults.addNetReceive(targetRank, 30000, Communicator.INTERNAL_MPI);
				return;
			}
		}else{
			// transfer data back
			OUTresults.setNextStep(current_iteration + 1 + RECV_DATA);			

			OUTresults.addNetSend(targetRank, new NetworkSimpleMessage(cmd.getSize() + 20),  
					30000, Communicator.INTERNAL_MPI);

			return;
		}
	}

}
