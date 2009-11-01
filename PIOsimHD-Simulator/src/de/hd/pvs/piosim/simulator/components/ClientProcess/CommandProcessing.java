
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

package de.hd.pvs.piosim.simulator.components.ClientProcess;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Class encapsulates the results for by any command invocation.
 * CommandStepResults can be stacked to encapsulate nested operations, a Allreduce can be combined
 * as a sequence of a reduce and a broadcast operation.
 *
 * @author Julian M. Kunkel
 */
public class CommandProcessing{

	// global values for processing:

	/** first step of all computations, set upon starting the command */
	public static final int STEP_START = 0; // MUST BE ZERO !

	/** signals completion of the command */
	public static final int STEP_COMPLETED = 9999999;

	/**
	 * By default no parent operation is set, i.e. the operations are not stacked.
	 */
	private CommandProcessing parentOperation = null;

	/**
	 * By default no nested operation is set i.e. the operations are not stacked and independent.
	 */
	private CommandProcessing nestedOperation = null;


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
	NetworkJobs networkJobs;

	/**
	 * The command which got invoked.
	 */
	final Command invokingCommand;

	/**
	 * if this field is set the invoking client must obey to use the implementation.
	 */
	private Class<? extends CommandImplementation> enforcedProcessingMethod = null;

	/**
	 * The next step of the command which shall be invoked.
	 */
	int         nextStep ;

	/**
	 * True if the job must be blocked. Must be controlled within the Command.
	 */
	boolean     blockingForced ;

	/**
	 * Create a new result of a command
	 * @param networkJobs
	 * @param nextStep The next step in the command which should be invoked
	 */
	public CommandProcessing(
			Command invokingCommand,
			GClientProcess invokingComponent,
			Epoch startTime)
	{
		this.invokingCommand = invokingCommand;
		this.invokingComponent = invokingComponent;
		this.startTime = startTime;
		resetState();
	}

	/**
	 * Remove all the processing from the old states.
	 */
	public void resetState(){
		this.networkJobs = new NetworkJobs(this);
		this.blockingForced = false;
		nextStep = STEP_COMPLETED;
		this.nestedOperation = null;
	}

	/**
	 * This method allows the command to create a nested operation.
	 * The command shall not be modified afterwards.
	 *
	 * @param childOperation
	 */
	public void invokeChildOperation(final Command nestedCmd, int nextStep, Class<? extends CommandImplementation>
	enforceProcessingMethod){

		setNextStep(nextStep);

		assert(nestedCmd.getAsynchronousID() == null);

		nestedCmd.setProgram(getInvokingCommand().getProgram());

		CommandProcessing childOp = new CommandProcessing(
				nestedCmd, getInvokingComponent(),
				getInvokingComponent().getSimulator().getVirtualTime());

		childOp.enforcedProcessingMethod = enforceProcessingMethod;

		childOp.setNextStep(STEP_START);
		childOp.parentOperation = this;

		nestedOperation = childOp;
	}

	/**
	 * Return the class or null if not set
	 * @return
	 */
	public Class<? extends CommandImplementation> getEnforcedProcessingMethod() {
		return enforcedProcessingMethod;
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
		return ! isCommandWaitingForResponse() && nextStep == CommandProcessing.STEP_COMPLETED;
	}

	public boolean isCommandWaitingForResponse(){
		return blockingForced || networkJobs.getNetworkJobs().size() != 0 || nestedOperation != null;
	}

	@Override
	public String toString() {
		return "<" + getInvokingCommand() + " nextStep: " + nextStep + " blockingForced:" + blockingForced + " nst: " + isNestedOperation() + " nwOps: " +  getNetworkJobs().getSize() + ">";
	}

	/**
	 * Add a network receive to be performed by the command.
	 *
	 * @param from
	 * @param tag
	 * @param comm
	 */
	final public void addNetReceive(int rankFrom, int tag, Communicator comm){
		addNetReceive(getTargetfromRank(rankFrom), tag, comm);
	}

	final public void addNetReceive(INodeHostedComponent from, int tag, Communicator comm){
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJob.createReceiveOperation( new MessageMatchingCriterion(
						from, getInvokingComponent().getModelComponent(), tag, comm), getInvokingComponent().getCallback() ));
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
			IMessageUserData jobData, int tag, Communicator comm)
	{
		addNetSend(getTargetfromRank(rankTo), jobData, tag, comm);
	}

	final public void addNetSend(INodeHostedComponent to,
			IMessageUserData jobData, int tag, Communicator comm)
	{
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJob.createSendOperation(
						new MessageMatchingCriterion(getInvokingComponent().getModelComponent(),
								to, tag, comm),
						jobData, getInvokingComponent().getCallback() ));
	}

	final public void addNetSendRoutable(ClientProcess client, INodeHostedComponent finalTarget,
			INodeHostedComponent nextHop, IMessageUserData jobData, int tag, Communicator comm)
	{
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJobRoutable.createRoutableSendOperation(
						new MessageMatchingCriterion(getInvokingComponent().getModelComponent(),
								nextHop, tag, comm),
						jobData, getInvokingComponent().getCallback(),
						client, finalTarget)	);
	}

	final public void addNetJob(InterProcessNetworkJob job){
		getNetworkJobs().addNetworkJob(job);
	}

	final public boolean isNestedOperation(){
		return parentOperation != null;
	}

	public CommandProcessing getNestedOperation() {
		return nestedOperation;
	}

	public CommandProcessing getParentOperation() {
		return parentOperation;
	}

	/**
	 * Lookup a target rank from the application this client has.
	 *
	 * @param gclient
	 * @param rank
	 * @return The target rank of the application.
	 */
	final private INodeHostedComponent getTargetfromRank(int rank){
		return getInvokingComponent().getSimulator().getApplicationMap().
			getClient( getInvokingComponent().getModelComponent().getApplication(),  rank).getModelComponent();
	}


}