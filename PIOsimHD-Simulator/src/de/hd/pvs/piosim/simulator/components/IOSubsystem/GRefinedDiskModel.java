
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

package de.hd.pvs.piosim.simulator.components.IOSubsystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SSchedulableBlockingComponent;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.IOJobRefined.IOEfficiency;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;

/**
 * A IOSubystem can run only one job at a time, however the server might keep a small
 * list of pending I/O jobs to optimize access on this layer. This is especially
 * useful for RAID systems which might be able to schedule multiple requests concurrently.
 * Implements NCQ with some kind of Scan algorithm to find the best next job.
 *
 */
public class GRefinedDiskModel
	extends SSchedulableBlockingComponent<RefinedDiskModel, IOJobRefined>
	implements IGIOSubsystem<RefinedDiskModel>
{

	IIOSubsystemCaller callback;

	public class GRefinedDiskModelInformation extends ComponentRuntimeInformation{
		/**
		 * Total number of I/Os done by this component
		 */
		int totalOperations = 0;

		/**
		 * Total amount of data accessed
		 */
		long totalAmountOfData = 0;

		/**
		 * Number of I/Os which did not require seeks
		 */
		int noSeekAccesses = 0;

		/**
		 * Number of I/Os which used short seeks
		 * If the last access is close to the current access use the track-to-track seek time.
		 */
		int fastAccesses = 0;


		public int getTotalOperations() {
			return totalOperations;
		}

		public long getTotalAmountOfData() {
			return totalAmountOfData;
		}

		public int getNoSeekAccesses() {
			return noSeekAccesses;
		}

		public int getFastAccesses() {
			return fastAccesses;
		}

		@Override
		public String toString() {
			return " <#ops, noSeekAccesses, fastAccesses, slowAccesses, dataAccessed> = <" +
					totalOperations + ", " + noSeekAccesses + ", " + fastAccesses + ", " +
					(totalOperations - noSeekAccesses - fastAccesses) + ", " + totalAmountOfData + ">";
		}


	}

	final GRefinedDiskModelInformation runtimeInformation = new GRefinedDiskModelInformation();

	/**
	 * The last file accessed
	 */
	MPIFile lastFile = null;

	/**
	 * The last offset used
	 */
	long lastAccessPosition = 0;

	/**
	 * These jobs are scheduled before another MPIFile is chosen from the HashMap
	 */
	PriorityQueue<Event<IOJobRefined>> pendingJobsWithLargerOffset = new PriorityQueue<Event<IOJobRefined>>();

	static private Comparator<Event<IOJobRefined>> offsetComparator = new Comparator<Event<IOJobRefined>>(){
		public int compare(Event<IOJobRefined> o1, Event<IOJobRefined> o2) {
			return o1.getEventData().getOffset() < o2.getEventData().getOffset() ? -1
					:  (o1.getEventData().getOffset() > o2.getEventData().getOffset() ? +1 :0);
		}
	};

	/**
	 * Number of pending I/Os
	 */
	int pendingIOs = 0;

	/**
	 * Pending file list, first element get scheduled when pendingJobsWithLargerOffset is empty
	 */
	LinkedList<MPIFile> pendingFiles = new LinkedList<MPIFile>();

	HashMap<MPIFile, PriorityQueue<Event<IOJobRefined>>> pendingOps =
		new HashMap<MPIFile, PriorityQueue<Event<IOJobRefined>>>();


	@Override
	public int getNumberOfBlockedJobs() {
		debug(pendingIOs + "");
		return pendingIOs;
	}

	@Override
	public void printWaitingEvents() {

	}

	@Override
	public ComponentRuntimeInformation getComponentInformation() {
		return runtimeInformation;
	}

	@Override
	protected Event<IOJobRefined> getNextPendingAndSchedulableEvent() {
		pendingIOs--;

		if(pendingJobsWithLargerOffset.size() > 0){
			return pendingJobsWithLargerOffset.poll();
		}

		MPIFile nextFile = pendingFiles.poll();
		assert(nextFile != null);

		pendingJobsWithLargerOffset = pendingOps.remove(nextFile);

		return pendingJobsWithLargerOffset.poll();
	}

	@Override
	protected void addNewEvent(Event<IOJobRefined> job) {
		pendingIOs++;

		IOJobRefined io = job.getEventData();

		// if the same file is accessed with a larger offset add it to the jobs currently to process
		if(io.getFile() == lastFile && io.getOffset() >= lastAccessPosition){
			pendingJobsWithLargerOffset.add(job);
			return;
		}

		PriorityQueue<Event<IOJobRefined>> set = pendingOps.get(io.getFile());
		if (set == null){
			set = new PriorityQueue<Event<IOJobRefined>>(2, offsetComparator);

			// schedule new operations:
			pendingOps.put(io.getFile(), set);
			pendingFiles.add(io.getFile());
		}

		set.add(job);
	}

	@Override
	public void setIOCallback(IIOSubsystemCaller callback) {
		this.callback = callback;
	}

	@Override
	protected Epoch getProcessingTimeOfScheduledJob(IOJobRefined job) {
		double avgRotationalLatency = 30.0 / getModelComponent().getRPM();
		double latency = 0;
		double transferTime = job.getSize() / (double) getModelComponent().getSequentialTransferRate();

		// compute if it is close to the old region.

		if(
				lastFile == job.getFile()
		)
		{
			if (job.getOffset() == lastAccessPosition){
				// we assume the position is now fixed.
				//if (job.getType() == IOOperation.READ){
				latency = 0;
				//}else{
				// add the rotational latency, because the data had to be transfered to the cache.
				//latency = avgRotationalLatency;
				//}
				runtimeInformation.noSeekAccesses++;

				job.setEfficiency(IOEfficiency.NOSEEK);
			}else	if(Math.abs(job.getOffset() - lastAccessPosition) < getModelComponent().getPositionDifferenceConsideredToBeClose()){
				// it is close to the old position
				latency = avgRotationalLatency + getModelComponent().getTrackToTrackSeekTime().getDouble();
				runtimeInformation.fastAccesses++;

				job.setEfficiency(IOEfficiency.SHORTSEEK);
			}
		}

		if (job.getEfficiency() == null){
			latency = avgRotationalLatency + getModelComponent().getAverageSeekTime().getDouble();

			job.setEfficiency(IOEfficiency.AVGSEEK);
		}

		lastFile = job.getFile();
		lastAccessPosition = job.getOffset() + job.getSize();

		return new Epoch(latency + transferTime);
	}


	@Override
	protected void jobStarted(Event<IOJobRefined> event, Epoch startTime) {
		IOJobRefined job = event.getEventData();
		IOSubsytemHelper.traceIOStart(this, job, job.getEfficiency().toString());
		//System.out.println("jobStarted "  + startTime + " " + event.getEventData());
	}

	@Override
	protected void jobCompleted(Event<IOJobRefined> event, Epoch endTime) {
		IOJobRefined job = event.getEventData();

//		System.err.println("jobCompleted " + endTime + " " + event.getEventData());

		IOSubsytemHelper.traceIOEnd(this, job, job.getEfficiency().toString());

		runtimeInformation.totalOperations++;
		runtimeInformation.totalAmountOfData += job.getSize();

		callback.IOComplete(endTime, job.getOldJob());
	}

	@Override
	public void startNewIO(IOJob job) {
		Epoch time = getSimulator().getVirtualTime();
		addNewEvent(new Event<IOJobRefined>(this, this, time, new IOJobRefined(job)));

		startNextPendingEventIfPossible(time);
	}

	@Override
	public void simulationFinished() {
		assert(pendingIOs == 0);
	}

}
