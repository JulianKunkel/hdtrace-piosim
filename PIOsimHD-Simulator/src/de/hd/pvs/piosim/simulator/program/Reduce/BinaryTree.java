
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

package de.hd.pvs.piosim.simulator.program.Reduce;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Complex Binary Tree Algorithm, Root collects
 * @author Julian M. Kunkel
 */
public class BinaryTree 
extends CommandImplementation<Reduce>
{
	
	@Override
	public void process(Reduce cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}
		
		int lastRank = cmd.getCommunicator().getSize()-1;
		int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(lastRank);
		
		int current_iteration;
		int mask = 0;
		
		int clientRankInComm = cmd.getCommunicator().getCommRank(client.getModelComponent().getRank());
		
		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = cmd.getRootRank();
		}
		
		current_iteration = step;
		
		boolean send = false;
		
		while(current_iteration < iterations){
			mask = 1 << current_iteration;
			send = (clientRankInComm & mask) > 0;
			if ((clientRankInComm | mask) <= lastRank)
				break;
			current_iteration++;
		}
		
		if(current_iteration == iterations){
			return; // no more work to do
		}
		
		int partner = -1;
		if( send ){			
			//determine lower one
			partner = clientRankInComm & ~(mask);
			assert(partner >= 0);			
		}else{			
			//determine higher one
			partner = clientRankInComm | (mask);
			assert(partner >= 0);
		}
		
		
		
		
		int targetRank = cmd.getCommunicator().getWorldRank(partner);
		
		//System.out.println(client.getName() + " Send: " + send + " " + " " + current_iteration + " partners with "  + partner);
		
		
		if(send){
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			OUTresults.addNetSend(targetRank, new NetworkSimpleMessage(cmd.getSize() + 20),  
					30000, Communicator.INTERNAL_MPI);
			
			return;
		}else{
			int nextIter;
			if(current_iteration + 1== iterations){
				nextIter = CommandProcessing.STEP_COMPLETED;
			}else{
				nextIter = current_iteration + 1;
			}
			OUTresults.setNextStep(nextIter);
			OUTresults.addNetReceive(targetRank, 30000, Communicator.INTERNAL_MPI);
			return;
		}
	}
	
}
