package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.simulator.base.SBasicComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;

abstract public class AGNetworkEdge<ModelType extends INetworkEdge> extends SBasicComponent<ModelType> implements IGNetworkEdge<ModelType> {
	private IGNetworkNode targetNode;

	final public void setTargetNode(IGNetworkNode targetNode) {
		this.targetNode = targetNode;
	}

	final public IGNetworkNode getTargetNode() {
		return targetNode;
	}
}
