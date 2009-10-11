package de.hd.pvs.piosim.model.networkTopology;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public interface INetworkGraph {

	public LinkedList<INetworkEdge> getEdges(INetworkNode src);

	/**
	 * Contains the source node and all outgoing edges
	 * @return
	 */
	public HashMap<INetworkNode, LinkedList<INetworkEdge>> getSourceGraph();

	public INetworkNode getEdgeTarget(INetworkEdge edge);

	/**
	 * Contains a target node and all incoming edges.
	 * Note the graph edges are now inverted, i.e. incoming edges are outgoing
	 * Similar to graph inverting.
	 *
	 * @return
	 */
	public INetworkGraph computeInversedGraph();

	/**
	 * Get all network nodes
	 * @return
	 */
	public List<INetworkNode> getNetworkNodes();

	public List<INetworkExit> getNetworkExitNodes();
}
