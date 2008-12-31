
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

package de.hd.pvs.piosim.simulator.component.Commands;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.component.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.component.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.component.Node.GNode;
import de.hd.pvs.piosim.simulator.event.CommandDescription;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;

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
	abstract public CommandStepResults process(CommandType cmd, GClientProcess client, int step, NetworkJobs compNetJobs);
	
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

	// global values for processing:
	
	/** first step of all computations, set upon starting the command */
	public static final int STEP_START = 0;
	
	/** signals completion of the command */
	public static final int STEP_COMPLETED = 9999999;
	
	final private GClientProcess getMyFlowComponentFromReq(NetworkJobs jobs){
		return jobs.getInitialRequestDescription().getInvokingComponent();
	}
	
	final private GNode getGNode(NetworkJobs jobs){
		return jobs.getInitialRequestDescription().getInvokingComponent().getAttachedNode(); 
	}
	
	/**
	 * Add a network receive to be performed by the command.
	 * 
	 * @param stepResult
	 * @param from 
	 * @param tag
	 * @param comm
	 */
	final protected void netAddReceive(CommandStepResults stepResult, ISNodeHostedComponent from, int tag, Communicator comm){
		NetworkJobs jobs = stepResult.getJobs();
		jobs.addNetworkJob(
				SingleNetworkJob.createReceiveOperation(getMyFlowComponentFromReq(jobs), from, tag, comm, jobs));
	}

	/**
	 * Add a network send to be performed by the command. 
	 * 
	 * @param stepResult
	 * @param to
	 * @param jobData
	 * @param tag
	 * @param comm
	 * @param shouldPartialRecv
	 */
	final protected void netAddSend(CommandStepResults stepResult, ISNodeHostedComponent to,  
			INetworkMessage jobData, int tag, Communicator comm, 
			boolean shouldPartialRecv ){
		NetworkJobs jobs = stepResult.getJobs();
		jobs.addNetworkJob(
				SingleNetworkJob.createSendOperation(jobData, getMyFlowComponentFromReq(jobs), to, tag, comm, jobs, false, shouldPartialRecv));		
	}

	/**
	 * Add a network send to be performed by the command. 
	 * 
	 * @param stepResult
	 * @param to
	 * @param jobData
	 * @param tag
	 * @param comm
	 */
	final protected void netAddSend(CommandStepResults stepResult, ISNodeHostedComponent to,  INetworkMessage jobData, int tag, Communicator comm){
		netAddSend(stepResult, to, jobData, tag, comm, false);
	}

	/**
	 * Lookup a target rank from the application this client has. 
	 * 
	 * @param gclient
	 * @param rank
	 * @return The target rank of the application.
	 */
	final protected ISNodeHostedComponent getTargetfromRank(GClientProcess gclient, int rank){
		return gclient.getSimulator().getApplicationMap().getClient(
				gclient.getModelComponent().getApplication(),rank);
	}

	final private CommandStepResults prepareStepResults(CommandDescription cmdDesc, int Step, Epoch time){
		return new CommandStepResults(new NetworkJobs(cmdDesc, time), Step);
	}

	/**
	 * Initialize the <code>CommandStepResults</code> in case this command step should block.
	 * 
	 * @return
	 */
	final protected CommandStepResults prepareStepResultsForBlocking(){
		return new CommandStepResults();
	}
	
	/**
	 * Initialize the <code>CommandStepResults</code> for network operations.
	 * 
	 * @param gclient
	 * @param cmd
	 * @param Step
	 * @return
	 */
	final protected CommandStepResults prepareStepResultsForJobs(GClientProcess gclient, Command cmd, int Step){
		assert(gclient != null);
		return prepareStepResults(new CommandDescription(gclient, cmd, gclient.getSimulator().getVirtualTime()), Step, gclient.getSimulator().getVirtualTime());
	}

	/**
	 * Initialize the <code>CommandStepResults</code> for network operations.
	 * Copy the old command description.
	 *
	 */
	final protected CommandStepResults prepareStepResultsForJobs(NetworkJobs oldJob, int Step){
		assert(oldJob != null);
		
		return prepareStepResults(oldJob.getInitialRequestDescription(), Step, oldJob.getInitialRequestDescription().getInvokingComponent().getSimulator().getVirtualTime());
	}

}
