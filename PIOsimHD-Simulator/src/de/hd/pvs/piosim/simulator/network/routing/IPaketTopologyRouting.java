package de.hd.pvs.piosim.simulator.network.routing;

import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface IPaketTopologyRouting{
	/**
	 * Determine the route for a given packet.
	 * This method guarantees that for a given MessagePart and target calls return the same edge.
	 * However, once the part changes then the function might return a different value.
	 *
	 * @param target
	 * @param part
	 * @return
	 */
	public IGNetworkEdge getTargetRouteForMessage(INetworkNode src, MessagePart part);

}
