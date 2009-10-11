package de.hd.pvs.piosim.simulator.network.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
 *
 * @author julian
 *
 */
public class GPaketFirstRoute extends AGPaketRouting<PaketFirstRoute> {

	/**
	 * maps from source-node to the edge delivering to a particular target
	 * Default routes are for nodes which have only one interconnect i.e. all targets are routed via the same edge.
	 * Such a node uses null as the only network exit.
	 */
	final HashMap<INetworkNode, HashMap<INetworkExit, IGNetworkEdge>> routingTable = new HashMap<INetworkNode, HashMap<INetworkExit,IGNetworkEdge>>();

	/**
	 * Build the route by using a BFS from EACH target.
	 */
	@Override
	public void buildRoutingTable(INetworkTopology networkTopology,	IModelToSimulatorMapper objs)
	{
		final INetworkGraph inversedGraph = networkTopology.computeInversedGraph();
		final HashMap<INetworkNode, LinkedList<INetworkEdge>>  tgtMap = inversedGraph.getSourceGraph();

		// determine targets == exit nodes
		final List<INetworkExit> exitNodes = networkTopology.getNetworkExitNodes();
		for(INetworkNode node: networkTopology.getNetworkNodes()){
			routingTable.put(node,	new HashMap<INetworkExit, IGNetworkEdge>());
		}

		// start a BFS from all exit nodes.
		for(INetworkExit exitNode: exitNodes){
			LinkedList<INetworkNode> toProcess;
			LinkedList<INetworkNode> toProcessNext = new LinkedList<INetworkNode>();

			toProcessNext.add(exitNode);

			final HashSet<INetworkNode> processedNodes = new HashSet<INetworkNode>();

			int hops = 0;

			// do a BFS, count depth
			while(! toProcessNext.isEmpty()){
				toProcess = toProcessNext;
				toProcessNext = new LinkedList<INetworkNode>();
				hops++;

				while(! toProcess.isEmpty()){
					final INetworkNode cur = toProcess.poll();

					processedNodes.add(cur);

					final LinkedList<INetworkEdge> edges = tgtMap.get(cur);

					if(edges == null){
						continue;
					}

					for(INetworkEdge edge: edges){
						final INetworkNode tgt = inversedGraph.getEdgeTarget(edge);
						// bfs, target edge already checked.
						if(processedNodes.contains(tgt)){
							continue;
						}

						// not checked
						toProcessNext.add(tgt);

						final HashMap<INetworkExit, IGNetworkEdge> routes = routingTable.get(tgt);
						assert(routes != null);

						if(routes.containsKey(exitNode)){
							// could be used for multiple routes
							continue;
						}

						// target not contained => add route via this edge.
						final IGNetworkEdge sEdge = (IGNetworkEdge) objs.getSimulatedComponent(edge);
						routes.put(exitNode, sEdge);
					}
				}
			}
			// best routes for target are set.
		}

		// remove default routes.
		for(INetworkNode node: networkTopology.getSourceGraph().keySet()){
			if(networkTopology.getEdges(node).size() == 1){
				// default route!
				HashMap<INetworkExit, IGNetworkEdge> map = new HashMap<INetworkExit, IGNetworkEdge>();
				map.put(null, (IGNetworkEdge) objs.getSimulatedComponent(
						// only one edge:
						networkTopology.getEdges(node).get(0)));
				// overwrite all routes
				routingTable.put(node, map);
			}
		}
	}

	@Override
	public IGNetworkEdge getTargetRouteForMessage(INetworkNode src, MessagePart part) {
		HashMap<INetworkExit, IGNetworkEdge> routes = routingTable.get(src);
		final IGNetworkEdge edge;

		if(routes.size() == 1){
			// default route:
			edge =  routes.get(null);

			assert(edge != null);

			return edge;
		}else{
			edge = routes.get(part.getMessageTarget());

			assert(edge != null);

			return edge;
		}
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + " route: \n");

		for(INetworkNode node: routingTable.keySet()){
			HashMap<INetworkExit, IGNetworkEdge> nodeRoutes = routingTable.get(node);
			buf.append("Table for " + node.getIdentifier().getName() +"\n");
			if(nodeRoutes.size() == 1){
				IGNetworkEdge edge = nodeRoutes.get(null);
				buf.append("\tDefaults to " + edge.getTargetNode().getIdentifier().getName() + " via edge \"" + edge.getIdentifier().getName() + "\"\n");
			}else{
				for(INetworkExit exit: nodeRoutes.keySet()){
					IGNetworkEdge edge = nodeRoutes.get(exit);
					buf.append("\tto " + exit.getIdentifier().getName() + " next hop " +  edge.getTargetNode().getIdentifier().getName()  + " via edge \"" + edge.getIdentifier().getName() + "\" \n");
				}
			}
		}

		return buf.toString();
	}
}
