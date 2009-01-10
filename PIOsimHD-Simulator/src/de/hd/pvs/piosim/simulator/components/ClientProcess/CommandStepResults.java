
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

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;

/**
 * Class encapsulates the results for by any command invocation.
 *  
 * @author Julian M. Kunkel
 */
public class CommandStepResults{

	// global values for processing:
	
	/** first step of all computations, set upon starting the command */
	public static final int STEP_START = 0;
	
	/** signals completion of the command */
	public static final int STEP_COMPLETED = 9999999;
	
	/**
	 * Which client process started the command.
	 */
	final GClientProcess  invokingComponent;
	
	/**
	 * The time this processing step of the command got started.
	 */
	final Epoch    startTime;
	
	/**
	 * The network jobs which should be processed.
	 */
	final NetworkJobs networkJobs;		
	
	/**
	 * The command which got invoked.
	 */
	final Command invokingCommand;
	
	/**
	 * The next step of the command which shall be invoked.
	 */
	int         nextStep = STEP_COMPLETED;
	
	/**
	 * True if the job must be blocked. Must be controlled within the Command.
	 */
	boolean     blockingForced = false;
	
	/**
	 * Create a new result of a command
	 * @param networkJobs 
	 * @param nextStep The next step in the command which should be invoked
	 */
	public CommandStepResults(
			Command invokingCommand, 			
			GClientProcess invokingComponent, 
			Epoch startTime) 
	{				
		this.invokingCommand = invokingCommand;
		this.invokingComponent = invokingComponent;
		this.startTime = startTime;
		this.networkJobs = new NetworkJobs(this);
	}
	
	/**
	 * Must the command block even if there are no network operations to perform.
	 * @return
	 */
	public boolean isBlockingEnforced() {
		return blockingForced;
	}		
	
	
	/**
	 * Return the network jobs which must be submitted to the NIC.
	 * @return
	 */
	public NetworkJobs getNetworkJobs() {
		return networkJobs;
	}
	
	/**
	 * Call this method if the command should always block.
	 * 
	 * @param value
	 */
	public void setBlocking(){
		blockingForced = true;
	}
	
	/**
	 * Set the next step of this command which should be invoked.
	 * @param nextStep
	 */
	public void setNextStep(int nextStep) {
		this.nextStep = nextStep;
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
	
	public GClientProcess getInvokingComponent() {
		return invokingComponent;
	}

	public Epoch getStartTime() {
		return startTime;
	}	
	
	public boolean isCommandComplete(){
		return ! (blockingForced || networkJobs.getNetworkJobs().size() > 0);
	}
	
	@Override
	public String toString() {		
		return "CommandStepResult " + getInvokingCommand() + " nextStep: " + nextStep + " blockingForced:" + blockingForced;
	}
		
	/**
	 * Add a network receive to be performed by the command.
	 * 
	 * @param from 
	 * @param tag
	 * @param comm
	 */
	final public void addNetReceive(int rankFrom, int tag, Communicator comm){
		getNetworkJobs().addNetworkJob(
				SingleNetworkJob.createReceiveOperation(
						getInvokingComponent(), getTargetfromRank(rankFrom), 
						tag, comm, getNetworkJobs())
						);
	}

	final public void addNetReceive(ISNodeHostedComponent from, int tag, Communicator comm){
		getNetworkJobs().addNetworkJob(
				SingleNetworkJob.createReceiveOperation(
						getInvokingComponent(), from, 
						tag, comm, getNetworkJobs())
						);
	}
	
	/**
	 * Add a network send to be performed by the command. 
	 * 
	 * @param to
	 * @param jobData
	 * @param tag
	 * @param comm
	 * @param shouldPartialRecv
	 */
	final public void addNetSend(int rankTo,  
			INetworkMessage jobData, int tag, Communicator comm, 
			boolean shouldPartialRecv )
	{
		getNetworkJobs().addNetworkJob(
				SingleNetworkJob.createSendOperation(jobData, 
						getInvokingComponent(), getTargetfromRank(rankTo), tag, comm, 
						getNetworkJobs(), false, shouldPartialRecv));		
	}
	
	final public void addNetSend(ISNodeHostedComponent to,  
			INetworkMessage jobData, int tag, Communicator comm, 
			boolean shouldPartialRecv )
	{
		getNetworkJobs().addNetworkJob(
				SingleNetworkJob.createSendOperation(jobData, 
						getInvokingComponent(), to, tag, comm, 
						getNetworkJobs(), false, shouldPartialRecv));		
	}

	/**
	 * Add a network send to be performed by the command. 
	 * 
	 * @param to
	 * @param jobData
	 * @param tag
	 * @param comm
	 */
	final public void addNetSend(int rankTo,  
		INetworkMessage jobData, int tag, Communicator comm)
	{
		addNetSend(rankTo, jobData, tag, comm, false);
	}
	
	final public void addNetSend(ISNodeHostedComponent to,  
			INetworkMessage jobData, int tag, Communicator comm)
		{
			addNetSend(to, jobData, tag, comm, false);
		}

	/**
	 * Lookup a target rank from the application this client has. 
	 * 
	 * @param gclient
	 * @param rank
	 * @return The target rank of the application.
	 */
	final private ISNodeHostedComponent getTargetfromRank(int rank){
		return getInvokingComponent().getSimulator().getApplicationMap().
			getClient( getInvokingComponent().getModelComponent().getApplication(),  rank);
	}
}