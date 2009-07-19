/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2009 Michael Kuhn
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Allgather;
import de.hd.pvs.piosim.model.program.commands.Filereadall;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class TwoPhase extends CommandImplementation<Filereadall> {
	Communicator myComm = new Communicator("TWO-Phase");
	final long twoPhaseBufferSize = 1 * 1024 * 1024;

	final class FilereadallWrapper {
		private Filereadall command;

		public Filereadall getCommand() {
			return command;
		}

		public FilereadallWrapper(Filereadall cmd) {
			command = cmd;
		}

		/**
		 * Used to determine the right Communicator command. Different clients using
		 * the same command e.g. a particular barrier. It does not test class
		 * specific parameters for instance tags.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != getClass()) {
				return false;
			}

			FilereadallWrapper compare = (FilereadallWrapper) obj;

			return (compare.command.getCommunicator() == this.command.getCommunicator())
					&& (compare.command.getProgram().getApplication() == this.command.getProgram().getApplication())
					&& (compare.command.getClass() == this.command.getClass());
		}

		@Override
		public int hashCode() {
			return command.getCommunicator().hashCode() + command.getClass().hashCode();
		}
	}

	final class FilereadallContainer {
		private Filereadall command;
		private GClientProcess clientProcess;
		private CommandProcessing commandProcessing;

		private long twoPhaseOffset;
		private long twoPhaseSize;

		private ListIO listIO;

		public FilereadallContainer(Filereadall command, GClientProcess clientProcess, CommandProcessing commandProcessing) {
			this.command = command;
			this.clientProcess = clientProcess;
			this.commandProcessing = commandProcessing;

			twoPhaseOffset = -1;
			twoPhaseSize = -1;

			listIO = null;
		}

		public Filereadall getCommand() {
			return command;
		}

		public GClientProcess getClientProcess() {
			return clientProcess;
		}

		public CommandProcessing getCommandProcessing() {
			return commandProcessing;
		}

		public Integer getRank() {
			return clientProcess.getModelComponent().getRank();
		}

		public long getTwoPhaseOffset() {
			return twoPhaseOffset;
		}

		public void setTwoPhaseOffset(long twoPhaseOffset) {
			this.twoPhaseOffset = twoPhaseOffset;
		}

		public long getTwoPhaseSize() {
			return twoPhaseSize;
		}

		public void setTwoPhaseSize(long twoPhaseSize) {
			this.twoPhaseSize = twoPhaseSize;
		}

		public ListIO getListIO() {
			return listIO;
		}

		public void setListIO(ListIO listIO) {
			this.listIO = listIO;
		}
	}

	final class FilereadallContainerComparator implements Comparator<FilereadallContainer> {
		public int compare(FilereadallContainer a, FilereadallContainer b) {
			return a.getRank().compareTo(b.getRank());
		}
	}

	private static HashMap<FilereadallWrapper, List<FilereadallContainer>> sync_blocked_clients = new HashMap<FilereadallWrapper, List<FilereadallContainer>>();
	private static HashMap<Filereadall, List<FilereadallContainer>> xxx = new HashMap<Filereadall, List<FilereadallContainer>>();

	@Override
	public void process(Filereadall cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		final int SEND_REQUEST = 2;
		final int CHECK_TWO_PHASE = 3;
		final int TWO_PHASE = 4;
		final int TWO_PHASE_RECV = 5;

		switch (step) {
		case (CommandProcessing.STEP_START): {
			FilereadallWrapper wrapper = new FilereadallWrapper(cmd);
			List<FilereadallContainer> waitingClients = sync_blocked_clients.get(wrapper);
			FilereadallContainer container = new FilereadallContainer(cmd, client, OUTresults);

			if (waitingClients == null) {
				/* first client waiting */
				waitingClients = new ArrayList<FilereadallContainer>();
				sync_blocked_clients.put(wrapper, waitingClients);
			}

			waitingClients.add(container);

			if (waitingClients.size() < cmd.getCommunicator().getSize()) {
				/* just block up */
				client.debug("Block for " + cmd + " by " + client.getIdentifier());
				OUTresults.setBlocking();
			} else {
				List<FilereadallContainer> cmds = new ArrayList<FilereadallContainer>();

				for (FilereadallContainer c : waitingClients) {
					cmds.add(c);
				}

				Collections.sort(cmds, new FilereadallContainerComparator());

				for (FilereadallContainer c : waitingClients) {
					xxx.put(c.getCommand(), cmds);
				}

				waitingClients.remove(container);

				client.debug("Activate other clients for barrier " + cmd + " by " + client.getIdentifier());

				/* we finish, therefore reactivate all other clients! */
				for (FilereadallContainer c : waitingClients) {
					c.getClientProcess().activateBlockedCommand(c.getCommandProcessing());
				}

				/* remove Barrier */
				sync_blocked_clients.remove(wrapper);
			}

			OUTresults.setNextStep(CHECK_TWO_PHASE);

			return;
		}
		case (CHECK_TWO_PHASE): {
			boolean twoPhase = false;

			Allgather gather = new Allgather();
			gather.setCommunicator(cmd.getCommunicator());
			// Two 64 bit offsets
			gather.setSize(16);

			OUTresults.invokeChildOperation(gather, CommandProcessing.STEP_START, null);

			for (int i = 1; i < xxx.get(cmd).size(); i++) {
				if (xxx.get(cmd).get(i).getCommand().getStartOffset() <= xxx.get(cmd).get(i - 1).getCommand().getEndOffset()) {
					twoPhase = true;
				}
			}

			if (twoPhase) {
				OUTresults.setNextStep(TWO_PHASE);
			} else {
				OUTresults.invokeChildOperation(cmd, SEND_REQUEST, de.hd.pvs.piosim.simulator.program.Filereadall.Direct.class);
				xxx.remove(cmd);
			}

			return;
		}
		case (TWO_PHASE): {
			long minOffset = xxx.get(cmd).get(0).getCommand().getStartOffset();
			long maxOffset = xxx.get(cmd).get(0).getCommand().getEndOffset();
			long perRank;
			long myOffset;
			long mySize;
			long nextOffset;
			int myIndex = -1;
			FilereadallContainer myContainer = null;

			for (FilereadallContainer c : xxx.get(cmd)) {
				minOffset = Math.min(minOffset, c.getCommand().getStartOffset());
				maxOffset = Math.max(maxOffset, c.getCommand().getEndOffset());

				if (c.getCommand() == cmd) {
					myIndex = xxx.get(cmd).indexOf(c);
					myContainer = c;
				}
			}

			assert(myIndex >= 0);
			assert(myContainer != null);

			// FIXME Alltoall
			FilereadallWrapper wrapper = new FilereadallWrapper(cmd);
			List<FilereadallContainer> waitingClients = sync_blocked_clients.get(wrapper);

			if (waitingClients == null) {
				waitingClients = new ArrayList<FilereadallContainer>();
				sync_blocked_clients.put(wrapper, waitingClients);
			}

			waitingClients.add(myContainer);

			if (waitingClients.size() < cmd.getCommunicator().getSize()) {
				OUTresults.setBlocking();
			} else {
				waitingClients.remove(myContainer);

				for (FilereadallContainer c : waitingClients) {
					c.getClientProcess().activateBlockedCommand(c.getCommandProcessing());
				}

				sync_blocked_clients.remove(wrapper);
			}

			perRank = (maxOffset - minOffset) / xxx.get(cmd).size();
			myOffset = minOffset + (myIndex * perRank);
			nextOffset = Math.min(minOffset + ((myIndex + 1) * perRank), maxOffset);
			mySize = Math.min(twoPhaseBufferSize, nextOffset - myOffset);

			System.out.println("min " + minOffset + " max " + maxOffset);
			System.out.println("myOffset " + myOffset);
			System.out.println("mySize " + mySize);

			if (myContainer.getTwoPhaseOffset() < 0) {
				myContainer.setTwoPhaseOffset(myOffset);
				myContainer.setTwoPhaseSize(mySize);
			} else {
				myContainer.setTwoPhaseOffset(myContainer.getTwoPhaseOffset() + myContainer.getTwoPhaseSize());
				myContainer.setTwoPhaseSize(mySize);
			}

			myContainer.setListIO(cmd.getIOList().getPartition(myContainer.getTwoPhaseOffset(), myContainer.getTwoPhaseSize()));

			System.out.println("twoPhaseOffset " + myContainer.getTwoPhaseOffset());
			System.out.println("twoPhaseSize " + myContainer.getTwoPhaseSize());

			if (myContainer.getTwoPhaseOffset() < nextOffset) {
				ListIO list = new ListIO();
				HashMap<Server, ListIO> servers;

				list.addIOOperation(myContainer.getTwoPhaseOffset(), myContainer.getTwoPhaseSize());
				servers = cmd.getFile().getDistribution().distributeIOOperation(list, client.getSimulator().getModel().getServers());

				for (Server server : servers.keySet()) {
					IGServer sserver = (IGServer)client.getSimulator().getSimulatedComponent(server);
					ISNodeHostedComponent targetNIC = sserver;
					ListIO iolist = servers.get(server);

					for (SingleIOOperation op : iolist.getIOOperations()) {
						System.out.println(server + ": " + op);
					}

					OUTresults.addNetSend(targetNIC, new RequestRead(iolist, cmd.getFile()), RequestIO.INITIAL_REQUEST_TAG, Communicator.IOSERVERS);
					OUTresults.addNetReceive(targetNIC, RequestIO.IO_DATA_TAG, Communicator.IOSERVERS);
				}

				OUTresults.setNextStep(TWO_PHASE_RECV);
			} else {
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				xxx.remove(cmd);
			}

			return;
		}
		case (TWO_PHASE_RECV): {
			long minOffset = xxx.get(cmd).get(0).getCommand().getStartOffset();
			long maxOffset = xxx.get(cmd).get(0).getCommand().getEndOffset();
			long perRank;
			long myOffset;
			int myIndex = -1;
			FilereadallContainer myContainer = null;

			for (FilereadallContainer c : xxx.get(cmd)) {
				minOffset = Math.min(minOffset, c.getCommand().getStartOffset());
				maxOffset = Math.max(maxOffset, c.getCommand().getEndOffset());

				if (c.getCommand() == cmd) {
					myIndex = xxx.get(cmd).indexOf(c);
					myContainer = c;
				}
			}

			assert(myIndex >= 0);
			assert(myContainer != null);

			perRank = (maxOffset - minOffset) / xxx.get(cmd).size();
			myOffset = minOffset + (myIndex * perRank);

			System.out.println("rank " + myContainer.getRank());

			for (FilereadallContainer c : xxx.get(cmd)) {
				long theirOffset;

				if (c.getRank() == myContainer.getRank()) {
					continue;
				}

				theirOffset = minOffset + (xxx.get(cmd).indexOf(c) * perRank);

				if (c.getCommand().getStartOffset() < myOffset + perRank && c.getCommand().getEndOffset() > myOffset) {
					System.out.println("send to " + c.getRank());
					// FIXME
					OUTresults.addNetSend(c.getRank(), new NetworkSimpleMessage(c.getListIO().getTotalSize() + 20), 50000, Communicator.INTERNAL_MPI);
				}

				if (cmd.getStartOffset() < theirOffset + perRank && cmd.getEndOffset() > theirOffset) {
					System.out.println("recv from " + c.getRank());
					OUTresults.addNetReceive(c.getRank(), 50000, Communicator.INTERNAL_MPI);
				}
			}

			OUTresults.setNextStep(TWO_PHASE);

			return;
		}
		}

		return;
	}

}
