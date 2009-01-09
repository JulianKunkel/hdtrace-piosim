
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

package de.hd.pvs.piosim.simulator.program.Compute;

import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Time 
extends CommandImplementation<Compute>
{
	@Override
	public long getInstructionCount(Compute cmd, int step) {
		return cmd.getCycles();
	}

	@Override
	public CommandStepResults process(Compute cmd, GClientProcess client, int step, NetworkJobs compNetJobs) {
		return null;
	}
}