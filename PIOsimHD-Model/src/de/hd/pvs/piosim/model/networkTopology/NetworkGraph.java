package de.hd.pvs.piosim.model.networkTopology;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;

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

	public List<INetworkExit> getNetworkExitNodes(){
		final LinkedList<INetworkExit> exitNodes = new LinkedList<INetworkExit>();
		for(INetworkNode node: getNetworkNodes()){
			if(NetworkNode.isExitNode(node)){
				exitNodes.add((INetworkExit) node);
			}
		}
		return exitNodes;
	}

	public List<INetworkNode> getNetworkNodes(){
		final LinkedList<INetworkNode> networkNodes = new LinkedList<INetworkNode>(graph.keySet());

		networkNodes.addAll(edgeMap.values());

		return networkNodes;
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
