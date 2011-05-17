
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

import de.hd.pvs.TraceFormat.relation.RelationToken;
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
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;
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
	public static final long STEP_START = 0; // MUST BE ZERO !

	/** signals completion of the command */
	public static final long STEP_COMPLETED = -1;

	/**
	 * By default no parent operation is set, i.e. the operations are not stacked.
	 */
	private CommandProcessing parentOperation = null;


	/**
	 * By default no nested operation is set, i.e. the operations are not stacked and independent.
	 */
	private CommandProcessing [] nestedOperation = null;

	/**
	 * Number of currently uncompleted child operations of this state machine
	 */
	private int unfinishedChildOperations = 0;

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
	 * If this is a nested operation, then the root of the command stack is stored here.
	 */
	final CommandImplementation rootOperation;

	/**
	 * the specified implementation is used.
	 */
	final CommandImplementation processingMethod ;

	public CommandImplementation getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * The next step of the command which shall be invoked.
	 */
	long         nextStep ;

	/**
	 * True if the job must be blocked. Must be controlled within the Command.
	 */
	boolean     blockingForced ;


	final RelationToken 	relationToken;


	/**
	 * Create a new result of a command
	 * @param networkJobs
	 * @param nextStep The next step in the command which should be invoked
	 */
	public CommandProcessing(
			Command invokingCommand,
			CommandImplementation  processingMethod,
			GClientProcess invokingComponent,
			Epoch startTime,
			RelationToken relationToken)
	{
		this(invokingCommand, processingMethod, processingMethod, invokingComponent, startTime, relationToken);
	}

	private CommandProcessing(
			Command invokingCommand,
			CommandImplementation  processingMethod,
			CommandImplementation  rootMethod,
			GClientProcess invokingComponent,
			Epoch startTime,
			RelationToken relationToken)
	{
		this.processingMethod = processingMethod;
		this.rootOperation = rootMethod;

		this.invokingCommand = invokingCommand;
		this.invokingComponent = invokingComponent;
		this.startTime = startTime;
		resetState();
		this.relationToken = relationToken;
	}

	public RelationToken getRelationToken() {
		return relationToken;
	}

	/**
	 * Remove all the processing from the old states.
	 */
	void resetState(){
		this.networkJobs = new NetworkJobs(this);
		this.blockingForced = false;
		nextStep = STEP_COMPLETED;
		this.nestedOperation = null;
	}

	/**
	 * This method allows the command to create a set of nested operations, which are run concurrently .
	 * The commands shall not be modified afterwards.
	 * @param enforceProcessingMethod optional parameter, if null the default implementation is used
	 */
	public void invokeChildOperation(final Command nestedCmd, long nextStep, Class<? extends CommandImplementation> enforceProcessingMethod){
		Class<? extends CommandImplementation> [] enforceProcessingMethods = null;
		if(enforceProcessingMethod != null){
			enforceProcessingMethods = new Class[1];
			enforceProcessingMethods[0] = enforceProcessingMethod;
		}

		Command [] nestedCmds = new Command[1];
		nestedCmds[0] = nestedCmd;

		invokeChildOperations(nestedCmds, nextStep, enforceProcessingMethods);
	}

	/**
	 * This method allows the command to create a nested operation.
	 * The command shall not be modified afterwards.
	 *
	 *
	 * Child operations are always tagged as asynchronous operations if multiple are issued.
	 * 	When one child operation is issued it is mapped as a nested operation, otherwise as an asynchronous operation.
	 *
	 */
	public void invokeChildOperations(final Command [] nestedCmd, long nextStep, Class<? extends CommandImplementation> [] enforceProcessingMethod){
		setNextStep(nextStep);

		assert(nestedCmd != null);
		if(enforceProcessingMethod != null){
			assert(nestedCmd.length == enforceProcessingMethod.length);
		}
		nestedOperation = new CommandProcessing[nestedCmd.length];

		unfinishedChildOperations = nestedCmd.length;
		for(int i=0; i < nestedCmd.length; i++){
			final Command cmd = nestedCmd[i];

			cmd.setProgram(getInvokingCommand().getProgram());

			final RelationToken relation;

			if(nestedCmd.length == 1){
				relation = this.relationToken;
			}else{ // > 0
				// create a new relation token.
				relation = createNestedToken();
			}


			CommandImplementation<?> childProcessingMethod;
			if( enforceProcessingMethod == null ){
				childProcessingMethod = DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(cmd.getClass());
			}else{
				childProcessingMethod = DynamicImplementationLoader.getInstance().getCommandInstanceIfNotForced(enforceProcessingMethod[i], cmd.getClass());
			}

			CommandProcessing childOp = new CommandProcessing(
					cmd, childProcessingMethod, rootOperation, getInvokingComponent(),
					getInvokingComponent().getSimulator().getVirtualTime(),
					relation);

			childOp.setNextStep(STEP_START);
			childOp.parentOperation = this;

			nestedOperation[i] = childOp;
		}
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
	public void setNextStep(long nextStep) {
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
	public long getNextStep() {
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


	private final RelationToken createNestedToken(){
		return this.getInvokingComponent().getSimulator().getTraceWriter().relRelateProcessLocalToken(TraceType.INTERNAL, this.getInvokingComponent(), relationToken);
	}

	/**
	 * @param from if you want to receive from any source, then use addNetReceiveAnySource
	 * @param tag
	 * @param comm
	 * @param matchMessageType
	 */
	final public void addNetReceive(INodeHostedComponent from, int tag, Communicator comm){
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJob.createReceiveOperation(
						new MessageMatchingCriterion(	from, getInvokingComponent().getModelComponent(), tag, comm, rootOperation, processingMethod	),
						getInvokingComponent().getCallback(), createNestedToken() ) );
	}

	final public void addNetReceive(int from, int tag, Communicator comm,  Class<? extends CommandImplementation> expectedRootOperation,  Class<? extends CommandImplementation> expectedProcessingMethod){
		addNetReceive(getTargetfromRank(from), tag, comm, expectedRootOperation, expectedProcessingMethod);
	}


	final public void addNetReceive(INodeHostedComponent from, int tag, Communicator comm,  Class<? extends CommandImplementation> expectedRootOperation,  Class<? extends CommandImplementation> expectedProcessingMethod){
		addNetReceive(from, tag, comm,
				// root operation could be null:
				expectedRootOperation == null ? null :
						DynamicImplementationLoader.getInstance().getInstanceForClass(expectedRootOperation),
				DynamicImplementationLoader.getInstance().getInstanceForClass(expectedProcessingMethod));
	}

	/**
	 * Allow to override the rootOperation and processingMethod to enable communication between collective calls (or client/server matching).
	 * This is also used by Receive / Send command and SendReceive.
	 *
	 * @param from
	 * @param tag
	 * @param comm
	 * @param expectedRootOperation
	 * @param expectedProcessingMethod
	 */
	final private void addNetReceive(INodeHostedComponent from, int tag, Communicator comm, CommandImplementation expectedRootOperation, CommandImplementation expectedProcessingMethod){
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJob.createReceiveOperation(
						new MessageMatchingCriterion(	from, getInvokingComponent().getModelComponent(), tag, comm, expectedRootOperation, expectedProcessingMethod	),
						getInvokingComponent().getCallback(), createNestedToken() ) );
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
								to, tag, comm, rootOperation, processingMethod),
								jobData, getInvokingComponent().getCallback() , createNestedToken() ));
	}

	final public void addNetSend(int rankTo,
			IMessageUserData jobData, int tag, Communicator comm,  Class<? extends CommandImplementation> definedRootOperation,  Class<? extends CommandImplementation> definedProcessingMethod)
	{
		addNetSend(getTargetfromRank(rankTo), jobData, tag, comm, definedRootOperation, definedProcessingMethod);
	}

	final public void addNetSend(INodeHostedComponent to,
			IMessageUserData jobData, int tag, Communicator comm,  Class<? extends CommandImplementation> definedRootOperation,  Class<? extends CommandImplementation> definedProcessingMethod)
	{
		addNetSend(to, jobData, tag, comm,
				DynamicImplementationLoader.getInstance().getInstanceForClass(definedRootOperation),
				DynamicImplementationLoader.getInstance().getInstanceForClass(definedProcessingMethod));
	}


	/**
	 * Allow to override the rootOperation and processingMethod to enable communication between collective calls (or client/server matching).
	 *
	 * @param from
	 * @param tag
	 * @param comm
	 * @param expectedRootOperation
	 * @param expectedProcessingMethod
	 */
	final public void addNetSend(INodeHostedComponent to,
			IMessageUserData jobData, int tag, Communicator comm, CommandImplementation definedRootOperation, CommandImplementation definedProcessingMethod)
	{
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJob.createSendOperation(
						new MessageMatchingCriterion(getInvokingComponent().getModelComponent(),
								to, tag, comm, definedRootOperation, definedProcessingMethod),
								jobData, getInvokingComponent().getCallback() , createNestedToken() ));
	}


	final public void addNetSendRoutable(ClientProcess client, INodeHostedComponent finalTarget,
			INodeHostedComponent nextHop, IMessageUserData jobData, int tag, Communicator comm)
	{
		getNetworkJobs().addNetworkJob(
				InterProcessNetworkJobRoutable.createRoutableSendOperation(
						new MessageMatchingCriterion(getInvokingComponent().getModelComponent(),
								nextHop, tag, comm, rootOperation, processingMethod),
								jobData, getInvokingComponent().getCallback(),
								client, finalTarget , createNestedToken() )	);
	}

	final public void addNetJob(InterProcessNetworkJob job){
		getNetworkJobs().addNetworkJob(job);
	}

	final public boolean isNestedOperation(){
		return parentOperation != null;
	}

	public CommandProcessing [] getNestedOperations() {
		return nestedOperation;
	}

	public CommandProcessing getParentOperation() {
		return parentOperation;
	}

	public void childOperationCompleted(){
		unfinishedChildOperations--;
	}

	public int getUnfinishedChildOperationCount(){
		return unfinishedChildOperations;
	}

	/**
	 * Lookup a target rank from the application this client has.
	 *
	 * @param gclient
	 * @param rank
	 * @return The target rank of the application.
	 */
	final private INodeHostedComponent getTargetfromRank(int rank){
		assert(rank >= 0);
		return getInvokingComponent().getSimulator().getApplicationMap().
		getClient( getInvokingComponent().getModelComponent().getApplication(),  rank).getModelComponent();
	}


	final public CommandImplementation getRootOperation() {
		return rootOperation;
	}
}