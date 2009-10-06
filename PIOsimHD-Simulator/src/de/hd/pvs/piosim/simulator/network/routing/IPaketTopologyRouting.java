package de.hd.pvs.piosim.simulator.network.routing;

import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.event.MessagePart;

public interface IPaketTopologyRouting{
	/**
	 * Determine the route for a given packet
	 *
	 * @param target
	 * @param part
	 * @return
	 */
	public IGNetworkEdge getTargetRouteForMessage(INetworkNode src, INetworkExit target, MessagePart part);

}
