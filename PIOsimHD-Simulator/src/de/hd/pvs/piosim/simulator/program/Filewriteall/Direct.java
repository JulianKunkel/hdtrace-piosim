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
package de.hd.pvs.piosim.simulator.program.Filewriteall;

import java.util.HashMap;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Filewriteall;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct extends CommandImplementation<Filewriteall> {
	final class FilewriteallWrapper {
		private Filewriteall command;

		public Filewriteall getCommand() {
			return command;
		}

		public FilewriteallWrapper(Filewriteall cmd) {
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

			FilewriteallWrapper compare = (FilewriteallWrapper) obj;

			return (compare.command.getCommunicator() == this.command.getCommunicator())
					&& (compare.command.getProgram().getApplication() == this.command.getProgram().getApplication())
					&& (compare.command.getClass() == this.command.getClass());
		}

		@Override
		public int hashCode() {
			return command.getCommunicator().hashCode() + command.getClass().hashCode();
		}
	}

	private static HashMap<FilewriteallWrapper, HashMap<GClientProcess, CommandProcessing>> sync_blocked_clients = new HashMap<FilewriteallWrapper, HashMap<GClientProcess, CommandProcessing>>();

	@Override
	public void process(Filewriteall cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		final int SEND_REQUEST = 2;
		final int RECV_ACK = 3;
		final int UPDATE_SIZE = 4;

		int rank = cmd.getProgram().getRank();

		switch (step) {
		case (CommandProcessing.STEP_START): {
			FilewriteallWrapper wrapper = new FilewriteallWrapper(cmd);

			HashMap<GClientProcess, CommandProcessing> waitingClients = sync_blocked_clients.get(wrapper);

			if (waitingClients == null) {
				/* first client waiting */
				waitingClients = new HashMap<GClientProcess, CommandProcessing>();
				sync_blocked_clients.put(wrapper, waitingClients);
			}

			if (waitingClients.size() < cmd.getCommunicator().getSize() - 1) {
				waitingClients.put(client, OUTresults);

				/* just block up */
				client.debug("Block for " + cmd + " by " + client.getIdentifier());
				OUTresults.setBlocking();
			} else {
				client.debug("Activate other clients for barrier " + cmd + " by " + client.getIdentifier());

				/* we finish, therefore reactivate all other clients! */
				for (GClientProcess c : waitingClients.keySet()) {
					c.activateBlockedCommand(waitingClients.get(c));
				}

				/* remove Barrier */
				sync_blocked_clients.remove(wrapper);
			}

			OUTresults.setNextStep(SEND_REQUEST);

			return;
		}
		case (SEND_REQUEST): {
			/* determine I/O targets */
			assert (client.getSimulator() != null);

			Model m = client.getSimulator().getModel();

			HashMap<Server, ListIO> targetIOServers = cmd.getFile().getDistribution().distributeIOOperation(cmd.getIOList(), m.getServers());

			/* create an I/O request for each of these servers */
			OUTresults.setNextStep(RECV_ACK);

			assert (targetIOServers.keySet().size() > 0);

			for (Server server : targetIOServers.keySet()) {
				IGServer sserver = (IGServer) client.getSimulator().getSimulatedComponent(server);

				/*
				 * data to transfer depends on actual command size, but is
				 * defined in send
				 */
				ISNodeHostedComponent targetNIC = sserver;

				ListIO iolist = targetIOServers.get(server);

				/* initial job request */
				OUTresults.addNetSend(targetNIC, new RequestWrite(iolist, cmd.getFile()), RequestIO.INITIAL_REQUEST_TAG, Communicator.IOSERVERS);
			}

			return;
		}
		case (RECV_ACK): {
			/* determine I/O targets */

			/* create an I/O request for each of these servers */
			OUTresults.setNextStep(UPDATE_SIZE);

			for (InterProcessNetworkJob job : compNetJobs.getNetworkJobs()) {
				RequestWrite writeRequest = (RequestWrite) job.getJobData();
				ListIO iolist = writeRequest.getListIO();

				/* STEP_START I/O job directly */
				OUTresults.addNetSend(job.getTargetComponent(), new NetworkIOData(writeRequest), RequestIO.IO_DATA_TAG, Communicator.IOSERVERS, true);

				/* wait for incoming msg (write completion notification) */
				OUTresults.addNetReceive(job.getTargetComponent(), RequestIO.IO_COMPLETION_TAG, Communicator.IOSERVERS);
			}

			return;
		}
		case (UPDATE_SIZE): {
			/* update the file size if necessary */

			SingleIOOperation op = cmd.getIOList().getIOOperations().get(cmd.getIOList().getIOOperations().size() - 1);

			long lastWrittenByte = op.getAccessSize() + op.getOffset();

			if (cmd.getFile().getSize() < lastWrittenByte) {
				cmd.getFile().setSize(lastWrittenByte);

				client.debug("File \"" + cmd.getFile().getName() + "\" enlarged to \"" + lastWrittenByte + "\" Bytes");
			}

			return;
		}
		}

		return;
	}
}
