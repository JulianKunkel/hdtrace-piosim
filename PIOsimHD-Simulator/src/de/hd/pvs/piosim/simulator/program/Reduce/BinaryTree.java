
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

package de.hd.pvs.piosim.simulator.program.Reduce;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Complex Binary Tree Algorithm, Root collects.
 * In the algorithm, first the sender waits for acceptance of data. The receiver sends an request for data if there is more data than getMaxEagerSendSize.
 * This is necessary to ensure that buffers are reserved.
 *  
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

		final int RENDEVOUZ = 10;

		if (step == CommandProcessing.STEP_START){
			long eagerSize = client.getSimulator().getModel().getGlobalSettings().getMaxEagerSendSize();
			if (cmd.getSize() <= eagerSize){
				// run the eager protocoll:
				OUTresults.invokeChildOperation(cmd, CommandProcessing.STEP_COMPLETED, 
						de.hd.pvs.piosim.simulator.program.Reduce.BinaryTreeSimple.class);
				return;
			}

			// not eager
			OUTresults.setNextStep(RENDEVOUZ);

			//wait for receives from all "to receive data"			
			final int myRank = client.getModelComponent().getRank();
			final int rootRank = cmd.getRootRank();

			int clientRankInComm = myRank;

			//exchange rank 0 with cmd.root to receive data on the correct node
			if(clientRankInComm == cmd.getRootRank()) {
				clientRankInComm = 0;
			}else if(clientRankInComm == 0) {
				clientRankInComm = rootRank;
			}

			final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);

			final int commSize = cmd.getCommunicator().getSize();
			final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
			final int phaseStart = iterations - trailingZeros;
			
			if(clientRankInComm != 0){				
				int sendTo = (clientRankInComm ^ 1<<trailingZeros);

				if(sendTo == 0){
					sendTo = rootRank;
				}else if(sendTo == rootRank){
					sendTo = 0;
				}


				//System.out.println(myRank + " phaseStart: " + phaseStart +" tz:" + trailingZeros + " send to: " +  sendTo);

				// send confirmation to all clients sending data to me. 

				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					final int targetRank = (1<<iter | clientRankInComm);
					if (targetRank >= commSize) 
						continue;
					OUTresults.addNetSend(((targetRank != rootRank) ? targetRank : 0), 
							new NetworkSimpleMessage(20), // just 20 Bytes or something.
							30002, Communicator.INTERNAL_MPI);
				}


				// block until we get a confirmation from the target that we can send.

				OUTresults.addNetReceive(sendTo, 
						30002, Communicator.INTERNAL_MPI);			
			}else{
				// send to all receivers that we accept data.				
				for (int iter = iterations-1 ; iter >= 0 ; iter--){
					final int targetRank =  1<<iter; 
					//System.out.println(myRank +" from " + ((targetRank != rootRank) ? targetRank : 0) );
					OUTresults.addNetSend( (targetRank != rootRank) ? targetRank : 0, new NetworkSimpleMessage(20), 30002, Communicator.INTERNAL_MPI);
				}
			}


		}else if(step == RENDEVOUZ){	
			OUTresults.invokeChildOperation(cmd, CommandProcessing.STEP_COMPLETED, 
					de.hd.pvs.piosim.simulator.program.Reduce.BinaryTreeSimple.class);
		}

	}

}
