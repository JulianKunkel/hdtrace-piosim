package de.hd.pvs.piosim.simulator.network.routing;

import java.util.HashMap;

import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;


/**
 * This class is a wrapper to provide always the same route for the same message part.
 * The message will wander from node to node, we need to save only the current node.
 *
 * @author julian
 *
 */
abstract public class GPaketRoutingPersistentRoute<ModelType extends PaketRoutingAlgorithm>
	extends AGPaketRouting<ModelType>
{
	private static class InternalPacketTracking{
		INetworkNode lastSrc;
		IGNetworkEdge nextEdge;
	}

	final HashMap<MessagePart, InternalPacketTracking> trackedPackets = new HashMap<MessagePart, InternalPacketTracking>();

	abstract protected IGNetworkEdge getTargetRouteForMessageNow(INetworkNode src,
			MessagePart part);

	@Override
	final public void messagePartRemoved(MessagePart part) {
		trackedPackets.remove(part);
	}

	@Override
	final public IGNetworkEdge getTargetRouteForMessage(INetworkNode src,
			MessagePart part)
	{
		InternalPacketTracking tracking = trackedPackets.get(part);

		if(tracking == null){
			tracking = new InternalPacketTracking();
			trackedPackets.put(part, tracking);
		}

		if(tracking.lastSrc != src){
			tracking.nextEdge = getTargetRouteForMessageNow(src, part);
			tracking.lastSrc = src;
			return tracking.nextEdge;
		}
		return tracking.nextEdge;
	}
}
