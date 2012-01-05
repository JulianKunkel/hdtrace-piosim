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
package de.hd.pvs.piosim.simulator.program.Global;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
import de.hd.pvs.piosim.simulator.program.Filereadall.Splitter.IOSplitter;

/**
 * Simplyfied Multi-phase superclass.
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 *
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
abstract public class MultiPhase<FileCOMMAND extends FileIOCommand> extends CommandImplementation<FileCOMMAND> {
	abstract protected IOSplitter initalizeIOSplitter();

	private IOSplitter splitter = null;


	private IOSplitter getIOSplitter(){
		if(splitter != null){
			return splitter;
		}
		splitter = initalizeIOSplitter();
		return splitter;
	}

	static public class MultiPhaseClientOP{
		final public GClientProcess client;
		final public FileIOCommand cmd;

		final public long firstByteAccessed;
		final public long lastByteAccessed;

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
	static public class IOData{
		public long size;
		public long offset;
	}

	static public class ClientSinglePhaseOperations{
		public final ListIO phaseAggregatorIOOperation;

		/**
		 * Contains the amount of data we need to transfer between aggregator and client in each phase
		 */
		public final HashMap<GClientProcess, Long> aggregatorComm;

		/**
		 * Contains the communication operations per phase, for each client with which aggregator it must transfer data in the given phase.
		 */
		final public HashMap<GClientProcess, Long> clientOps = new HashMap<GClientProcess, Long>();

		public ClientSinglePhaseOperations(ListIO phaseAggregatorIOOperation) {
			this.phaseAggregatorIOOperation = phaseAggregatorIOOperation;

			if(phaseAggregatorIOOperation == null){
				aggregatorComm = null;
			}else{
				aggregatorComm = new HashMap<GClientProcess, Long>();
			}
		}
	}

	/**
	 * Maps the phases to the clients (I/O aggregators) and performed operations per client.
	 * Therefore, we know which operation to perform in each phase.
	 */
	static public class ClientPhaseOperations{
		private int curPhase = -1;

		final public ClientSinglePhaseOperations [] phases;

		public ClientSinglePhaseOperations getCurPhase(){
			return phases[curPhase];
		}

		public void goToNextPhase(){
			curPhase++;

			// skip empty phases
			while(curPhase < phases.length && phases[curPhase] == null ){
				curPhase++;
			}
		}

		public boolean phasesCompleted(){
			assert(curPhase >= 0);
			return curPhase >= phases.length;
		}

		public ClientPhaseOperations(int phaseCount) {
			this.phases = new ClientSinglePhaseOperations[phaseCount];
		}

		public void setPhase( ListIO phaseAggregatorIOOperation, int phase) {
			phases[phase] = new ClientSinglePhaseOperations(phaseAggregatorIOOperation);
		}

		public ClientSinglePhaseOperations getClientPhase(int phase){
			if (phases[phase] == null){
				phases[phase] = new ClientSinglePhaseOperations(null);
			}

			return phases[phase];
		}
	}

	/**
	 * Once the phase is run.
	 * @author julian
	 */
	static public class MultiPhaseRun{
		/**
		 * the number of phases which will be performed
		 */
		public int fullPhases;

		/**
		 * If we need a partial phase, then the number of aggregators here should be > 0
		 */
		public int lastAndPartialPhaseAggregators;

		/**
		 * The number of i/o aggregators we will use from the client list.
		 */
		public int ioAggregators;

		/**
		 * The total amount of data accessed.
		 */
		public final long totalAccessSize;

		public final HashMap<GClientProcess, ClientPhaseOperations> clientOps = new HashMap<GClientProcess, ClientPhaseOperations>();

		public ClientPhaseOperations addClientOrAggregator(GClientProcess client, int phaseCount){
			ClientPhaseOperations c = new ClientPhaseOperations(phaseCount);
			clientOps.put(client, c);
			return c;
		}

		public int getPhaseCount(){
			return fullPhases + (lastAndPartialPhaseAggregators > 0 ? 1 : 0);
		}

		/**
		 * Gives each client a number.
		 */
		//private HashMap<GClientProcess, Integer>            clientRankMapping = new HashMap<GClientProcess, Integer>();

		public MultiPhaseRun(long totalSize) {
			this.totalAccessSize = totalSize;
		}
	}

	static public class MultiPhaseContainer{
		private boolean useMultiPhase;

		public MultiPhaseRun phaseRun;

		public LinkedList<IOData> dataCollectivlyToAccess = null;
		public LinkedList<MultiPhaseClientOP> clients = new LinkedList<MultiPhaseClientOP>();

		public boolean isUseMultiPhase() {
			return useMultiPhase;
		}

		public void addClientCommand(GClientProcess client, FileIOCommand cmd){
			clients.add(new MultiPhaseClientOP(client, cmd));
		}

		/**
		 * This method can be invoked to compute the data accessed by this collection of nodes.
		 */
		public long groupCollectiveData(){
			LinkedList<IOData> tmpList = new LinkedList<IOData>();

			// populate:
			for(MultiPhaseClientOP client: clients){
				for(SingleIOOperation op:  client.cmd.getListIO().getIOOperations()){
					IOData newData = new IOData();
					newData.size = op.getAccessSize();
					newData.offset = op.getOffset();

					tmpList.add(newData);
				}
			}


			// now sort the list based on the offset
			Collections.sort( tmpList, new Comparator<IOData>(){
				@Override
				public int compare(IOData o1, IOData o2) {
					return (o1.offset < o2.offset) ? -1 : ((o1.offset > o2.offset) ? + 1 : 0);
				}
			});

			long totalAccessSize = 0;

			// now combine the operations into the output:
			dataCollectivlyToAccess = new LinkedList<IOData>();

			IOData last = null;

			for(IOData op : tmpList){
				if( last != null){
					// check if we can combine them:
					assert(last.offset <= op.offset);

					long overlap = last.offset + last.size - op.offset;
					if ( overlap >= 0 ){
						// combination possible:
						last.size += op.size - overlap;
						assert(op.size >= overlap);
						assert(last.size >= 0);

					}else{
						// no combination possible: add operation:
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

			assert(totalAccessSize >= 0);

			return totalAccessSize;
		}
	}

	/**
	 * In addition to the synchronize wrapper compare the files as well.
	 */
	static protected class FileIOCommandWrapper{
		final private FileIOCommand command;
		protected MultiPhaseContainer globalPhaseContainer;
		HashMap<GClientProcess, ICommandProcessing> clientsStarted = null;


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
	 * Map the aggregator I/O to corresponding I/O behavior i.e. communication patterns for the aggregator
	 * and the clients.
	 * @param aggregatorIO
	 * @param cAgg
	 * @param cClient
	 */
	protected void computePhaseTransferCommunication(
			LinkedList<MultiPhaseClientOP> clientOps,
			HashMap<GClientProcess, ClientPhaseOperations> phaseOps
			)
	{

		for(GClientProcess aggIO: phaseOps.keySet()){
			final ClientPhaseOperations pop = phaseOps.get(aggIO);

			// determine all clients which need to do this I/O

			int curPhase = 0;
			for (ClientSinglePhaseOperations spops: pop.phases){

				if(spops == null || spops.phaseAggregatorIOOperation == null){
					// we are a regular client
					continue;
				}
				// now we are scanning through one particular phase
				for(SingleIOOperation aggOp: spops.phaseAggregatorIOOperation.getIOOperations()){
					// TODO make this method more efficient by using HashMaps.
					// currently the complexity is about N^2, where N = # IOOPS

					final long aggIOend = aggOp.getAccessSize() + aggOp.getOffset();
					final long aggIOstart = aggOp.getOffset();

					for(MultiPhaseClientOP client: clientOps){
						if (client.client == aggIO){
							// ignore local communication
							continue;
						}

						long totalOverlap = 0;

						for(SingleIOOperation op: client.cmd.getListIO().getIOOperations()){
							// check if the operations overlap:
							long opEnd = op.getAccessSize() + op.getOffset();
							long opStart = op.getOffset();

							if (opEnd <= aggIOstart || opStart >= aggIOend){
								// both operations do not overlap
								continue;
							}

							// 4 cases of overlapping, first is aggOp starts earlier:
							if( aggIOstart > opStart ){
								// aggOp starts earlier
								opStart = aggIOstart;
							}

							if(aggIOend < opEnd){
								opEnd = aggIOend;
							}

							// now we determine the amount of overlap:
							final long overlap = opEnd - opStart;
							assert(overlap > 0);
							totalOverlap += overlap;
						}

						if (totalOverlap > 0){
							// this aggregator performs operations for this client.
							spops.aggregatorComm.put(client.client, totalOverlap);

							// add operations to the client
							final ClientPhaseOperations oldClientsPhaseOps = phaseOps.get(client.client);
							final ClientSinglePhaseOperations cphase = oldClientsPhaseOps.getClientPhase(curPhase);
							cphase.clientOps.put(aggIO, totalOverlap);
						}
					}
				}

				curPhase++;
			}

		}
	}


	/**
	 * @param cmd
	 * @return true if blocked (i.e. sync with further)
	 */
	protected boolean synchronizeClientsWithoutCommunication(ICommandProcessing cmdResults){
		final GClientProcess client = cmdResults.getInvokingComponent();
		final FileIOCommand cmd = (FileIOCommand) cmdResults.getInvokingCommand();
		final FileIOCommandWrapper dummyWrapper = new FileIOCommandWrapper(cmd);

		FileIOCommandWrapper wrapper = sync_blocked_clients.get(dummyWrapper);

		HashMap<GClientProcess, ICommandProcessing> waitingClients;
		if (wrapper == null){
			/* first client waiting */
			waitingClients = new HashMap<GClientProcess, ICommandProcessing>();
			dummyWrapper.clientsStarted = waitingClients;
			sync_blocked_clients.put(dummyWrapper, dummyWrapper);
			dummyWrapper.initMultiPhaseContainer();

			//System.out.println(mpr.fullPhases + " " + mpr.lastAndPartialPhaseAggregators + " " + lastPhaseBytes);

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
//			client.debug("Activate other clients for two phase " + cmd + " by " + client.getIdentifier() );

			/* we finish, therefore reactivate all other clients! */
			for(GClientProcess c: waitingClients.keySet()){
				c.activateBlockedCommand(waitingClients.get(c));
			}

			/* remove Barrier */
			sync_blocked_clients.remove(wrapper);

			// check if multiphase shall be applied
			if(wrapper.globalPhaseContainer.clients.size() > 1){
				wrapper.globalPhaseContainer.useMultiPhase = getIOSplitter().checkMultiPhase(wrapper.globalPhaseContainer);
			}else{
				wrapper.globalPhaseContainer.useMultiPhase = false;
			}


			if(wrapper.globalPhaseContainer.useMultiPhase){
				// initalize structures
				long totalSize = wrapper.globalPhaseContainer.groupCollectiveData();
				assert(totalSize >= 0);
				final MultiPhaseRun mpr = getIOSplitter().initMultiphasesOnce(totalSize, wrapper.globalPhaseContainer, wrapper.globalPhaseContainer.dataCollectivlyToAccess);
				wrapper.globalPhaseContainer.phaseRun = mpr;

				assert(wrapper.globalPhaseContainer.clients.size() == mpr.clientOps.size());

				//final int phaseCount = mpr.getPhaseCount();
				// now add phases for all the non-I/O aggregator clients.
				//for(MultiPhaseClientOP mpcop: wrapper.globalPhaseContainer.clients){
				//	if(! mpr.clientOps.containsKey(mpcop.client)){
				//		mpr.addClientOrAggregator(mpcop.client);
				//	}
				//}


				computePhaseTransferCommunication(wrapper.globalPhaseContainer.clients, mpr.clientOps);

				// for each client used, assign a unique ID
				//int clientNum = 0;
				//for ( GClientProcess clientUsed: wrapper.globalPhaseContainer.phaseRun.phaseOperation.keySet() ){
				//	wrapper.globalPhaseContainer.phaseRun.clientRankMapping.put(client, clientNum);
				//	clientNum++;
				//}
			}

		}else{
			waitingClients.put(client, cmdResults);

			/* just block up */
//			client.debug("Block for " + cmd + " by " + client.getIdentifier() );
			return true;
		}

		return false;
	}

}
