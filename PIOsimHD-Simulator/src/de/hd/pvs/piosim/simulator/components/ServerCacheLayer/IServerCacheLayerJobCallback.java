package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;

public interface IServerCacheLayerJobCallback<UserData extends Object>
{
	/**
	 * Called if the complete request completed
	 * @param req
	 * @param time
	 */
	public void JobCompleted(FileRequest req, UserData data, Epoch time);

	/**
	 * Called if some data from an IO Request is read/or written
	 * @param req
	 * @param time
	 * @param size
	 */
	public void IORequestPartiallyCompleted(RequestIO req, UserData data, Epoch time, long size);
}
