package de.hd.pvs.piosim.simulator.program.Bcast;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class BinaryTree 
extends CommandImplementation<Bcast>
{

	@Override
	public void process(Bcast cmd, CommandProcessing OUTresults,
			GClientProcess client, int step, NetworkJobs compNetJobs) 
	{
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
		
		boolean recv = false;
		
		while(current_iteration < iterations){
			mask = 1 << current_iteration;
			recv = (clientRankInComm & mask) > 0;
			if ((clientRankInComm | mask) <= lastRank)
				break;
			current_iteration++;
		}
		
		if(current_iteration == iterations){
			return; // no more work to do
		}
		
		int partner = -1;
		if( recv ){			
			//determine lower one
			partner = clientRankInComm & ~(mask);
			assert(partner >= 0);			
		}else{			
			//determine higher one
			partner = clientRankInComm | (mask);
			assert(partner >= 0);
		}
		
		int targetRank = cmd.getCommunicator().getWorldRank(partner);
		
		if(recv){
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			OUTresults.addNetReceive(targetRank,   
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
			OUTresults.addNetSend(targetRank,
					new NetworkSimpleMessage(cmd.getSize() + 20), 30000, Communicator.INTERNAL_MPI);
			return;
		}
	}
	
}
