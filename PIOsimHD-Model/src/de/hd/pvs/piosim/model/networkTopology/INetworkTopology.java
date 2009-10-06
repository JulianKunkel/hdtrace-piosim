package de.hd.pvs.piosim.model.networkTopology;

import de.hd.pvs.piosim.model.interfaces.ISerializableObject;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

public interface INetworkTopology extends ISerializableObject, INetworkGraph {
	public void addEdge(INetworkNode src, INetworkEdge via, INetworkNode tgt);

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routingAlgorithm);

	public PaketRoutingAlgorithm getRoutingAlgorithm();

	public String getName();
}
