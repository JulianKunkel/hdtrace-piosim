package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.base.IGNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

public interface IGNetworkNode<ModelType extends INetworkNode>
	extends IGNetworkFlowComponent<ModelType>, ISPassiveComponent<ModelType>
{
	public void setPaketRouting(IPaketTopologyRouting routing);
}