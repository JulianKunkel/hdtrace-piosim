package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;

public interface IServerCacheLayerJobCallback<UserData extends Object>
{
	/**
	 * Called if the request completed, except for read / write path!
	 * @param time
	 * @param req
	 */
	public void JobCompleted(Epoch time, FileRequest req, UserData data);

	public void WritePartialData(Epoch time, FileRequest req, UserData userdata, long size);

	public void ReadPartialData(Epoch time, FileRequest req, UserData userdata, long size);
}
