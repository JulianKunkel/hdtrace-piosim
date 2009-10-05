package de.hd.pvs.piosim.model.networkTopology;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

public interface INetworkTopology {
	public void addEdge(INetworkNode src, INetworkEdge via, INetworkNode tgt);

	public LinkedList<ITopologyEdge> getEdges(INetworkNode src);

	/**
	 * Contains the source node and all outgoing edges
	 * @return
	 */
	public HashMap<INetworkNode, LinkedList<ITopologyEdge>> getGraph();

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routingAlgorithm);

	public PaketRoutingAlgorithm getRoutingAlgorithm();

	public String getName();
}
