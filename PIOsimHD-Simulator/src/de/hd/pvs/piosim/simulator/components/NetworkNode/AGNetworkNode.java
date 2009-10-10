package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.base.SBlockingNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

abstract public class AGNetworkNode<ModelType extends INetworkNode>
	extends SBlockingNetworkFlowComponent<ModelType>
	implements IGNetworkNode<ModelType>
{
	protected IPaketTopologyRouting routing;

	@Override
	public void setPaketRouting(IPaketTopologyRouting routing) {
		this.routing = routing;
	}

	public IPaketTopologyRouting getRouting() {
		return routing;
	}
}