package de.hd.pvs.piosim.simulator.network.routing;

import java.util.HashMap;
import java.util.Iterator;

import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GNetworkNodeExit;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.event.MessagePart;

/**
 * Truly sends messages in round robin among the connected components.
 * @author julian
 *
 */
public class GPaketRoundRobinRoute implements IPaketTopologyRouting {
	final HashMap<GNode, Iterator<SNetworkComponent>> lastUsedRoute = new HashMap<GNode, Iterator<SNetworkComponent>>();

	public IGNetworkEdge getTargetRouteForMessage(
			GNetworkNodeExit target, MessagePart part) {

		Iterator<SNetworkComponent> lastIt = lastUsedRoute.get(targetNode);

		if(lastIt != null && lastIt.hasNext()){
			return lastIt.next();
		}else{
			Iterator<SNetworkComponent> it = getRoutesToTarget(targetNode).iterator();
			lastUsedRoute.put(targetNode, it);
			return it.next();
		}
	}
}
