
/** Version Control Information $Id: GRefinedDiskModel.java 781 2010-07-18 10:51:59Z kunkel $
 * @lastmodified    $Date: 2010-07-18 12:51:59 +0200 (So, 18. Jul 2010) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 781 $
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
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SSchedulableBlockingComponent;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOJob;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;

/**
 * A IOSubystem can run only one job at a time, however the server might keep a small
 * list of pending I/O jobs to optimize access on this layer. This is especially
 * useful for RAID systems which might be able to schedule multiple requests concurrently.
 * Implements NCQ with some kind of Scan algorithm to find the best next job.
 *
 */
public class GRefinedDiskModel
extends SSchedulableBlockingComponent<RefinedDiskModel, IOJob>
implements IGIOSubsystem<RefinedDiskModel>
{

	public enum IOEfficiency{
		NOSEEK,
		SHORTSEEK,
		AVGSEEK
	}

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

	static private Comparator<IOJob<?,StreamIOOperation>> offsetComparator = new Comparator<IOJob<?,StreamIOOperation>>(){
		public int compare(IOJob<?,StreamIOOperation> o1, IOJob<?,StreamIOOperation> o2) {
			return o1.getOperationData().getOffset() < o2.getOperationData().getOffset() ? -1
					:  (o1.getOperationData().getOffset() > o2.getOperationData().getOffset() ? +1 :0);
		}
	};;

	static private class JobQueuePerFile{
		PriorityQueue<IOJob<?,StreamIOOperation>> pendingIOJobs = new PriorityQueue<IOJob<?,StreamIOOperation>>(2, offsetComparator);
		LinkedList<IOJob>  pendingFlushOperations = new LinkedList<IOJob>();
	}

	final GRefinedDiskModelInformation runtimeInformation = new GRefinedDiskModelInformation();


	/**
	 * Number of pending I/Os
	 */
	int pendingIOs = 0;

	/**
	 * Pending file list, first element get scheduled when pendingJobsWithLargerOffset is empty
	 */
	LinkedList<FileMetadata> scheduledFiles = new LinkedList<FileMetadata>();

	/**
	 * The last file accessed
	 */
	FileMetadata lastFileScheduled = null;

	/**
	 * The last offset used
	 */
	long lastAccessPosition = 0;

	/**
	 * The I/O efficiency of the currently scheduled I/O.
	 */
	IOEfficiency jobIOEfficiency = null;

	/**
	 * These jobs are scheduled before another MPIFile is chosen from the HashMap
	 */
	PriorityQueue<IOJob<?,StreamIOOperation>> pendingJobsWithLargerOffset = new PriorityQueue<IOJob<?,StreamIOOperation>>();
	LinkedList<IOJob>                		  pendingFlushOperations = new LinkedList<IOJob>();

	HashMap<FileMetadata, JobQueuePerFile> pendingOps = new HashMap<FileMetadata, JobQueuePerFile>();

	@Override
	public int getNumberOfBlockedJobs() {
//		debug(pendingIOs + "");
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
	protected Event<IOJob> getNextPendingAndSchedulableEvent() {
		if(pendingIOs == 0){
			return null;
		}

		// first run I/O jobs, then flush operations:
		if(pendingJobsWithLargerOffset.size() > 0){
			pendingIOs--;
			return new Event(this, this, Epoch.ZERO, pendingJobsWithLargerOffset.poll(), null);
		}

		if(pendingFlushOperations.size() > 0){
			pendingIOs--;
			// run flush operations.
			return new Event(this, this, Epoch.ZERO, pendingFlushOperations.poll(), null);
		}

		// no more ops for current file
		FileMetadata nextFile = scheduledFiles.poll();
		assert(nextFile != null);

		JobQueuePerFile jobs = pendingOps.remove(nextFile);
		assert(jobs != null);
		pendingJobsWithLargerOffset = jobs.pendingIOJobs;
		pendingFlushOperations = jobs.pendingFlushOperations;

		return getNextPendingAndSchedulableEvent();
	}

	@Override
	protected void addNewEvent(Event<IOJob> job) {
		throw new IllegalArgumentException("Not allowed!");
	}

	@Override
	public void setIOCallback(IIOSubsystemCaller callback) {
		this.callback = callback;
	}

	@Override
	protected Epoch getProcessingTimeOfScheduledJob(IOJob job) {
		switch(job.getOperationType()){
		case FLUSH:
			jobIOEfficiency = IOEfficiency.NOSEEK;
			return new Epoch(0);
		case READ:
		case WRITE:
			jobIOEfficiency = null;
			final StreamIOOperation sio = ((IOJob<?,StreamIOOperation>) job).getOperationData();

			final double avgRotationalLatency = 30.0 / getModelComponent().getRPM();
			double latency = 0;
			double transferTime = sio.getSize() / (double) getModelComponent().getSequentialTransferRate();

			// compute if it is close to the old region.

			final FileMetadata file = job.getFile();

			if(	lastFileScheduled == file	) {
				if (sio.getOffset() == lastAccessPosition){
					// we assume the position is now fixed.
					//if (job.getType() == IOOperation.READ){
					latency = 0;
					//}else{
					// add the rotational latency, because the data had to be transfered to the cache.
					//latency = avgRotationalLatency;
					//}
					runtimeInformation.noSeekAccesses++;

					jobIOEfficiency = IOEfficiency.NOSEEK;
				}else	if(Math.abs(sio.getOffset() - lastAccessPosition) < getModelComponent().getPositionDifferenceConsideredToBeClose()){
					// it is close to the old position
					latency = avgRotationalLatency + getModelComponent().getTrackToTrackSeekTime().getDouble();
					runtimeInformation.fastAccesses++;

					jobIOEfficiency = IOEfficiency.SHORTSEEK;
				}
			}

			if (jobIOEfficiency == null){
				latency = avgRotationalLatency + getModelComponent().getAverageSeekTime().getDouble();

				jobIOEfficiency = IOEfficiency.AVGSEEK;
			}

			runtimeInformation.totalAmountOfData += sio.getSize();

			lastFileScheduled = file;
			lastAccessPosition = sio.getOffset() + sio.getSize();

			return new Epoch(latency + transferTime);
		default:
			throw new IllegalArgumentException("Not implemented");
		}
	}


	@Override
	protected void jobStarted(Event<IOJob> event, Epoch startTime) {
		IOJob job = event.getEventData();
		IOSubsytemHelper.traceIOStart(this, job, jobIOEfficiency.toString());
		//System.out.println("jobStarted "  + startTime + " " + event.getEventData());
	}

	@Override
	protected void jobCompleted(Event<IOJob> event, Epoch endTime) {
		IOJob job = event.getEventData();
		//		System.err.println("jobCompleted " + endTime + " " + event.getEventData());

		IOSubsytemHelper.traceIOEnd(this, job, jobIOEfficiency.toString());

		runtimeInformation.totalOperations++;

		callback.IOComplete(endTime, job);
	}

	@Override
	public void startNewIO(IOJob io) {

		pendingIOs++;

		final FileMetadata file = io.getFile();

		// try to schedule operations if possible:
		if(file == lastFileScheduled){
			switch(io.getOperationType()){
			case READ:
			case WRITE:
				StreamIOOperation sio = ((IOJob<?,StreamIOOperation>) io).getOperationData();
				// if the same file is accessed with a larger offset add it to the jobs currently to process
				if(sio.getOffset() >= lastAccessPosition){
					pendingJobsWithLargerOffset.add(io);

					startNextPendingEventIfPossible(getSimulator().getVirtualTime());
					return;
				}
			}
		}


		JobQueuePerFile fileQueue = pendingOps.get(file);

		if(fileQueue == null){
			fileQueue = new JobQueuePerFile();
			pendingOps.put(file, fileQueue);

			scheduledFiles.add(file);
		}

		if(io.getOperationType() == IOOperationType.FLUSH){
			// flush must be run always after the current ops finished!
			fileQueue.pendingFlushOperations.add(io);
		}else{
			fileQueue.pendingIOJobs.add(io);
		}

		startNextPendingEventIfPossible(getSimulator().getVirtualTime());
	}

	@Override
	public void simulationFinished() {
		assert(pendingIOs == 0);
	}

}
