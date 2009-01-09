
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

import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;


/**
 * Always takes the earliest submitted I/O operation and tries to combine it with as many (overlapping) operations.
 * 
 * No starvation of jobs.
 * 
 * @author Julian M. Kunkel
 *
 */
public class GAggregationCache extends GSimpleWriteBehind {
	
	private IOJob combineOperation(LinkedList<IOJob> jobQueue, IOOperation type) {
	// pick up a write call
		long size;
		long offset;

		IOJob io = jobQueue.poll();
		
		if(io == null){
			return null;
		}
		
		size = io.getSize();
		offset = io.getOffset();
		
		// try to combine several operations. Runtime: N^2 
		boolean changed = true;
		outer: while(changed) {
			Iterator<IOJob> it = jobQueue.iterator();
			changed = false;
			
			while(it.hasNext()) {
				IOJob akt = it.next();

				if( io.getFile() == akt.getFile()) {
					
					if(size + offset == akt.getOffset()) {
						// forward combination.
						if (size + akt.getSize()  > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
							break outer;
						}										
						
						size += akt.getSize();
						it.remove();
						changed = true;
					}else if( akt.getOffset() + akt.getSize() == offset ) {
						// backwards combination 
						if (size + akt.getSize()  > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
							break outer;
						}

						size += akt.getSize();
						offset = akt.getOffset();
						
						it.remove();
						changed = true;						
					}
				}
								
			}
		}
		
		return new IOJob(io.getFile(), size, offset, type);		
	}
	
	@Override
	protected IOJob getNextSchedulableJob() {
		// prefer read requests for write requests
		IOJob io = null;			
		if(  ! queuedReadJobs.isEmpty() && 
				parentNode.isEnoughFreeMemory(queuedReadJobs.peek().getSize())  )
		{
			// reserve memory for READ requests
			//io = combineOperation(queuedReadJobs, IOOperation.READ);
			io = queuedReadJobs.poll();
			
			parentNode.reserveMemory(io.getSize());
		}
		
		if(io == null){		
			io = combineOperation(queuedWriteJobs, IOOperation.WRITE);			
		}
		
		return io;	
	}
}