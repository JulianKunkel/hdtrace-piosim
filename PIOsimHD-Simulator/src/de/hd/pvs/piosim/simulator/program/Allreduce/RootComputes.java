
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


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
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementationWithCommunicatorLocalRanks;

public class RootComputes
	extends CommandImplementationWithCommunicatorLocalRanks<Allreduce>
{
	final int ROOT_RECVD_ALLDATA = 2;
	final int DATA_WAS_SEND = 3;

	@Override
	public long getInstructionCount(Allreduce cmd, GClientProcess client, long step) {
		if(step == ROOT_RECVD_ALLDATA){
			return cmd.getCommunicator().getSize() * cmd.getSize() + 1;
		}else{
			return 1;
		}
	}

	@Override
	public void processWithLocalRanks(Allreduce cmd, ICommandProcessingMapped OUTresults, Communicator comm,  int clientRankInComm, GClientProcess client, long step,	NetworkJobs compNetJobs) {

		//trivial implementation, all send to "rank 0" in this communicator which then sends result back.
		if ( clientRankInComm != 0){

			if( step == CommandProcessing.STEP_START){
				// wait for an initial ACKnowledge message before data is transferred to ensure receiver is ready.
				OUTresults.addNetReceive(0, 30002, cmd.getCommunicator());
				OUTresults.setNextStep(DATA_WAS_SEND);
			}else if (step == DATA_WAS_SEND){
			   OUTresults.addNetSend(0, new NetworkSimpleData(cmd.getSize() + 20), 	30000, cmd.getCommunicator());
				/* wait for incoming data (read data) */
			   OUTresults.addNetReceive(0, 30001, cmd.getCommunicator());
			}

			return;
		}else{// rank 0
			if (step == CommandProcessing.STEP_START){
				// receive data from all jobs:
				OUTresults.setNextStep(DATA_WAS_SEND);

				for(int rank=1; rank < comm.getSize(); rank++){
					OUTresults.addNetSend(rank, new NetworkSimpleData(20), 30002, cmd.getCommunicator());
				}
			}else if(step == DATA_WAS_SEND){
				// receive data from all jobs:
				OUTresults.setNextStep(ROOT_RECVD_ALLDATA);

				for(int rank=1; rank < comm.getSize(); rank++){
					OUTresults.addNetReceive(rank, 30000, cmd.getCommunicator());
				}
			}else{ // send data back:
				for(int rank=1; rank < comm.getSize(); rank++){
					OUTresults.addNetSend(rank, new NetworkSimpleData(cmd.getSize() + 20),	30001, cmd.getCommunicator());
				}
			}

			return;
		}
	}

}
