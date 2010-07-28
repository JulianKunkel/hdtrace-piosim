
/** Version Control Information $Id: GAggregationCache.java 718 2009-10-16 13:22:41Z kunkel $
 * @lastmodified    $Date: 2009-10-16 15:22:41 +0200 (Fr, 16. Okt 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 718 $
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
package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;


/**
 * Reorders I/O operations and tries to combine them with as many (overlapping) operations.
 * Flush operations are combined as well and performed in the correct order.
 * However, later operations might be scheduled first. Starvation of operations is possible!
 *
 * Reads are preferred over write requests.
 * Once no further read is possible (memory full), writeback all data, then start to read again.
 * Operations of the same file are preferred.
 *
 * Complexity of fetching next job: O(1) (amortized) - only the file with the next bigger offset is checked.
 * Complexity to add a new job: O(log(N)) - PriorityQueue.
 *
 * @author Julian M. Kunkel
 *
 */
public class GAggregationReorderCache extends GSimpleWriteBehind {

	@Override
	protected IOJobQueue initJobQueue(){
		return new IOJobQueue() {

			final Comparator<IOJob<?,StreamIOOperation>> offsetComparator = new Comparator<IOJob<?,StreamIOOperation>>(){
				public int compare(IOJob<?,StreamIOOperation> o1, IOJob<?,StreamIOOperation> o2) {
					return o1.getOperationData().getOffset() < o2.getOperationData().getOffset() ? -1
							:  (o1.getOperationData().getOffset() > o2.getOperationData().getOffset() ? +1 :0);
				}
			};

			final class JobQueuePerFile{
				PriorityQueue<IOJob> pendingWriteJobs = new PriorityQueue<IOJob>(2, (Comparator) offsetComparator);
				LinkedList<IOJob>  pendingFlushOperations = new LinkedList<IOJob>();
			}

			/**
			 * Pending file list, first element get scheduled when pendingJobsWithLargerOffset is empty
			 */
			LinkedList<FileMetadata> scheduledModifyingFiles = new LinkedList<FileMetadata>();

			LinkedList<FileMetadata> scheduledReadFiles = new LinkedList<FileMetadata>();

			/**
			 * The last file accessed
			 */
			FileMetadata lastFileScheduled = null;

			/**
			 * Defines if we write/flush or read currently.
			 */
			IOOperationType scheduledType = IOOperationType.READ;

			HashMap<FileMetadata, JobQueuePerFile>      pendingWriteOps = new HashMap<FileMetadata, JobQueuePerFile>();
			HashMap<FileMetadata, PriorityQueue<IOJob>> pendingReadOps = new HashMap<FileMetadata, PriorityQueue<IOJob>>();


			@Override
			public void addIOJob(IOJob<InternalIOData, IOOperationData> job) {
				final FileMetadata file = job.getFile();
				// try to schedule operations if possible:

				if(job.getOperationType() == IOOperationType.READ){
					PriorityQueue<IOJob> pendingReads = pendingReadOps.get(file);
					if(pendingReads == null){
						pendingReads = new PriorityQueue<IOJob>(2, (Comparator) offsetComparator);
						pendingReadOps.put(file, pendingReads);

						scheduledReadFiles.add(file);
					}
					pendingReads.add(job);
				}else{
					// now add operation to the pending operations.
					JobQueuePerFile fileQueue = pendingWriteOps.get(file);

					if(fileQueue == null){
						fileQueue = new JobQueuePerFile();
						pendingWriteOps.put(file, fileQueue);

						scheduledModifyingFiles.add(file);
					}


					switch(job.getOperationType()){
					case WRITE:
						fileQueue.pendingWriteJobs.add(job);
						break;
					case FLUSH:
						fileQueue.pendingFlushOperations.add(job);
						break;
					}
				}
			}


			private IOJob combineIOJobs(PriorityQueue<IOJob> list, final long ioGranularity){
				final IOJob<InternalIOData, StreamIOOperation> scheduledJob = list.poll();

				assert(scheduledJob.getOperationType() !=  IOOperationType.FLUSH);

				// stream I/O:
				long size;
				long offset;

				final LinkedList<IOJob> combinedOps = new LinkedList<IOJob>();

				size = scheduledJob.getOperationData().getSize();
				offset = scheduledJob.getOperationData().getOffset();

				final IOOperationType ioType = scheduledJob.getOperationType();

				if (ioType == IOOperationType.READ){
					nodeRessources.reserveMemory(size);
				}

				// try to combine several operations. Once the data is combined, rerun - Runtime: N^2

				IOJob<InternalIOData, StreamIOOperation> nextJob = list.peek();
				// check if the nextJob overlaps AND if we can combine them i.e. size of the new operation is smaller than ioGranularity.
				if(nextJob != null){
					//System.out.println(size + " " + offset + " next one " +  nextJob.getOperationData().getOffset() + " " + nextJob.getOperationData().getSize());

					while(true){
						long myOffset = nextJob.getOperationData().getOffset();
						long mySize = nextJob.getOperationData().getSize();

						// check if we read data multiple times:
						long overlapSize = (offset + size) -  myOffset;

						if((offset + size) >= myOffset && mySize + size - overlapSize <= ioGranularity ){

							// reserve additional memory for the I/O operation, because we will free it, once data is sent completely!
							if (ioType == IOOperationType.READ ){
								if(nodeRessources.isEnoughFreeMemory(mySize)){
									nodeRessources.reserveMemory(mySize);
								}else{
									break;
								}
							}

							list.poll(); // nextJob = list.poll() is identical!

							// combine them:
							combinedOps.add(nextJob);

							size += mySize - overlapSize;

							nextJob = list.peek();
							if(nextJob == null){
								break;
							}
						}else{
							break;
						}
					}
				}

				if( combinedOps.size() == 0){
					// no manipulation made
					return scheduledJob;
				}else{
					combinedOps.add(scheduledJob);
					// use a combined operation.
					return new IOJobCoalesced(scheduledJob.getFile(), scheduledJob.getOperationType(), new StreamIOOperation(size, offset), combinedOps);
				}
			}

			@Override
			public IOJob getNextSchedulableJob(GlobalSettings settings) {
				final boolean enoughMemoryFreeForReads = nodeRessources.isEnoughFreeMemory( settings.getIOGranularity() );

				if(scheduledType == IOOperationType.READ){
					if(enoughMemoryFreeForReads){
						// do we have further reads to start?

						if(scheduledReadFiles.size() > 0){
							// prefer last scheduled file
							PriorityQueue<IOJob> pendingJobs = pendingReadOps.get(lastFileScheduled);
							if(pendingJobs == null){
								// no further reads for last accessed file.
								lastFileScheduled = scheduledReadFiles.peek();
								pendingJobs = pendingReadOps.get(lastFileScheduled);
							}

							IOJob job = combineIOJobs(pendingJobs, settings.getIOGranularity());
							if(pendingJobs.size() == 0){
								scheduledReadFiles.remove(lastFileScheduled);
								pendingReadOps.remove(lastFileScheduled);
							}
							return job;
						}
						// check if we have further reads:
					}

					scheduledType = IOOperationType.WRITE;
					// otherwise not enough RAM free or no pending operations
				}

				// write path
				assert(scheduledType != IOOperationType.READ);

				// check if we have further write operations:

				if(scheduledModifyingFiles.size() > 0){
					// prefer last scheduled file
					JobQueuePerFile pendingJobs = pendingWriteOps.get(lastFileScheduled);
					if(pendingJobs == null){
						// no further reads for last accessed file.
						lastFileScheduled = scheduledModifyingFiles.peek();
						pendingJobs = pendingWriteOps.get(lastFileScheduled);
					}

					// try to schedule pending write operations
					if(pendingJobs.pendingWriteJobs.size() > 0 ){
						return combineIOJobs(pendingJobs.pendingWriteJobs, settings.getIOGranularity());
					}

					// now start flush:
					// check if we have flush operations pending:
					if(pendingJobs.pendingFlushOperations.size() > 0){
						// combine all pending flush operations together. This works, because all pending writes completed.
						final LinkedList<IOJob> combinedOps = pendingJobs.pendingFlushOperations;

						// clear all currently pending flush operations:
						pendingJobs.pendingFlushOperations = new LinkedList<IOJob>();

						if(combinedOps.size() == 1){
							return combinedOps.poll();
						}else{
							return new IOJobCoalesced(lastFileScheduled, IOOperationType.FLUSH, null, combinedOps);
						}
					}

					scheduledModifyingFiles.remove(lastFileScheduled);
					pendingWriteOps.remove(lastFileScheduled);

					// check if other files have data to write:
					if(scheduledModifyingFiles.size() > 0){
						return getNextSchedulableJob(settings);
					}
					// no more write operations.
				}

				// check if there are further read operations.
				scheduledType = IOOperationType.READ;
				if( scheduledReadFiles.size() > 0 && enoughMemoryFreeForReads ){
					return getNextSchedulableJob(settings);
				}

				return null;

			}
		};
	}

	@Override
	public boolean canIPutDataIntoCache(RequestWrite clientJob, long bytesOfWrite) {
		return serverProcess.getNodeRessources().isEnoughFreeMemory(bytesOfWrite);
	}
}
