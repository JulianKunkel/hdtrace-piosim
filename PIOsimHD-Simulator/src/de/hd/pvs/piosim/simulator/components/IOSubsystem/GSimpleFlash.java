
 /** Version Control Information $Id: GSimpleFlash.java 718 2009-10-16 13:22:41Z kunkel $
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

package de.hd.pvs.piosim.simulator.components.IOSubsystem;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.IOSubsystem.SimpleFlash;
import de.hd.pvs.piosim.simulator.base.SSequentialBlockingComponent;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOJob;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;

/**
 * A IOSubystem can run only one job at a time, however the server might keep a small
 * list of pending I/O jobs to optimize access on this layer. This is especially
 * useful for RAID systems which might be able to schedule multiple requests concurrently.
 */
public class GSimpleFlash extends SSequentialBlockingComponent<SimpleFlash, IOJob>
	implements IGIOSubsystem<SimpleFlash>
{
	IIOSubsystemCaller callback;

	int totalOperations = 0;
	long totalAmountOfData = 0;

	@Override
	public void setIOCallback(IIOSubsystemCaller callback) {
		this.callback = callback;
	}

	@Override
	protected Epoch getProcessingTimeOfScheduledJob(IOJob job) {
		switch(job.getOperationType()){
		case FLUSH:
			return Epoch.ZERO;
		case READ:
		case WRITE:
			return getModelComponent().getAvgAccessTime().add(
					((StreamIOOperation) job.getOperationData()).getSize() / (float) getModelComponent().getMaxThroughput()
					);
		default:
			throw new IllegalArgumentException("Not implemented job type " + job.getOperationType());
		}
	}

	@Override
	protected void jobStarted(Event<IOJob> event, Epoch startTime) {
		IOSubsytemHelper.traceIOStart(this, event.getEventData());
	}

	@Override
	protected void jobCompleted(Event<IOJob> event, Epoch endTime) {
		//immediately notify caller upon completion of job
		IOJob job = event.getEventData();

		IOSubsytemHelper.traceIOEnd(this, job);

		totalOperations++;
		switch(job.getOperationType()){
		case READ:
		case WRITE:
			totalAmountOfData += ((StreamIOOperation) job.getOperationData()).getSize();
		}

		//System.out.println(getIdentifier() +  " IOSubsystem " + job);

		callback.IOComplete(endTime, job);
	}

	@Override
	public void startNewIO(IOJob job) {
		Epoch time = getSimulator().getVirtualTime();
		addNewEvent(new Event<IOJob>(this, this,  time, job));

		startNextPendingEventIfPossible(time);
	}

	@Override
	public void simulationFinished() {
		System.out.println("IOSubsystem " + getIdentifier() + " <#ops,dataAccessed> = <" + totalOperations + ", " + totalAmountOfData + ">");
	}

}
