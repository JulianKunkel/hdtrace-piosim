package de.hd.pvs.piosim.model.networkTopology;

import java.util.HashMap;
import java.util.LinkedList;

public class NetworkGraph implements INetworkGraph{

	// map a node to all outgoing edges
	protected HashMap<INetworkNode, LinkedList<INetworkEdge>> graph = new HashMap<INetworkNode, LinkedList<INetworkEdge>>();

	// maps an edge directly to its target node
	protected HashMap<INetworkEdge, INetworkNode> edgeMap = new HashMap<INetworkEdge, INetworkNode>();

	public INetworkNode getEdgeTarget(INetworkEdge edge) {
		return edgeMap.get(edge);
	}

	public HashMap<INetworkNode, LinkedList<INetworkEdge>> getSourceGraph() {
		return graph;
	}

	public NetworkGraph computeInversedGraph() {
		NetworkGraph newGraph = new NetworkGraph();

		// compute targets.
		for(INetworkNode src: graph.keySet()){
			for(INetworkEdge edge: graph.get(src)){
				newGraph.addEdge(edgeMap.get(edge), edge, src);
			}
		}

		return newGraph;
	}

	public LinkedList<INetworkEdge> getEdges(INetworkNode src){
		return graph.get(src);
	}


	public void addEdge(INetworkNode src, INetworkEdge via, INetworkNode tgt){
		LinkedList<INetworkEdge> tgts = graph.get(src);
		if(tgts == null){
			tgts = new LinkedList<INetworkEdge>();
			graph.put(src,tgts);
		}

		tgts.add(via);

		edgeMap.put(via, tgt);
	}


}
