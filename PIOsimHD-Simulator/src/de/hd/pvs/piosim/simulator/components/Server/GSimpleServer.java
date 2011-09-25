
/** Version Control Information $Id: GSimpleServer.java 779 2010-07-17 18:49:10Z kunkel $
 * @lastmodified    $Date: 2010-07-17 20:49:10 +0200 (Sa, 17. Jul 2010) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 779 $
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

import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.IInterProcessNetworkJobCallback;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Server.requests.ServerAcknowledge;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IGServerCacheLayer;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;

/**
 * Simulates a server process together with an I/O subsystem.
 * Glues the cache together with the I/O-subsystem.
 *
 * @author Julian M. Kunkel
 */
public class GSimpleServer extends SPassiveComponent<Server>
implements IGServer<SPassiveComponent<Server>>, IGRequestProcessingServerInterface
{
	public static final int STEP_COMPLETED = 1000000;

	private IProcessNetworkInterface networkInterface;

	private INodeRessources nodeRessources;

	private IGServerCacheLayer<?> cacheLayer;

	/**
	 * Processors for different data requests
	 */
	final private HashMap<Class<? extends IMessageUserData>, IServerRequestProcessor> requestProcessors = new HashMap<Class<? extends IMessageUserData>, IServerRequestProcessor>();

	private final IInterProcessNetworkJobCallback dummyCallback = new InterProcessNetworkJobCallbackAdaptor();

	private GSimpleServer getServer(){
		return this;
	}

	final private IInterProcessNetworkJobCallback unexpectedCallback = new InterProcessNetworkJobCallbackAdaptor() {
		@Override
		public void recvCompletedCB(InterProcessNetworkJob job, InterProcessNetworkJob announcedJob, Epoch endTime) {
			debug( "Unexpected job starting " + job.getMatchingCriterion().getSourceComponent().getIdentifier());

			final Class<? extends IMessageUserData> dataType = job.getJobData().getClass();

			Class<?> partypes[] = {dataType , InterProcessNetworkJobRoutable.class};

			// get processor for jobdata type
			IServerRequestProcessor processor = requestProcessors.get(dataType);
			// if not found, load processor.
			if(processor == null){
				// instantiate processor dynamically:
				// TODO load dynamically...
				try{
					processor = (IServerRequestProcessor) Class.forName("de.hd.pvs.piosim.simulator.components.Server.requests.RequestProcessor" + dataType.getSimpleName().replace("Request", "") ).newInstance();
					processor.setServerInterface(getMe());

					requestProcessors.put(dataType, processor);
				}catch(Exception e){
					System.out.println("Received " + job);
					throw new IllegalArgumentException(e);
				}
			}

			assert(InterProcessNetworkJobRoutable.class.isInstance(job));

			assert( ((InterProcessNetworkJobRoutable) job).getOriginalSource() != getServer().getModelComponent());

			processor.process(job.getJobData(), (InterProcessNetworkJobRoutable) job, endTime);
		}

		public void messagePartMatchesAnnounced(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime) {
			// start a new recv for unexpected msgs.
			submitRecv();
		};
	};

	private GSimpleServer getMe(){
		return this;
	}

	/**
	 * Call this method to issue an acknowledge to the client
	 * @param request
	 */
	@Override
	public void sendAcknowledgeToClient(InterProcessNetworkJobRoutable request) {
		final MessageMatchingCriterion reqCrit = request.getMatchingCriterion();
		final InterProcessNetworkJob resp = InterProcessNetworkJobRoutable.createRoutableSendOperation(
				new MessageMatchingCriterion(getModelComponent(),
						reqCrit.getSourceComponent(),
						reqCrit.getTag(),
						reqCrit.getCommunicator(),
						reqCrit.getRootCommand(),
						reqCrit.getCurrentCommand()),
						new ServerAcknowledge(15), dummyCallback,
						getModelComponent(), request.getOriginalSource(), request.getRelationToken());

		assert(getModelComponent() != request.getOriginalSource());

		getNetworkInterface().initiateInterProcessSend(resp, getSimulator().getVirtualTime());
	}


	private void submitRecv(){
		networkInterface.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(
				null, unexpectedCallback, null),
						getSimulator().getVirtualTime());
	}

	@Override
	public void setModelComponent(Server comp) throws Exception {
		super.setModelComponent(comp);

		final Simulator sim = getSimulator();

		cacheLayer  = (IGServerCacheLayer) sim.instantiateSimObjectForModelObj(comp.getCacheImplementation());

		assert(cacheLayer != null);
	}

	@Override
	public void simulationModelIsBuild() {
		super.simulationModelIsBuild();

		/* submit a new receiver msg */
		submitRecv();
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

	public IGServerCacheLayer getCacheLayer() {
		return cacheLayer;
	}
}


