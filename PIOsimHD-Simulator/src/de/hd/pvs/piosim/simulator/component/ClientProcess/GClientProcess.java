
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
package de.hd.pvs.piosim.simulator.component.ClientProcess;

import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper.CommandType;
import de.hd.pvs.piosim.model.program.Program;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.component.Commands.CommandImplementation;
import de.hd.pvs.piosim.simulator.component.Commands.IWaitCommand;
import de.hd.pvs.piosim.simulator.component.NIC.GNIC;
import de.hd.pvs.piosim.simulator.component.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.component.Node.GNode;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

/**
 * Simulates a single client process, which processes instructions.
 * 
 * @author Julian M. Kunkel
 *
 */
public class GClientProcess 
extends SPassiveComponent<ClientProcess> 
implements ISNodeHostedComponent<SPassiveComponent<ClientProcess>>
{	
	/**
	 * Data structure which collects statistics per command type.
	 * 
	 * @author Julian M. Kunkel
	 */
	private static class CommandUsageStatistics{
		int calls = 0;
		Epoch totalTimeSpend = Epoch.ZERO;
	}


	/**
	 * Data structure mapping a bunch of network job to the command and the next step to 
	 * perform when the network operations finish.  
	 * 
	 * @author Julian M. Kunkel
	 */
	final private static class StructMapComputeJob{
		final Command     cmd;
		final int         nextStep;
		final NetworkJobs oldJobs;

		public StructMapComputeJob(Command cmd, NetworkJobs oldJobs, int nextStep) {
			this.cmd =  cmd;
			this.oldJobs = oldJobs;
			this.nextStep = nextStep;
		}
	}



	/**
	 * Maps the commands to the command's implementation. For all clients the 
	 * same implementation must be picked.  
	 */
	private final static HashMap<Class<? extends Command>, CommandImplementation> commandMap = 
		new HashMap<Class<? extends Command>, CommandImplementation>();;


		/**
		 * The node hosting this client process.
		 */
		private GNode attachedNode;

		/**
		 * Commands which are blocked.
		 */
		private HashSet<Command> blockedCommands = new HashSet<Command>(); 

		/**
		 * The currently processing blocking command.  
		 */
		private Command blockingCommand = null;

		/**
		 * the client program.
		 */
		private Program clientProgram = null;

		private HashMap<Class<? extends Command>, CommandUsageStatistics> commandStats = 
			new HashMap<Class<? extends Command>, CommandUsageStatistics>(); 

		/**
		 * did this client finish
		 */
		private boolean finished = false;;

		/** instruction counter, which command is the next one */
		private int nextCommandNumber = 0;

		private HashMap<ComputeJob, StructMapComputeJob>  pendingComputeJobs = new HashMap<ComputeJob, StructMapComputeJob>(); 

		/**
		 * if the client waits for a remote network operation this operation is stored here
		 */
		private HashMap<NetworkJobs, CommandStepResults> pendingNetworkOperations = new HashMap<NetworkJobs, CommandStepResults>();

		/**
		 * maps from AID / Asynchronous ID to the actual Command
		 */
		private HashMap<Integer, Command>     pendingNonBlockingOps = new HashMap<Integer, Command>();

		/**
		 * use this method to enforce the completion of the currently blocked job
		 * @param cmd
		 */
		public void activateBlockedCommand(Command cmd, int nextStep){
			if (! blockedCommands.remove(cmd)){
				throw new IllegalArgumentException("Error removing " + cmd + " from queue on client: " + this.getIdentifier());
			}

			assert(nextStep != CommandImplementation.STEP_START);

			processCommand(cmd, getSimulator().getVirtualTime(), true, nextStep, null);
		}

		@Override
		public void computeJobCompletedCV(ComputeJob job) {
			debug("reactivating client " + job);

			StructMapComputeJob cmd = pendingComputeJobs.remove(job);

			assert(cmd != null);

			//assert(cmd.nextStep != CommandImplementation.STEP_START); this can actually happen, before the first step is started it computes.

			processCommand(cmd.cmd, getSimulator().getVirtualTime(), false, cmd.nextStep, cmd.oldJobs);
		}

		@Override
		public GNode getAttachedNode() {
			return attachedNode;
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
		public void jobsCompletedCB(NetworkJobs jobs, Epoch endTime) {
			debug(" resp: " + jobs.getResponses().size() + " " + endTime);
			
			/* reactivate this client, we have to process the next command */
			CommandStepResults pendingOp = pendingNetworkOperations.remove(jobs);
			assert(pendingOp != null);


			/* If too many eager operations queue up this might lead to a stack overflow, therefore we add a virtual instruction
			 * count. The error sequence otherwise looks like:
			 *    at de.hd.pvs.piosim.simulator.component.glue.GClient.processCommand(GClient.java:405)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.processNextCommands(GClient.java:256)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.commandCompleted(GClient.java:329)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.processCommand(GClient.java:387)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.jobsCompletedCB(GClient.java:240)
	        at de.hd.pvs.piosim.simulator.component.glue.GNIC.completeReceive(GNIC.java:328)
	        at de.hd.pvs.piosim.simulator.component.glue.GNIC.submitNewNetworkJob(GNIC.java:395)
	        at de.hd.pvs.piosim.simulator.component.glue.GNODE.submitNewNetworkJob(GNODE.java:104)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.processCommand(GClient.java:405)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.processNextCommands(GClient.java:256)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.commandCompleted(GClient.java:329)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.processCommand(GClient.java:387)
	        at de.hd.pvs.piosim.simulator.component.glue.GClient.jobsCompletedCB(GClient.java:240)
	        at de.hd.pvs.piosim.simulator.component.glue.GNIC.completeReceive(GNIC.java:328)

			 */

			assert(pendingOp.getNextStep() != CommandImplementation.STEP_START);

			processCommand(pendingOp.getInvokingCommand(), endTime, true, pendingOp.getNextStep(), 
					pendingOp.getJobs());
		}

		/** 
		 * This shall never be called in a client:
		 */
		@Override
		public void receiveCB(SingleNetworkJob job, SingleNetworkJob response, Epoch endTime) {
			assert(false);
		}


		@Override
		public void recvMsgPartCB(GNIC nic, MessagePart part, Epoch endTime) {
			assert(false);
		}


		@Override
		public void sendMsgPartCB(GNIC nic, MessagePart part, Epoch endTime) {
			assert(false);
		}

		@Override	
		public void simulationFinished() {
			System.out.println(this.getIdentifier());
			for(Class<?> cmdClass: commandStats.keySet()){
				CommandUsageStatistics stat = commandStats.get(cmdClass);
				infoFollowUpLine(cmdClass.getSimpleName() + " " + stat.calls + " calls " + stat.totalTimeSpend);
			}
			
			if (pendingNonBlockingOps.size() != 0)
				System.out.println( " still pending non-blocking operations: " + pendingNonBlockingOps.size());

			if( isProgramFinished())
				return;		
			
			System.out.println("Client got pending operations: " + this.getIdentifier() + ": ");

			for (CommandStepResults pending: pendingNetworkOperations.values()) {
				System.out.println("   " + pending + " with NetworkOperation: " + pendingNetworkOperations.get(pending));
			}

			for(Command pendingBlocked: pendingNonBlockingOps.values()) {
				System.out.println("   " + pendingBlocked);
			}
		}


		/**
		 * Gets invoked from the simulator to start with the  first set of commands.
		 */
		public void startProcessing(){
			clientProgram = getSimulator().getModel().getProgram(getModelComponent()); 

			if(clientProgram == null){
				warn("does not have a valid Program ");
				finished = true;
			}else{
				info("uses Program: \"" + clientProgram.getApplication().getApplicationName() + "\" alias: \"" + 
						clientProgram.getApplication().getAlias() + "\" rank " + getModelComponent().getRank());
				processNextCommands();
			}		 
		}

		/**
		 * Check if this client is already finished.
		 */
		private void checkSetFinishState(){
			if( pendingComputeJobs.size() == 0 && pendingNetworkOperations.size() == 0 && 
					clientProgram.getSize() == nextCommandNumber){
				finished = true;

				info("finished");
			}
		}

		private void traceCommand(Command cmd, boolean start){

			if(start == false) {
				if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class){
					getSimulator().getTraceWriter().end(TraceType.CLIENT, this, cmd.getClass().getSimpleName());
				}else{
					getSimulator().getTraceWriter().event(TraceType.CLIENT, this, cmd.getClass().getSimpleName() + " end", 0);
				}
			}else {
				if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class){
					getSimulator().getTraceWriter().start(TraceType.CLIENT, this, cmd.getClass().getSimpleName());
				}else{
					getSimulator().getTraceWriter().event(TraceType.CLIENT, this, cmd.getClass().getSimpleName() + " start", 0);
				}
			}


		}

		private void commandCompleted(Command cmd, Epoch time){
			//if(cmd.getClass() != Compute.class)
			//	getSimulator().getTraceWriter().end(this, cmd.getClass().getSimpleName() + " s " + nextStep);

			debug("command completed: " + cmd);

			CommandUsageStatistics statistic = commandStats.get(cmd.getClass());
			if(statistic == null){
				statistic = new CommandUsageStatistics();
				commandStats.put(cmd.getClass(), statistic);
			}
			//TODO statistic.totalTimeSpend =  new Time(time.subtract(commandStartTime), statistic.totalTimeSpend); 
			statistic.calls++;

			traceCommand(cmd, false);

			//if it was the blocking Command, then we might continue!
			if(cmd.isAsynchronous()){
				pendingNonBlockingOps.remove(cmd.getAsynchronousID());

				if(blockingCommand == null){
					checkSetFinishState();
				}else if(blockingCommand.getClass() == Wait.class){ // check if we are blocked with a WAIT command right now
					// TODO extract interface for WAIT
					CommandImplementation<Wait> cme = commandMap.get(blockingCommand.getClass());				
					((IWaitCommand) cme).pendingAIOfinished((Wait) blockingCommand, this, cmd.getAsynchronousID());
				}

			}else{
				processNextCommands();
			}


		}

		/**
		 * Increment instruction counter and return the next command to process
		 * Sets the command or null if the client finished processing
		 */
		private Command getNextCommand(){		
			if(clientProgram.getSize() == nextCommandNumber){
				checkSetFinishState();
				return null;
			}

			return clientProgram.getCommands().get(nextCommandNumber++);
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
			CommandType cMethodMapping = DynamicCommandClassMapper.getCommandImplementationGroup(what.getClass().getSimpleName());
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
		 * @param time
		 * @param shallCompute
		 * @param nextStep
		 * @param compJobs
		 */
		private void processCommand(Command cmd, Epoch time, boolean shallCompute, int nextStep, NetworkJobs compJobs){
			//System.out.println("ProcessingCommnad " + cmd + " " + time + " " + shallCompute + " " + nextStep);


			if(nextStep == CommandImplementation.STEP_COMPLETED){
				commandCompleted(cmd, time);
				return;
			} // END STEP_COMPLETED

			CommandImplementation cme = commandMap.get(cmd.getClass());

			if(cme == null)
				cme = instanciateCommandImplemtation(cmd);


			try{			
				if(shallCompute){					
					if(nextStep == CommandImplementation.STEP_START){
						traceCommand(cmd, true);
					}

					if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
						getSimulator().getTraceWriter().start(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);			
					}


					/* 
					 * in order to make a single call visible we have to make sure that the simulator time increases between two
					 * subsequent operations. Also this prevents buffer overflows in commands which finish immediately.
					 */
					long instr = getAttachedNode().getMinimumNumberInstructions();

					Object arglist[] = {cmd, nextStep};

					long cinstr = cme.getInstructionCount(cmd, nextStep);
					if(cinstr >= instr) instr = cinstr;

					/* wait if the command requires to wait for a while */					
					ComputeJob job =  new ComputeJob(instr, this);
					getAttachedNode().addComputeJob(job);

					pendingComputeJobs.put(job, new StructMapComputeJob(cmd, compJobs, nextStep));

					return ; /* we shall not run the command directly, instead wait for a time */		
				}else if(! cmd.isAsynchronous() && cmd.getClass() != Compute.class ) {
					getSimulator().getTraceWriter().end(TraceType.CLIENT_STEP, this, cmd.getClass().getSimpleName() + " s " + nextStep);			
				}

				/* now run the appropriate command to generate new events */
				debug("processing step: " + nextStep + " cmd: " + cmd);

				CommandStepResults newJob = cme.process(cmd, this, nextStep, compJobs);

				if (newJob == null){ /* blocking operation completed */							
					commandCompleted(cmd, time);				
				}else{				
					newJob.setInvokingCommand(cmd);

					if(newJob.isBlockingEnforced()){
						/* just block, the job SHOULD wake up itself... ! */

						//System.out.println("blocking on " +  cmd + " " + this.getIdentifier());

						blockedCommands.add(newJob.getInvokingCommand());
						return;
					}

					assert(newJob.getJobs() != null);
					assert(newJob.getJobs().getNetworkJobs() != null);

					pendingNetworkOperations.put(newJob.getJobs(), newJob);

					for(SingleNetworkJob j: newJob.getJobs().getNetworkJobs()){
						getAttachedNode().submitNewNetworkJob(j);
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
			Command cmd;

			do{
				cmd = getNextCommand();
				if (cmd == null) 
					return;

				processCommand(cmd, time, true, CommandImplementation.STEP_START, null);


				if( cmd.getAsynchronousID() == null) // command blocks
					break;

				Command oldCommand = pendingNonBlockingOps.put(cmd.getAsynchronousID(), cmd);
				if(oldCommand != null){
					throw new IllegalArgumentException( getIdentifier() + " uses an asynchronous ID which is currently used\n" +
							" instruction #" + nextCommandNumber + " aid:" + cmd.getAsynchronousID() );
				}
			}while(true);

			blockingCommand = cmd;
		}
		
		@Override
		public void setSimulatedModelComponent(ClientProcess comp, Simulator sim)
				throws Exception {
			super.setSimulatedModelComponent(comp, sim);
			
			attachedNode = (GNode) sim.getSimulatedComponent(comp.getParentComponent());
		}
}
