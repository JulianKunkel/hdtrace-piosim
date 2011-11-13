
 /** Version Control Information $Id: GNoCache.java 782 2010-07-18 12:42:38Z kunkel $
  * @lastmodified    $Date: 2010-07-18 14:42:38 +0200 (So, 18. Jul 2010) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 782 $
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
import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.IGIOSubsystem;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Server.IGRequestProcessingServerInterface;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
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
	static protected class InternalIOData {
		/**
		 * Callback invoked, once the operation completed.
		 */
		final IServerCacheLayerJobCallback callback;
		final FileRequest request;
		/**
		 * The user data is the data from the layer using the cache layer.
		 */
		final Object userData;

		public InternalIOData(FileRequest request, Object userData, IServerCacheLayerJobCallback callback) {
			this.request = request;
			this.callback = callback;
			this.userData = userData;
		}

	}

	/**
	 * How many operations are in the IOsubsystem
	 */
	private int numberOfScheduledIOOperations = 0;

	private int numberOfPendingIOOperations = 0;

	private int numberOfPendingOrScheduledWriteOperations = 0;

	/**
	 * The I/O Job queue
	 */
	private IOJobQueue jobQueue;

	IGRequestProcessingServerInterface serverProcess;
	INodeRessources nodeRessources;
	IGIOSubsystem ioSubsystem;


	public GNoCache() {
		jobQueue = initJobQueue();
	}

	/**
	 * Override this method to change the job queue.
	 * @return
	 */
	protected IOJobQueue initJobQueue(){
		return new IOJobQueue() {

			/**
			 * Queued read operations, read and write operations are split.
			 */
			final LinkedList<IOJob<InternalIOData,IOOperationData>> queuedReadJobs = new LinkedList<IOJob<InternalIOData,IOOperationData>>();

			/**
			 * Contains write or flush operations.
			 */
			final LinkedList<IOJob<InternalIOData,IOOperationData>> queuedWriteJobs = new LinkedList<IOJob<InternalIOData,IOOperationData>>();

			@Override
			public IOJob getNextSchedulableJob(GlobalSettings settings) {
				// prefer read requests for write requests
				IOJob io = null;

				if(  ! queuedReadJobs.isEmpty() &&
						nodeRessources.isEnoughFreeMemory((((StreamIOOperation) queuedReadJobs.peek().getOperationData()).getSize()))  )
				{
					// reserve memory for READ requests
					io = queuedReadJobs.poll();

					nodeRessources.reserveMemory( ((StreamIOOperation) io.getOperationData()).getSize());
				}

				if(io == null){
					// pick up a write call

					io = queuedWriteJobs.poll();
				}

				return io;
			}

			@Override
			public void addIOJob(IOJob<InternalIOData,IOOperationData> job){
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

	final void scheduleNextIOJobIfPossible() {
		while(numberOfScheduledIOOperations < getModelComponent().getMaxNumberOfConcurrentIOOps()
				&& numberOfPendingIOOperations > 0)
		{
			final IOJob io = jobQueue.getNextSchedulableJob(getSimulator().getModel().getGlobalSettings());

			if (io == null){
				// might happen if there are only read requests but no RAM is available to cache data.
				return;
			}

			//logger.info(time + " " + this.getIdentifier() +  " starting I/O " + io);
			numberOfPendingIOOperations-= io.getNumberOfJobs();
			numberOfScheduledIOOperations++;

			ioSubsystem.startNewIO( io);
		}
	}

	//////////////////////////////////WRITE PATH////////////////////////////////////////////////////////////////
	HashMap<NetworkIOData, ArrayList<SingleIOOperation>> notReceivedWriteExtendsFromReqs = new HashMap<NetworkIOData, ArrayList<SingleIOOperation>>();

	@Override
	public boolean canIPutDataIntoCache(RequestWrite clientJob, long bytesOfWrite) {
		//System.out.println(numberOfPendingWrites);
		return numberOfPendingOrScheduledWriteOperations == 0;
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

	public void dataWrittenCompletelyToDisk(IOJob<InternalIOData,?> job, Epoch endTime) {
		debug("job " + job);

		if(job.getNumberOfJobs() == 1){
			final InternalIOData data = ((InternalIOData) job.getUserData());
			data.callback.WritePartialData(endTime, data.request, data.userData, ((StreamIOOperation) job.getOperationData()).getSize() );

			nodeRessources.freeMemory(((StreamIOOperation) (job.getOperationData())).getSize());
		}else{
			for(IOJob<InternalIOData,?> sjob  : ((IOJobCoalesced) job).getAggregatedJobs()){
				final InternalIOData data = sjob.getUserData();

				nodeRessources.freeMemory(((StreamIOOperation) (sjob.getOperationData())).getSize());
				data.callback.WritePartialData(endTime, data.request, data.userData,   ((StreamIOOperation) sjob.getOperationData()).getSize());
			}
		}
	}

	///////////////////////////////////READ PATH////////////////////////////////////////////////////////////////

	@Override
	public void readDataFragmentSendByNIC(RequestRead req, long bytesSendByNIC) {
		// free memory
		nodeRessources.freeMemory(bytesSendByNIC);

		// try to schedule another operation, this is necessary if our RAM has been full
		scheduleNextIOJobIfPossible();
	}


	private void dataReadCompletelyFromDisk(IOJob<InternalIOData,?> job, Epoch endTime) {
		if(job.getNumberOfJobs() == 1){
			final InternalIOData data = job.getUserData();
			data.callback.ReadPartialData(endTime, data.request, data.userData, ((StreamIOOperation) job.getOperationData()).getSize() );
		}else{
			for(IOJob<InternalIOData, ?> sjob  : ((IOJobCoalesced) job).getAggregatedJobs()){
				final InternalIOData data = sjob.getUserData();
				data.callback.ReadPartialData(endTime, data.request, data.userData,   ((StreamIOOperation) sjob.getOperationData()).getSize());
			}
		}
	}

	@Override
	public void announceIORequest(RequestFlush req, Object userData, IServerCacheLayerJobCallback callback, Epoch time) {
		final InternalIOData downLayerUserData = new InternalIOData(req, userData, callback);

		numberOfPendingIOOperations++;

		jobQueue.addIOJob(new IOJob<InternalIOData,IOOperationData>(
				req.getFile(), downLayerUserData,
				IOOperationType.FLUSH, null));

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
		final InternalIOData downLayerUserData = new InternalIOData(req, userData, callback);

		numberOfPendingIOOperations++;

		jobQueue.addIOJob(new IOJob<InternalIOData,IOOperationData>(
				req.getFile(), downLayerUserData, IOOperationType.READ,
				new StreamIOOperation(size, offset)));

		scheduleNextIOJobIfPossible();
	}

	protected void addWriteIOJob(long size, long offset, RequestWrite req, Object userData, IServerCacheLayerJobCallback callback){
		final InternalIOData internalUserData = new InternalIOData(req, userData, callback);

		numberOfPendingIOOperations++;

		numberOfPendingOrScheduledWriteOperations++;

		jobQueue.addIOJob(new IOJob<InternalIOData,IOOperationData>(
				req.getFile(), internalUserData, IOOperationType.WRITE,
				new StreamIOOperation(size, offset)));

		scheduleNextIOJobIfPossible();
	}


	public void IOComplete(Epoch endTime, IOJob job) {
		debug("I/O done " + job);

		// it is mandatory to first schedule new operations internally!
		numberOfScheduledIOOperations--;
		scheduleNextIOJobIfPossible();

		switch(job.getOperationType()){
		case READ:{
			dataReadCompletelyFromDisk(job, endTime);
			break;
		}case WRITE:{
			// write request
			numberOfPendingOrScheduledWriteOperations--;
			dataWrittenCompletelyToDisk(job, endTime);

			break;
		}case FLUSH:{

			if(job.getNumberOfJobs() == 1){
				final InternalIOData data = ((InternalIOData) job.getUserData());

				data.callback.JobCompleted(endTime, data.request, data.userData);
			}else{
				for(IOJob<InternalIOData,IOOperationData> sjob  : ((IOJobCoalesced) job).getAggregatedJobs()){
					final InternalIOData data = sjob.getUserData();
					data.callback.JobCompleted(endTime, data.request, data.userData);
				}
			}
			break;
		}default:
			assert(false);
		}
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
		assert(nodeRessources != null);
		assert(serverProcess != null);
	}
}
