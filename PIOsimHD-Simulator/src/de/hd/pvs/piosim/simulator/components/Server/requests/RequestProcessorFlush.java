package de.hd.pvs.piosim.simulator.components.Server.requests;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NIC.IInterProcessNetworkJobCallback;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IServerCacheLayerJobCallback;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.ServerCacheLayerJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;

public class RequestProcessorFlush extends RequestProcessor<RequestFlush>
{
	private final IInterProcessNetworkJobCallback dummyCallback = new InterProcessNetworkJobCallbackAdaptor();


	private final IServerCacheLayerJobCallback acknowledgeCallback = new ServerCacheLayerJobCallbackAdaptor() {
		@Override
		public void JobCompleted(Epoch time, FileRequest req, Object data) {
			server.sendAcknowledgeToClient((InterProcessNetworkJobRoutable) data);
			finishRequest(req);
		}
	};

	@Override
	public void process(RequestFlush req, InterProcessNetworkJobRoutable remoteJob, Epoch time) {
		startRequest(req, remoteJob);
		server.getCacheLayer().announceIORequest( req, remoteJob,
				acknowledgeCallback,	time );
	}
}
