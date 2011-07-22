//	Copyright (C) 2010 Julian Kunkel
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
package de.hd.pvs.piosim.simulator.program.Filewriteall;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase;

/**
 * Multi-phase write-protocol.
 *
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If all I/Os overlapp i.e. no hole is in the client access then perform any Multi-Phase:
 *  prepare: combine and sort I/O operations from all clients.
 *    divide the byte range by the number of clients and phases
 *  phases:
 *    transfer data to responsible aggregator(s) (if not self). COMMUNICATION_PHASE_SEND if it is an aggregator, use COMMUNICATION_PHASE_SENDRECV_AGGREGATOR.
 *    client reads data in (necessary if data contains holes). PHASE_READ
 *    write data to disk. PHASE_WRITE
 *
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
public abstract class MultiPhaseWrite extends MultiPhase<FileIOCommand> {

	/**
	 * This method asks if we should do a read operation, even if we overwrite the whole data.
	 * @return
	 */
	abstract public boolean avoidUnnecessaryReads();

	@Override
	public void process(FileIOCommand cmd, ICommandProcessing outCommand, GClientProcess client, long step,
			NetworkJobs compNetJobs)
	{
		final int CHECK_TWO_PHASE = 2;

		final int COMMUNICATION_PHASE_SENDRECV_AGGREGATOR = 3;
		final int COMMUNICATION_PHASE_SEND                = 4;
		final int PHASE_READ                              = 5;
		final int PHASE_WRITE                             = 6;
		final int CHECK_STATE                             = 7;

		if(step == CommandProcessing.STEP_START){
			if(cmd.getListIO().getTotalSize() == 0){
				return;
			}

			boolean ret = synchronizeClientsWithoutCommunication(outCommand);

			outCommand.setNextStep(CHECK_TWO_PHASE);

			// gather all ops into the MultiPhaseContainer

			if (ret == true){
				/* just block */
				outCommand.setBlocking();
				return;
			}
			return;
		}else if (step == CHECK_TWO_PHASE){
			final MultiPhaseContainer mp = globalState.get(cmd);

			if( mp == null){
				// we do not need any data.
				assert(cmd.getListIO().getTotalSize() == 0);
				// we finish
				return;
			}

			if(mp.isUseMultiPhase()){
				outCommand.setNextStep(CHECK_STATE);
			}else{
				// don't use two phase, we use the normal write operation.
				Filewrite rd = new Filewrite();
				rd.setFileDescriptor(cmd.getFileDescriptor());
				rd.setListIO(cmd.getListIO());
				outCommand.invokeChildOperation(rd, CommandProcessing.STEP_COMPLETED, null);

				// remove command mapping.
				globalState.remove(cmd);
			}

			return;

		}else if (step == CHECK_STATE){
			final MultiPhaseContainer mp = globalState.get(cmd);
			// we only enter this state, if we are an aggregator
			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);

			cpo.goToNextPhase();

			if (cpo.phasesCompleted()){
				outCommand.setNextStep(CommandProcessing.STEP_COMPLETED);
				globalState.remove(cmd);
				return;
			}

			// issue reads for each client:
			final ClientSinglePhaseOperations spops = cpo.getCurPhase();

			if(spops.phaseAggregatorIOOperation == null){
				// no further I/O operation necessary.
				outCommand.setNextStep(COMMUNICATION_PHASE_SEND);
				return;
			}

			outCommand.setNextStep(COMMUNICATION_PHASE_SENDRECV_AGGREGATOR);

			return;

		}else if (step == COMMUNICATION_PHASE_SENDRECV_AGGREGATOR){
			final MultiPhaseContainer mp = globalState.get(cmd);
			// we only enter this state, if we are an aggregator
			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);
			// issue reads for each client:
			final ClientSinglePhaseOperations spops = cpo.getCurPhase();

			// perform communication, send data to all aggregators from this client.
			if (spops.clientOps != null){
				for(GClientProcess sendToAgg: spops.clientOps.keySet()){
					outCommand.addNetSend(sendToAgg.getModelComponent(), new NetworkSimpleData(spops.clientOps.get(sendToAgg)), 30003, cmd.getCommunicator());
				}
			}

			// perform recvs from clients:
			for(GClientProcess sendTo: spops.aggregatorComm.keySet()){
				outCommand.addNetReceive(sendTo.getModelComponent(), 30003, cmd.getCommunicator());
			}

			outCommand.setNextStep(PHASE_READ);

			return;
		}else if (step == PHASE_READ){
			final MultiPhaseContainer mp = globalState.get(cmd);
			// issue reads for each client:
			final ClientSinglePhaseOperations ops = mp.phaseRun.clientOps.get(client).getCurPhase();

			final ListIO iops = ops.phaseAggregatorIOOperation;
			// check if we should do reads first:

			if( avoidUnnecessaryReads() && ( iops.getFirstAccessedByte() + iops.getTotalSize() == iops.getLastAccessedByte() ) ){
				// no holes at all => no read modify write necessary!!
				outCommand.setNextStep(PHASE_WRITE);
			}else{
				Fileread rd = new Fileread();
				rd.setFileDescriptor(cmd.getFileDescriptor());

				rd.setListIO(iops);
				outCommand.invokeChildOperation(rd, PHASE_WRITE, null);
			}

			return;
		}else if (step == PHASE_WRITE){
			final MultiPhaseContainer mp = globalState.get(cmd);
			// issue reads for each client:
			final ClientSinglePhaseOperations ops = mp.phaseRun.clientOps.get(client).getCurPhase();

			// modification was done => write back:
			Filewrite write = new Filewrite();
			write.setFileDescriptor(cmd.getFileDescriptor());
			write.setListIO(ops.phaseAggregatorIOOperation);

			outCommand.invokeChildOperation(write, CHECK_STATE, null);
			return;

		}else if (step == COMMUNICATION_PHASE_SEND){
			// only a non-aggregator client can reach this step.
			final MultiPhaseContainer mp = globalState.get(cmd);
			// we only enter this state, if we are an aggregator


			final ClientPhaseOperations cpo = mp.phaseRun.clientOps.get(client);

			if (cpo.phasesCompleted()){
				outCommand.setNextStep(CommandProcessing.STEP_COMPLETED);
				globalState.remove(cmd);
				return;
			}

			// issue reads for each client:
			final ClientSinglePhaseOperations spops = cpo.getCurPhase();
			cpo.goToNextPhase();

			// stay in this phase until we completed.
			outCommand.setNextStep(COMMUNICATION_PHASE_SEND);

			// perform communication, send data to all aggregators from this client.
			if (spops.clientOps == null){
				return;
			}
			for(GClientProcess sendToAgg: spops.clientOps.keySet()){
				outCommand.addNetSend(sendToAgg.getModelComponent(), new NetworkSimpleData(spops.clientOps.get(sendToAgg)), 30003,
						cmd.getCommunicator());
			}
			return;
		}

		assert(false);
	}
}
