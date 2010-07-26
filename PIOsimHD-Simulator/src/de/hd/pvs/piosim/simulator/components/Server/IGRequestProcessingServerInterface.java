package de.hd.pvs.piosim.simulator.components.Server;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IGServerCacheLayer;

public interface IGRequestProcessingServerInterface {
	public IGServerCacheLayer getCacheLayer();
	public IProcessNetworkInterface getNetworkInterface();
	public INodeHostedComponent getModelComponent();
	public INodeRessources getNodeRessources();
	public Simulator getSimulator();

	/**
	 * Call this method to issue an acknowledge to the client
	 * @param request
	 */
	public void sendAcknowledgeToClient(InterProcessNetworkJobRoutable request);
}
