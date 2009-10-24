
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

package de.hd.pvs.piosim.simulator.components.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.components.superclasses.NodeHostedComponent;
import de.hd.pvs.piosim.simulator.base.SBasicComponent;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;


/**
 * Implements a Node containing several CPUs.
 * Ensure computation of all participating tasks is done by multiplexing the resources
 * among them.
 *
 * @author Julian M. Kunkel
 *
 */
public class GNode<ModelComp extends Node>
extends SBasicComponent<ModelComp>
implements INodeRessources
{
	/**
	 * Clients contained in this node.
	 */
	private ArrayList<GClientProcess> clients = new ArrayList<GClientProcess>();

	/**
	 * Servers contained in this node.
	 */
	private ArrayList<IGServer> servers = new ArrayList<IGServer>();

	/**
	 * List of compute jobs which are executed right now.
	 */
	private ArrayList<ComputeJob> pendingComputeJob = new ArrayList<ComputeJob>();

	/**
	 * List of hosted components on this node.
	 */
	private HashMap<ComponentIdentifier, ISNodeHostedComponent<?>> hosted =
		new HashMap<ComponentIdentifier, ISNodeHostedComponent<?>>();

	/**
	 * Amount of free memory.
	 */
	private long freeMemory;

	/**
	 * Check if enough memory is available.
	 *
	 * @param required
	 * @return
	 */
	@Override
	public boolean isEnoughFreeMemory(long required){
		return freeMemory > required;
	}

	/**
	 * Reserve an amount of main memory on this node.
	 * @param required
	 */
	@Override
	public void reserveMemory(long required){
		freeMemory -= required;
	}

	/**
	 * Free some memory. Beware to free not more than which got used.
	 * @param howMuch
	 */
	@Override
	public void freeMemory(long howMuch){
		freeMemory += howMuch;

		assert(freeMemory <= getModelComponent().getMemorySize());
	}

	@Override
	public void setModelComponent(ModelComp comp) throws Exception {
		super.setModelComponent(comp);

		// initialize freeMemroy
		freeMemory = comp.getMemorySize();

		// add the hosted components
		for(final NodeHostedComponent c: comp.getHostedComponents()){
			ISNodeHostedComponent scomp = null;

			if(c.getObjectType().equals("Server")){
				IGServer serv = (IGServer) getSimulator().instantiateSimObjectForModelObj(c);
				servers.add(serv);
				scomp = serv;

			}else if(c.getObjectType().equals("ClientProcess")){
				GClientProcess e = (GClientProcess) getSimulator().instantiateSimObjectForModelObj(c);
				clients.add(e);
				scomp = e;
			}else{
				assert(false);
			}

			hosted.put(scomp.getIdentifier(), scomp);

			// initalize network-interface for process
			IProcessNetworkInterface networkInterface = (IProcessNetworkInterface) getSimulator().instantiateSimObjectForModelObj(c.getNetworkInterface());
			scomp.setNetworkInterface(networkInterface);
			scomp.setNodeRessources(this);
		}
	}

	public Collection<GClientProcess> getClients() {
		return clients;
	}

	public Collection<IGServer> getServers() {
		return servers;
	}

	/**
	 * Check if the target component is hosted on this node.
	 * @param component
	 * @return
	 */
	public boolean isNodeLocal(ComponentIdentifier component){
		return hosted.containsKey(component);
	}

	/**
	 * @return the hosted processes
	 */
	public HashMap<ComponentIdentifier, ISNodeHostedComponent<?>> getHostedComponents() {
		return hosted;
	}

	public int getNumberOfActiveComputeJobs(){
		return pendingComputeJob.size();
	}

	/**
	 * Calculate the number of instructions per Job. Share the CPUs equal among the
	 * jobs. If there are more jobs than CPUs some of them stay idle.
	 *
	 * @param activeJobs
	 * @return
	 */
	final private long getInstructionsPerJobAndSecond(int activeJobs){
		return getNumberOfActiveComputeJobs() <= getModelComponent().getCPUs() ? getModelComponent().getInstructionsPerSecond() :
			getModelComponent().getInstructionsPerSecond() * getModelComponent().getCPUs() / activeJobs;
	}

	/**
	 * The epoch the first (or a set of) compute job(s) will finish.
	 */
	private Epoch computeNextActiveEpoch = null;

	/**
	 * The epoch the computation started.
	 */
	private Epoch computeStartEpoch = null;

	/**
	 * Used by the hosted components to add a compute job.
	 * @param job
	 */
	@Override
	public void addComputeJob(ComputeJob job){
		int activeJobs = getNumberOfActiveComputeJobs();

		assert(! pendingComputeJob.contains(job));

		pendingComputeJob.add(job);

		ComputeJob shortestJobNow = job;

		if(activeJobs != 0){
			Epoch currentTime = getSimulator().getVirtualTime();
			shortestJobNow = someProcessingHappened(activeJobs, currentTime);
		}

		UpdateActiveTime(shortestJobNow);
	}

	/**
	 * Compute the time for the earliest compute job.
	 * @param earliestJob
	 */
	private void UpdateActiveTime(ComputeJob earliestJob){
		// remove old waiting time

		if (earliestJob == null){
			// no more jobs pending
			computeNextActiveEpoch = null;
			computeStartEpoch = null;
		}else{
			Epoch currentTime = getSimulator().getVirtualTime();
			computeStartEpoch = currentTime;

			computeNextActiveEpoch = currentTime.add(
					(double) earliestJob.remainingCycles      /      getInstructionsPerJobAndSecond(getNumberOfActiveComputeJobs())
			);

			debug(" " + earliestJob.getRemainingCycles() + " next active: " + computeNextActiveEpoch);

			updateWakeupTimer(computeNextActiveEpoch);
		}
	}

	/**
	 * Update the pending cycles in all pending compute jobs (expect new ones).
	 * Complete the jobs if no cycles are left by calling the method.
	 *
	 * @param activeJobsBefore The number of active jobs in the previous update phase.
	 * @return One of the currently earliest finished job.
	 */
	private ComputeJob someProcessingHappened(int activeJobsBefore, Epoch currentTime){
		// now the time is nextActiveTime, i.e. we have to update all currently pending I/O jobs
		// One Job should be finished now !
		ComputeJob shortestJobNow = null;

		assert(computeStartEpoch != null);

		// how many cycles got processed so far.
		long processedCycles = 0;

		if ( ! computeStartEpoch.equals(currentTime)){
			processedCycles = (long) (currentTime.subtract(computeStartEpoch).getDouble() *
					getInstructionsPerJobAndSecond(activeJobsBefore)) + 1; // add 1 for rounding errors.

			computeStartEpoch = currentTime;
		}

		//System.out.println(this.getName() + " "  + currentTime.subtract(startTime) +  " SPH: " + processedCycles + " T0: " + startTime  + " " + nextActiveTime + " " + " IPJS " + getInstructionsPerJobAndSecond(getActiveComputeJobs()));

		// Update the pending cycles in all pending compute jobs (expect new ones).
		int max = activeJobsBefore; //pendingComputeJob.size(); Is this correct now ?

		debug(processedCycles + " pendingComputeJob " + pendingComputeJob.size());

		for(int i=0; i < max; i++){
			ComputeJob job = pendingComputeJob.get(i);

			debugFollowUpLine("Job remaining cycles: " + job.remainingCycles + " " + job.getComponent().getIdentifier() + " " + job);

			job.remainingCycles -= processedCycles;

			if(job.remainingCycles <= 0){
				// job finished
				pendingComputeJob.remove(i--);
				max--; //decrease maximum, computeJobCompletedCV could create new jobs (appended to the list)

				assert(! pendingComputeJob.contains(job));

				//notify pending client / server !!!
				job.getComponent().computeJobCompletedCV(job);
			}else if( shortestJobNow == null || shortestJobNow.remainingCycles > job.remainingCycles ){
				shortestJobNow = job;
			}
		}

		// check new jobs for shortest Job Time:
		for(int i=max; i < pendingComputeJob.size(); i++){
			ComputeJob job = pendingComputeJob.get(i);
			if( shortestJobNow == null || shortestJobNow.remainingCycles > job.remainingCycles ){
				shortestJobNow = job;
			}
		}
		//System.out.println(this.getName() + " fin " +  shortestJobNow);

		return shortestJobNow;
	}

	@Override
	public void processEvent(Event event, Epoch time) {
		assert(false);
	}

	@Override
	public void processInternalEvent(InternalEvent event, Epoch time) {
		// note: this method can be called even if there is no job finished

		if (computeNextActiveEpoch != null && computeNextActiveEpoch.equals(time)){
			ComputeJob shortestJobNow = someProcessingHappened(getNumberOfActiveComputeJobs(), time);
			UpdateActiveTime(shortestJobNow);
		}
	}

	/**
	 * Minimum number of instructions a compute job can have to be visible at the time scale.
	 * @return
	 */
	public long getMinimumNumberInstructions() {
		final long minInstructions = (long) (getModelComponent().getInstructionsPerSecond() * Epoch.getTimeResolution());
		if(minInstructions == 0) return 1;
		return minInstructions;
	}

}
