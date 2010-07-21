package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;

public class ServerCacheLayerJobCallbackAdaptor<UserData extends Object>
	implements IServerCacheLayerJobCallback<UserData>
{

	@Override
	public void WritePartialData(Epoch time, FileRequest req, UserData userdata, long size) {

	}

	@Override
	public void ReadPartialData(Epoch time, FileRequest req, UserData userdata, long size) {

	}

	@Override
	public void JobCompleted(Epoch time, FileRequest req, UserData data) {

	}

}
