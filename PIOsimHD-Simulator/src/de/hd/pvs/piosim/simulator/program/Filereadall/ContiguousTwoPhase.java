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

public class ContiguousTwoPhase extends CommandImplementation<Filereadall> {
	final long twoPhaseBufferSize = 5 * 1024 * 1024;

	final class FilereadallWrapper {
		private Filereadall command;

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

		private long twoPhaseIteration;

		private ListIO listIO;

		public FilereadallContainer(Filereadall command, GClientProcess clientProcess, CommandProcessing commandProcessing) {
			this.command = command;
			this.clientProcess = clientProcess;
			this.commandProcessing = commandProcessing;

			twoPhaseIteration = 0;

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

		public long getTwoPhaseIteration() {
			return twoPhaseIteration;
		}

		public void setTwoPhaseIteration(long twoPhaseIteration) {
			this.twoPhaseIteration = twoPhaseIteration;
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
	private static HashMap<Filereadall, List<FilereadallContainer>> meta_info = new HashMap<Filereadall, List<FilereadallContainer>>();

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
					meta_info.put(c.getCommand(), cmds);
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

			for (int i = 1; i < meta_info.get(cmd).size(); i++) {
				if (meta_info.get(cmd).get(i).getCommand().getStartOffset() <= meta_info.get(cmd).get(i - 1).getCommand().getEndOffset()) {
					twoPhase = true;
				}
			}

			if (twoPhase) {
				OUTresults.setNextStep(TWO_PHASE);
			} else {
				OUTresults.invokeChildOperation(cmd, SEND_REQUEST, de.hd.pvs.piosim.simulator.program.Filereadall.Direct.class);
				meta_info.remove(cmd);
			}

			return;
		}
		case (TWO_PHASE): {
			long minOffset = meta_info.get(cmd).get(0).getCommand().getStartOffset();
			long maxOffset = meta_info.get(cmd).get(0).getCommand().getEndOffset();
			long myOffset;
			long mySize;
			int myIndex = -1;
			FilereadallContainer myContainer = null;

			for (FilereadallContainer c : meta_info.get(cmd)) {
				minOffset = Math.min(minOffset, c.getCommand().getStartOffset());
				maxOffset = Math.max(maxOffset, c.getCommand().getEndOffset());

				if (c.getCommand() == cmd) {
					myIndex = meta_info.get(cmd).indexOf(c);
					myContainer = c;
				}
			}

			assert(myIndex >= 0);
			assert(myContainer != null);

			// FIXME Alltoall

			myOffset = minOffset + ((myContainer.getTwoPhaseIteration() * meta_info.get(cmd).size()) + myIndex) * twoPhaseBufferSize;
			mySize = Math.min(twoPhaseBufferSize, maxOffset - myOffset);
			mySize = Math.max(mySize, 0);

			System.out.println("min " + minOffset + " max " + maxOffset);
			System.out.println("myOffset " + myOffset);
			System.out.println("mySize " + mySize);

			myContainer.setListIO(cmd.getIOList().getPartition(myOffset, mySize));

			for (FilereadallContainer c : meta_info.get(cmd)) {
				long theirOffset;
				long theirSize;

				if (c.getRank() == myContainer.getRank()) {
					continue;
				}

				theirOffset = minOffset + ((myContainer.getTwoPhaseIteration() * meta_info.get(cmd).size()) + meta_info.get(cmd).indexOf(c)) * twoPhaseBufferSize;
				theirSize = Math.min(twoPhaseBufferSize, maxOffset - theirOffset);
				theirSize = Math.max(theirSize, 0);

				if (c.getCommand().getStartOffset() < myOffset + mySize && c.getCommand().getEndOffset() > myOffset) {
					OUTresults.addNetReceive(c.getRank(), 50001, Communicator.INTERNAL_MPI);
				}

				if (cmd.getStartOffset() < theirOffset + theirSize && cmd.getEndOffset() > theirOffset) {
					// offset-length pairs
					OUTresults.addNetSend(c.getRank(), new NetworkSimpleMessage(myContainer.getListIO().getIOOperations().size() * 16 + 20), 50001, Communicator.INTERNAL_MPI);
				}
			}

			if (mySize > 0) {
				ListIO list = new ListIO();
				HashMap<Server, ListIO> servers;

				list.addIOOperation(myOffset, mySize);
				servers = cmd.getFile().getDistribution().distributeIOOperation(list, client.getSimulator().getModel().getServers());

				for (Server server : servers.keySet()) {
					IGServer sserver = (IGServer)client.getSimulator().getSimulatedComponent(server);
					ISNodeHostedComponent targetNIC = sserver;
					ListIO iolist = servers.get(server);

					for (SingleIOOperation op : iolist.getIOOperations()) {
						System.out.println(server + ": " + op);
					}

					// FIXME optimize
					OUTresults.addNetSend(targetNIC, new RequestRead(iolist, cmd.getFile()), RequestIO.INITIAL_REQUEST_TAG, Communicator.IOSERVERS);
					OUTresults.addNetReceive(targetNIC, RequestIO.IO_DATA_TAG, Communicator.IOSERVERS);
				}
			}

			boolean terminate = true;

			for (FilereadallContainer c : meta_info.get(cmd)) {
				long theirOffset;
				long theirSize;

				theirOffset = minOffset + ((myContainer.getTwoPhaseIteration() * meta_info.get(cmd).size()) + meta_info.get(cmd).indexOf(c)) * twoPhaseBufferSize;
				theirSize = Math.min(twoPhaseBufferSize, maxOffset - theirOffset);
				theirSize = Math.max(theirSize, 0);

				if (theirSize > 0) {
					terminate = false;
				}
			}

			if (!terminate) {
				OUTresults.setNextStep(TWO_PHASE_RECV);
			} else {
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

				if (myContainer.getRank() == 0) {
					meta_info.remove(cmd);
				}
			}

			return;
		}
		case (TWO_PHASE_RECV): {
			long minOffset = meta_info.get(cmd).get(0).getCommand().getStartOffset();
			long maxOffset = meta_info.get(cmd).get(0).getCommand().getEndOffset();
			long myOffset;
			long mySize;
			int myIndex = -1;
			FilereadallContainer myContainer = null;

			for (FilereadallContainer c : meta_info.get(cmd)) {
				minOffset = Math.min(minOffset, c.getCommand().getStartOffset());
				maxOffset = Math.max(maxOffset, c.getCommand().getEndOffset());

				if (c.getCommand() == cmd) {
					myIndex = meta_info.get(cmd).indexOf(c);
					myContainer = c;
				}
			}

			assert(myIndex >= 0);
			assert(myContainer != null);

			myOffset = minOffset + ((myContainer.getTwoPhaseIteration() * meta_info.get(cmd).size()) + myIndex) * twoPhaseBufferSize;
			mySize = Math.min(twoPhaseBufferSize, maxOffset - myOffset);
			mySize = Math.max(mySize, 0);

			System.out.println("rank " + myContainer.getRank());

			for (FilereadallContainer c : meta_info.get(cmd)) {
				long theirOffset;
				long theirSize;

				if (c.getRank() == myContainer.getRank()) {
					continue;
				}

				theirOffset = minOffset + ((myContainer.getTwoPhaseIteration() * meta_info.get(cmd).size()) + meta_info.get(cmd).indexOf(c)) * twoPhaseBufferSize;
				theirSize = Math.min(twoPhaseBufferSize, maxOffset - theirOffset);
				theirSize = Math.max(theirSize, 0);

				if (c.getCommand().getStartOffset() < myOffset + mySize && c.getCommand().getEndOffset() > myOffset) {
					System.out.println("send to " + c.getRank());
					OUTresults.addNetSend(c.getRank(), new NetworkSimpleMessage(c.getCommand().getIOList().getPartition(myOffset, mySize).getTotalSize() + 20), 50000, Communicator.INTERNAL_MPI);
				}

				if (cmd.getStartOffset() < theirOffset + theirSize && cmd.getEndOffset() > theirOffset) {
					System.out.println("recv from " + c.getRank());
					OUTresults.addNetReceive(c.getRank(), 50000, Communicator.INTERNAL_MPI);
				}
			}

			myContainer.setTwoPhaseIteration(myContainer.getTwoPhaseIteration() + 1);

			OUTresults.setNextStep(TWO_PHASE);

			return;
		}
		}

		return;
	}

}
