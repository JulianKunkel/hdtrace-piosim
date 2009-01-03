
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

package de.hd.pvs.piosim.simulator.components.ClientProcess;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;

/**
 * Class encapsulates the results for by any command invocation.
 *  
 * @author Julian M. Kunkel
 */
public class CommandStepResults{
	/**
	 * The network jobs which should be processed.
	 */
	final NetworkJobs jobs;		
	
	/**
	 * The next step of the command which shall be invoked.
	 */
	final int         nextStep;
	
	/**
	 * True if the job must be blocked. Must be controlled within the Command.
	 */
	final boolean     blockingForced;
	
	/**
	 * The command which got invoked.
	 */
	Command invokingCommand;
	
	/**
	 * Create a new list of pending network operations which are blocked in 
	 * this command until a particular event occurs.
	 */
	public CommandStepResults() {
		this.jobs = null;
		this.nextStep = 0;
		this.blockingForced = true;
	}
	
	/**
	 * Create a new list of pending network operations
	 * @param jobs 
	 * @param nextStep The next step in the command which should be invoked
	 */
	public CommandStepResults(NetworkJobs jobs, int nextStep) {		
		assert(jobs != null);
		
		this.jobs = jobs;
		this.nextStep = nextStep;
		this.blockingForced = false;
	}
	
	/**
	 * Must the command block even if there are no network operations to perform.
	 * @return
	 */
	public boolean isBlockingEnforced() {
		return blockingForced;
	}		
	
	/**
	 * Set the command which should be invoked once the blocking operations completed. 
	 * 
	 * @param invokingCommand
	 */
	void setInvokingCommand(Command invokingCommand) {
		this.invokingCommand = invokingCommand;
	}
	
	/**
	 * Return the network jobs which must be submitted to the NIC.
	 * @return
	 */
	public NetworkJobs getJobs() {
		return jobs;
	}
	
	/**
	 * Return the command which lead to this description. This command must be rerun once
	 * the blocking operations finished. 
	 */
	public Command getInvokingCommand() {
		return invokingCommand;
	}
	
	/**
	 * Return the next step to run in the particular command.
	 * @return
	 */
	public int getNextStep() {
		return nextStep;
	}
	
	@Override
	public String toString() {		
		return "CommandStepResult " + getInvokingCommand() + " nextStep: " + nextStep + " blockingForced:" + blockingForced;
	}
}