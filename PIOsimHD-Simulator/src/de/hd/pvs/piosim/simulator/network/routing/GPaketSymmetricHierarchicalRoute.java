package de.hd.pvs.piosim.simulator.network.routing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkGraph;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.simulator.IModelToSimulatorMapper;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * This class uses one fixed route with the smallest number of hops.
 * It tries to minimize the memory overhead by storing one default "node".
 * This is the node that has more edges than the current node.
 * If both have an equal number of edges, then none of them is the default.
 * Works only properly in topologies with a single path.
 *
 * @author julian
 *
 */
public class GPaketSymmetricHierarchicalRoute extends AGPaketRouting<PaketFirstRoute> {

	final class RoutingTable{
		HashMap<INetworkExit, IGNetworkEdge> detailRouting = null;
		IGNetworkEdge defaultEdge = null;
	}

	final HashMap<INetworkNode, RoutingTable> routingTable = new HashMap<INetworkNode, RoutingTable>();

	/**
	 * Build the route by using a BFS from EACH target.
	 */
	@Override
	public void buildRoutingTable(INetworkTopology networkTopology,	IModelToSimulatorMapper objs)
	{
		final INetworkGraph inversedGraph = networkTopology.computeInversedGraph();
		final HashMap<INetworkNode, LinkedList<INetworkEdge>>  tgtMap = inversedGraph.getSourceGraph();

		// determine targets == exit nodes
		final Collection<INetworkExit> exitNodes = networkTopology.getNetworkExitNodes();


		// HashTable contains the distance from a node to an exit.
		final HashMap<INetworkNode, Integer> distanceMap = new HashMap<INetworkNode, Integer>();

		// Depth of the graph to its center
		int graphDepth = -1;

		// determine distance with a BFS.
		{
			LinkedList<INetworkNode> toProcess;
			LinkedList<INetworkNode> toProcessNext = new LinkedList<INetworkNode>();
			final HashSet<INetworkNode> processedNodes = new HashSet<INetworkNode>();

			// take any iterator
			toProcessNext.addAll(exitNodes);

			// do a BFS, count depth
			while(! toProcessNext.isEmpty()){
				toProcess = toProcessNext;
				toProcessNext = new LinkedList<INetworkNode>();
				graphDepth++;

				for(INetworkNode cur: toProcess){
					distanceMap.put(cur, graphDepth);

					final LinkedList<INetworkEdge> edges = networkTopology.getEdges(cur);
					// Check if we find a default route:
					for(INetworkEdge edge: edges){
						final INetworkNode tgt = networkTopology.getEdgeTarget(edge);
						// bfs, target edge already checked.
						if(processedNodes.contains(tgt)){
							continue;
						}

						//System.out.println(graphDistance + " " + cur + " " + tgt);

						toProcessNext.add(tgt);
					}
					processedNodes.add(cur);
				}
			}
		}

		// create a list for each depth with the nodes.
		// now we know the maximum distance.

		final int maxDepthToProcess = ((graphDepth -1) / 2 * 2);
		final LinkedList<INetworkNode> depthNodes[] = new  LinkedList[graphDepth + 1 ];

		for(int i=0; i < graphDepth + 1; i++){
			depthNodes[i] = new LinkedList<INetworkNode>();
		}

		for(INetworkNode node: distanceMap.keySet()){
			int depth = distanceMap.get(node);
			depthNodes[ depth ].add(node);
		}

		// initialize data structures:
		for(INetworkNode node: networkTopology.getNetworkNodes()){
			RoutingTable currentNodeRoutingTable = new RoutingTable();
			routingTable.put(node,	currentNodeRoutingTable);
		}

		// determine default routes
		for(int depth=0; depth <= graphDepth; depth++){
			for(INetworkNode node:  depthNodes[depth]){
				RoutingTable currentNodeRoutingTable = routingTable.get(node);

				final LinkedList<INetworkEdge> edges = networkTopology.getEdges(node);
				int maxOutEdges = edges.size();

				if(maxOutEdges == 0){
					System.err.println("Warning: " + node + " hos no outgoing edge!");
					continue;
				}

				if (maxOutEdges == 1){
					// the route is fixed
					currentNodeRoutingTable.defaultEdge = (IGNetworkEdge) objs.getSimulatedComponent(edges.get(0));
					continue;
				}
				currentNodeRoutingTable.detailRouting = new HashMap<INetworkExit, IGNetworkEdge>();

				// Check if we find a default route:
				for(INetworkEdge edge: edges){
					final INetworkNode tgt = networkTopology.getEdgeTarget(edge);

					if (distanceMap.get(tgt) > depth){
						assert(currentNodeRoutingTable.defaultEdge == null);
						currentNodeRoutingTable.defaultEdge = (IGNetworkEdge) objs.getSimulatedComponent(edge);
					}
				}
			}
		}

		//System.out.println(this);

		// start a BFS from all exit nodes.
		for(INetworkExit exitNode: exitNodes){
			LinkedList<INetworkNode> toProcess;
			LinkedList<INetworkNode> toProcessNext = new LinkedList<INetworkNode>();

			toProcessNext.add(exitNode);

			// do a BFS
			while(! toProcessNext.isEmpty()){
				toProcess = toProcessNext;
				toProcessNext = new LinkedList<INetworkNode>();

				for(INetworkNode cur : toProcess){
					// Update routes of all adjacent nodes.

					final LinkedList<INetworkEdge> edgesIn = tgtMap.get(cur);
					for(INetworkEdge edge: edgesIn){
						final INetworkNode tgt = networkTopology.getEdgeSource(edge);
						final IGNetworkEdge edgeToTarget = (IGNetworkEdge) objs.getSimulatedComponent(edge);
						final RoutingTable sourceRoutingTable = routingTable.get(tgt);

						if(sourceRoutingTable.defaultEdge == edgeToTarget ){
							// nothing to do, because this node is the default routing target
							continue;
						}

						// not the default route, add this node
						sourceRoutingTable.detailRouting.put(exitNode, edgeToTarget);

						if (distanceMap.get(tgt) < graphDepth){ // optimization for the last element in the hierarchy
							toProcessNext.add(tgt);
						}
					}
				}
			}
			// best routes for target are set.
		}
	}

