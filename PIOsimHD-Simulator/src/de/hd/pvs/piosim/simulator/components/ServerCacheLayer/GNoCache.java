
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
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.Message;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
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
	GNode   parentNode;
	IGIOSubsystem ioSubsystem;



	protected int getNumberOfQueuedOperations(){
		return queuedReadJobs.size() + queuedWriteJobs.size();
	}


	protected IOJob getNextSchedulableJob() {
		// prefer read requests for write requests
		IOJob io = null;
		if(  ! queuedReadJobs.isEmpty() &&
				parentNode.isEnoughFreeMemory(queuedReadJobs.peek().getSize())  )
		{
			// reserve memory for READ requests
			io = queuedReadJobs.poll();
			parentNode.reserveMemory(io.getSize());
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
	HashMap<IOJob, HashMap<IOJob, RequestRead>> pendingReadRequestMap = new HashMap<IOJob, HashMap<IOJob, RequestRead>>();

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
	public boolean canIPutDataIntoCache(SingleNetworkJob clientJob, long amount) {
		//System.out.println(numberOfPendingWrites);
		return queuedWriteJobs.size() == 0;
	}

	@Override
	public void writeDataToCache(NetworkIOData ioData, SingleNetworkJob clientJob, long amountToWrite) {
		//decide which data actually is contained in the network packet
		debug("amount " + amountToWrite);

		parentNode.reserveMemory(amountToWrite);

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
		parentNode.freeMemory(job.getSize());

		debug("job " + job);

		// now try to submit a new write request by combining pending requests
	}

	///////////////////////////////////READ PATH////////////////////////////////////////////////////////////////

	HashMap<RequestRead, Message> pendingReadJobs = new HashMap<RequestRead, Message>();

	@Override
	public void readDataFragmentSendByNIC(Message msg, long amount) {
		// free memory
		parentNode.freeMemory(amount);
	}

	@Override
	public void startReadRequest(Message msg, RequestRead req) {
		pendingReadJobs.put(req, msg);
	}

	@Override
	public void dataReadCompletelyFromDisk(IOJob job) {
		HashMap<IOJob, RequestRead> reqMap = pendingReadRequestMap.remove(job);

		for (IOJob io : reqMap.keySet()) {
			RequestRead req = reqMap.get(io);
			Message msg = pendingReadJobs.get(req);

			assert(msg != null);

			//System.out.println(gServer.getIdentifier() +  " dataReadCompletelyFromDisk " + " " + job.getSize());

			msg.getOutgoingNIC().appendAvailableDataToIncompleteSend(msg, io.getSize());

			if( msg.isAllMessageDataAvailable() ){ // All data read completely
				//System.out.println("Completed");
				pendingReadJobs.remove(req);
			}
		}
	}

	@Override
	public void announceIORequest(RequestWrite req, SingleNetworkJob request) {

	}

	@Override
	public void announceIORequest( RequestRead req, SingleNetworkJob request){
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

			addReadIOJob(size, offset, req);
		}
	}

	protected void addReadIOJob(long size, long offset, RequestRead req){
		System.err.println("addReadIOJob " + offset + " " +size);

		IOJob iojob = new IOJob(req.getFile(), size, offset,  IOOperation.READ);
		queuedReadJobs.add(iojob);

		if (pendingReadRequestMap.get(iojob) == null) {
			pendingReadRequestMap.put(iojob, new HashMap<IOJob, RequestRead>());
		}
		pendingReadRequestMap.get(iojob).put(iojob, req);

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
	public void setSimulatedModelComponent(NoCache comp,
			Simulator sim) throws Exception {
		super.setSimulatedModelComponent(comp, sim);

		serverProcess = (IGServer) sim.getSimulatedComponent(comp.getParentComponent());
		parentNode = (GNode) sim.getSimulatedComponent(comp.getParentComponent().getParentComponent());

		ioSubsystem = (IGIOSubsystem)  sim.instantiateSimObjectForModelObj(comp.getParentComponent().getIOsubsystem());
		ioSubsystem.setIOCallback(this);

		assert(ioSubsystem != null);
		assert(serverProcess != null);
	}
}
