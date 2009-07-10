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

import de.hd.pvs.piosim.model.Model;
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
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class TwoPhase extends CommandImplementation<Filereadall> {
	Communicator myComm = new Communicator("TWO-Phase");

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

		public FilereadallContainer(Filereadall command, GClientProcess clientProcess, CommandProcessing commandProcessing) {
			this.command = command;
			this.clientProcess = clientProcess;
			this.commandProcessing = commandProcessing;
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

		public int getRank() {
			return clientProcess.getModelComponent().getRank();
		}
	}

	final class FilereadallContainerComparator implements Comparator<FilereadallContainer> {
		public int compare(FilereadallContainer a, FilereadallContainer b) {
			if (a.getRank() < b.getRank()) {
				return -1;
			} else if (a.getRank() > b.getRank()) {
				return 1;
			}

			return 0;
		}
	}

	private static HashMap<FilereadallWrapper, List<FilereadallContainer>> sync_blocked_clients = new HashMap<FilereadallWrapper, List<FilereadallContainer>>();
	private static HashMap<Filereadall, List<FilereadallContainer>> xxx = new HashMap<Filereadall, List<FilereadallContainer>>();

	@Override
	public void process(Filereadall cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		final int SEND_REQUEST = 2;
		final int CHECK_TWO_PHASE = 3;

		int rank = cmd.getProgram().getRank();

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
			Model m = client.getSimulator().getModel();
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
			} else {
			}

			OUTresults.setNextStep(SEND_REQUEST);

			return;
		}
		case (SEND_REQUEST): {
			/* determine I/O targets */
			Model m = client.getSimulator().getModel();

			long actualFileSize = cmd.getFile().getSize();
			long amountOfDataToRead = cmd.getIOList().getTotalSize();
			/* check if the file is smaller than expected, if yes, crop data */

			ArrayList<SingleIOOperation> ops = cmd.getIOList().getIOOperations();

			for (int i = ops.size() - 1; i >= 0; i--) {
				if (ops.get(i).getOffset() + ops.get(i).getAccessSize() < actualFileSize) {
					break; // done checking due to order
				}

				if (ops.get(i).getOffset() < actualFileSize) {
					ops.get(i).setAccessSize(actualFileSize - ops.get(i).getOffset());
				} else {
					ops.remove(i);
				}
			}

			if (amountOfDataToRead != cmd.getIOList().getTotalSize()) {
				client.debug("Short read: " + cmd.getIOList().getTotalSize() + " instead of " + amountOfDataToRead + " should be read => file too small \""
						+ actualFileSize + "\"");
			}

			HashMap<Server, ListIO> targetIOServers = cmd.getFile().getDistribution().distributeIOOperation(cmd.getIOList(), m.getServers());

			/* create an I/O request for each of these servers */
			for (Server server : targetIOServers.keySet()) {
				IGServer sserver = (IGServer) client.getSimulator().getSimulatedComponent(server);

				/*
				 * data to transfer depends on actual command size, but is
				 * defined in send
				 */
				ISNodeHostedComponent targetNIC = sserver;

				ListIO iolist = targetIOServers.get(server);

				/* initial job request */
				OUTresults.addNetSend(targetNIC, new RequestRead(iolist, cmd.getFile()), RequestIO.INITIAL_REQUEST_TAG, Communicator.IOSERVERS);

				OUTresults.addNetReceive(targetNIC, RequestIO.IO_DATA_TAG, Communicator.IOSERVERS);
			}

			return;
		}
		}

		return;
	}

}
