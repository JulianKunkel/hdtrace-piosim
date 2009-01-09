
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

package de.hd.pvs.piosim.simulator.event;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;

/**
 * Describes the reason for events to allow tracing of the caused actions.
 * 
 * @author Julian M. Kunkel
 *
 */
public class CommandDescription {
	/**
	 * Which client process started the command.
	 */
	protected GClientProcess  invokingComponent = null;
	
	/**
	 * the command leading to the events.
	 */
	protected Command command = null;
	
	/**
	 * The time the sequence of events got started.
	 */
	protected Epoch    startTime = null;
		
	public CommandDescription(
			GClientProcess invokingComponent, 
			Command initiating_command,
			Epoch startTime
			) {
		this.invokingComponent = invokingComponent;
		this.command = initiating_command;
		this.startTime = startTime;
	}

	public GClientProcess getInvokingComponent() {
		return invokingComponent;
	}

	public Command getCommand() {
		return command;
	}

	public Epoch getStartTime() {
		return startTime;
	}	
}