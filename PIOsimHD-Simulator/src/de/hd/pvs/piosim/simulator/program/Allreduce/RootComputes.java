
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
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class RootComputes  
	extends CommandImplementation<Allreduce>
{
	@Override
	public void process(Allreduce cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {

		final int RECV_DATA = 2;
		//trivial implementation, all send to "rank 0" in this communicator which then sends result back. 

		final Integer[] commParts =  cmd.getCommunicator().getParticipatingtRanks().toArray(new Integer[0]);
		
		int rankZero = commParts[0];
		
		if ( client.getModelComponent().getRank() != rankZero){				
			OUTresults.addNetSend(rankZero, new NetworkSimpleMessage(cmd.getSize() + 20),  
					30000, Communicator.INTERNAL_MPI);
			/* wait for incoming data (read data) */			
			OUTresults.addNetReceive(rankZero, 30001, Communicator.INTERNAL_MPI);

			return;
		}else{// rank 0
			if (step == CommandProcessing.STEP_START){
				// receive data from all jobs:
				OUTresults.setNextStep(RECV_DATA);

				for(int i=1; i < commParts.length; i++){
					int rank = commParts[i];

					OUTresults.addNetReceive(rank, 30000, Communicator.INTERNAL_MPI);
				}

			}else{ // send data back:
				for(int i=1; i < commParts.length; i++){
					int rank = commParts[i];
					OUTresults.addNetSend(rank, new NetworkSimpleMessage(cmd.getSize() + 20),  
							30001, Communicator.INTERNAL_MPI);
				}				
			}

			return;
		}
	}

}
