
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

package de.hd.pvs.piosim.simulator.program;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;

/**
 * Basic superclass for all Commands.
 * A command consists of one or several atomic steps which can create network jobs.
 * Between two steps a small computation phase is inserted.
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class CommandImplementation<CommandType extends Command>
{	
	/**
	 * Called if a particular client will process this command. 
	 * 
	 * @param cmd The command with its data.
	 * @param client The client processing the command.
	 * @param step The current step of the command the client is doing.
	 * @param compNetJobs The completed network jobs. (null if none finished).
	 *  
	 * @return A description what to do next. This description is null if the command finished.
	 */
	abstract public void process(CommandType cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs);
	
	/**
	 * How many instructions are needed to compute the particular step of the command.
	 * 
	 * @param cmd
	 * @param step
	 * @return 
	 */
	public long getInstructionCount(CommandType cmd, int step) {
		return 0;
	}
	
}
