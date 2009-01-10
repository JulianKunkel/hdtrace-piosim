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
		final int RECEIVED = 2;
		
		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize();
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		int clientRankInComm = cmd.getCommunicator().getCommRank(client.getModelComponent().getRank());

		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == cmd.getRootRank()) {
			clientRankInComm = 0;
		}else if(clientRankInComm == 0) {
			clientRankInComm = cmd.getRootRank();
		}
		
		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
		final int phaseStart = iterations - trailingZeros;
		
	
		
		if(clientRankInComm != 0){				
			// recv first, then send.
			//System.out.println(clientRankInComm + " phaseStart: " + phaseStart +" tz:" + trailingZeros + " rcv from: " + 
			//		(clientRankInComm ^ 1<<trailingZeros));
			
			if (step == CommandProcessing.STEP_START){
				OUTresults.setNextStep(RECEIVED);
				
				OUTresults.addNetReceive((clientRankInComm ^ 1<<trailingZeros),   
						30000, Communicator.INTERNAL_MPI);
				
			}else if(step == RECEIVED){
				// send
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				
				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					int target = (1<<iter | clientRankInComm);
					if (target >= commSize) continue;
					//System.out.println(clientRankInComm +" to " + (1<<iter | clientRankInComm) );
					OUTresults.addNetSend(target,
							new NetworkSimpleMessage(cmd.getSize() + 20), 30000, Communicator.INTERNAL_MPI);
				}
			}
		}else{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			
			// send to all receivers				
			for (int iter = iterations-1 ; iter >= 0 ; iter--){				
				//System.out.println(clientRankInComm +" to " + (1<<iter | clientRankInComm) );
				OUTresults.addNetSend((1<<iter | clientRankInComm),
						new NetworkSimpleMessage(cmd.getSize() + 20), 30000, Communicator.INTERNAL_MPI);
			}
		}
	}

}
