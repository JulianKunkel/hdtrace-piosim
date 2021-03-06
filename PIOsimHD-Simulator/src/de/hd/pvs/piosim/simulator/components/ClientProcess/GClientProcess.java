
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

/**
 *
 */
package de.hd.pvs.piosim.simulator.components.ClientProcess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.NodeHostedComponent;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Program;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SBasicComponent;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.IInterProcessNetworkJobCallback;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobType;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.inputOutput.IORedirectionHelper;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.output.STraceWriter;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
import de.hd.pvs.piosim.simulator.program.IWaitCommand;

/**
 * Simulates a single client process, which processes commands.
 * The first event initiates processing.
 *
 * @author Julian M. Kunkel
 *
 */
public class GClientProcess
	extends SBasicComponent<ClientProcess>
	implements ISNodeHostedComponent<SPassiveComponent<ClientProcess>>
{
	private IProcessNetworkInterface networkInterface;
	private INodeRessources   nodeRessources;

	private int  lastUsedTag = 0;

	/**
	 * Use this function to create unique communication among a communicator.
	 * Return a tag which was not used on this client. (Besides overflow of the long).
	 * @return
	 */
	public int getNextUnusedTag() {
		lastUsedTag++;
		if(lastUsedTag < 0) lastUsedTag = 0;
		return lastUsedTag;
	}

	/**
	 * If applicable.
	 */
	private IORedirection     ioRedirection = null;

	private final IInterProcessNetworkJobCallback callback = new InterProcessNetworkJobCallbackAdaptor(){
		@Override
		public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime)
		{
			//System.out.println(endTime + " " + getIdentifier() +  " RECV completed " + announcedJob + "\n\tREMOTE: " + remoteJob);

			final NetworkJobs status = pendingJobs.remove(announcedJob);
			assert(status != null);
			status.jobCompletedRecv(remoteJob, announcedJob);
			checkJobCompleted(status);

			// trace output
			if(getSimulator().getTraceWriter().isTracableComponent(TraceType.CLIENT_STEP)){
				// trace values
				final String [] attr = new String[4];
				attr[0] = "size";
				attr[1] = "" + remoteJob.getSize();
				attr[2] = "tag";
				attr[3] = "" + remoteJob.getMatchingCriterion().getTag();

				getSimulator().getTraceWriter().relEndState(TraceType.CLIENT_STEP, announcedJob.getRelationToken(), "", attr);
				getSimulator().getTraceWriter().relDestroy(TraceType.CLIENT_STEP, announcedJob.getRelationToken());
			}
		}

		@Override
		public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime)
		{
			//System.out.println(endTime + " " + getIdentifier() +  " SEND completed " + myJob);

			final NetworkJobs status = pendingJobs.remove(myJob);
			assert(status != null);

			// trace output
			if(getSimulator().getTraceWriter().isTracableComponent(TraceType.CLIENT_STEP)){
				// trace values
				final String [] attr = new String[4];
				attr[0] = "size";
				attr[1] = "" + myJob.getSize();
				attr[2] = "tag";
				attr[3] = "" + myJob.getMatchingCriterion().getTag();

				getSimulator().getTraceWriter().relEndState(TraceType.CLIENT_STEP, myJob.getRelationToken(), "", attr);
				getSimulator().getTraceWriter().relDestroy(TraceType.CLIENT_STEP, myJob.getRelationToken());
			}

			status.jobCompletedSend();
			checkJobCompleted(status);
		}
	};

	/**
	 * Data structure which collects statistics per command type.
	 *
	 * @author Julian M. Kunkel
	 */
	public static class CommandUsageStatistics {
		int calls = 0;
		Epoch totalTimeSpend = Epoch.ZERO;

		public int getCalls() {
			return calls;
		}

		public Epoch getTotalTimeSpend() {
			return totalTimeSpend;
		}
	}




	/**
	 * Distribute the IO operation to eventual I/O forwarders
	 *
	 * @param file
	 * @param listIO
	 * @return
	 */
	public List<SClientListIO> distributeIOOperations(FileMetadata file, ListIO listIO){
		final HashMap<Server, ListIO> IOtargets = file.getDistribution().distributeIOOperation(listIO,	getSimulator().getModel().getServers()  );

		final LinkedList<SClientListIO> newTargets = new LinkedList<SClientListIO>();

		// check IO forwarders
		if(ioRedirection == null){
			for(Server o: IOtargets.keySet()){
				newTargets.add(new SClientListIO(o, o, IOtargets.get(o)));
			}
			return newTargets;
		}

		// IO forwarder is set => redirect IO

		for(Server o: IOtargets.keySet()){
			newTargets.add(new SClientListIO(o, IORedirectionHelper.getNextHopFor(o, ioRedirection, getSimulator().getModel()), IOtargets.get(o)));
		}

		return newTargets;
	}

	public static class ClientRuntimeInformation extends ComponentRuntimeInformation{
		private HashMap<Class<? extends Command>, CommandUsageStatistics> commandStats = new HashMap<Class<? extends Command>, CommandUsageStatistics>();

		public HashMap<Class<? extends Command>, CommandUsageStatistics> getCommandStats() {
			return commandStats;
		}

		Epoch endTime = Epoch.ZERO;

		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append(" end time: " + endTime.getDouble());
			for(Class<?> cmdClass: commandStats.keySet()){
				CommandUsageStatistics stat = commandStats.get(cmdClass);
				buff.append("\n\t" + cmdClass.getSimpleName() + " " + stat.calls + " calls");
			}
			return buff.toString();
		}
	}

	private final ClientRuntimeInformation runtimeInformation = new ClientRuntimeInformation();

	/**
	 * Commands which are blocked.
	 */
	private HashSet<CommandProcessing> blockedCommands = new HashSet<CommandProcessing>();

	/**
	 * The currently processing blocking command.
	 */
	private CommandProcessing blockedCommand = null;

	/**
	 * the client program.
	 */
	private Program clientProgram = null;

	/**
	 * did this client finish
	 */
	private boolean finished = false;;

	/** instruction counter, which command is the next one */
	private int nextCommandNumber = 0;

	/**
	 * If a set of jobs belongs to one command.
	 */
	private HashMap<InterProcessNetworkJob, NetworkJobs> pendingJobs = new HashMap<InterProcessNetworkJob, NetworkJobs>();

	private HashMap<ComputeJob, CommandProcessing>  pendingComputeJobs = new HashMap<ComputeJob, CommandProcessing>();

	/**
	 * if the client waits for a remote network operation this operation is stored here
	 */
	private HashMap<NetworkJobs, CommandProcessing> pendingNetworkOperations = new HashMap<NetworkJobs, CommandProcessing>();

	/**
	 * maps from AID / Asynchronous ID to the actual Command
	 */
	private HashMap<Integer, Command>     pendingNonBlockingOps = new HashMap<Integer, Command>();

	@Override
	public ComponentRuntimeInformation getComponentInformation() {
		return runtimeInformation;
	}

	/**
	 * use this method to enforce the completion of the currently blocked job
	 * @param cmd
	 */
	public void activateBlockedCommand(ICommandProcessingMapped cmdStep_){
		CommandProcessing cmdStep = (CommandProcessing) cmdStep_;

		if (! blockedCommands.remove(cmdStep)){
			throw new IllegalArgumentException("Error removing " + cmdStep + " from queue on client: " + this.getIdentifier());
		}

		assert(cmdStep.nextStep != CommandProcessing.STEP_START);

		processCommandStep(cmdStep, getSimulator().getVirtualTime(), true);
	}

	@Override
	public void computeJobCompletedCV(ComputeJob job) {
//////		debug("reactivating client " + job);

		CommandProcessing cmd = pendingComputeJobs.remove(job);

		assert(cmd != null);

		processCommandStep(cmd, getSimulator().getVirtualTime(), false);
	}

	/**
	 * @return the pendingNonBlockingOps
	 */
	public HashMap<Integer, Command> getPendingNonBlockingOps() {
		return pendingNonBlockingOps;
	}

	/**
	 * Check if this program finished
	 * @return
	 */
	public boolean isProgramFinished(){
		return finished;
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();

		if (pendingNonBlockingOps.size() == 0 && isProgramFinished())
			return;

		getSimulator().errorDuringProcessing();

		System.err.println("Client got pending operations: " + this.getIdentifier() + ": ");

		if(pendingNonBlockingOps.size() != 0)
			System.err.println( "   pending NON-blocking operations: " + pendingNonBlockingOps.size());

		for (NetworkJobs jobs: pendingNetworkOperations.keySet()) {
			System.err.println("\t" + pendingNetworkOperations.get(jobs) + " with NetworkOperations: ");
			for(InterProcessNetworkJob job: jobs.getNetworkJobs()){
				System.err.println("\t\t " + job);
			}
			if(jobs.getResponses() != null){
				System.err.println("\tpending Responses");
				for(InterProcessNetworkJob job: jobs.getResponses()){
					System.err.println("\t\t" + job);
				}
			}
		}

		for(Command pendingBlocked: pendingNonBlockingOps.values()) {
			System.err.println("   " + pendingBlocked);
		}

		System.err.println("Blocked command: " + blockedCommand);
		System.err.println("Next command would be: " + clientProgram.getNextCommand());
	}


	/**
	 * Gets invoked from the simulator to start with the  first set of commands.
	 */
	@Override
	public void simulationModelIsBuild(){

		clientProgram = getSimulator().getModel().getProgram(getModelComponent());

		if(clientProgram == null){
			warn("does not have a valid Program ");
			finished = true;
		}else{
			// wakeup this component => start processing
			setNewWakeupTimeNow();
		}

		// now setup IORedirection Layer if applicable.
		ioRedirection = IORedirectionHelper.getIORedirectionLayerFor(getSimulator().getModel().getIORedirectionLayers(), this.getIdentifier().getID());
	}

	/**
	 * Check if this client is already finished.
	 */
	private void checkSetFinishState(){
		if( pendingComputeJobs.size() == 0 && pendingNetworkOperations.size() == 0 &&
				clientProgram.isFinished()){
			finished = true;

			runtimeInformation.endTime = getSimulator().getVirtualTime();

			//info("finished");
		}
	}

	private void traceCommand(CommandProcessing step, Command cmd, CommandImplementation cme, boolean start){
		if(cmd.getClass() == Compute.class){
			return;
		}
		final STraceWriter tw = getSimulator().getTraceWriter();

		final TraceType traceType ;
		if(step.getParentOperation() == null){
			traceType = TraceType.CLIENT;
		}else{
			traceType = TraceType.CLIENT_NESTING;

			if(! getSimulator().getRunParameters().isTraceClientNestingOperations()){
				return;
			}
		}

		if(start == false) {

			if(cmd.isAsynchronous()){
				tw.relEndState(traceType, step.getRelationToken(), null, new String[] {"aid", "" + cmd.getAsynchronousID()});
			}else{
				tw.relEndState(traceType, step.getRelationToken());
			}

			if(step.getParentOperation() == null || step.getParentOperation().getNestedOperations().length > 1){
				tw.relDestroy(traceType, step.getRelationToken());
			}
		}else {
			// tracing of I/O commands adds size and offset pairs.
			String tag = cme.getAdditionalTraceTag(cmd);
			if(FileIOCommand.class.isAssignableFrom(cmd.getClass())){
				final ListIO list = ((FileIOCommand) cmd).getListIO();

				StringBuffer strBuff = new StringBuffer();
				for(SingleIOOperation op : list.getIOOperations()){
					strBuff.append("<op size=\"" + op.getAccessSize() +  "\" offset=\"" + op.getOffset()  + "\"/>");
				}

				if(tag == null){
					tag = strBuff.toString();
				}else{
					tag = tag + "\n" + strBuff;
				}
			}

			tw.relStartState(traceType, step.getRelationToken(), cmd.getClass().getSimpleName() + "/" + cme.getClass().getSimpleName(),
					tag, cme.getAdditionalTraceAttributes(cmd));
		}
	}

	private void commandCompleted(CommandProcessing step, CommandImplementation cme, Epoch time){
		Command cmd = step.getInvokingCommand();
		assert(cmd != null);
		//if(cmd.getClass() != Compute.class)
		//	getSimulator().getTraceWriter().end(this, cmd.getClass().getSimpleName() + " s " + nextStep);

//////		debug("command completed: " + cmd);

		CommandUsageStatistics statistic = runtimeInformation.commandStats.get(cmd.getClass());
		if(statistic == null){
			statistic = new CommandUsageStatistics();
			runtimeInformation.commandStats.put(cmd.getClass(), statistic);
		}
		//TODO statistic.totalTimeSpend =  new Time(time.subtract(commandStartTime), statistic.totalTimeSpend);
		statistic.calls++;

		traceCommand(step, cmd, cme, false);
		// check if it is a nested operation
		if ( step.isNestedOperation() ){
			final CommandProcessing parent = step.getParentOperation();
			parent.childOperationCompleted();

			if(parent.getUnfinishedChildOperationCount() == 0){
				// if yes remove last operation from stack trace.
				processCommandStep(step.getParentOperation(), time, true);
			}
			return;
		}

		//if it was the blocking Command, then we might continue!
		if(cmd.isAsynchronous()){
			pendingNonBlockingOps.remove(cmd.getAsynchronousID());

			if(blockedCommand == null){
				checkSetFinishState();
			}else if(blockedCommand.getInvokingCommand().getClass() == Wait.class){ // check if we are blocked with a WAIT command right now
				CommandImplementation<Wait> wcme = DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(Wait.class);
				assert(wcme != null);
				((IWaitCommand) wcme).pendingAIOfinished((Wait) blockedCommand.getInvokingCommand(),
						blockedCommand, this, cmd.getAsynchronousID());
			}

		}else{ // blocking command:
			// simple start the next command:
			blockedCommand = null;
			processNextCommands();
		}
	}

	/**
	 * Increment instruction counter and return the next command to process
	 * Sets the command or null if the client finished processing
	 */
	private Command getNextCommand(){
		if(clientProgram.isFinished()){
			checkSetFinishState();
			return null;
		}

		return clientProgram.getNextCommand();
	}

	/**
	 * Continue the processing of the given command. Invoke the required computation or implementation
	 * to continue with the given step.
	 *
	 * @param cmd
	 * @param curTime
	 * @param shallCompute
	 * @param nextStep
	 * @param compJobs
	 */
	private void processCommandStep(CommandProcessing cmdStep,
			Epoch curTime,
			boolean shallCompute)
	{
		Command cmd = cmdStep.getInvokingCommand();
		final long nextStep = cmdStep.nextStep;


		CommandImplementation cme = cmdStep.getProcessingMethod();

		if(nextStep == CommandProcessing.STEP_COMPLETED){
			commandCompleted(cmdStep, cme, curTime);
			return;
		} // END STEP_COMPLETED


		try{
			if(shallCompute){
				if(nextStep == CommandProcessing.STEP_START){
					traceCommand(cmdStep, cmd, cme, true);
				}

				//if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
				//	getSimulator().getTraceWriter().startState(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);
				//}

				getSimulator().getTraceWriter().relStartState(TraceType.CLIENT_STEP, cmdStep.getRelationToken(), cmd.getClass().getSimpleName() + " s" + nextStep);

				/*
				 * in order to make a single call visible we have to make sure that the simulator time increases between two
				 * subsequent operations. Also this prevents buffer overflows in commands which finish immediately.
				 */
				long instr = nodeRessources.getMinimumNumberInstructions();
				long cinstr = cme.getInstructionCount(cmd, this, nextStep);

				if(cinstr >= instr) instr = cinstr;

				/* wait if the command requires to wait for a while */
				ComputeJob job =  new ComputeJob(instr, this);

				nodeRessources.addComputeJob(job);

				pendingComputeJobs.put(job, cmdStep);

				return ; /* we shall not run the command directly, instead wait for a time */
			}
			//else if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
			//	getSimulator().getTraceWriter().endState(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);
			//}
			getSimulator().getTraceWriter().relEndState(TraceType.CLIENT_STEP, cmdStep.getRelationToken());

			/* now run the appropriate command to generate new events */
//////			debug("processing step: " + nextStep + " cmd: " + cmd);

			NetworkJobs oldJobs = cmdStep.getNetworkJobs();

			final CommandProcessing newJob = cmdStep;
			newJob.resetState();
			cme.process(cmd, newJob, this, nextStep, oldJobs);

			//System.out.println(getIdentifier() +  " processing step: " + nextStep + " cmd: " + cmd + " " +  newJob.isCommandWaitingForResponse());

			// now iterate until a state can be found which does some work.
			while(! newJob.isCommandWaitingForResponse() && newJob.getNextStep() != CommandProcessing.STEP_COMPLETED){
				long nextPStep = newJob.getNextStep();

				//System.out.println(getIdentifier() +  " -processing step: " + nextStep + " cmd: " + cmd + " " +  newJob.isCommandWaitingForResponse());

				newJob.resetState();

				cme.process(cmd, newJob, this, nextPStep, null);

				//if (newJob.nextStep == nextPStep){
				//	throw new IllegalArgumentException("(likely) endless iteration detected in command " + cmd);
				//}
			}

			//System.out.println(this + " " + nextStep + " starting " + newJob.nextStep + " " + newJob.getNetworkJobs().getSize());

			if (! newJob.isCommandWaitingForResponse()){ /* all blocking operation completed */
				commandCompleted(cmdStep, cme, curTime);
			}else{
				if(newJob.getNestedOperations() != null){
					// start nested operations.

					for(CommandProcessing child: newJob.getNestedOperations()){
						assert(child.getParentOperation() == newJob);
						processCommandStep(child, curTime, true);
					}

					return;
				}

				if(newJob.isBlockingEnforced()){
					/* just block, the job SHOULD wake up itself... ! */
					//System.out.println("blocking on " +  cmd + " " + this.getIdentifier());

					blockedCommands.add(newJob);
				}

				// start new network jobs:
				assert(newJob.getNetworkJobs() != null);
				assert(newJob.getNetworkJobs().getNetworkJobs() != null);

				if(newJob.getNetworkJobs().getSize() > 0){

					pendingNetworkOperations.put(newJob.getNetworkJobs(), newJob);

					for(InterProcessNetworkJob j: newJob.getNetworkJobs().getNetworkJobs()){
						assert(j.getMatchingCriterion().getTargetComponent() != null);

						pendingJobs.put(j, newJob.getNetworkJobs());
						//System.out.println( "PENDING " + getIdentifier() );

						if(j.getJobOperation() == InterProcessNetworkJobType.RECEIVE){
							// trace
							final String txt;

							if( j.getMatchingCriterion().getSourceComponent() != null ){
								// handle any-source
								txt = j.getMatchingCriterion().getSourceComponent().getIdentifier().toString().replace(" ", "_");
							}else{
								txt = " AnySource";
							}

							getSimulator().getTraceWriter().relStartState(TraceType.CLIENT_STEP, j.getRelationToken(), "Receive_" + txt);

							getNetworkInterface().initiateInterProcessReceive(j, curTime);
						}else{
							// trace
							getSimulator().getTraceWriter().relStartState(TraceType.CLIENT_STEP, j.getRelationToken(), "Send_" + ((NodeHostedComponent) j.getMatchingCriterion().getTargetComponent()).getIdentifier().toString().replace(" ", "_") );

							getNetworkInterface().initiateInterProcessSend(j, curTime);
						}
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * startup the next commands which can be started.
	 */
	private void processNextCommands(){
		Epoch time = getSimulator().getVirtualTime();

		CommandProcessing newJob;
		do{
			Command cmd = getNextCommand();
			if (cmd == null)
				return;

			newJob = new CommandProcessing(cmd, DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(cmd.getClass()) , this,
					time, getSimulator().getTraceWriter().relCreateTopLevelRelation(TraceType.CLIENT, this));

			newJob.setNextStep(CommandProcessing.STEP_START);
			processCommandStep(newJob, time, true);


			if( cmd.getAsynchronousID() == null) // command blocks
				break;

			Command oldCommand = pendingNonBlockingOps.put(cmd.getAsynchronousID(), cmd);
			if(oldCommand != null){
				throw new IllegalArgumentException( getIdentifier() + " uses an asynchronous ID which is currently used\n" +
						" instruction #" + nextCommandNumber + " aid:" + cmd.getAsynchronousID() );
			}
		}while(true);

		blockedCommand = newJob;
	}

	private void checkJobCompleted(NetworkJobs jobs){
		if(jobs.isCompleted()){
			Epoch endTime = getSimulator().getVirtualTime();
//////			debug(" resp: " + jobs.getResponses() + " " + endTime);

			/* reactivate this client, we have to process the next command */
			CommandProcessing pendingOp = pendingNetworkOperations.remove(jobs);
			assert(pendingOp != null);

			assert(pendingOp.getNextStep() != CommandProcessing.STEP_START);

			processCommandStep(pendingOp, endTime, true);
		}
	}

	@Override
	public void setNodeRessources(INodeRessources ressources) {
		this.nodeRessources = ressources;
	}

	@Override
	public INodeRessources getNodeRessources() {
		return this.nodeRessources;
	}

	@Override
	public void processEvent(Event event, Epoch time) {
		assert(false);
	}

	@Override
	public void processInternalEvent(InternalEvent event, Epoch time) {
		assert(time.equals(time.ZERO));
		//info("uses Program: \"" + clientProgram.getApplication().getApplicationName() + "\" alias: \"" +	clientProgram.getApplication().getAlias() + "\" rank " + getModelComponent().getRank());
		processNextCommands();
	}

	@Override
	public String toString() {
		return "GClientProcess " + getIdentifier();
	}

	@Override
	public void setNetworkInterface(IProcessNetworkInterface nic) {
		this.networkInterface = nic;
	}

	@Override
	public IProcessNetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public IInterProcessNetworkJobCallback getCallback() {
		return callback;
	}

}
