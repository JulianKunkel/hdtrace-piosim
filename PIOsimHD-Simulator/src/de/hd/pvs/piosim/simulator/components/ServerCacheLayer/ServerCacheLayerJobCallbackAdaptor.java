package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;

public class ServerCacheLayerJobCallbackAdaptor<UserData extends Object>
	implements IServerCacheLayerJobCallback<UserData>
{

	@Override
	public void IORequestPartiallyCompleted(RequestIO req, UserData data, Epoch time, long size) {
	}

	@Override
	public void JobCompleted(FileRequest req, UserData data, Epoch time) {

	}

}
