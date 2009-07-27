
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

package de.hd.pvs.piosim.simulator.components.Server;

import java.lang.reflect.Method;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.GNIC;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IGServerCacheLayer;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleMessage;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;

/**
 * Simulates a server process together with an I/O subsystem.
 * Glues the cache together with the I/O subsystem.
 *
 * @author Julian M. Kunkel
 */
public class GSimpleServer extends SPassiveComponent<Server>
implements IGServer<SPassiveComponent<Server>>
{
	public static final int STEP_COMPLETED = 1000000;

	private GNode attachedNode;

	private IGServerCacheLayer<?> cacheLayer;

	LinkedList<MessagePart> blockedReceives = new LinkedList<MessagePart>();

	@Override
	public void computeJobCompletedCV(ComputeJob job) {

	}


	public SingleNetworkJob process(RequestRead req, SingleNetworkJob request) {

		cacheLayer.announceIORequest( req, request );
		// getAttachedNode().getGNICToNode(request.getSourceComponent()),

		return SingleNetworkJob.createSendOperation(
				new NetworkIOData(req),
				this,
				request.getSourceComponent(),
				RequestIO.IO_DATA_TAG,
				request.getCommunicator(),
				null,
				true, false);
	}

	public SingleNetworkJob process(RequestWrite req, SingleNetworkJob request) {
		cacheLayer.announceIORequest( req, request );

		/* post receive of further message parts as fragmented flow parts */
		return SingleNetworkJob.createReceiveOperation(
				this,
				request.getSourceComponent(),
				RequestIO.IO_DATA_TAG,
				request.getCommunicator(),
				null);
	}

	public SingleNetworkJob process(NetworkIOData data, SingleNetworkJob request) {
		return null;
	}


	public SingleNetworkJob prepareFinalWriteAcknowledge(SingleNetworkJob request) {
		return SingleNetworkJob.createSendOperation(
				new NetworkSimpleMessage(15),
				this,
				request.getSourceComponent(),
				RequestIO.IO_COMPLETION_TAG,
				request.getCommunicator(),
				null,
				false,
				false);
	}


	public GNode getAttachedNode() {
		return attachedNode;
	}


	void processRequest(SingleNetworkJob job){
		Class<?> partypes[] = {job.getJobData().getClass() , SingleNetworkJob.class};
		try{
			Method meth = this.getClass().getMethod("process", partypes);

			Object arglist[] = {job.getJobData(), job};

			SingleNetworkJob resp = (SingleNetworkJob) meth.invoke(this, arglist);

			if(resp == null){
				/* finished processing */
				return;
			}

			//NetworkJobs jobs = new NetworkJobs(job.getParentNetworkJobs().getInitialRequestDescription());

			/* return the response or another receive message */
			this.getAttachedNode().submitNewNetworkJob(resp);

		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	private void unblockNetworkRecvTransfer(){
		getAttachedNode().unblockReceiveFlow( this);
	}

	private void blockNetworkRecvTransfer(){
		getAttachedNode().blockReceiveFlow( this );
	}

	@Override
	public void jobsCompletedCB(NetworkJobs jobs, Epoch endTime) {

	}

	@Override
	public void receiveCB(SingleNetworkJob job, SingleNetworkJob response, Epoch endTime) {
		debug( "Unexpected job starting " + response.getSourceComponent().getIdentifier() + "," + response.getJobData());
		processRequest(response);
		submitRecv();
	}

	private void processWritePart(MessagePart part, Epoch endTime){
		SingleNetworkJob job = part.getNetworkJob();
		cacheLayer.writeDataToCache((NetworkIOData) job.getJobData(),  job, part.getSize());

		if(part.isLastPart()){
			/*
			 * Send an acknowledge to the client.
			 */
			SingleNetworkJob resp = prepareFinalWriteAcknowledge(job);

			/* return the response or another receive message */
			if(resp != null){
				this.getAttachedNode().submitNewNetworkJob(resp);
			}
		}
	}

	@Override
	public void recvMsgPartCB(GNIC gnic, MessagePart part, Epoch endTime) {
		SingleNetworkJob job = part.getNetworkJob();

		debug("received MSG Part " + part.getSize() + " cmd: " + job.getParentNetworkJobs().getInitialRequestDescription());

		//this function is called right now only for writes
		boolean doesFit = cacheLayer.canIPutDataIntoCache(job, part.getSize());

		System.err.println("recvMsgPartCB " + part);

		if (doesFit){
			processWritePart( part, endTime);
		}else{
			debugFollowUpLine("WRITE: cache layer does not accept more data => stall NIC is automatically");

			blockedReceives.add(part);

			blockNetworkRecvTransfer();
		}
	}

	@Override
	public void sendMsgPartCB(GNIC gnic, MessagePart part, Epoch endTime) {
		if(part.getSize() == 0){
			cacheLayer.startReadRequest(part.getMessage(),  (RequestRead) ((NetworkIOData) part.getNetworkJob().getJobData()).getIORequest() );
		}else{
			debug("sent MSG Part "  + part.getSize());

			cacheLayer.readDataFragmentSendByNIC(part.getMessage(), part.getSize());
		}
	}


	private void submitRecv(){
		getAttachedNode().submitNewNetworkJob(
				SingleNetworkJob.createReceiveOperation(this, null, RequestIO.INITIAL_REQUEST_TAG,  Communicator.IOSERVERS, null));
	}

	@Override
	public void setSimulatedModelComponent(Server comp, Simulator sim) throws Exception {
		super.setSimulatedModelComponent(comp, sim);

		cacheLayer  = (IGServerCacheLayer) sim.instantiateSimObjectForModelObj(comp.getCacheImplementation());

		attachedNode = (GNode) sim.getSimulatedComponent(comp.getParentComponent());
		/* submit a new receiver msg */

		submitRecv();
	}

	@Override
	public void startupBlockedIOReceiveIfPossible(Epoch startTime) {
		if( blockedReceives.size() > 0 ){
			while( ! blockedReceives.isEmpty() ){

				MessagePart p = blockedReceives.get(0);

				if (! cacheLayer.canIPutDataIntoCache(p.getNetworkJob(), p.getSize())){
					break;
				}

				blockedReceives.remove(0);

				processWritePart(p, startTime);
			}

			if(blockedReceives.size() == 0){
				unblockNetworkRecvTransfer();
			}
		}
	}
}
