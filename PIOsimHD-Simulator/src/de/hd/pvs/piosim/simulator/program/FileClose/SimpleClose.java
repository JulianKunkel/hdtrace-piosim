
 /** Version Control Information $Id: SimpleFlagOpener.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27 Mrz 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

package de.hd.pvs.piosim.simulator.program.FileClose;

import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.model.program.commands.Fileclose;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class SimpleClose
extends CommandImplementation<de.hd.pvs.piosim.model.program.commands.Fileclose>
{
	@Override
	public void process(Fileclose cmd, ICommandProcessing OUTresults,
			GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (step == CommandProcessing.STEP_START){
			Barrier barrier = new Barrier();
			barrier.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(barrier, CommandProcessing.STEP_COMPLETED,
				de.hd.pvs.piosim.simulator.program.Global.VirtualSync.class);
		}
	}
}
