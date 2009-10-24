
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
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IGServerCacheLayer;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;

/**
 * Simulates a server process together with an I/O subsystem.
 * Glues the cache together with the I/O-subsystem.
 *
 * @author Julian M. Kunkel
 */
public class GSimpleServer extends SPassiveComponent<Server>
implements IGServer<SPassiveComponent<Server>>
{
	public static final int STEP_COMPLETED = 1000000;

	private IProcessNetworkInterface networkInterface;

	private INodeRessources nodeRessources;

	private IGServerCacheLayer<?> cacheLayer;

	LinkedList<MessagePart> blockedReceives = new LinkedList<MessagePart>();

	public void process(RequestRead req, InterProcessNetworkJob request) {
		final InterProcessNetworkJob resp =  InterProcessNetworkJob.createEmptySendOperation(
				new MessageMatchingCriterion(this,
						request.getMatchingCriterion().getSourceComponent(),
						RequestIO.IO_DATA_TAG,
						request.getMatchingCriterion().getCommunicator()),
						new NetworkIOData(req),
						true);

		Message<InterProcessNetworkJob> msg = networkInterface.initiateInterProcessSend(resp, getSimulator().getVirtualTime());

		cacheLayer.announceIORequest( msg, req, request );
	}

	public void process(RequestWrite req, InterProcessNetworkJob request) {
		cacheLayer.announceIORequest( req, request );

		/* post receive of further message parts as fragmented flow parts */
		final InterProcessNetworkJob resp =  InterProcessNetworkJob.createReceiveOperation(
				new MessageMatchingCriterion(
						request.getMatchingCriterion().getSourceComponent(), this,
						RequestIO.IO_DATA_TAG,
						request.getMatchingCriterion().getCommunicator()),
						true);

		networkInterface.initiateInterProcessReceive(resp, getSimulator().getVirtualTime());
	}

	public void process(RequestFlush req, InterProcessNetworkJob request) {
		cacheLayer.announceIORequest( req, request );
	}

	public void process(NetworkIOData data, InterProcessNetworkJob request) {

	}


	public InterProcessNetworkJob prepareFinalWriteAcknowledge(InterProcessNetworkJob request) {
		return InterProcessNetworkJob.createSendOperation(
				new MessageMatchingCriterion(this,
						request.getMatchingCriterion().getSourceComponent(),
						RequestIO.IO_COMPLETION_TAG,
						request.getMatchingCriterion().getCommunicator()),
						new NetworkSimpleData(15), false);
	}

	void processRequest(InterProcessNetworkJob job){
		Class<?> partypes[] = {job.getJobData().getClass() , InterProcessNetworkJob.class};
		try{
			Method meth = this.getClass().getMethod("process", partypes);

			Object arglist[] = {job.getJobData(), job};

			meth.invoke(this, arglist);

		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	private void processWritePart(MessagePart part, Epoch endTime){
		InterProcessNetworkJob job = (InterProcessNetworkJob) part.getMessage().getContainedUserData();
		cacheLayer.writeDataToCache((NetworkIOData) job.getJobData(),  job, part.getSize());

		if(part.getMessage().isReceivedCompletely()){
			/*
			 * Send an acknowledge to the client.
			 */
			final InterProcessNetworkJob resp = prepareFinalWriteAcknowledge(job);

			/* return the response or another receive message */
			if(resp != null){
				networkInterface.initiateInterProcessSend(resp, endTime);
			}
		}
	}

	private void submitRecv(){
		networkInterface.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(
				new MessageMatchingCriterion(null, this,  RequestIO.INITIAL_REQUEST_TAG,  Communicator.IOSERVERS),
				false), getSimulator().getVirtualTime());
	}

	@Override
	public void setModelComponent(Server comp) throws Exception {
		super.setModelComponent(comp);

		final Simulator sim = getSimulator();

		cacheLayer  = (IGServerCacheLayer) sim.instantiateSimObjectForModelObj(comp.getCacheImplementation());
	}

	@Override
	public void simulationModelIsBuild() {
		super.simulationModelIsBuild();

		/* submit a new receiver msg */
		submitRecv();
	}

	@Override
	public void startupBlockedIOReceiveIfPossible(Epoch startTime) {
		if( blockedReceives.size() > 0 ){
			while( ! blockedReceives.isEmpty() ){

				MessagePart p = blockedReceives.get(0);

				if (! cacheLayer.canIPutDataIntoCache((InterProcessNetworkJob) p.getMessage().getContainedUserData(), p.getSize())){
					break;
				}

				blockedReceives.remove(0);

				processWritePart(p, startTime);
			}

			if(blockedReceives.size() == 0){
				networkInterface.unblockFurtherDataReceives();
			}
		}
	}

	@Override
	public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob,
			InterProcessNetworkJob announcedJob, Epoch endTime)
	{
		debug("received MSG Part " + part.getSize());

		//this function is called right now only for writes
		boolean doesFit = cacheLayer.canIPutDataIntoCache(remoteJob, part.getSize());

		if (doesFit){
			processWritePart( part, endTime);
		}else{
			debugFollowUpLine("WRITE: cache layer does not accept more data => stall NIC");

			blockedReceives.add(part);

			networkInterface.blockFurtherDataReceives();
		}
	}


	@Override
	public void messagePartSendCB(MessagePart part, InterProcessNetworkJob myJob, Epoch endTime) {
		debug("sent MSG Part "  + part.getSize());

		cacheLayer.readDataFragmentSendByNIC(part.getMessage(), part.getSize());
	}


	@Override
	public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime) {
		if(announcedJob.isPartialCallbackActive()){
			// directly handled by partial callback
			return;
		}

		debug( "Unexpected job starting " + remoteJob.getMatchingCriterion().getSourceComponent().getIdentifier());
		processRequest(remoteJob);
		submitRecv();
	}


	@Override
	public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime) {

	}

	@Override
	public void computeJobCompletedCV(ComputeJob job) {

	}

	@Override
	public IProcessNetworkInterface getNetworkInterface() {
		return this.networkInterface;
	}

	@Override
	public void setNetworkInterface(IProcessNetworkInterface nic) {
		this.networkInterface = nic;
	}

	@Override
	public void setNodeRessources(INodeRessources ressources) {
		this.nodeRessources = ressources;
	}

	@Override
	public INodeRessources getNodeRessources() {
		return this.nodeRessources;
	}

	@Override
	public String toString() {
		return "GSimpleServer " + getIdentifier();
	}
}


