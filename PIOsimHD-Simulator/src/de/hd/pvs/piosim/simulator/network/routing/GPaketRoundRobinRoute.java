package de.hd.pvs.piosim.simulator.network.routing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkGraph;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoundRobinRoute;
import de.hd.pvs.piosim.simulator.IModelToSimulatorMapper;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Truly sends messages in round robin among the connected components.
 * @author julian
 *
 */
public class GPaketRoundRobinRoute extends GPaketRoutingPersistentRoute<PaketRoundRobinRoute> {

	private static class RouteSettings{
		final int minHops;
		final HashSet<IGNetworkEdge> edges = new HashSet<IGNetworkEdge>();

		Iterator<IGNetworkEdge> it = null;

		public RouteSettings(int minHops) {
			this.minHops = minHops;
		}

		public void add(IGNetworkEdge edge){
			edges.add(edge);
		}
	}

	/**
	 * maps from source-node to all edges delivering to a particular target
	 * Default routes are for nodes which have only one interconnect i.e. all targets are routed via the same edge.
	 * Such a node uses null as the only network exit.
	 */
	final HashMap<INetworkNode, HashMap<INetworkExit, RouteSettings>> routingTableSrcMap = new HashMap<INetworkNode, HashMap<INetworkExit, RouteSettings>>();

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
		for(INetworkNode node: networkTopology.getNetworkNodes()){
			routingTableSrcMap.put(node, new HashMap<INetworkExit, RouteSettings>());
		}

		// start a BFS from all exit nodes.
		for(INetworkExit exitNode: exitNodes){
			HashSet<INetworkNode> toProcess;
			HashSet<INetworkNode> toProcessNext = new HashSet<INetworkNode>();

			toProcessNext.add(exitNode);

			final HashSet<INetworkNode> processedNodes = new HashSet<INetworkNode>();

			int hops = 0;

			// do a BFS, count depth
			while(! toProcessNext.isEmpty()){
				toProcess = toProcessNext;
				toProcessNext = new HashSet<INetworkNode>();
				hops++;

				while(! toProcess.isEmpty()){
					final INetworkNode cur = toProcess.iterator().next();
					toProcess.remove(cur);

					processedNodes.add(cur);

					final LinkedList<INetworkEdge> edges = tgtMap.get(cur);

					if(edges == null){
						continue;
					}

					for(INetworkEdge edge: edges){
						final INetworkNode tgt = inversedGraph.getEdgeTarget(edge);
						// bfs, target node already checked.
						if(processedNodes.contains(tgt)){
							continue;
						}

						// not checked
						toProcessNext.add(tgt);

						final HashMap<INetworkExit, RouteSettings> routes = routingTableSrcMap.get(tgt);
						assert(routes != null);

						RouteSettings routeSettings = routes.get(exitNode);
						if(routeSettings == null){
							routeSettings = new RouteSettings(hops);
							routes.put(exitNode, routeSettings);
						}

						if(routeSettings.minHops == hops){
							// target not contained => add route via this edge.
							final IGNetworkEdge sEdge = (IGNetworkEdge) objs.getSimulatedComponent(edge);
							routeSettings.add(sEdge);
						}
					}
				}
			}
			// best routes for target are set.
		}
	}

	@Override
	public IGNetworkEdge getTargetRouteForMessageNow(INetworkNode src, MessagePart part) {
		HashMap<INetworkExit, RouteSettings> routes = routingTableSrcMap.get(src);
		final IGNetworkEdge edge;

		final RouteSettings routeSetting = routes.get(part.getMessageTarget());
		if(routeSetting.edges.size() == 0){
			return routeSetting.edges.iterator().next();
		}

		if(routeSetting.it == null || ! routeSetting.it.hasNext()){
			routeSetting.it = routeSetting.edges.iterator();
		}

		edge = routeSetting.it.next();

		assert(edge != null);

		return edge;
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + " route: \n");

		for(INetworkNode node: routingTableSrcMap.keySet()){
			HashMap<INetworkExit, RouteSettings> nodeRoutes = routingTableSrcMap.get(node);
			buf.append("Table for " + node.getIdentifier().getName() +"\n");

			for(INetworkExit exit: nodeRoutes.keySet()){
				final  RouteSettings routes = nodeRoutes.get(exit);
				buf.append("\tto " + exit.getIdentifier().getName()+ " hops: " +  routes.minHops + " \n");

				for(IGNetworkEdge edge: routes.edges){
					buf.append("\t\t hop " +  edge.getTargetNode().getIdentifier().getName()  + " via edge \"" + edge.getIdentifier().getName() + "\" \n");
				}
			}
		}

		return buf.toString();
	}
}
