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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase;

/**
 * Simplyfied two phase protocol.
 *
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If all I/Os overlapp i.e. no hole is in the client access then perform Two-Phase:
 *  prepare: combine and sort I/O operations from all clients.
 *    divide the byte range by the number of clients and phases
 *  phases:
 *    read in a maximum of buffersize
 *    transfer data to responsible client(s) (if not self).
 *    receive data from all other clients
 *
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 *
 * Range divided by 3 clients: 00000 111111  22222 2
 * Operations per phase:       01234 012345  01234 5 (the 5 phase is partial, the last client might read partial twoPhaseBufferSize)
 *
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
public class TwoPhase extends MultiPhase<FileIOCommand> {
	/**
	 * This function is called once, when it became clear we will use a multi-phase.
	 * It must set the number of i/o-aggregators used and maxIter
	 * @return the number of phases
	 */
	protected MultiPhaseRun initMultiphasesOnce(final long totalsize, MultiPhaseContainer mp, LinkedList<IOData> iops){
		final MultiPhaseRun mpr = new MultiPhaseRun(totalsize);
		mpr.ioAggregators = this.ioaggregators < 1 ? mp.clients.size() :  ( this.ioaggregators >  mp.clients.size() ?  mp.clients.size() : this.ioaggregators) ;
		mpr.fullPhases = (int) (mpr.totalAccessSize / mpr.ioAggregators / twoPhaseBufferSize);

		final long lastPhaseBytes = mpr.totalAccessSize - mpr.fullPhases *   mpr.ioAggregators * twoPhaseBufferSize;
		// assign the jobs to the clients AND to the phases

		mpr.lastAndPartialPhaseAggregators = (int) ((lastPhaseBytes + twoPhaseBufferSize -1) / twoPhaseBufferSize);

		final long remainderForLastAggregator =  lastPhaseBytes - (mpr.lastAndPartialPhaseAggregators -1) *  twoPhaseBufferSize;

		final int totalPhaseCount = mpr.getPhaseCount();

		//long bytesToPerform = totalsize;
		// this iterator points to the next item to perform.
		//final Iterator<IOData> it = iops.iterator();
		//IOData curIO = it.next();

		final long startOffset = iops.get(0).offset;
		final long endOffset = iops.get(iops.size()-1).size + iops.get(iops.size()-1).offset;

		final long bytesPerClient = mpr.fullPhases * twoPhaseBufferSize;

		final int lastAggregator =  mpr.lastAndPartialPhaseAggregators - 1;

		// assign the I/O jobs to the clients and phases


		// client number
		int c = 0;

		// the amount of data for the last client:
		final long clientsTillPartial = remainderForLastAggregator / twoPhaseBufferSize;

		// we know each client performs the full I/O
		for(MultiPhaseClientOP client: mp.clients){
			// here we must add also normal clients!
			final ClientPhaseOperations phases = mpr.addClientOrAggregator(client.client, totalPhaseCount);

			if ( c > mpr.ioAggregators ){
				break;
			}

			long remainderLastPhase  = (c - lastAggregator) * twoPhaseBufferSize;
			remainderLastPhase =  (remainderLastPhase > 0 ? remainderLastPhase : 0);

			for(int i=0; i < mpr.fullPhases; i++){
				ListIO io = new ListIO();

				// remainder from lastPhase (some clients might do).
				final long offset = startOffset + i * twoPhaseBufferSize + c * bytesPerClient;

				io.addIOOperation(offset + remainderLastPhase , twoPhaseBufferSize);

				phases.setPhase(io, i);
			}


			// perform last phase:

			if(c <= lastAggregator){
				// last aggregator might access partial data.
				final ListIO io = new ListIO();

				// remainder from lastPhase (some clients might do).
				final long offset = startOffset + mpr.fullPhases * twoPhaseBufferSize +	c * bytesPerClient;
				final long size = (c < clientsTillPartial ? twoPhaseBufferSize :  remainderForLastAggregator) ;

				io.addIOOperation(offset + remainderLastPhase, size);

				phases.setPhase(io, mpr.fullPhases);
			}

			c++;
		}
		return mpr;
	}

	/**
	 * This method is invoked, once all clients entered two-phase mode and synchronized virtually.
	 * @param container
	 * @return
	 */
	protected boolean checkMultiPhase(MultiPhaseContainer container){
		// sort the list of the I/O operations by the offset.
		Collections.sort(container.clients, new
			Comparator<MultiPhaseClientOP>() {
				@Override
				public int compare(MultiPhaseClientOP arg0, MultiPhaseClientOP arg1) {
					return arg0.firstByteAccessed < arg1.firstByteAccessed ? -1 :
						arg0.lastByteAccessed < arg1.lastByteAccessed ? -1 : +1; // maybe both starts are identical...
				}
			});

		boolean twoPhase = true;

		// check if the operations overlaps with the current op:
		long lastByteAccessed = container.clients.getFirst().firstByteAccessed;

		for(MultiPhaseClientOP client: container.clients){
			// the real two phase would have checked lastByteAccessed < client.firstByteAccessed ...
			if(lastByteAccessed > client.lastByteAccessed ){
				twoPhase = false;
				break;
			}
			lastByteAccessed = client.lastByteAccessed;
		}

		//System.out.println(this.getClass() + " MultiPhase: " + twoPhase);

		return twoPhase;
	}


	@Override
	public void process(FileIOCommand cmd, CommandProcessing outCommand, GClientProcess client, int step,
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
				return;
			}

			// issue reads for each client:
			final ClientSinglePhaseOperations ops = mp.phaseRun.clientOps.get(client).getCurPhase();
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

			// perform communication, receive data from clients.
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
