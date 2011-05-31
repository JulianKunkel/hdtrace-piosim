//	Copyright (C) 2008, 2009, 2010, 2011 Julian M. Kunkel
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

import java.util.HashMap;

import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Synchronize all processes without communication.
 * @author julian
 */
public class VirtualSync
extends CommandImplementation<CommunicatorCommand>
{
	static private class CommunicatorCommandWrapper{
		final private CommunicatorCommand command;

		public CommunicatorCommand getCommand() {
			return command;
		}

		public CommunicatorCommandWrapper(CommunicatorCommand cmd) {
			command = cmd;
		}

		/**
		 * Used to determine the right Communicator command.
		 * Different clients using the same command e.g. a particular barrier.
		 * It does not test class specific parameters for instance tags.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != getClass()){
				return false;
			}

			CommunicatorCommandWrapper compare = (CommunicatorCommandWrapper) obj;

			return (compare.command.getCommunicator() == this.command.getCommunicator()) &&
				(compare.command.getProgram().getApplication() == this.command.getProgram().getApplication())
				&& (compare.command.getClass() == this.command.getClass())
				;
		}

		@Override
		public int hashCode() {
			return command.getCommunicator().hashCode() + command.getClass().hashCode();
		}
	}

	/**
	 *  virtual barrier, performed without communication
	 */
	private static HashMap<CommunicatorCommandWrapper, HashMap<GClientProcess, ICommandProcessing>> sync_blocked_clients =
		new HashMap<CommunicatorCommandWrapper, HashMap<GClientProcess, ICommandProcessing>>();

	/**
	 *
	 * @param cmd
	 * @return true if blocked (i.e. sync with further)
	 */
	private boolean synchronizeClientsWithoutCommunication(ICommandProcessing cmdResults){
		GClientProcess client = cmdResults.getInvokingComponent();

		CommunicatorCommand cmd = (CommunicatorCommand) cmdResults.getInvokingCommand();
		CommunicatorCommandWrapper cmdWrapper = new CommunicatorCommandWrapper(cmd);

		HashMap<GClientProcess, ICommandProcessing> waitingClients = sync_blocked_clients.get(cmdWrapper);
		if (waitingClients == null){
			/* first client waiting */
			waitingClients = new HashMap<GClientProcess, ICommandProcessing>();
			sync_blocked_clients.put(cmdWrapper, waitingClients);
		}

		if (waitingClients.size() == cmd.getCommunicator().getSize() -1){
			client.debug("Activate other clients for barrier " + cmd + " by " + client.getIdentifier() );

			/* we finish, therefore reactivate all other clients! */
			for(GClientProcess c: waitingClients.keySet()){
				c.activateBlockedCommand(waitingClients.get(c));
			}

			/* remove Barrier */
			sync_blocked_clients.remove(cmdWrapper);

		}else{
			waitingClients.put(client, cmdResults);

			/* just block up */
			client.debug("Block for " + cmd + " by " + client.getIdentifier() );
			return true;
		}

		return false;
	}

	@Override
	public void process(CommunicatorCommand cmd,  ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		boolean ret = synchronizeClientsWithoutCommunication(OUTresults);

		if (ret == true){
			/* just block up */
			client.debug("Block for " + cmd + " by " + client.getIdentifier() );
			OUTresults.setBlocking();

			return;
		}

		return;
	}
}
