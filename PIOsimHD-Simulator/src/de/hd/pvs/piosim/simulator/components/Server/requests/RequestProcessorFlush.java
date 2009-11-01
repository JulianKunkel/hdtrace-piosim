package de.hd.pvs.piosim.simulator.components.Server.requests;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;

public class RequestProcessorFlush extends RequestProcessor<RequestFlush>
{
	@Override
	public void process(RequestFlush req, InterProcessNetworkJobRoutable remoteJob, Epoch time) {
		server.getCacheLayer().announceIORequest( req, remoteJob );
	}
}
