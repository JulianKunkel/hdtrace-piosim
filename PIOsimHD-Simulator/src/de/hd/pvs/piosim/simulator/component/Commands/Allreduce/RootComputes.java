
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

public class RootComputes  
	extends CommandImplementation<Allreduce>
{
	@Override
	public CommandStepResults process(Allreduce cmd, GClientProcess client, int step, NetworkJobs compNetJobs) {

		final int RECV_DATA = 2;
		//trivial implementierung, alle schicken zu rank 0, dieser schickt an alle.

		int [] commParts =  cmd.getCommunicator().getParticipantsWorldRank();

		if ( client.getModelComponent().getRank() != 0){
			CommandStepResults jobs = prepareStepResultsForJobs(client, cmd, STEP_COMPLETED);	
			ISNodeHostedComponent target = getTargetfromRank(client,  commParts[0] );

			netAddSend(jobs, target, new NetworkSimpleMessage(cmd.getSize() + 20),  
					30000, Communicator.INTERNAL_MPI);

			/* wait for incoming data (read data) */
			netAddReceive(jobs, target, 30001, Communicator.INTERNAL_MPI);

			return jobs;
		}else{// rank 0

			CommandStepResults jobs;
			if (step == 0){
				// receive data from all jobs:
				jobs = prepareStepResultsForJobs(client, cmd, RECV_DATA);

				for(int i=1; i < commParts.length; i++){
					int rank = commParts[i];

					ISNodeHostedComponent target = getTargetfromRank(client,  rank );
					netAddReceive(jobs, target, 30000, Communicator.INTERNAL_MPI);
				}

			}else{ // send data back:
				jobs = prepareStepResultsForJobs(compNetJobs, STEP_COMPLETED);

				for(int i=1; i < commParts.length; i++){
					int rank = commParts[i];

					ISNodeHostedComponent target = getTargetfromRank(client,  rank );
					netAddSend(jobs, target, new NetworkSimpleMessage(cmd.getSize() + 20),  
							30001, Communicator.INTERNAL_MPI);
				}				
			}

			return jobs;
		}
	}

}
