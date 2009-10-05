package de.hd.pvs.piosim.model.networkTopology;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

/**
 * Contains the network graph consisting of components
 *
 * @author julian
 */
public class NetworkTopology implements INetworkTopology {

	private String name = "";

	private static class TopologyEdgeImplk implements ITopologyEdge{
		final INetworkEdge edge;
		final INetworkNode target;

		public TopologyEdgeImplk(final INetworkEdge edge, final INetworkNode target) {
			this.edge = edge;
			this.target = target;
		}

		public INetworkEdge getEdge() {
			return edge;
		}

		public INetworkNode getTarget() {
			return target;
		}

		public ComponentIdentifier getIdentifier() {
			return edge.getIdentifier();
		}
	}
	// routing protocol could maybe be set on one network topology (Bus system, link together with
	// redundant 2-D Torus or sth.

	private PaketRoutingAlgorithm routingAlgorithm;

	private HashMap<INetworkNode, LinkedList<ITopologyEdge>> graph = new HashMap<INetworkNode, LinkedList<ITopologyEdge>>();


	public HashMap<INetworkNode, LinkedList<ITopologyEdge>> getGraph() {
		return graph;
	}

	public LinkedList<ITopologyEdge> getEdges(INetworkNode src){
		return graph.get(src);
	}

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

	public PaketRoutingAlgorithm getRoutingAlgorithm() {
		return routingAlgorithm;
	}


	public void addEdge(INetworkNode src, INetworkEdge via, INetworkNode tgt){
		LinkedList<ITopologyEdge> tgts = graph.get(src);
		if(tgts == null){
			tgts = new LinkedList<ITopologyEdge>();
			graph.put(src,tgts);
		}

		tgts.add(new TopologyEdgeImplk(via, tgt));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "\"" +  name + "\"";
	}
}
