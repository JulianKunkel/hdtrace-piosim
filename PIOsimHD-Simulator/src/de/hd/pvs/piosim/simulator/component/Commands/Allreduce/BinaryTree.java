
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

package de.hd.pvs.piosim.simulator.component.Commands.Allreduce;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Allreduce;
import de.hd.pvs.piosim.simulator.component.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.component.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.component.Commands.CommandImplementation;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;

/**
 * Complex Binary Tree Algorithm, Root collects and distributes again 
 * @author Julian M. Kunkel
 */
public class BinaryTree 
	extends CommandImplementation<Allreduce>
{
	
	@Override
	public CommandStepResults process(Allreduce cmd, GClientProcess client, int step, NetworkJobs compNetJobs) {

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return null;
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
				return null; // no more work to do
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
			
		ISNodeHostedComponent target = getTargetfromRank(client,  cmd.getCommunicator().getWorldRank(partner) );

		if (step < RECV_DATA){
			// receives data
			if(myBit){
				CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, (iterations - current_iteration) + RECV_DATA);
				netAddSend(jobs, target, new NetworkSimpleMessage(cmd.getSize() + 20),  
						30000, Communicator.INTERNAL_MPI);
				netAddReceive(jobs, target, 30000, Communicator.INTERNAL_MPI);
				
				return jobs;
			}else{
				int nextIter;
				if(current_iteration + 1== iterations){
					nextIter = 0 + RECV_DATA;
				}else{
					nextIter = current_iteration + 1;
				}
				CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, nextIter);
				netAddReceive(jobs, target, 30000, Communicator.INTERNAL_MPI);
				return jobs;
			}
		}else{
			// transfer data back
			CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, current_iteration + 1 + RECV_DATA);
			netAddSend(jobs, target, new NetworkSimpleMessage(cmd.getSize() + 20),  
					30000, Communicator.INTERNAL_MPI);
			return jobs;
		}
	}

}
