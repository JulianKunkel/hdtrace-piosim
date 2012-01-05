
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

package de.hd.pvs.piosim.simulator.program.FileOpen;

import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Synchronizing operation, open file and wait until all clients opened the file.
 * Then Broadcast file information via communicator.
 *
 * @author julian
 *
 */
public class BroadcastOpen
extends CommandImplementation<de.hd.pvs.piosim.model.program.commands.Fileopen>
{
	@Override
	public void process(de.hd.pvs.piosim.model.program.commands.Fileopen cmd,
			ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		if (step == CommandProcessing.STEP_START){
			Barrier barrier = new Barrier();
			barrier.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(barrier, 1,
				de.hd.pvs.piosim.simulator.program.Global.VirtualSync.class);
		}else{
			if (cmd.isTruncateOnOpen() && client.getModelComponent().getRank() == 0){
				// truncate file.
				cmd.getFileDescriptor().getFile().setSize(0);
			}
			Bcast bcast = new Bcast();

			bcast.setCommunicator(cmd.getCommunicator());
			bcast.setRootRank(0);
			bcast.setSize(32);
			OUTresults.invokeChildOperation(bcast, CommandProcessing.STEP_COMPLETED, null);
		}
	}

}