	@Override
	public IGNetworkEdge getTargetRouteForMessage(INetworkNode src, MessagePart part) {
		RoutingTable routes = routingTable.get(src);
		IGNetworkEdge edge;

		if(routes.detailRouting == null){
			// default route:
			edge =  routes.defaultEdge;

			assert(edge != null);

			return edge;
		}else{
			edge = routes.detailRouting.get(part.getMessageTarget());
			if(edge == null){
				// use the default routing.
				return routes.defaultEdge;
			}

			return edge;
		}
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + " route: \n");

		for(INetworkNode node: routingTable.keySet()){
			RoutingTable routes = routingTable.get(node);
			HashMap<INetworkExit, IGNetworkEdge> nodeRoutes = routes.detailRouting;
			buf.append("Table for " + node.getIdentifier() +"\n");
			if(routes.defaultEdge != null){
				buf.append("\tDefaults to " + routes.defaultEdge.getTargetNode().getIdentifier().getName() + " via edge \"" + routes.defaultEdge.getIdentifier().getName() + "\"\n");
			}
			if(nodeRoutes != null){
				for(INetworkExit exit: nodeRoutes.keySet()){
					IGNetworkEdge edge = nodeRoutes.get(exit);
					buf.append("\tto " + exit.getIdentifier().getName() + " next hop " +  edge.getTargetNode().getIdentifier().getName()  + " via edge \"" + edge.getIdentifier().getName() + "\" \n");
				}
			}
		}

		return buf.toString();
	}

	@Override
	public void messagePartRemoved(MessagePart part) {
		// not needed, we do not track the message position
	}
}
