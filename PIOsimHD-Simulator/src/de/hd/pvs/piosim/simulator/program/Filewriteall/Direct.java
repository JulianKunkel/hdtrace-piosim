/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2010 Julian Kunkel
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

import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.model.program.commands.Filewriteall;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct extends CommandImplementation<Filewriteall> {

	@Override
	public void process(Filewriteall cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		final int SYNCRONIZED = 2;

		if(step == CommandProcessing.STEP_START){
			Barrier barrier = new Barrier();
			barrier.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(barrier, SYNCRONIZED, null);

			return;
		}else if (step == SYNCRONIZED) {
			Filewrite rd = new Filewrite();
			rd.setFileDescriptor(cmd.getFileDescriptor());
			rd.setListIO(cmd.getListIO());

			OUTresults.invokeChildOperation(rd, CommandProcessing.STEP_COMPLETED, null);

			return;
		}

		return;
	}
}
