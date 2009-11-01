package de.hd.pvs.piosim.simulator.components.Server;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IGServerCacheLayer;

public interface IGRequestProcessingServerInterface {
	public IGServerCacheLayer getCacheLayer();
	public IProcessNetworkInterface getNetworkInterface();
	public INodeHostedComponent getModelComponent();
}
