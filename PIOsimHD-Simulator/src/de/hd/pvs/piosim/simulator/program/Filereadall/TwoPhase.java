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
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filereadall;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Simplyfied two phase protocol.
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If all I/Os overlapp i.e. no hole is in the client access then perform Two-Phase:
 *  prepare: combine and sort I/O operations from all clients.
 *  phases: read in buffersize, transfer data to responsible client (if not self).
 *
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 *
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
public class TwoPhase extends CommandImplementation<Filereadall> {
	final protected long twoPhaseBufferSize = 8388608;

	static protected class MultiPhaseClientOP{
		final GClientProcess client;
		final FileIOCommand cmd;

		final long firstByteAccessed;
		final long lastByteAccessed;

		public MultiPhaseClientOP(GClientProcess client, FileIOCommand cmd) {
			this.cmd = cmd;
			this.client = client;

			// set first and last byte accessed
			firstByteAccessed = cmd.getListIO().getFirstAccessedByte();
			lastByteAccessed = cmd.getListIO().getLastAccessedByte();
		}
	}

	/**
	 * Maps each block range to one or multiple clients which have data in this range.
	 * @author julian
	 */
	static protected class IOData{
		long size;
		long offset;

		// if it is only one, then:
		MultiPhaseClientOP one;

		HashMap<MultiPhaseClientOP, Long> amountOfDataPerProcess = null;
	}

	static protected class MultiPhaseContainer{
		boolean useMultiPhase;

		/**
		 * the current phase
		 */
		int phase = 0;

		int max_phases;

		long totalAccessSize = 0;

		LinkedList<IOData> dataCollectivlyToAccess = null;
		LinkedList<MultiPhaseClientOP> clients = new LinkedList<MultiPhaseClientOP>();

		public void addClientCommand(GClientProcess client, FileIOCommand cmd){
			clients.add(new MultiPhaseClientOP(client, cmd));
		}

		/**
		 * This method can be invoked to compute the data accessed by this collection of nodes.
		 */
		public void groupCollectiveData(){
			LinkedList<IOData> tmpList = new LinkedList<IOData>();

			// populate:
			for(MultiPhaseClientOP client: clients){
				for(SingleIOOperation op:  client.cmd.getListIO().getIOOperations()){
					IOData newData = new IOData();
					newData.size = op.getAccessSize();
					newData.offset = op.getOffset();
					newData.one = client;

					tmpList.add(newData);
				}
			}


			// now sort the list based on the offset
			Collections.sort( tmpList, new Comparator<IOData>(){
				@Override
				public int compare(IOData o1, IOData o2) {
					return (int) (o1.offset - o2.offset);
				}
			});

			// now combine the operations into the output:
			dataCollectivlyToAccess = new LinkedList<IOData>();

			IOData last = null;

			for(IOData op : tmpList){
				if( last != null){
					// check if we can combine them:
					long overlap = last.offset + last.size - op.offset;
					if ( overlap >= 0 ){
						// combination possible:
						last.size += op.size - overlap;

						// add all clients of op:
						assert(op.amountOfDataPerProcess == null);

						if ( last.one != null ){
							assert(last.one != op.one);	// the same client as before (huh?), should not happen.

							last.amountOfDataPerProcess = new HashMap<MultiPhaseClientOP, Long>();
							last.amountOfDataPerProcess.put(last.one, last.size);
							last.one = null;
						}

						Long lastSize = last.amountOfDataPerProcess.get(op.one);
						if(lastSize != null){
							last.amountOfDataPerProcess.put(op.one, op.size + lastSize);
						}else{
							last.amountOfDataPerProcess.put(op.one, op.size);
						}


					}else{
						// no combination possible: add op:
						dataCollectivlyToAccess.add(last);
						totalAccessSize += last.size;
						last = op;
					}
				}else{
					last = op;
				}
			}

			assert(last != null); // should never happen
			dataCollectivlyToAccess.add(last);
			totalAccessSize += last.size;
		}
	}

	/**
	 * In addition to the synchronize wrapper compare the files as well.
	 */
	static protected class FileIOCommandWrapper{
		final private FileIOCommand command;
		protected MultiPhaseContainer globalPhaseContainer;
		HashMap<GClientProcess, CommandProcessing> clientsStarted = null;


		public void initMultiPhaseContainer(){
			globalPhaseContainer = new MultiPhaseContainer();
		}

		public FileIOCommand getCommand() {
			return command;
		}

		public FileIOCommandWrapper(FileIOCommand cmd) {
			command = cmd;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != getClass()){
				return false;
			}

			final FileIOCommandWrapper compare = (FileIOCommandWrapper) obj;

