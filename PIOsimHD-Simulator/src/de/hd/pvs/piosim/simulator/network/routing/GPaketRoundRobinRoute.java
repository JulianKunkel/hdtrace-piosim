package de.hd.pvs.piosim.simulator.network.routing;

import java.util.HashMap;
import java.util.Iterator;

import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoundRobinRoute;
import de.hd.pvs.piosim.simulator.IModelToSimulatorMapper;
import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Truly sends messages in round robin among the connected components.
 * @author julian
 *
 */
public class GPaketRoundRobinRoute extends AGPaketRouting<PaketRoundRobinRoute> {
	final HashMap<GNode, Iterator<SNetworkComponent>> lastUsedRoute = new HashMap<GNode, Iterator<SNetworkComponent>>();

	@Override
	public void buildRoutingTable(INetworkTopology networkTopology,
			IModelToSimulatorMapper objs) {
		// TODO Auto-generated method stub

	}

	@Override
	public IGNetworkEdge getTargetRouteForMessage(INetworkNode src,
			INetworkExit target, MessagePart part) {
		// TODO Auto-generated method stub
		return null;
	}
}
