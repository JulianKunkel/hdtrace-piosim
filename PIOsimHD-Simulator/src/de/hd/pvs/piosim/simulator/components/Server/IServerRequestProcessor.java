package de.hd.pvs.piosim.simulator.components.Server;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;

public interface IServerRequestProcessor<Type extends IMessageUserData> {
	public void process(Type req, InterProcessNetworkJobRoutable remoteJob, Epoch time);

	public void setServerInterface(IGRequestProcessingServerInterface server);
}