			return (compare.command.getCommunicator() == this.command.getCommunicator()) &&
				(compare.command.getProgram().getApplication() == this.command.getProgram().getApplication())
				&& (compare.command.getClass() == this.command.getClass())
				&& compare.command.getFile() == this.command.getFile();
		}

		@Override
		public int hashCode() {
			return command.getCommunicator().hashCode() + command.getClass().hashCode() + command.getFile().hashCode();
		}
	}

	/**
	 *  Maps the state of one Collective I/O operation to a global state is used to perform a global synchronization as well.
	 */
	protected static HashMap<FileIOCommandWrapper,FileIOCommandWrapper> sync_blocked_clients = new HashMap<FileIOCommandWrapper, FileIOCommandWrapper>();

	/**
	 * Maps all client commands to the global state of the two phase protocol.
	 */
	protected static HashMap<FileIOCommand, MultiPhaseContainer> globalState = new HashMap<FileIOCommand, MultiPhaseContainer>();

	/**
	 * This function is called once, when it became clear we will use a multi-phase.

	 * @return the number of phases
	 */
	protected int initMultiphasesOnce(MultiPhaseContainer mp){
		int maxIter = (int) (mp.totalAccessSize / mp.clients.size() / twoPhaseBufferSize);


		return  maxIter;
	}

	/**
	 * @param cmd
	 * @return true if blocked (i.e. sync with further)
	 */
	protected boolean synchronizeClientsWithoutCommunication(CommandProcessing cmdResults){
		final GClientProcess client = cmdResults.getInvokingComponent();
		final FileIOCommand cmd = (FileIOCommand) cmdResults.getInvokingCommand();
		final FileIOCommandWrapper dummyWrapper = new FileIOCommandWrapper(cmd);

		FileIOCommandWrapper wrapper = sync_blocked_clients.get(dummyWrapper);

		HashMap<GClientProcess, CommandProcessing> waitingClients;
		if (wrapper == null){
			/* first client waiting */
			waitingClients = new HashMap<GClientProcess, CommandProcessing>();
			dummyWrapper.clientsStarted = waitingClients;
			sync_blocked_clients.put(dummyWrapper, dummyWrapper);
			dummyWrapper.initMultiPhaseContainer();

			wrapper = dummyWrapper;
		}else{
			waitingClients = wrapper.clientsStarted;
		}

		if(cmd.getListIO().getTotalSize() != 0){
			// then we will participate in I/O
			wrapper.globalPhaseContainer.addClientCommand(client, cmd);
			globalState.put(cmd, wrapper.globalPhaseContainer);
		}

		if (waitingClients.size() == cmd.getCommunicator().getSize() -1){
			client.debug("Activate other clients for two phase " + cmd + " by " + client.getIdentifier() );

			/* we finish, therefore reactivate all other clients! */
			for(GClientProcess c: waitingClients.keySet()){
				c.activateBlockedCommand(waitingClients.get(c));
			}

			/* remove Barrier */
			sync_blocked_clients.remove(wrapper);

			// check if multiphase shall be applied
			if(wrapper.globalPhaseContainer.clients.size() > 1){
				wrapper.globalPhaseContainer.useMultiPhase = checkMultiPhase(wrapper.globalPhaseContainer);
			}else{
				wrapper.globalPhaseContainer.useMultiPhase = false;
			}

			// now group operations:
			if(wrapper.globalPhaseContainer.useMultiPhase){
				wrapper.globalPhaseContainer.groupCollectiveData();

				wrapper.globalPhaseContainer.max_phases = initMultiphasesOnce(wrapper.globalPhaseContainer);
			}

		}else{
			waitingClients.put(client, cmdResults);

			/* just block up */
			client.debug("Block for " + cmd + " by " + client.getIdentifier() );
			return true;
		}

		return false;
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
	public void process(Filereadall cmd, CommandProcessing outCommand, GClientProcess client, int step,
			NetworkJobs compNetJobs)
	{
		final int CHECK_TWO_PHASE = 2;
		final int READ_PHASE = 3;
		final int COMMUNICATION_PHASE = 4;

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

			boolean useTwoPhase = mp.useMultiPhase;

			if(useTwoPhase){
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

			System.out.println(mp.totalAccessSize);

//			for (IOData data:  mp.dataCollectivlyToAccess){
//				System.out.println(data.size + " " + data.offset);
//				for(MultiPhaseClientOP cop: data.amountOfDataPerProcess.keySet()){
//					System.out.println( cop.client.getIdentifier() + " " + data.amountOfDataPerProcess.get(cop));
//				}
//			}

			return;

		}case (COMMUNICATION_PHASE):{

			return;

		}
		}

		assert(false);
	}
}
