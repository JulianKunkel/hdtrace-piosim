
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

import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;


/**
 * Always takes the earliest submitted I/O operation and tries to combine it with as many (overlapping) operations.
 * First a file is selected, then all READ operations are scheduled, then WRITE operations or FLUSH operations.
 * Flush operations are not combined at all.
 *
 * No starvation of jobs.
 *
 * @author Julian M. Kunkel
 *
 */
public class GAggregationCache extends GSimpleWriteBehind {

	@Override
	protected IOJobQueue initJobQueue(){
		return new IOJobQueue() {

			/**
			 * Queued read operations, read and write operations are split.
			 */
			final LinkedList<IOJob<?,IOOperationData>> queuedReadJobs = new LinkedList<IOJob<?,IOOperationData>>();

			/**
			 * Contains write or flush operations.
			 */
			final LinkedList<IOJob<?,IOOperationData>> queuedWriteJobs = new LinkedList<IOJob<?,IOOperationData>>();

			private IOJob combineIOJobs(LinkedList<IOJob<?,IOOperationData>> list){
				final IOJob scheduledJob = list.poll();

				if(scheduledJob == null){
					return null;
				}

				if(scheduledJob.getOperationType() ==  IOOperationType.FLUSH){
					return scheduledJob;
				}

				// stream I/O:
				long size;
				long offset;

				final StreamIOOperation opdata = ((IOJob<?, StreamIOOperation>) scheduledJob).getOperationData();

				final LinkedList<IOJob> combinedOps = new LinkedList<IOJob>();

				size = opdata.getSize();
				offset = opdata.getOffset();

				// try to combine several operations. Once the data is combined, rerun - Runtime: N^2
				boolean changed = true;
				outer:
					while(changed) {
						Iterator<IOJob<?, IOOperationData>> it = list.iterator();
						changed = false;

						while(it.hasNext()) {
							final IOJob cur = it.next();

							if( cur.getOperationType() == scheduledJob.getOperationType()
									&& scheduledJob.getFile() == cur.getFile()
									&& cur.getOperationType() != IOOperationType.FLUSH )
							{
								final StreamIOOperation akt = (StreamIOOperation) cur.getOperationData();

								if(size + offset == akt.getOffset()) {
									// forward combination.
									if (size + akt.getSize()  > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
										break outer;
									}

									size += akt.getSize();

									it.remove();
									combinedOps.add(cur);

									changed = true;
								}else if( akt.getOffset() + akt.getSize() == offset ) {
									// backwards combination
									if (size + akt.getSize()  > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
										break outer;
									}

									size += akt.getSize();
									offset = akt.getOffset();

									it.remove();
									combinedOps.add(cur);
									changed = true;
								}
							}
						}
					}
				if( combinedOps.size() == 0){
					// no manipulation made
					return scheduledJob;
				}else{
					// use a combined operation.
					return new IOJobCoalesced(scheduledJob.getFile(), scheduledJob.getOperationType(), new StreamIOOperation(size, offset));
				}
			}

			@Override
			public IOJob getNextSchedulableJob(long freeMemory, GlobalSettings settings) {
				if(  ! queuedReadJobs.isEmpty() &&
						freeMemory > (((StreamIOOperation) queuedReadJobs.peek().getOperationData()).getSize())  )
				{
					// reserve memory for READ requests
					return combineIOJobs(queuedReadJobs);
				}

				return combineIOJobs(queuedWriteJobs);
			}

			@Override
			public void addIOJob(IOJob<InternalIOData, IOOperationData> job) {
				switch(job.getOperationType()){
				case FLUSH:
				case WRITE:
					queuedWriteJobs.add(job);
					break;
				case READ:
					queuedReadJobs.add(job);
				}
			}
		};
	}

	@Override
	public boolean canIPutDataIntoCache(RequestWrite clientJob, long bytesOfWrite) {
		return serverProcess.getNodeRessources().isEnoughFreeMemory(bytesOfWrite);
	}
}
