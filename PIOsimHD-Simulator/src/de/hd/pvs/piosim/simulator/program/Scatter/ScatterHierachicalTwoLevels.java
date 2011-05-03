//	Copyright (C) 2011 Julian Kunkel
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

package de.hd.pvs.piosim.simulator.program.Scatter;

import de.hd.pvs.TraceFormat.project.MPICommunicator;
import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


/**
 * Dummy implementation
 *
 * Process 0 sends data to every other even process, those processes send data to the odd processes.
 *
 * @author julian
 *
 */
public class ScatterHierachicalTwoLevels
extends CommandImplementation<Scatter>
{

	/**
	 * State machine has two steps,
	 * in send to neighoor state we transfer data to our even neighbour if necessary
	 */
	final int SEND_TO_NEIGHBOOR_STATE = 2;


	@Override
	public void process(Scatter cmd, CommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		final MPICommunicator comm =  cmd.getCommunicator();

		if(step == CommandProcessing.STEP_START){

			if( myRank == rootRank ){

				// send data to every other host in one step.

				IMessageUserData data = new NetworkSimpleData(30 + cmd.getSize() * 2 ); // TODO FIX if there is no further neighbour...

				// send data to all even hosts (except me)
				for( int rank: comm.getParticipatingRanks() ){
					if(rank != rootRank){
						// send data to odd hosts!

						// the real rank within the communicator! from 0 to sizeof(communicator) - 1
						final int commRank =  comm.getLocalRank(rank);

						if ( commRank % 2 == 0)
							OUTresults.addNetSend(commRank,  data  , 30002 , cmd.getCommunicator());
					}
				}


				final int myCommRank = comm.getLocalRank(myRank) ;

				// if you are root, you could be an odd host, then do not send data!
				if(myCommRank % 2 == 1){
					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}else{
					OUTresults.setNextStep(SEND_TO_NEIGHBOOR_STATE);
				}

				return;

			}else{

				// if you are an even process
				int myCommRank = comm.getLocalRank(myRank) ;

				if( myCommRank % 2 == 0){
					// even
					OUTresults.addNetReceive(rootRank, 30002, cmd.getCommunicator());
					OUTresults.setNextStep(SEND_TO_NEIGHBOOR_STATE);

				}else{
					// odd.
					OUTresults.addNetReceive(comm.getWorldRank( myCommRank - 1 ), 30002, cmd.getCommunicator());

					OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				}

				return;
			}

		}else if (step == SEND_TO_NEIGHBOOR_STATE){
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			// use the old data send by root.

			IMessageUserData data = new NetworkSimpleData(30 + cmd.getSize() );

			final int myCommRank = comm.getLocalRank(myRank) ;

			if( myCommRank < comm.getSize() - 1 ) {
				// do not send to root rank!!!
				int target = comm.getWorldRank( myCommRank + 1 );
				if (target != rootRank)
					OUTresults.addNetSend(target,  data  , 30002 , cmd.getCommunicator());
			}else{
				// you are the last process do not send!
			}

		}else{
			throw new IllegalArgumentException("STEP DOES NOT EXIST!");
		}

	}
}