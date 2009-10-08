package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.simulator.base.SFIFOBlockingNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;

abstract public class AGNetworkEdge<ModelType extends INetworkEdge>
	extends SFIFOBlockingNetworkFlowComponent<ModelType>
	implements IGNetworkEdge<ModelType>
{
	private IGNetworkNode targetNode;

	final public void setTargetNode(IGNetworkNode targetNode) {
		assert(targetNode != null);

		this.targetNode = targetNode;
	}

	final public IGNetworkNode getTargetNode() {
		assert(targetNode != null);

		return targetNode;
	}
}
