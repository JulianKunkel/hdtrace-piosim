
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

/**
 *
 */
package de.hd.pvs.piosim.simulator.program.SendReceive.Rendezvous;

import de.hd.pvs.piosim.model.program.commands.Recv;
import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.model.program.commands.Sendrecv;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 *
 * TODO: a bug leads to hung up for sendRecv with rendevouz protocol, recheck execution graph
 *
 * @author Julian M. Kunkel
 *
 */

public class RendezvousSendrecv extends CommandImplementation<Sendrecv>
{
	public void process(Sendrecv cmd,  ICommandProcessing OUTresults, GClientProcess client, long step,  NetworkJobs compNetJobs) {
		// start concurrent send and receive operations.

		final Send send = new Send();
		final Recv recv = new Recv();

		send.setCommunicator(cmd.getCommunicator());
		send.setToRank(cmd.getToRank());
		send.setToTag(cmd.getToTag());
		send.setSize(cmd.getSize());

		recv.setCommunicator(cmd.getCommunicator());
		recv.setFromRank(cmd.getFromRank());
		recv.setFromTag(cmd.getFromTag());

		final Command [] sendRecv = new Command[2];
		sendRecv[0] = send;
		sendRecv[1] = recv;

		OUTresults.invokeChildOperations(sendRecv, CommandProcessing.STEP_COMPLETED, null);

		return;
	}

	@Override
	public String[] getAdditionalTraceAttributes(Sendrecv cmd) {
		return new String[] { "toRank",  cmd.getToRank() + "", "size", cmd.getSize() + "", "toTag", ""+ cmd.getToTag() };
	}
}

