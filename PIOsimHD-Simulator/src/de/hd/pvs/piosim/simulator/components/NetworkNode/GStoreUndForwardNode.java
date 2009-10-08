package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardForwardNode;
import de.hd.pvs.piosim.simulator.base.IGNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.base.SFIFOBlockingNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

/**
 * Simulates a unidirectional bus-system i.e. a single upstream channel to use one of
 * several possible channels == lanes at a time.
 *
 * @author julian
 */
public class GStoreUndForwardNode<ModelType extends StoreForwardForwardNode>
	extends SFIFOBlockingNetworkFlowComponent<ModelType>
	implements IGNetworkNode<ModelType>
{
	protected IPaketTopologyRouting routing;

	@Override
	public void setPaketRouting(IPaketTopologyRouting routing) {
		this.routing = routing;
	}

	@Override
	protected Epoch computeTransportTime(MessagePart part) {
		return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
	}

	@Override
	protected Epoch getMaximumTransportDurationForAMessagePart() {
		return new Epoch(((double) getSimulator().getModel().getGlobalSettings().getTransferGranularity()) / getModelComponent().getTotalBandwidth());
	}

	@Override
	protected IGNetworkFlowComponent getTargetComponent(MessagePart part) {
		return routing.getTargetRouteForMessage(this.getModelComponent(), part);
	}
}
