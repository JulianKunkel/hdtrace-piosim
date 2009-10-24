package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;

public interface IGNetworkEdge<ModelComp extends INetworkEdge>
	extends ISNetworkComponent<ModelComp>
{
	/**
	 * The target node this edge points to
	 */
	public IGNetworkNode getTargetNode();

	public void setTargetNode(IGNetworkNode targetNode);
}
