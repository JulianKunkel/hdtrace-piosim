/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2010 Julian Kunkel
//          based on the version of Michael Kuhn in 2009.
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
package de.hd.pvs.piosim.simulator.program.Filereadall;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase;

/**
 * Multi-phase protocol abstract class.
 *
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If all I/Os overlapp i.e. no hole is in the client access then perform any Multi-Phase:
 *  prepare: combine and sort I/O operations from all clients.
 *    divide the byte range by the number of clients and phases
 *  phases:
 *    read in a maximum of buffersize
 *    transfer data to responsible client(s) (if not self).
 *    receive data from all other clients
 *
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
public abstract class MultiPhaseRead extends MultiPhase<FileIOCommand> {

	@Override
	public void process(FileIOCommand cmd, CommandProcessing outCommand, GClientProcess client, long step,
			NetworkJobs compNetJobs)
	{
		final int CHECK_TWO_PHASE = 2;
		final int READ_PHASE = 3;
		final int COMMUNICATION_PHASE_SEND_RECV = 4;
		final int COMMUNICATION_PHASE_RECV      = 5;

		switch (step) {
		case (CommandProcessing.STEP_START): {
			boolean ret = synchronizeClientsWithoutCommunication(outCommand);

			outCommand.setNextStep(CHECK_TWO_PHASE);

			// gather all ops into the MultiPhaseContainer

			if (ret == true){
				/* just block */
				outCommand.setBlocking();
				return;
			}
			return;
		}case (CHECK_TWO_PHASE): {
			final MultiPhaseContainer mp = globalState.get(cmd);

			if( mp == null){
				// we do not need any data.
				assert(cmd.getListIO().getTotalSize() == 0);
				// we finish
				return;
			}

			if(mp.isUseMultiPhase()){
				// go to the first phase:
				mp.phaseRun.clientOps.get(client).goToNextPhase();
				outCommand.setNextStep(READ_PHASE);
			}else{
				// don't use two phase, we use the normal read operation.
				Fileread rd = new Fileread();
				rd.setFileDescriptor(cmd.getFileDescriptor());
				rd.setListIO(cmd.getListIO());
				outCommand.invokeChildOperation(rd, CommandProcessing.STEP_COMPLETED, null);

				// remove command mapping.
				globalState.remove(cmd);
			}

			return;

		}case (READ_PHASE):{
			final MultiPhaseContainer mp = globalState.get(cmd);
			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);

			if (cpo.phasesCompleted()){
				outCommand.setNextStep(CommandProcessing.STEP_COMPLETED);
				globalState.remove(cmd);
				return;
			}

			// issue reads for each client:
			final ClientSinglePhaseOperations ops = cpo.getCurPhase();
			if(ops.phaseAggregatorIOOperation == null){
				// no further ops => recv if necessary:
				outCommand.setNextStep(COMMUNICATION_PHASE_RECV);
				return;
			}

			// otherwise perform I/O.
			Fileread rd = new Fileread();
			rd.setFileDescriptor(cmd.getFileDescriptor());
			rd.setListIO(ops.phaseAggregatorIOOperation);

			outCommand.invokeChildOperation(rd, COMMUNICATION_PHASE_SEND_RECV, null);

//			for (IOData data:  mp.dataCollectivlyToAccess){
//				System.out.println(data.size + " " + data.offset);
//				for(MultiPhaseClientOP cop: data.amountOfDataPerProcess.keySet()){
//					System.out.println( cop.client.getIdentifier() + " " + data.amountOfDataPerProcess.get(cop));
//				}
//			}

			return;

		}case (COMMUNICATION_PHASE_RECV):{
			// if we enter this stage, then we are only a client, and no I/O aggregator

			final MultiPhaseContainer mp = globalState.get(cmd);
			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);

			if (cpo.phasesCompleted()){
				outCommand.setNextStep(CommandProcessing.STEP_COMPLETED);
				globalState.remove(cmd);
				return;
			}

			// issue reads for each client:
			final ClientSinglePhaseOperations spops = mp.phaseRun.clientOps.get(client).getCurPhase();
			cpo.goToNextPhase();

			outCommand.setNextStep(COMMUNICATION_PHASE_RECV);

			if(spops.clientOps == null){
				return;
			}

			//we might not have receive operations for some states.
			// initalize receive operations
			for(GClientProcess recvFromAggregator: spops.clientOps.keySet()){
				outCommand.addNetReceive(recvFromAggregator.getModelComponent(), 30004, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
			}

			return;
		}case (COMMUNICATION_PHASE_SEND_RECV):{
			final MultiPhaseContainer mp = globalState.get(cmd);
			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);

			// we only enter this state, if we had a read phase first
			assert(!cpo.phasesCompleted());

			// issue reads for each client:
			final ClientSinglePhaseOperations spops = mp.phaseRun.clientOps.get(client).getCurPhase();
			cpo.goToNextPhase();

			// perform communication, receive my data from aggregators.
			if (spops.clientOps != null){
				for(GClientProcess recvFromAggregator: spops.clientOps.keySet()){
					outCommand.addNetReceive(recvFromAggregator.getModelComponent(), 30004, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
				}
			}
			// perform sends:
			for(GClientProcess sendTo: spops.aggregatorComm.keySet()){
				outCommand.addNetSend(sendTo.getModelComponent(),
						new NetworkSimpleData(spops.aggregatorComm.get(sendTo)), 30004, Communicator.INTERNAL_MPI);
			}

			if(! cpo.phasesCompleted()){
				// do we have further read operations?
				if(cpo.getCurPhase().aggregatorComm == null){
					outCommand.setNextStep(COMMUNICATION_PHASE_RECV);
				}else{
					outCommand.setNextStep(READ_PHASE);
				}
			} // else we finished!


			return;
		}
		}

		assert(false);
	}
}
