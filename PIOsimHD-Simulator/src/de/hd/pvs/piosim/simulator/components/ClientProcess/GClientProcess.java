
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
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.Program;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
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

	/**
	 * If applicable.
	 */
	private IORedirection     ioRedirection = null;

	private final IInterProcessNetworkJobCallback callback = new InterProcessNetworkJobCallbackAdaptor(){
		@Override
		public void recvCompletedCB(InterProcessNetworkJob remoteJob,
				InterProcessNetworkJob announcedJob, Epoch endTime)
		{
			//System.out.println("RECV completed");

			final NetworkJobs status = pendingJobs.remove(announcedJob);
			assert(status != null);
			status.jobCompletedRecv(remoteJob);
			checkJobCompleted(status);
		}

		@Override
		public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime)
		{
			//System.out.println("SEND completed");

			final NetworkJobs status = pendingJobs.remove(myJob);
			assert(status != null);
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
	public List<SClientListIO> distributeIOOperations(MPIFile file, ListIO listIO){
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

		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer();
			for(Class<?> cmdClass: commandStats.keySet()){
				CommandUsageStatistics stat = commandStats.get(cmdClass);
				buff.append("\n\t" + cmdClass.getSimpleName() + " " + stat.calls + " calls");
			}
			return buff.toString();
		}
	}

	private final ClientRuntimeInformation runtimeInformation = new ClientRuntimeInformation();

	/**
	 * Maps the commands to the command's implementation. For all clients the
	 * same implementation must be picked.
	 */
	private final static HashMap<Class<? extends Command>, CommandImplementation> commandMap =
		new HashMap<Class<? extends Command>, CommandImplementation>();

	/**
	 * if a command implementation is enforced then a instance get added to this set here, to allow
	 * a consistent view.
	 */
	private final static HashMap<Class<? extends CommandImplementation>, CommandImplementation> enforcedCommandImplementations =
		new HashMap<Class<? extends CommandImplementation>, CommandImplementation>();

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
	public void activateBlockedCommand(CommandProcessing cmdStep){
		if (! blockedCommands.remove(cmdStep)){
			throw new IllegalArgumentException("Error removing " + cmdStep + " from queue on client: " + this.getIdentifier());
		}

		assert(cmdStep.nextStep != CommandProcessing.STEP_START);

		processCommandStep(cmdStep, getSimulator().getVirtualTime(), true);
	}

	@Override
	public void computeJobCompletedCV(ComputeJob job) {
		debug("reactivating client " + job);

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
				System.err.println("\t\t" + job);
			}
			System.err.println("\tResponses");
			for(InterProcessNetworkJob job: jobs.getResponses()){
				System.err.println("\t\t" + job);
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

			info("finished");
		}
	}

	private void traceCommand(Command cmd, CommandImplementation cme, boolean start){
		final String string = cmd.getClass().getSimpleName() + "/" + cme.getClass().getSimpleName();

		if(start == false) {
			if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class){
				getSimulator().getTraceWriter().endState(TraceType.CLIENT, this, string);
			}else{
				getSimulator().getTraceWriter().event(TraceType.CLIENT, this, string + " end", 0);
			}
		}else {
			if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class){
				getSimulator().getTraceWriter().startState(TraceType.CLIENT, this, string);
			}else{
				getSimulator().getTraceWriter().event(TraceType.CLIENT, this, string + " start", 0);
			}
		}
	}

	private void commandCompleted(CommandProcessing step, CommandImplementation cme, Epoch time){
		Command cmd = step.getInvokingCommand();
		assert(cmd != null);
		//if(cmd.getClass() != Compute.class)
		//	getSimulator().getTraceWriter().end(this, cmd.getClass().getSimpleName() + " s " + nextStep);

		debug("command completed: " + cmd);

		CommandUsageStatistics statistic = runtimeInformation.commandStats.get(cmd.getClass());
		if(statistic == null){
			statistic = new CommandUsageStatistics();
			runtimeInformation.commandStats.put(cmd.getClass(), statistic);
		}
		//TODO statistic.totalTimeSpend =  new Time(time.subtract(commandStartTime), statistic.totalTimeSpend);
		statistic.calls++;

		traceCommand(cmd, cme, false);
		// check if it is a nested operation
		if ( step.isNestedOperation() ){
			// if yes remove last operation from stack trace.
			processCommandStep(step.getParentOperation(), time, true);
			return;
		}

		//if it was the blocking Command, then we might continue!
		if(cmd.isAsynchronous()){
			pendingNonBlockingOps.remove(cmd.getAsynchronousID());

			if(blockedCommand == null){
				checkSetFinishState();
			}else if(blockedCommand.getInvokingCommand().getClass() == Wait.class){ // check if we are blocked with a WAIT command right now
				CommandImplementation<Wait> wcme = commandMap.get(blockedCommand.getClass());
				((IWaitCommand) wcme).pendingAIOfinished((Wait) blockedCommand.getInvokingCommand(),
						step, this, cmd.getAsynchronousID());
			}

		}else{ // blocking command:
			// simple start the next command:
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
	 * Pick a <code>CommandImplementation</code> for a command of a particular type..
	 * @param what
	 * @return
	 */
	private CommandImplementation instanciateCommandImplemtation(Command what){
		CommandImplementation imp = null;
		Class<? extends Command>    commandClass = what.getClass();

		//determine global setting value
		final CommandType cMethodMapping = DynamicCommandClassMapper.getCommandImplementationGroup(what.getClass());
		assert(cMethodMapping != null);

		String implChoosen = getSimulator().getModel().getGlobalSettings().getClientFunctionImplementation(cMethodMapping);

		assert(implChoosen != null);

		// instantiate an object for the command implementation.
		try{
			Class<?> implClass = Class.forName(implChoosen);
			assert(implClass != null);
			Object obj = implClass.newInstance();
			//singular implementation.
			imp = (CommandImplementation) obj;
		}catch(Exception e){
			System.err.println("Problem in class: " + implChoosen + " does not implement the command: " +
					commandClass.getCanonicalName());
			e.printStackTrace();
			System.exit(1);
		}
		info("Methods for command: " + commandClass.getSimpleName() + " in class: " +
				imp.getClass().getCanonicalName());

		commandMap.put(commandClass, imp);
		return imp;
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
		final int nextStep = cmdStep.nextStep;


		CommandImplementation cme;

		if (cmdStep.getEnforcedProcessingMethod() == null){
			cme = commandMap.get(cmd.getClass());

			if(cme == null){
				cme = instanciateCommandImplemtation(cmd);
			}
		}else{
			Class<? extends CommandImplementation> forcedImpl = cmdStep.getEnforcedProcessingMethod();
			// check if a instance exists
			cme = enforcedCommandImplementations.get(forcedImpl);
			if(cme == null){
				// instantiate now:
				try{
					cme = forcedImpl.newInstance();
				}catch(Exception e){
					System.err.println("Problem in enforced class: " + forcedImpl.getCanonicalName());
					e.printStackTrace();
					System.exit(1);
				}

				enforcedCommandImplementations.put(forcedImpl, cme);
			}
		}

		if(nextStep == CommandProcessing.STEP_COMPLETED){
			commandCompleted(cmdStep, cme, curTime);
			return;
		} // END STEP_COMPLETED


		try{
			if(shallCompute){
				if(nextStep == CommandProcessing.STEP_START){
					traceCommand(cmd, cme, true);
				}

				if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
					getSimulator().getTraceWriter().startState(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);
				}


				/*
				 * in order to make a single call visible we have to make sure that the simulator time increases between two
				 * subsequent operations. Also this prevents buffer overflows in commands which finish immediately.
				 */
				long instr = nodeRessources.getMinimumNumberInstructions();

				Object arglist[] = {cmd, nextStep};

				long cinstr = cme.getInstructionCount(cmd, nextStep);
				if(cinstr >= instr) instr = cinstr;

				/* wait if the command requires to wait for a while */
				ComputeJob job =  new ComputeJob(instr, this);

				nodeRessources.addComputeJob(job);

				pendingComputeJobs.put(job, cmdStep);

				return ; /* we shall not run the command directly, instead wait for a time */
			}else if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
				getSimulator().getTraceWriter().endState(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);
			}

			/* now run the appropriate command to generate new events */
			debug("processing step: " + nextStep + " cmd: " + cmd);

			NetworkJobs oldJobs = cmdStep.getNetworkJobs();

			final CommandProcessing newJob = cmdStep;
			newJob.resetState();

			cme.process(cmd, newJob, this, nextStep, oldJobs);

			// now iterate until a state can be found which does some work.
			while(! newJob.isCommandWaitingForResponse() && newJob.getNextStep() != CommandProcessing.STEP_COMPLETED){
				int nextPStep = newJob.getNextStep();
				newJob.resetState();

				cme.process(cmd, newJob, this, nextPStep, null);

				if (newJob.nextStep == nextPStep){
					throw new IllegalArgumentException("(likely) endless iteration detected in command " + cmd);
				}
			}

			//System.out.println(this + " " + nextStep + " starting " + newJob.nextStep + " " + newJob.getNetworkJobs().getSize());

			if (! newJob.isCommandWaitingForResponse()){ /* all blocking operation completed */
				commandCompleted(cmdStep, cme, curTime);
			}else{
				if(newJob.getNestedOperation() != null){
					// start nested operation.

					assert(newJob.getNestedOperation().getParentOperation() == newJob);
					processCommandStep(newJob.getNestedOperation(), curTime, true);

					return;
				}

				if(newJob.isBlockingEnforced()){
					/* just block, the job SHOULD wake up itself... ! */

					//System.out.println("blocking on " +  cmd + " " + this.getIdentifier());

					blockedCommands.add(newJob);
				}

				assert(newJob.getNetworkJobs() != null);
				assert(newJob.getNetworkJobs().getNetworkJobs() != null);

				if(newJob.getNetworkJobs().getSize() > 0){
					pendingNetworkOperations.put(newJob.getNetworkJobs(), newJob);

					for(InterProcessNetworkJob j: newJob.getNetworkJobs().getNetworkJobs()){
						assert(j.getMatchingCriterion().getTargetComponent() != null);

						pendingJobs.put(j, newJob.getNetworkJobs());

						if(j.getJobOperation() == InterProcessNetworkJobType.RECEIVE){
							getNetworkInterface().initiateInterProcessReceive(j, curTime);
						}else{
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

			newJob = new CommandProcessing(cmd, this, time);
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
			debug(" resp: " + jobs.getResponses().size() + " " + endTime);

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
		info("uses Program: \"" + clientProgram.getApplication().getApplicationName() + "\" alias: \"" +
				clientProgram.getApplication().getAlias() + "\" rank " + getModelComponent().getRank());
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
