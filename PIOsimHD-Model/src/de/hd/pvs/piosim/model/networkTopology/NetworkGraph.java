package de.hd.pvs.piosim.model.networkTopology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;

public class NetworkGraph implements INetworkGraph{

	// map a node to all outgoing edges
	protected HashMap<INetworkNode, LinkedList<INetworkEdge>> graph = new HashMap<INetworkNode, LinkedList<INetworkEdge>>();

	// maps an edge directly to its target node
	protected HashMap<INetworkEdge, INetworkNode> edgeMap = new HashMap<INetworkEdge, INetworkNode>();

	// maps an edge directly to its source node
	protected HashMap<INetworkEdge, INetworkNode> edgeSourceMap = new HashMap<INetworkEdge, INetworkNode>();


	public INetworkNode getEdgeTarget(INetworkEdge edge) {
		return edgeMap.get(edge);
	}


	public INetworkNode getEdgeSource(INetworkEdge edge) {
		return edgeSourceMap.get(edge);
	}


	public HashMap<INetworkNode, LinkedList<INetworkEdge>> getSourceGraph() {
		return graph;
	}

	public Collection<INetworkExit> getNetworkExitNodes(){
		final LinkedList<INetworkExit> exitNodes = new LinkedList<INetworkExit>();
		for(INetworkNode node: getNetworkNodes()){
			if(NetworkNode.isExitNode(node)){
				exitNodes.add((INetworkExit) node);
			}
		}
		return exitNodes;
	}

	public Collection<INetworkNode> getNetworkNodes(){
		final LinkedList<INetworkNode> networkNodes = new LinkedList<INetworkNode>(graph.keySet());
		networkNodes.addAll(edgeMap.values());
		return networkNodes;
	}

	public Collection<INetworkEdge> getNetworkEdges() {
		return edgeMap.keySet();
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
		if(src == null){
			throw new IllegalArgumentException("src == null");
		}
		if(via == null){
			throw new IllegalArgumentException("via == null");
		}
		if(tgt == null){
			throw new IllegalArgumentException("tgt == null");
		}


		LinkedList<INetworkEdge> tgts = graph.get(src);
		if(tgts == null){
			tgts = new LinkedList<INetworkEdge>();
			graph.put(src,tgts);
		}

		tgts.add(via);

		edgeMap.put(via, tgt);

		edgeSourceMap.put(via, src);
	}


}
