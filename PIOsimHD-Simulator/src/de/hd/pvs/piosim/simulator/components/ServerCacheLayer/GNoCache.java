
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
import java.util.List;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.IGIOSubsystem;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
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
implements  IGServerCacheLayer<SPassiveComponent<NoCache>>,
IIOSubsystemCaller
{
	protected class PendingReadRequest {
		IOJob job;
		RequestRead request;

		public PendingReadRequest(IOJob job, RequestRead request) {
			this.job = job;
			this.request = request;
		}

		public IOJob getJob() {
			return job;
		}

		public RequestRead getRequest() {
			return request;
		}
	}

	/**
	 * How many operations are in the IOsubsystem
	 */
	int numberOfScheduledIOOperations = 0;

	/**
	 * Queued read operations, read and write operations are split.
	 */
	LinkedList<IOJob> queuedReadJobs = new LinkedList<IOJob>();

	LinkedList<IOJob> queuedWriteJobs = new LinkedList<IOJob>();

	IGServer<?> serverProcess;
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

				io = new IOJob(io.getFile(), size, offset, IOOperation.WRITE);
			}
		}

		return io;
	}

	/**
	 * Maps the serviced read-jobs to the read-requests
	 */
	HashMap<IOJob, List<PendingReadRequest>> pendingReadRequestMap = new HashMap<IOJob, List<PendingReadRequest>>();

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

	// TODO use a new data structure to pick the best write operation depending on all pending requests
	//   when it is really used.
	//TODO evaluate this new strategy with the default strategy when
	//  clients send all pending requests in normal order.


	@Override
	public boolean canIPutDataIntoCache(InterProcessNetworkJob clientJob, long amount) {
		//System.out.println(numberOfPendingWrites);
		return queuedWriteJobs.size() == 0;
	}

	@Override
	public void writeDataToCache(NetworkIOData ioData, InterProcessNetworkJob clientJob, long amountToWrite) {
		//decide which data actually is contained in the network packet
		debug("amount " + amountToWrite);

		//System.out.println(serverProcess.getIdentifier() +  " writeDataToCache " + amountToWrite);

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
				addWriteIOJob(iogran, offset, ioData.getIORequest());
				offset += iogran;
				dataToWrite -= iogran;
			}

			addWriteIOJob( dataToWrite, offset,   	ioData.getIORequest());
		}

		// if we processed all write requests we are done
		if( writeList.isEmpty() ){
			notReceivedWriteExtendsFromReqs.remove(ioData);
		}
	}

	@Override
	public void dataWrittenCompletelyToDisk(IOJob job) {
		nodeRessources.freeMemory(job.getSize());

		debug("job " + job);

		// now try to submit a new write request by combining pending requests
	}

	///////////////////////////////////READ PATH////////////////////////////////////////////////////////////////

	HashMap<RequestRead, Message> pendingReadJobs = new HashMap<RequestRead, Message>();

	@Override
	public void readDataFragmentSendByNIC(Message msg, long amount) {
		// free memory
		nodeRessources.freeMemory(amount);
	}

	@Override
	public void dataReadCompletelyFromDisk(IOJob job) {
		final List<PendingReadRequest> reqList = pendingReadRequestMap.remove(job);

		for (PendingReadRequest p : reqList) {
			RequestRead req = p.getRequest();
			Message msg = pendingReadJobs.get(req);

			assert(msg != null);

			serverProcess.getNetworkInterface().appendAvailableDataToIncompleteSend(msg, p.getJob().getSize());

			if( msg.isAllMessageDataAvailable() ){ // All data read completely
				pendingReadJobs.remove(req);
			}
		}
	}

	@Override
	public void announceIORequest(RequestFlush req, InterProcessNetworkJob request) {
		addFlush(req);
	}

	@Override
	public void announceIORequest(RequestWrite req, InterProcessNetworkJob request) {

	}

	@Override
	public void announceIORequest( Message msg, RequestRead req, InterProcessNetworkJob request){
		pendingReadJobs.put(req, msg);

		final long iogran = getSimulator().getModel().getGlobalSettings().getIOGranularity();

		/**
		 * right now queue up all pending requests...
		 */
		for(SingleIOOperation op: req.getListIO().getIOOperations()){
			//split requests if necessary:

			long size = op.getAccessSize() ;
			long offset = op.getOffset();

			while(size > iogran){
				addReadIOJob(iogran, offset, req);
				offset += iogran;
				size -= iogran;
			}

			if(size > 0){
				addReadIOJob(size, offset, req);
			}
		}
	}

	protected void addFlush(RequestFlush req){
		scheduleNextIOJobIfPossible();
	}

	protected void addReadIOJob(long size, long offset, RequestRead req){

		IOJob iojob = new IOJob(req.getFile(), size, offset,  IOOperation.READ);
		queuedReadJobs.add(iojob);

		if (pendingReadRequestMap.get(iojob) == null) {
			pendingReadRequestMap.put(iojob, new ArrayList<PendingReadRequest>());
		}
		pendingReadRequestMap.get(iojob).add(new PendingReadRequest(iojob, req));

		scheduleNextIOJobIfPossible();
	}

	protected void addWriteIOJob(long size, long offset, RequestIO req){
		queuedWriteJobs.add(new IOJob(req.getFile(), size, offset,  IOOperation.WRITE));
		scheduleNextIOJobIfPossible();
	}


	public void IOComplete(Epoch endTime, IOJob job) {
		debug("I/O done " + job);

		if(job.getType() == IOOperation.READ){
			dataReadCompletelyFromDisk(job);
		}else{
			// write request
			dataWrittenCompletelyToDisk(job);
			serverProcess.startupBlockedIOReceiveIfPossible(endTime);
		}

		numberOfScheduledIOOperations--;

		scheduleNextIOJobIfPossible();
	}


	@Override
	public void setModelComponent(NoCache comp) throws Exception {
		super.setModelComponent(comp);

		final Simulator sim = getSimulator();

		serverProcess = (IGServer) sim.getSimulatedComponent(comp.getParentComponent());
		nodeRessources = (INodeRessources) sim.getSimulatedComponent(comp.getParentComponent().getParentComponent());

		ioSubsystem = (IGIOSubsystem)  sim.instantiateSimObjectForModelObj(comp.getParentComponent().getIOsubsystem());
		ioSubsystem.setIOCallback(this);

		assert(ioSubsystem != null);
		assert(serverProcess != null);
	}
}
