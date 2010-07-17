
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
package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.IGIOSubsystem;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Server.IGRequestProcessingServerInterface;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;



/**
 * Basic Implementation.
 * Does not implement any caching at all, instead each read and write is blocking.
 * Provides separate read and write queues and prefers read for write requests.
 *
 * A maximum number of concurrent requests is scheduled.
 * Owns the IOSubsystem of the server.
 *
 * @author Julian M. Kunkel
 */
public class GNoCache
	extends SPassiveComponent<NoCache>
	implements  IGServerCacheLayer<SPassiveComponent<NoCache>>, IIOSubsystemCaller
{
	static protected class InternalIOData<RequestType extends FileRequest> {
		final IServerCacheLayerJobCallback callback;
		final RequestType request;
		final Object userData;

		public InternalIOData(RequestType request, Object userData, IServerCacheLayerJobCallback callback) {
			this.request = request;
			this.callback = callback;
			this.userData = userData;
		}
	}

	/**
	 * How many operations are in the IOsubsystem
	 */
	int numberOfScheduledIOOperations = 0;

	/**
	 * Queued read operations, read and write operations are split.
	 */
	LinkedList<IOJob<InternalIOData<RequestRead>>> queuedReadJobs = new LinkedList<IOJob<InternalIOData<RequestRead>>>();

	LinkedList<IOJob<InternalIOData>> queuedWriteJobs = new LinkedList<IOJob<InternalIOData>>();

	IGRequestProcessingServerInterface serverProcess;
	INodeRessources nodeRessources;
	IGIOSubsystem ioSubsystem;

	protected int getNumberOfQueuedOperations(){
		return queuedReadJobs.size() + queuedWriteJobs.size();
	}


	protected IOJob getNextSchedulableJob() {
		// prefer read requests for write requests
		IOJob io = null;
		if(  ! queuedReadJobs.isEmpty() &&
				nodeRessources.isEnoughFreeMemory(queuedReadJobs.peek().getSize())  )
		{
			// reserve memory for READ requests
			io = queuedReadJobs.poll();
			nodeRessources.reserveMemory(io.getSize());
		}

		if(io == null){
			// pick up a write call
			long size;
			long offset;

			io = queuedWriteJobs.poll();

			if(true){
				size = io.getSize();
				offset = io.getOffset();

				// try to combine several write operations.
				while (! queuedWriteJobs.isEmpty()
						&& io.getFile() == queuedWriteJobs.peek().getFile()
						&& size + offset ==  queuedWriteJobs.peek().getOffset()
						&& size + queuedWriteJobs.peek().getSize()  <= getSimulator().getModel().getGlobalSettings().getIOGranularity()
				)
				{
					io = queuedWriteJobs.poll();
					size += io.getSize();
				}

				io = new IOJob(io.getFile(), io.getUserData(), size, offset, io.getType());
			}
		}

		return io;
	}

	protected void scheduleNextIOJobIfPossible() {
		while(numberOfScheduledIOOperations < getModelComponent().getMaxNumberOfConcurrentIOOps()
				&& getNumberOfQueuedOperations() > 0)
		{
			IOJob io = getNextSchedulableJob();

			if (io == null){
				// might happen if there are only read requests but no RAM is available to cache data.
				return;
			}

			//logger.info(time + " " + this.getIdentifier() +  " starting I/O " + io);
			ioSubsystem.startNewIO( io);

			numberOfScheduledIOOperations++;
		}
	}

	//////////////////////////////////WRITE PATH////////////////////////////////////////////////////////////////
	HashMap<NetworkIOData, ArrayList<SingleIOOperation>> notReceivedWriteExtendsFromReqs = new HashMap<NetworkIOData, ArrayList<SingleIOOperation>>();

	@Override
	public boolean canIPutDataIntoCache(RequestWrite clientJob, long bytesOfWrite) {
		//System.out.println(numberOfPendingWrites);
		return queuedWriteJobs.size() == 0;
	}

	@Override
	public void writeDataToCache(NetworkIOData ioData, InterProcessNetworkJob clientJob, long amountToWrite, Object userData, IServerCacheLayerJobCallback callback) {
		//decide which data actually is contained in the network packet
		debug("amount " + amountToWrite);

		nodeRessources.reserveMemory(amountToWrite);

		ArrayList<SingleIOOperation> writeList = null;

		if( notReceivedWriteExtendsFromReqs.containsKey(ioData) ){
			writeList = notReceivedWriteExtendsFromReqs.get(ioData);
		}else{
			// add all jobs to the list
			try{
				writeList = (ArrayList<SingleIOOperation>) ioData.getIORequest().getListIO().getIOOperations().clone();
				notReceivedWriteExtendsFromReqs.put(ioData, writeList );
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		final long iogran = getSimulator().getModel().getGlobalSettings().getIOGranularity();

		while(amountToWrite > 0 ){

			long availData = writeList.get(0).getAccessSize();
			long dataToWrite;
			long offset = writeList.get(0).getOffset();;

			if ( amountToWrite >= availData ){
				dataToWrite =  availData;

				writeList.remove(0);

			}else{ // amountToWrite < availData
				dataToWrite = amountToWrite;


				writeList.get(0).setAccessSize(writeList.get(0).getAccessSize() - dataToWrite);
				writeList.get(0).setOffset( writeList.get(0).getOffset() + dataToWrite );
			}

			amountToWrite -= dataToWrite;

			while(dataToWrite > iogran){
				addWriteIOJob(iogran, offset, (RequestWrite) ioData.getIORequest(), userData, callback);
				offset += iogran;
				dataToWrite -= iogran;
			}

			addWriteIOJob( dataToWrite, offset, (RequestWrite) ioData.getIORequest(), userData, callback);
		}

		// if we processed all write requests we are done
		if( writeList.isEmpty() ){
			notReceivedWriteExtendsFromReqs.remove(ioData);
		}
	}

	public void dataWrittenCompletelyToDisk(IOJob<InternalIOData<RequestWrite>> job, Epoch endTime) {
		nodeRessources.freeMemory(job.getSize());

		debug("job " + job);

		final InternalIOData<RequestWrite> userData = job.getUserData();
		userData.callback.IORequestPartiallyCompleted(userData.request, userData.userData, endTime, job.getSize());
	}

	///////////////////////////////////READ PATH////////////////////////////////////////////////////////////////

	@Override
	public void readDataFragmentSendByNIC(RequestRead req, long bytesSendByNIC) {
		// free memory
		nodeRessources.freeMemory(bytesSendByNIC);
	}


	public void dataReadCompletelyFromDisk(IOJob<InternalIOData<RequestRead>> job, Epoch endTime) {
		final InternalIOData<RequestRead> data = job.getUserData();
		data.callback.IORequestPartiallyCompleted(data.request, data.userData, endTime, job.getSize());
	}

	@Override
	public void announceIORequest(RequestFlush req, Object userData, IServerCacheLayerJobCallback callback, Epoch time) {
		final InternalIOData downLayerUserData = new InternalIOData(req, userData, callback);

		queuedWriteJobs.add(new IOJob<InternalIOData>(
				req.getFile(), downLayerUserData, 0, 0,  IOOperation.FLUSH));

		scheduleNextIOJobIfPossible();
	}

	@Override
	public void announceIORequest(RequestWrite req, Object userData, IServerCacheLayerJobCallback callback, Epoch time) {

	}

	@Override
	public void announceIORequest(RequestRead req, Object userData, IServerCacheLayerJobCallback callback, Epoch time) {
		final long iogran = getSimulator().getModel().getGlobalSettings().getIOGranularity();

		/**
		 * right now queue up all pending requests...
		 */
		for(SingleIOOperation op: req.getListIO().getIOOperations()){
			//split requests if necessary:

			long size = op.getAccessSize() ;
			long offset = op.getOffset();

			while(size > iogran){
				addReadIOJob(iogran, offset, req, userData, callback);
				offset += iogran;
				size -= iogran;
			}

			if(size > 0){
				addReadIOJob(size, offset, req, userData, callback);
			}
		}
	}

	protected void addReadIOJob(long size, long offset,  RequestRead req, Object userData, IServerCacheLayerJobCallback callback){
		final InternalIOData<RequestRead> downLayerUserData = new InternalIOData<RequestRead>(req, userData, callback);

		queuedReadJobs.add(new IOJob<InternalIOData<RequestRead>>(
				req.getFile(), downLayerUserData, size, offset,  IOOperation.READ));

		scheduleNextIOJobIfPossible();
	}

	protected void addWriteIOJob(long size, long offset, RequestWrite req, Object userData, IServerCacheLayerJobCallback callback){
		final InternalIOData<RequestWrite> internalUserData = new InternalIOData<RequestWrite>(req, userData, callback);

		queuedWriteJobs.add(new IOJob<InternalIOData>(
				req.getFile(), internalUserData, size, offset,  IOOperation.WRITE));
		scheduleNextIOJobIfPossible();
	}


	public void IOComplete(Epoch endTime, IOJob job) {
		debug("I/O done " + job);

		switch(job.getType()){
		case READ:{
			dataReadCompletelyFromDisk(job, endTime);
			break;
		}case WRITE:{
			// write request
			dataWrittenCompletelyToDisk(job, endTime);
			break;
		}case FLUSH:{
			final InternalIOData<RequestFlush> userData = (InternalIOData<RequestFlush>) job.getUserData();
			userData.callback.JobCompleted(userData.request, userData.userData, endTime);
			break;
		}default:
			assert(false);
		}

		numberOfScheduledIOOperations--;

		scheduleNextIOJobIfPossible();
	}


	@Override
	public void setModelComponent(NoCache comp) throws Exception {
		super.setModelComponent(comp);

		final Simulator sim = getSimulator();

		assert(comp.getParentComponent() != null);
		assert(comp.getParentComponent().getParentComponent() != null);

		serverProcess = (IGRequestProcessingServerInterface) sim.getSimulatedComponent(comp.getParentComponent());
		nodeRessources = (INodeRessources) sim.getSimulatedComponent(comp.getParentComponent().getParentComponent());

		ioSubsystem = (IGIOSubsystem)  sim.instantiateSimObjectForModelObj(comp.getParentComponent().getIOsubsystem());
		ioSubsystem.setIOCallback(this);

		assert(ioSubsystem != null);
		assert(serverProcess != null);
	}
}
